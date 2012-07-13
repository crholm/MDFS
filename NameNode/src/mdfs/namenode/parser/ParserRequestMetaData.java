package mdfs.namenode.parser;


import mdfs.namenode.io.DataNodeQuerier;
import mdfs.namenode.repositories.*;
import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.*;
import mdfs.utils.parser.FileNameOperations;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;


/**
 * A Parser implementing Parser that handles all Modes when Stage = Request and Type = Meta-Data 
 * @author Rasmus Holm
 *
 */
public class ParserRequestMetaData implements Parser {

	private Mode mode;
	private Session session;
	private String errorMsg = "";
    private UserDataRepositoryNode user;
	
	/**
	 * 
	 * @param mode is in what mode the session should be parsed, "Write",  "Read",  "Remove", "Info" and so on
	 */
	public ParserRequestMetaData(Mode mode) {
		this.mode = mode;
	}
	
	/**
	 * If an error happens this returns the Error Message
	 * @return error message
	 */
	public String getErrorMsg(){
		return errorMsg;
	}
	

	/*
	 * Sets a error message and prints it
	 * 
	 */
	private void setErrorMsg(String errorMsg){
		Verbose.print("ParserRequestMetaData Error message: " + errorMsg, this, Config.getInt("verbose")-5);
		session.setStatus("error");
		this.errorMsg = errorMsg;
	}
	
	@Override
	public boolean parse(Session session) {
		this.session = session;

		String pass;

        UserDataRepository userdata = UserDataRepository.getInstance();
        user = userdata.getUser(session.getRequest().getUser());
        pass = session.getRequest().getPass();

        //Checks so that user credentials are provided
        if(user == null || pass == null){
            //Creates Error Response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "In valid user or password" ));
            setErrorMsg("User or Pass was not included in Header");

            return false;
        }
        //Checks so that the user credentials are correct
        if(!userdata.authUser(user, pass)){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "In valid user or password"));
            setErrorMsg("User or pass was wrong");

            return false;
        }

        //Selects the correct sub-parser that handles the set Mode.
        switch (mode){
            case WRITE:
            case WRITESTREAM:
                return parseWrite();
            case READ:
                return parseRead();
            case REMOVE:
                return parseRemove();
            case INFO:
                return parseInfo();
            default:
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Mode: " + mode + " is an non-valid mode"));
                setErrorMsg("Mode: " + mode + " is an non-valid mode");
                return false;
        }

	}

	/*
	 * This parser handles when the Mode = Remove, this involves to remove a File or Dir from the
	 * file system.
	 * The Filsystem can only remove files and empty dir:s, this making it up to the user to
	 * make sure the dir is first empty.
	 */
	private boolean parseRemove() {
		MDFSProtocolMetaData metadata;
		MetaDataRepository metaDataRepo = MetaDataRepository.getInstance();


        //Retrieves the Meta-data in the request that the request is regarding
        metadata = session.getRequest().getMetadata();



        // Checks that all parameters needed are provided
        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data was not included in request"));
            return false;
        }else if(metadata.getPath() == null ){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> path was not included in request"));
            return false;
        }


        //Fetches the full path to the file in MDFS
        String path = metadata.getPath();


        MetaDataRepositoryNode node = metaDataRepo.get(path);
        //If node==null the file did not exist.
        if(node == null){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, " cannot remove " + path +": No such file or directory"));
            return false;
        }

        //Checks so that user are allowed to remove file or dir
        if(!new ACL().operationAllowed(ACLEnum.WRITE, user, node)){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Operation not permitted"));
            return false;
        }

        //Checks so that the file/dir dose not have children
        if(metaDataRepo.hasChildren(path)){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode,"failed to remove " + path +": Directory not empty "));
            return false;
        }



        //If the file/dir did note have children, it is removed from the Repository and are
        //returned in to node
        node = metaDataRepo.remove(path);


        /* If the node represents a File all the raw data stored at the datanodes has to be removed aswell
        *  This since dir:s only exists as metadata on the name node while files reside on the NameNode as
        *  Metadata and on the DataNode as raw anonymous data.
        */
        if(node.getFileType() == MetadataType.FILE){
            new DataNodeQuerier().removeData(node);
        }

        //Creates a response for the removal

        MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
        response.getInfo().setRemoved(EventStatus.SUCCESSFUL);
        response.setMetadata(node);

        session.setResponse(response);
        return true;
			
			

	}

	/*
	 * This handles a request to read data from MDFS
	 * For a File it will create a response containing the metadata and location of raw data 
	 * For a Dir it will create a resoponse containing the metadata for the dir and the metadata for all its children 
	 */
	private boolean parseRead() {
		MDFSProtocolMetaData metadata;

		MetaDataRepository metaDataRepo;
		

        //Retriving nessesary information from the request
        metadata = session.getRequest().getMetadata();
        metaDataRepo = MetaDataRepository.getInstance();

        // Checks that all parameters needed are provided
        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data was not included in request"));
            return false;
        }else if(metadata.getPath() == null ){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> path was not included in request"));
            return false;
        }

        //Retriving the node that are to be read
        String filePath = metadata.getPath();
        MetaDataRepositoryNode node = metaDataRepo.get(filePath);

        //if the node==null it did not exist and an error us sent as a response
        if(node == null){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, " cannot read " + filePath +": No such file or directory"));
            return false;
        }

        //Checks so that user are allowed to remove file or dir
        if(!new ACL().operationAllowed(ACLEnum.READ, user, node)){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Operation not permitted"));
            return false;
        }



        //Creating a response header
        MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);

        response.setMetadata(node);

        //If the file request is a dir.
        if(node.getFileType() == MetadataType.DIR){

            //Checks so that user are allowed to open dir file or dir
            if(!new ACL().operationAllowed(ACLEnum.EXECUTE, user, node)){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Operation not permitted"));
                return false;
            }


            //Fetching all the childern, if any of the dir an building the response
            MetaDataRepositoryNode[] nodes = metaDataRepo.getChildren(filePath);
            if(nodes != null){
                for (MDFSProtocolMetaData child : nodes) {
                    response.getMetadata().addChild(child);
                }
            }

            session.setResponse(response);
            return true;

        //If node is a file, a the metadata and location of it is set as a response
        }else if(node.getFileType() == MetadataType.FILE){

            response.getInfo().addToken(node.getStorageName(), mode, Config.getString("Token.key"));
            session.setResponse(response);
            return true;

        }else{

            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> type, Data type is not defined"));
            return false;
        }

	}

	/*
	 * Parses session where mode is Write
	 * This will create the meta-data that is associated with the file and return it.
	 * If it is a dir that is to written it only stores it in the MetaDataRepository
	 * If it is a file that is to be written it stores the meta data in the MetaDataRepositoy and return the
	 *   metadata along with a list of what data nodes the raw data can be written to
	 */
	private boolean parseWrite(){
		MDFSProtocolMetaData metadata;
		MetaDataRepository metaDataRepo;

        //Fetches the metadata that is to written along with the MetaDataRepository
        metadata = session.getRequest().getMetadata();
        metaDataRepo = MetaDataRepository.getInstance();


        // Checks that all parameters needed are provided
        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data was not included in request"));
            return false;
        }
        if(metadata.getPath() == null ){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> path was not included in request"));
            return false;
        }
        if(metadata.getType() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> type was not included in request"));
            return false;
        }


        String filePath = metadata.getPath();

        //Fetches the node that are to be written to, in case it is to be overwritten
        MetaDataRepositoryNode node = metaDataRepo.get(filePath);
        /*
         * File or Dir to write dose not exist previously
         */
        if(node == null){
            MetaDataRepositoryNode parent = metaDataRepo.getParent(filePath);

            if(parent == null){
                //This happens if one is trying to add a file or dir of which the parent dirs dose not exist
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "File could not be added to the FS, No such file or directory"));
                return false;
            }

            //Checks so that user are allowed to open parent dir
            if(!new ACL().operationAllowed(ACLEnum.EXECUTE, user, parent)){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Parent directory could not be opened"));
                return false;
            }
            //Checks so that user are allowed to write new file to parent dir
            if(!new ACL().operationAllowed(ACLEnum.WRITE, user, parent)){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Operation not permitted"));
                return false;
            }


            //Creates the new node with the metadata to be stored in the repository
            node = new MetaDataRepositoryNode();

            node.setFilePath(metadata.getPath());
            node.setFileType(metadata.getType());


            if(node.getType() == MetadataType.FILE)
                node.setStorageName(new FileNameOperations().createUniqName());


        //Sets Permissions
            if(metadata.getPermission() == -1){
                if(node.getType() == MetadataType.FILE)
                    node.setPermission(664);  //Basic permissions for a File
                else
                    node.setPermission(775);  //Basic permissions for a DIR

            }else{
                node.setPermission(metadata.getPermission());
            }


        //Sets File/Dir size
            if(node.getType() == MetadataType.DIR)
                node.setSize(0);
            else
                node.setSize(metadata.getSize());



        //Sets Owner, Group, uid, gid
            String user = session.getRequest().getUser();
            node.setOwner(user);
            node.setGroup(user);

            node.setUid(UserDataRepository.getInstance().getUid(user));
            node.setGid(GroupDataRepository.getInstance().getGid(user));




         //Sets diffrent time for file/dir.
            long time = Time.currentTimeMillis();

            if(metadata.getCreated() == -1)
                node.setCreated(time);
            else
                node.setCreated(metadata.getCreated());

            if(metadata.getLastEdited() == -1)
                node.setLastEdited(time);
            else
                node.setLastEdited(metadata.getLastEdited());

            node.setLastTouched(time);



            //Adding new node to MetaDataRepository
            if(!metaDataRepo.add(node.getKey(), node)){
                //This happens if one is trying to add a file or dir of which the parent dirs dose not exist
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "File could not be added to the FS, No such file or directory"));

                return false;
            }

        //This in case of overwriting a file, we first check that is a file that we are trying to overwrite
        }else if(node.getFileType() == MetadataType.FILE && metadata.getType() != MetadataType.FILE){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "can not overwrite a DIR with a FILE"));
                return false;


        //This in case of overwriting a dir, we first check that is a dir that we are trying to overwrite
        }else if(node.getFileType() == MetadataType.DIR){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "can not overwrite a DIR"));
                return false;

        }else{

            //Checks so that user are allowed to write to existing file
            if(!new ACL().operationAllowed(ACLEnum.WRITE, user, node)){
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Operation not permitted"));
                return false;
            }

            node.setSize(metadata.getSize());


            if(metadata.getCreated() != -1)
                node.setCreated(metadata.getCreated());

            if(metadata.getLastEdited() != -1)
                node.setLastEdited(metadata.getLastEdited());

            node.setLastTouched(Time.currentTimeMillis());


        }

        //Creates the header for the response
        MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
        response.setMetadata(node);

        if(node.getType() == MetadataType.FILE)
            response.getInfo().addToken(node.getStorageName(), mode, Config.getString("Token.key"));

        session.setResponse(response);

        return true;
		
	}
	

	/* 
	 * This parser is currently not used for the protocol
	 * It returns the Metadata for one file or dir.
	 */
	private boolean parseInfo(){
		MDFSProtocolMetaData metadata;
		MetaDataRepositoryNode node;
		

        metadata = session.getRequest().getMetadata();

        // Checks that all parameters needed are provided
        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data was not included in request"));
            return false;
        }
        if(metadata.getPath() == null ){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.METADATA, mode, "Field Meta-data -> path was not included in request"));
            return false;
        }

        node = MetaDataRepository.getInstance().get( metadata.getPath() );

        MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
        response.setMetadata(node);
        session.setResponse(response);

		return true;
	}
	
	
	/*
	 * Creats a standard JSON header for the sessions response 
	 */
	private MDFSProtocolHeader createHeader(Stage stage, Type type, Mode mode){
		MDFSProtocolHeader header = new MDFSProtocolHeader();
        header.setStage(stage);
		header.setType(type);
		header.setMode(mode);
		
		MDFSProtocolInfo info = new MDFSProtocolInfo();

        info.setDatanodes(DataNodeInfoRepository.getInstance().toList());
        info.setLocalTime(Time.currentTimeMillis());

		header.setInfo(info);
		
		return header;
	}



}

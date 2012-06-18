package mdfs.namenode.parser;


import mdfs.namenode.io.DataNodeQuerier;
import mdfs.namenode.repositories.*;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.*;
import mdfs.utils.parser.FileNameOperations;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;
import org.json.JSONException;

import java.util.LinkedList;


/**
 * A Parser implementing Parser that handles all Modes when Stage = Request and Type = Meta-Data 
 * @author Rasmus Holm
 *
 */
public class ParserRequestMetaData implements Parser {

	private Mode mode;
	private Session session;
	private String errorMsg = "";
	
	/**
	 * 
	 * @param mode is in what mode the session should be parsed, "Write",  "Read",  "Remove", "Info" and so on
	 */
	public ParserRequestMetaData(Mode mode) {
		this.mode = mode;
	}
	
	/**
	 * If an error happens this returns the Error Message
	 * @return
	 */
	public String getErrorMsg(){
		return errorMsg;
	}
	
	/*
	 * Verify the user tyring to access and/or modify the stored meta-data
	 */
	private boolean authUser(String user, String pass){
		UserDataRepository userData = UserDataRepository.getInstance();
		return userData.authUser(user, pass);		
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
		String user = null;
		String pass = null;

        user = session.getRequest().getUser();
        pass = session.getRequest().getPass();
        //Checks so that user credentials are provided
        if(user == null || pass == null){
            MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
            response.setError("No full user or password was given in request");
            session.setResponse(response);

            setErrorMsg("User or Pass was not included in Header");
            return false;
        }
        //Checks so that the user credentials are correct
        if(!authUser(user, pass)){
            //Creates a error response
            MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
            response.setError("In valid user or password");
            session.setResponse(response);

            setErrorMsg("User or pass was wrong");
            return false;
        }

        //Selects the correct sub-parser that handles the set Mode.
        if(mode == Mode.WRITE){

            return parseWrite();
        }else if(mode == Mode.READ){
            return parseRead();
        }else if(mode == Mode.REMOVE){
            return parseRemove();
        }else if(mode == Mode.INFO){
            return parseInfo();
        }else{
            //Creates a error response
            MDFSProtocolHeader response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
            response.setError("Mode: \"" + mode + "\" is an unvalid mode");
            session.setResponse(response);
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
		MDFSProtocolHeader response;
		MetaDataRepository metaDataRepo = MetaDataRepository.getInstance();
		

        //Retrieves the Meta-data in the request that the request is regarding
        metadata = session.getRequest().getMetadata();
        response = createHeader(Stage.RESPONSE, Type.METADATA, mode);

        //Fetches the full path to the file in MDFS
        String path = metadata.getPath();

        //Checks so that the file/dir dose not have children
        if(metaDataRepo.hasChildren(path)){
            //Creates a error response
            response.setError("failed to remove `" + path +"': Directory not empty ");
            session.setResponse(response);
            return false;
        }

        //If the file/dir did note have children, it is removed from the Repository and are
        //returned in to node
        MetaDataRepositoryNode node = metaDataRepo.remove(path);

        //If node==null the file did not exist.
        if(node == null){
            //Creates a error response
            response.setError(" cannot remove `" + path +"': No such file or directory");
            session.setResponse(response);
            return false;
        }

        /* If the node represents a File all the raw data stored at the datanodes has to be removed aswell
        *  This since dir:s only exists as metadata on the name node while files reside on the NameNode as
        *  Metadata and on the DataNode as raw anonymous data.
        */
        if(node.getFileType() == DataTypeEnum.FILE){
            new DataNodeQuerier().removeData(node);
        }

        //Creates a response for the removal
        // TODO Implement metadata node as extention oc MDFSProtocolMetadata
        response.setMetadata(node);

        MDFSProtocolInfo info = new MDFSProtocolInfo();
        info.setRemoved(EventStatus.SUCCESSFUL);

        response.setInfo(info);

        session.setResponse(response);
        return true;
			
			

		return false;
	}

	/*
	 * This handles a request to read data from MDFS
	 * For a File it will create a response containing the metadata and location of raw data 
	 * For a Dir it will create a resoponse containing the metadata for the dir and the metadata for all its children 
	 */
	private boolean parseRead() {
		MDFSProtocolMetaData metadata;
		MDFSProtocolHeader response;
		MetaDataRepository metaDataRepo;
		

        //Creating a response header
        response = createHeader(Stage.RESPONSE, Type.METADATA, mode);

        //Retriving nessesary information from the request
        metadata = session.getRequest().getMetadata();
        metaDataRepo = MetaDataRepository.getInstance();

        //Retriving the node that are to be read
        String filePath = metadata.getPath();
        MetaDataRepositoryNode node = metaDataRepo.get(filePath);

        //if the node==null it did not exist and an error us sent as a response
        if(node == null){
            response.setError("No such file or directory");
            session.setResponse(response);
            return false;
        }

        response.setMetadata(node);

        //If the file request is a dir.
        if(node.getFileType() == DataTypeEnum.DIR){

            LinkedList<MetaDataRepositoryNode> children = new LinkedList<MetaDataRepositoryNode>();

            //Fetching all the childern, if any of the dir an building the response
            MetaDataRepositoryNode[] nodes = metaDataRepo.getChildren(filePath);
            if(nodes != null){
                for (MetaDataRepositoryNode child : nodes) {
                    children.add(child);
                }

                response.getMetadata().setChildren(children);
            }

            session.setResponse(response);
            return true;

        //If node is a file, a the metadata and location of it is set as a response
        }else if(node.getFileType() == DataTypeEnum.FILE){

            session.setResponse(response);
            return true;
        }else{
            response.setError("Data type is not defined");
            response.setMetadata(null);
            session.setResponse(response);
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
		MDFSProtocolHeader response;
		MetaDataRepository metaDataRepo;
		try {
			//Fetches the metadata that is to written along with the MetaDataRepository
			metadata = session.getRequest().getMetadata();
			metaDataRepo = MetaDataRepository.getInstance();
			
			//Creates the header for the response
            response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
			
			String filePath = metadata.getPath();

			//Fetches the node that are to be written to, in case it is to be overwritten
			MetaDataRepositoryNode node = metaDataRepo.get(filePath);
			
			/*
			 * File to write dose not exist previously
			 */
			if(node == null){
				//Creates the new node with the metadata to be stored in the repository
				node = new MetaDataRepositoryNode();
				node.setFilePath(metadata.getPath());
				node.setSize(metadata.getSize());
				node.setOwner(metadata.getOwner());
				node.setGroup(metadata.getGroup());
				node.setCreated(metadata.getCreated());
				node.setLastEdited(metadata.getLastEdited());
                node.setLastTouched(metadata.getLastToutched());
				
				//Determines the the datatype
				if(metadata.getType() == MetadataType.DIR){
					node.setFileType(DataTypeEnum.DIR);
				}else if(metadata.getType() == MetadataType.FILE){
					node.setFileType(DataTypeEnum.FILE);
					node.setStorageName(new FileNameOperations().createUniqName());
				}else{
					node.setFileType(null);
				}
				
				//Adding new node to MetaDataRepository
				if(!metaDataRepo.add(node.getKey(), node)){
					//This happens if one is trying to add a file or dir of which the parent dirs dose not exist
					response.setError("File could not be added to the FS, No such file or directory");
					session.setResponse(response);
				
					return false;
				}
			//This in case of overwriting a file, we first check that is a file that we are trying to overwrite	
			}else if(node.getFileType() == DataTypeEnum.FILE){
				if(metadata.getType() != MetadataType.FILE){
					response.setError("A file at that path already exist");
					session.setResponse(response);
				
					return false;
				}

			//This in case of overwriting a dir, we first check that is a dir that we are trying to overwrite
			}else if(node.getFileType() == DataTypeEnum.DIR){
				if(metadata.getType() != MetadataType.DIR){
					response.setError("A dir at that path already exist");
					session.setResponse(response);
					
					return false;
				}
			/*
			 * TODO: Bug would aper here in the case of overwriting the nested else if if statemets above 
			 * 		 should be change to only one else if statement, teste must be done.
			 * 	ex:	 else if(node.getFileType() == DataTypeEnum.DIR && !metaDataJson.getString("type").equals("dir"))   	
			 */
			}else{
                node.setSize(metadata.getSize());
                node.setOwner(metadata.getOwner());
                node.setGroup(metadata.getGroup());
                node.setCreated(metadata.getCreated());
                node.setLastEdited(metadata.getLastEdited());
                node.setLastTouched(metadata.getLastToutched());;
				metaDataRepo.replace(node.getKey(), node);
			}
			
			
			response.setMetadata(node);
			session.setResponse(response);
			
		} catch (JSONException e) {
			e.printStackTrace();
			setErrorMsg("Meta-data or path did not exist in Header");
			return false;
		}

		return true;
		
	}
	

	/* 
	 * This parser is currently not used for the protocol
	 * It returns the Metadata for one file or dir.
	 */
	private boolean parseInfo(){
		MDFSProtocolMetaData metadata;
		MDFSProtocolHeader response;
		MetaDataRepositoryNode node;
		

        metadata = session.getRequest().getMetadata();
        node = MetaDataRepository.getInstance().get( metadata.getPath() );
        response = createHeader(Stage.RESPONSE, Type.METADATA, mode);
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

		header.setInfo(info);
		
		return header;
	}
}

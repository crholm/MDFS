package mdfs.datanode.parser;

import mdfs.datanode.io.NameNodeInformer;
import mdfs.datanode.io.Replicator;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.*;
import mdfs.utils.io.protocol.enums.*;
import mdfs.utils.parser.FileNameOperations;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

import java.io.File;

/**
 * A parser that handles all communication with the Stage=Request and Type=File and parses it.
 * This class wraps {@link Session} and parses its content. 
 * @author Rasmus Holm
 *
 */
public class ParserRequestFile implements Parser{
	private Mode mode;
	private Session session;
	private FileNameOperations nameTranslation = new FileNameOperations();
	private SocketFunctions socketFunctions = new SocketFunctions();
	@SuppressWarnings("unused")
	private String errorMsg = null;
	
	/**
	 * This constructor sets in which mode the parser parses the session that it wraps
	 * Modes can be = Write / Read / Remove / Edit / Info / Cascade
	 * @param mode Write / Read / Remove / Edit / Info / Cascade
	 */
	public ParserRequestFile(Mode mode) {
		this.mode = mode;
	}

	
	
	
	/*
	 * Sets a error message and prints it
	 * 
	 */
	private void setErrorMsg(String errorMsg){
		Verbose.print("Error message: " + errorMsg, this, Config.getInt("verbose")-1);
		session.setStatus("error");
		this.errorMsg = errorMsg;
	}

	
	@Override
	public boolean parse(Session session) {
		this.session = session;

		//Selects what "sub-parser" to use

        switch (mode){

            case WRITE:
            case WRITESTREAM:
                return parseWrite();

            case READ:
                return parseRead();

            case REMOVE:
                return parseRemove();

            case CASCADE:
                return parseCascade();

            case INFO:
            default:
                //Creates a error response
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Mode: " + mode + " is an non-valid mode"));

                setErrorMsg("None valid Mode in Header");
                return false;
        }

	}



    /**
	 * Parsing in the cases of Cascading, a file is replicating from one datanode to this one.
	 * @return true if successfully receiving a cascading file
	 */
	private boolean parseCascade() {
		//Gets the request as a MDFSProtocolHeader from the session
		MDFSProtocolHeader request = session.getRequest();
				

        //Retrieves metadata information about the file
        MDFSProtocolMetaData metadata = request.getMetadata();

        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data was not included in request"));
            return false;

        }if(metadata.getLocation() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location was not included in request"));
            return false;

        }
        if(metadata.getLocation().getName() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location -> name was not included in request"));
            return false;
        }
        if(request.getInfo() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Info was not included in request"));
            return false;
        }


        //Gets the loaction information
        MDFSProtocolLocation location = metadata.getLocation();

        //Retrivse the file name and builds the path to it
        String fileName = location.getName();

        if(!request.getInfo().authToken(metadata.getPath(), fileName, mode, Config.getString("Token.key"), Config.getInt("Token.window"))){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Token failed to authenticate"));
            return false;
        }

        String fullPath = nameTranslation.translateFileNameToFullPath(fileName);

        //Creates all dirs on the local FS necessary to write the file to it
        new File(nameTranslation.translateFileNameToDirPath(fileName)).mkdirs();

        //Creates a file to the path
        File file = new File(fullPath);

        boolean overwrite = file.exists();

        //Receives and saves the file to the local file system
        boolean written = socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);

        //Creates information about the file and updates the Name Node with it
        MDFSProtocolInfo info = new MDFSProtocolInfo();
        info.setWritten(written ? EventStatus.SUCCESSFUL : EventStatus.FAILED);
        info.setOverwrite(overwrite ? Overwrite.TRUE : Overwrite.FALSE);
        info.setPath(metadata.getPath());
        info.setName(fileName);
        info.setHost(Config.getString("address"));
        info.setPort(Config.getString("port"));

        new NameNodeInformer().newDataLocation(Type.FILE, Mode.WRITE, info);

        return true;

		

	}



	/**
	 * Removes a file from the data nodes local file system.
	 * @return true if successfully removing the file
	 */
	private boolean parseRemove() {
		//Retrieves the request
		MDFSProtocolHeader request = session.getRequest();

        //Figurers out what file is to be removed
        MDFSProtocolMetaData metadata = request.getMetadata();


        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data was not included in request"));
            return false;

        }if(metadata.getLocation() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location was not included in request"));
            return false;

        }
        if(metadata.getLocation().getName() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location -> name was not included in request"));
            return false;
        }
        if(request.getInfo() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Info was not included in request"));
            return false;
        }


        String fileName = metadata.getLocation().getName();

        if(!request.getInfo().authToken(metadata.getPath(), fileName, mode, Config.getString("Token.key"), Config.getInt("Token.window"))){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Token failed to authenticate"));
            return false;
        }

        String fullPath = nameTranslation.translateFileNameToFullPath(fileName);

        //Removes it and returnes if successfull or not
        File file = new File(fullPath);

        return file.delete();

	}




	/**
	 * Parser for retring a file from the local FS and sending back.
	 * @return true if successfylle setting {@link Session} setFileForResponse
	 */
	private boolean parseRead() {
		//Get request and builds a basic response
		MDFSProtocolHeader request = session.getRequest();


        if(request.getMetadata() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data was not included in request"));
            return false;
        }
        if(request.getMetadata().getLocation() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location was not included in request"));
            return false;

        }
        if(request.getMetadata().getLocation().getName() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location -> name was not included in request"));
            return false;
        }
        if(request.getInfo() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Info was not included in request"));
            return false;
        }



        //Figuers out what file to send back
        String fileName = request.getMetadata().getLocation().getName();
        String logicalPath = request.getMetadata().getPath();

        if(!request.getInfo().authToken(logicalPath ,fileName, mode, Config.getString("Token.key"), Config.getInt("Token.window"))){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Token failed to authenticate"));
            return false;
        }

        File file = new File(nameTranslation.translateFileNameToFullPath(fileName));



        //If the file exists and are to be sent othewise an error is set in the response
        if(file.exists()){
            //Sets the response
            MDFSProtocolHeader response = new MDFSProtocolHeader();
            response.setStage(Stage.RESPONSE);
            response.setType(Type.FILE);
            response.setMode(mode);
            response.setMetadata(request.getMetadata());

            session.setResponse(response);
            session.setFileForResponse(file);
            return true;
        }else{
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "File dose not exist on DataNode local filesystem"));
            return false;
        }
			

	}


	/**
	 * Writes a file to the local fs
	 * @return true if the file was written successfully, otherwise false
	 */
	/*
	 * Parses and execute commands that are related to the mode Write
	 */
	private boolean parseWrite(){
		//Get request and builds a basic response
		MDFSProtocolHeader request = session.getRequest();


        //Gets the meta data
        MDFSProtocolMetaData metadata = request.getMetadata();


        if(metadata == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data was not included in request"));
            return false;

        }
        if(metadata.getLocation() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location was not included in request"));
            return false;

        }
        if(metadata.getPath() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> path was not included in request"));
            return false;

        }
        if(metadata.getLocation().getName() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Meta-data -> Location -> name was not included in request"));
            return false;
        }
        if(request.getInfo() == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Field Info was not included in request"));
            return false;
        }



        //Figures out the path to the file in the local file system
        String fileName = metadata.getLocation().getName();

        if(!request.getInfo().authToken(metadata.getPath(), fileName, mode, Config.getString("Token.key"), Config.getInt("Token.window"))){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Token failed to authenticate"));
            return false;
        }

        String fileFullPath = nameTranslation.translateFileNameToFullPath(fileName);
        String fileDir = nameTranslation.translateFileNameToDirPath(fileName);

        //Creats file and dirs nessesary for writing the file
        File file = new File(fileFullPath);
        File dir = new File(fileDir);
        dir.mkdirs();

        boolean overwrite = file.exists();

        Verbose.print("Reciving file...", this, Config.getInt("verbose")-2);




        //Creates info about the file that was just written
        MDFSProtocolInfo info = new MDFSProtocolInfo();
        info.setOverwrite( overwrite ? Overwrite.TRUE : Overwrite.FALSE);
        info.setPath(metadata.getPath());
        info.setName(fileName);
        info.setHost(Config.getString("address"));
        info.setPort(Config.getString("port"));

        boolean written = false;

        if(mode == Mode.WRITE){
           written = socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);

        }else if(mode == Mode.WRITESTREAM){
            long length = socketFunctions.receiveFileFromStream(session.getInputStreamFromRequest(), file);
            info.setLength(length);
            metadata.setSize(length);

            written = length == -1 ? false : true;

        }

        info.setWritten(written ? EventStatus.SUCCESSFUL : EventStatus.FAILED);



        //Updates the response for the client
        MDFSProtocolHeader response = new MDFSProtocolHeader();

        // Replicates the file to other data nodes and informs the Name Node if necessary
        if(overwrite && written){
            new Replicator().overwriteFile(metadata);

            if(mode == Mode.WRITESTREAM)
                new NameNodeInformer().newDataLocation(Type.FILE, mode, info);

        }else if(!overwrite && written){

            metadata.getLocation().addHost(Config.getString("address") + ":" + Config.getString("port"));

            //Only informs the name node if it is a new file, otherwise it already have the information
            new NameNodeInformer().newDataLocation(Type.FILE, mode, info);
            new Replicator().newFile(metadata);

        }else{

            response.setErrorCode(MDFSErrorCode.EIO);
            response.setError("I/O Error, failed to write data");
        }



        response.setStage(Stage.RESPONSE);
        response.setType(Type.FILE);
        response.setMode(mode);
        response.setMetadata(metadata);
        response.setInfo(info);
        session.setResponse(response);

        return true;
	}
}

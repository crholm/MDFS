package mdfs.datanode.parser;

import mdfs.datanode.io.NameNodeInformer;
import mdfs.datanode.io.Replicator;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolLocation;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
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
		if(mode == Mode.WRITE){
			return parseWrite();	
		}else if(mode == Mode.READ){
			return parseRead();
		}else if(mode == Mode.REMOVE){
			return parseRemove();
		}else if(mode == Mode.INFO){
			
		}else if(mode == Mode.CASCADE){
			return parseCascade();
		}else{
			setErrorMsg("None valid Mode in Header");
			return false;
		}		
		
		setErrorMsg("Some thing went wrong parsing Header");
		return false;
		
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
        //Gets the loaction information
        MDFSProtocolLocation location = metadata.getLocation();

        //Retrivse the file name and builds the path to it
        String fileName = location.getName();
        String fullPath = nameTranslation.translateFileNameToFullPath(fileName);

        //Creates all dirs on the local FS necessary to write the file to it
        new File(nameTranslation.translateFileNameToDirPath(fileName)).mkdirs();

        //Creates a file to the path
        File file = new File(fullPath);

        boolean overwrite = file.exists();

        //Receives and saves the file to the local file system
        socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);

        //Creates information about the file and updates the Name Node with it
        MDFSProtocolInfo info = new MDFSProtocolInfo();
        info.setWritten(ActionStatus.SUCCESSFUL);
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
        String fileName = metadata.getLocation().getName();
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

        MDFSProtocolHeader response = new MDFSProtocolHeader();
        response.setStage(Stage.RESPONSE);
        response.setType(Type.FILE);
        response.setMode(mode);

        //Add metadata to the response
        response.setMetadata(request.getMetadata());


        //Figuers out what file to send back
        String fileName = request.getMetadata().getLocation().getName();
        File file = new File(nameTranslation.translateFileNameToFullPath(fileName));

        //Sets the response
        session.setResponse(response);

        //If the file exists and are to be sent othewise an error is set in the response
        if(file.exists()){
            session.setFileForResponse(file);
            return true;
        }else{
            response.setError("File dose not exist");
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

        MDFSProtocolHeader response = new MDFSProtocolHeader();
        response.setStage(Stage.RESPONSE);
        response.setType(Type.FILE);
        response.setMode(mode);
		

			
        //Gets the meta data
        MDFSProtocolMetaData metadata = request.getMetadata();

        //Figures out the path to the file in the local file system
        String fileName = metadata.getLocation().getName();
        String fileFullPath = nameTranslation.translateFileNameToFullPath(fileName);
        String fileDir = nameTranslation.translateFileNameToDirPath(fileName);

        //Creats file and dirs nessesary for writing the file
        File file = new File(fileFullPath);
        File dir = new File(fileDir);
        dir.mkdirs();
        boolean overwrite = file.exists();

        //Writing the file to the local FS from stream
        // TODO Implement check so that file was written
        Verbose.print("Reciving file...", this, Config.getInt("verbose")-2);
        socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);

        //Creates info about the file that was just written
        MDFSProtocolInfo info = new MDFSProtocolInfo();
        info.setWritten(ActionStatus.SUCCESSFUL);
        info.setOverwrite( overwrite ? Overwrite.TRUE : Overwrite.FALSE);

        info.setPath(metadata.getPath());
        info.setName(fileName);
        info.setHost(Config.getString("address"));
        info.setPort(Config.getString("port"));


        // Replicates the file to other data nodes and informs the Name Node if necessary
        if(overwrite){
            new Replicator().overwriteFile(metadata);
        }else{

            metadata.getLocation().addHost(Config.getString("address") + ":" + Config.getString("port"));


            //Only informs the name node if it is a new file, otherwise it already have the information
            new NameNodeInformer().newDataLocation(Type.FILE, mode, info);
            new Replicator().newFile(metadata);

        }

        //Updates the response for the client
        response.setMetadata(metadata);
        response.setInfo(info);
        session.setResponse(response);

        return true;
			

	}

}

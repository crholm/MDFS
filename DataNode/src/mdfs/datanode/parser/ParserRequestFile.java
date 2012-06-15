package mdfs.datanode.parser;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import mdfs.datanode.io.NameNodeInformer;
import mdfs.datanode.io.Replicator;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.parser.FileNameOperations;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

/**
 * A parser that handles all communication with the Stage=Request and Type=File and parses it.
 * This class wraps {@link Session} and parses its content. 
 * @author Rasmus Holm
 *
 */
public class ParserRequestFile implements Parser{
	private String mode;
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
	public ParserRequestFile(String mode) {
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
	/**
	 * Creates a standard JSONHeader for MDFS
	 * @param stage
	 * @param type
	 * @param mode
	 * @return
	 */
	private JSONObject createJsonHeader(String stage, String type, String mode){
		JSONObject json = new JSONObject();
		try {
			json.put("From", Config.getString("address"));
			json.put("To", session.getJsonRequest().getString("From"));
			json.put("Stage", stage);
			json.put("Type", type);
			json.put("Mode", mode);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	@Override
	public boolean parse(Session session) {
		this.session = session;

		//Selects what "sub-parser" to use
		if(mode.equals("Write")){
			return parseWrite();	
		}else if(mode.equals("Read")){
			return parseRead();
		}else if(mode.equals("Remove")){
			return parseRemove();
		}else if(mode.equals("Info")){
			
		}else if(mode.equals("Cascade")){
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
		//Gets the request as a JSONObeject from the session
		JSONObject jsonRequest = session.getJsonRequest();
				
		try {
			//Retrieves metadata information about the file
			JSONObject metadata = jsonRequest.getJSONObject("Meta-data");
			//Gets the loaction information
			JSONObject location = metadata.getJSONObject("Location");
			
			//Retrivse the file name and builds the path to it
			String fileName = location.getString("name");
			String fullPath = nameTranslation.translateFileNameToFullPath(fileName);
			
			//Creates all dirs on the local FS necessary to write the file to it
			new File(nameTranslation.translateFileNameToDirPath(fileName)).mkdirs();
			
			//Creates a file to the path
			File file = new File(fullPath);
			
			boolean overwrite = file.exists();
			
			//Receives and saves the file to the local file system
			socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);
			
			//Creates information about the file and updates the Name Node with it
			JSONObject info = new JSONObject();
			info.put("written", "successful");
			info.put("overwrite", overwrite);
			info.put("path", jsonRequest.getJSONObject("Meta-data").getString("path") );
			info.put("name", fileName);
			info.put("host", Config.getString("address"));
			info.put("port", Config.getString("port"));
			new NameNodeInformer().newDataLocation("File", "Write", info);
			
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return false;
	}



	/**
	 * Removes a file from the data nodes local file system.
	 * @return true if successfully removing the file
	 */
	private boolean parseRemove() {
		//Retrieves the request
		JSONObject jsonRequest = session.getJsonRequest();
		try {
			//Figurers out what file is to be removed
			JSONObject metadata = jsonRequest.getJSONObject("Meta-data");
			String fileName = metadata.getJSONObject("Location").getString("name");
			String fullPath = nameTranslation.translateFileNameToFullPath(fileName);
			
			//Removes it and returnes if successfull or not
			File file = new File(fullPath);
			return file.delete();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}




	/**
	 * Parser for retring a file from the local FS and sending back.
	 * @return true if successfylle setting {@link Session} setFileForResponse
	 */
	private boolean parseRead() {
		//Get request and builds a basic response
		JSONObject jsonRequest = session.getJsonRequest();
		JSONObject jsonResponse = createJsonHeader("Response", "File", mode);
		try {			
			//Add metadata to the response
			jsonResponse.put("Meta-data", jsonRequest.get("Meta-data"));
			
			//Figuers out what file to send back
			String fileName = jsonRequest.getJSONObject("Meta-data").getJSONObject("Location").getString("name");
			File file = new File(nameTranslation.translateFileNameToFullPath(fileName));
			
			//Sets the response 
			session.setJsonResponse(jsonResponse);
			
			//If the file exists and are to be sent othewise an error is set in the response
			if(file.exists()){
				session.setFileForResponse(file);
				return true;
			}else{
				jsonResponse.put("Error", "File dose not exist");
				return false;
			}
			
		} catch (JSONException e) {
			try {
				jsonResponse.put("Error", "Invalid JSON header");
				session.setJsonResponse(jsonResponse);
			} catch (JSONException e1) {}
			e.printStackTrace();
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
		JSONObject jsonRequest = session.getJsonRequest();
		JSONObject jsonResponse = createJsonHeader("Response", "File", mode);
		
		try {
			
			//Gets the meta data
			JSONObject metadata = jsonRequest.getJSONObject("Meta-data");
			
			//Figures out the path to the file in the local file system
			String fileName = metadata.getJSONObject("Location").getString("name");
			String fileFullPath = nameTranslation.translateFileNameToFullPath(fileName);
			String fileDir = nameTranslation.translateFileNameToDirPath(fileName);
			
			//Creats file and dirs nessesary for writing the file
			File file = new File(fileFullPath);
			File dir = new File(fileDir);
			dir.mkdirs();
			boolean overwrite = file.exists();
			
			//Writing the file to the local FS from stream
			Verbose.print("Reciving file...", this, Config.getInt("verbose")-2);
			socketFunctions.receiveFile(session.getInputStreamFromRequest(), file);
			
			//Creates info about the file that was just written
			JSONObject info = new JSONObject();
			info.put("written", "successful");
			info.put("overwrite", overwrite);
			info.put("path", jsonRequest.getJSONObject("Meta-data").getString("path") );
			info.put("name", fileName);
			info.put("host", Config.getString("address"));
			info.put("port", Config.getString("port"));
			
			// Replicates the file to other data nodes and informs the Name Node if necessary
			if(overwrite){
				new Replicator().overwriteFile(metadata);
			}else{
				metadata.getJSONObject("Location").put("hosts", new JSONArray().
							put(Config.getString("address") + ":" + Config.getString("port")));
				
				//Only informs the name node if it is a new file, otherwise it already have the information
				new NameNodeInformer().newDataLocation("File", mode, info);
				new Replicator().newFile(metadata);

			}
			
			//Updates the response for the client
			jsonResponse.put("Meta-data", metadata);
			jsonResponse.put("Info", info);
			session.setJsonResponse(jsonResponse);
			
			return true;
			
		} catch (JSONException e) {
			try {
				jsonResponse.put("Error", "Invalid JSON header");
				session.setJsonResponse(jsonResponse);
			} catch (JSONException e1) {}
			e.printStackTrace();
			return false;
		}
	}

}

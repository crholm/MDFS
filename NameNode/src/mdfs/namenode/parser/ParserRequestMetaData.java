package mdfs.namenode.parser;


import mdfs.namenode.io.DataNodeQuerier;
import mdfs.namenode.repositories.*;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.parser.FileNameOperations;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * A Parser implementing Parser that handles all Modes when Stage = Request and Type = Meta-Data 
 * @author Rasmus Holm
 *
 */
public class ParserRequestMetaData implements Parser {

	private String mode;
	private Session session;
	private String errorMsg = "";
	
	/**
	 * 
	 * @param mode is in what mode the session should be parsed, "Write",  "Read",  "Remove", "Info" and so on
	 */
	public ParserRequestMetaData(String mode) {
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
		try {
			user = session.getRequest().getString("User");
			pass = session.getRequest().getString("Pass");
			//Checks so that user credentials are provided
			if(user == null || pass == null){
				JSONObject jsonResponse = createJsonHeader("Response", "Meta-data", mode);
				jsonResponse.put("Error", "No full user or password was given in request");
				session.setResponse(jsonResponse);
				
				setErrorMsg("User or Pass was not included in Header");
				return false;
			}
			//Checks so that the user credentials are correct
			if(!authUser(user, pass)){
				//Creates a error response
				JSONObject jsonResponse = createJsonHeader("Response", "Meta-data", mode);
				jsonResponse.put("Error", "In valid user or password");
				session.setResponse(jsonResponse);
				
				setErrorMsg("User or pass was wrong");
				return false;
			}
		
			//Selects the correct sub-parser that handles the set Mode.
			if(mode.equals("Write")){
				
				return parseWrite();	
			}else if(mode.equals("Read")){
				return parseRead();
			}else if(mode.equals("Remove")){
				return parseRemove();
			}else if(mode.equals("Info")){
				return parseInfo();			
			}else{
				//Creates a error response
				JSONObject jsonResponse = createJsonHeader("Response", "Meta-data", mode);
				jsonResponse.put("Error", "Mode: \"" + mode + "\" is an unvalid mode");
				session.setResponse(jsonResponse);
				return false;
			}
		
		} catch (JSONException e) {
			e.printStackTrace();
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
		JSONObject metaDataJson;
		JSONObject jsonResponse;
		MetaDataRepository metaDataRepo = MetaDataRepository.getInstance();
		
		try {
			//Retrieves the Meta-data in the request that the request is regarding
			metaDataJson = session.getRequest().getJSONObject("Meta-data");
			jsonResponse = createJsonHeader("Response", "Meta-data", mode);
			//Fetches the full path to the file in MDFS
			String path = metaDataJson.getString("path");
			
			//Checks so that the file/dir dose not have children
			if(metaDataRepo.hasChildren(path)){
				//Creates a error response
				jsonResponse.put("Error", "failed to remove `" + path +"': Directory not empty ");
				session.setResponse(jsonResponse);
				return false;
			}
			
			//If the file/dir did note have children, it is removed from the Repository and are
			//returned in to node
			MetaDataRepositoryNode node = metaDataRepo.remove(path);
			
			//If node==null the file did not exist.
			if(node == null){
				//Creates a error response
				jsonResponse.put("Error", " cannot remove `" + path +"': No such file or directory");
				session.setResponse(jsonResponse);
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
			jsonResponse.put("Meta-data", node.toJSON());
			JSONObject info = new JSONObject();
			info.put("removed", "successful");
			jsonResponse.put("Info", info);
			
			session.setResponse(jsonResponse);
			return true;
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * This handles a request to read data from MDFS
	 * For a File it will create a response containing the metadata and location of raw data 
	 * For a Dir it will create a resoponse containing the metadata for the dir and the metadata for all its children 
	 */
	private boolean parseRead() {
		JSONObject metaDataJson;
		JSONObject jsonResponse;
		MetaDataRepository metaDataRepo;
		
		
			try {
				//Creating a response header
				jsonResponse = createJsonHeader("Response", "Meta-data", mode);
				
				//Retriving nessesary information from the request
				metaDataJson = session.getRequest().getJSONObject("Meta-data");
				metaDataRepo = MetaDataRepository.getInstance();
				
				//Retriving the node that are to be read
				String filePath = metaDataJson.getString("path");
				MetaDataRepositoryNode node = metaDataRepo.get(filePath);
				
				//if the node==null it did not exist and an error us sent as a response
				if(node == null){
					jsonResponse.put("Error", "No such file or directory");
					session.setResponse(jsonResponse);
					return false;
				}
								
				//If the file request is a dir.
				if(node.getFileType() == DataTypeEnum.DIR){
					
					JSONObject metadata = node.toJSON();
					JSONArray children = new JSONArray();
					
					//Fetching all the childern, if any of the dir an building the response
					MetaDataRepositoryNode[] nodes = metaDataRepo.getChildren(filePath);
					if(nodes != null){
						for (MetaDataRepositoryNode child : nodes) {
							children.put(child.toJSON());
						}
						metadata.put("Children", children);
					}
					
					
					jsonResponse.put("Meta-data", metadata);
					session.setResponse(jsonResponse);
					return true;
					
				//If node is a file, a the metadata and location of it is set as a response
				}else if(node.getFileType() == DataTypeEnum.FILE){
					jsonResponse.put("Meta-data", node.toJSON());
					session.setResponse(jsonResponse);
					return true;
				}else{
					jsonResponse.put("Error", "Data type is not defined");
					session.setResponse(jsonResponse);
					return false;
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		
		
		return false;
	}

	/*
	 * Parses session where mode is Write
	 * This will create the meta-data that is associated with the file and return it.
	 * If it is a dir that is to written it only stores it in the MetaDataRepository
	 * If it is a file that is to be written it stores the meta data in the MetaDataRepositoy and return the
	 *   metadata along with a list of what data nodes the raw data can be written to
	 */
	private boolean parseWrite(){
		JSONObject metaDataJson;
		JSONObject jsonResponse;
		MetaDataRepository metaDataRepo;
		try {
			//Fetches the metadata that is to written along with the MetaDataRepository
			metaDataJson = session.getRequest().getJSONObject("Meta-data");
			metaDataRepo = MetaDataRepository.getInstance();
			
			//Creates the header for the response
			jsonResponse = createJsonHeader("Response", "Meta-data", mode);
			
			String filePath = metaDataJson.getString("path");
			
			//Fetches the node that are to be written to, in case it is to be overwritten
			MetaDataRepositoryNode node = metaDataRepo.get(filePath);
			
			/*
			 * File to write dose not exist previously
			 */
			if(node == null){
				//Creates the new node with the metadata to be stored in the repository
				node = new MetaDataRepositoryNode();
				node.setFilePath(metaDataJson.getString("path"));
				node.setSize(metaDataJson.getLong("size"));
				node.setOwner(metaDataJson.getString("owner"));
				node.setGroup(metaDataJson.getString("group"));
				node.setCreated(metaDataJson.getString("created"));
				node.setLastEdited(metaDataJson.getString("lastEdited"));
				
				//Determines the the datatype
				if(metaDataJson.getString("type").equals("dir")){
					node.setFileType(DataTypeEnum.DIR);
				}else if(metaDataJson.getString("type").equals("file")){
					node.setFileType(DataTypeEnum.FILE);
					node.setStorageName(new FileNameOperations().createUniqName());
				}else{
					node.setFileType(null);
				}
				
				//Adding new node to MetaDataRepository
				if(!metaDataRepo.add(node.getKey(), node)){
					//This happens if one is trying to add a file or dir of which the parent dirs dose not exist
					jsonResponse.put("Error", "File could not be added to the FS, No such file or directory");
					session.setResponse(jsonResponse);
				
					return false;
				}
			//This in case of overwriting a file, we first check that is a file that we are trying to overwrite	
			}else if(node.getFileType() == DataTypeEnum.FILE){
				if(!metaDataJson.getString("type").equals("file")){
					jsonResponse.put("Error", "A file at that path already exist");
					session.setResponse(jsonResponse);
				
					return false;
				}

			//This in case of overwriting a dir, we first check that is a dir that we are trying to overwrite
			}else if(node.getFileType() == DataTypeEnum.DIR){
				if(!metaDataJson.getString("type").equals("dir")){
					jsonResponse.put("Error", "A dir at that path already exist");
					session.setResponse(jsonResponse);
					
					return false;
				}
			/*
			 * TODO: Bug would aper here in the case of overwriting the nested else if if statemets above 
			 * 		 should be change to only one else if statement, teste must be done.
			 * 	ex:	 else if(node.getFileType() == DataTypeEnum.DIR && !metaDataJson.getString("type").equals("dir"))   	
			 */
			}else{
				node.setSize(metaDataJson.getLong("size"));
				node.setOwner(metaDataJson.getString("owner"));
				node.setGroup(metaDataJson.getString("group"));
				node.setCreated(metaDataJson.getString("created"));
				node.setLastEdited(metaDataJson.getString("lastEdited"));
				metaDataRepo.replace(node.getKey(), node);
			}
			
			
			jsonResponse.put("Meta-data", node.toJSON());
			session.setResponse(jsonResponse);
			
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
		JSONObject tmpJson;
		JSONObject jsonResponse;
		MetaDataRepositoryNode fileMetaData;
		
		try {
			tmpJson = session.getRequest().getJSONObject("Meta-data");
			fileMetaData = MetaDataRepository.getInstance().get( tmpJson.getString("path") );
			jsonResponse = createJsonHeader("Response", "Meta-data", mode);
			jsonResponse.put("Meta-data", fileMetaData.toJSON());
			session.setResponse(jsonResponse);
			
		} catch (JSONException e) {
			e.printStackTrace();
			setErrorMsg("Meta-data or path did not exist in Header");
			return false;
		}
		return true;
	}
	
	
	/*
	 * Creats a standard JSON header for the sessions response 
	 */
	private JSONObject createJsonHeader(String stage, String type, String mode) throws JSONException{
		JSONObject json = new JSONObject();
		json.put("From", Config.getString("address"));
		json.put("To", session.getRequest().getString("From"));
		json.put("Stage", stage);
		json.put("Type", type);
		json.put("Mode", mode);
		
		JSONObject info = new JSONObject();
		JSONArray datanodes = DataNodeInfoRepository.getInstance().toJSONArray();

		info.put("datanodes", datanodes);
		json.put("Info", info);
		
		return json;
	}
}

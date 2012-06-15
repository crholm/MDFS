package mdfs.client.parser;

import mdfs.utils.Config;
import mdfs.utils.Time;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * A small help class that creates JSON headers to be used when communicates with different parts of MDFS
 * @author Rasmus Holm
 *
 */
public class JSONHeaderCreator {
	
	/**
	 * Creats a standard header in comunication
	 * @param user - username 
	 * @param pass - password
	 * @param to - the dns address of the recipient 
	 * @param stage - the Stage of the comunication
	 * @param type - the Type of the comunication
	 * @param mode - the Mode of the comunication
	 * @return a header in the form of a JSONObject
	 */
	public JSONObject creatStandardHeader(String user, String pass, String to, String stage, String type, String mode){
		JSONObject json = new JSONObject();
		try {
			json.put("From", Config.getString("address"));
			json.put("To", to);
			json.put("Stage", stage);
			json.put("Type", type);
			json.put("Mode", mode);
			json.put("User", user);
			json.put("Pass", pass);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Creates the JSON Header for the communication with the NameNode when a file is to be stored in MDFS.
	 * @param user - username for MDFS
	 * @param pass - password for MDFS
	 * @param path - path in MDFS where the file is to be stored
	 * @param type - type of eantity, file or dir
	 * @param size - the size of the file, 0 if dir
	 * @param permission the permission of the data
	 * @param owner the owner of the data
	 * @param group the groupe of the data
	 * @param created created time of the data
	 * @param lastEdited last edited of the data
	 * @return a JSON header
	 */
	public String putToNameNode(String user, String pass, String path, String type, long size, short permission, String owner, String group, String created, String lastEdited){
		JSONObject json = creatStandardHeader(	user, pass, Config.getString("NameNode.address"), 
																"Request", "Meta-data", "Write");
		JSONObject metaData = new JSONObject();
		
		try {
						
			metaData.put("path", path);
			metaData.put("type", type);
			metaData.put("size", size);
			metaData.put("permission", permission);
			metaData.put("owner", owner);
			metaData.put("group", group);
			metaData.put("created", created);
			metaData.put("lastEdited", lastEdited);
			
			json.put("Meta-data", metaData);
			
			return json.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creats the JSON header for communicating with the NameNode in the case of creating a dir on the MDFS file system
	 * @param user - username for MDFS
	 * @param pass - password for MDFS
	 * @param path - the absolute path in MDFS where the dir is to be created
	 * @param permission the permission of the data
	 * @param owner the owner of the data
	 * @param group the groupe of the data
	 * @return a JSON header
	 */
	public String mkdirToNameNode(String user, String pass, String path, short permission, String owner, String group){
		String time = Time.getTimeStamp(System.currentTimeMillis());
		return putToNameNode(user, pass, path, "dir", 0, permission, owner, group, time, time);
	}

	/**
	 * Creates the a JSON Header for the communication with a DataNode in the case of writing a file to the MDFS File system
	 * @param jsonNameNodeResponse - the text response from the NameNode when asked to write a file to MDFS
	 * @param to - the dns address of the DataNode that the file is to be written to.
	 * @return a JSON header
	 */
	public String putToDataNode(String jsonNameNodeResponse, String to) {
		try {
			
			JSONObject jsonMetaData = (new JSONObject(jsonNameNodeResponse)).getJSONObject("Meta-data");
			
			
			JSONObject json = creatStandardHeader("", "", to, 
												  "Request", "File", "Write");
			json.put("Meta-data", jsonMetaData);
			
			return json.toString();
		} catch (JSONException e) {
			System.out.println("Failed to parse JSON");
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * Creates the a JSON Header for the communication with the NameNode in the case of querying Meta-data for a file or dir
	 * @param user - username for MDFS
	 * @param pass - password for MDFS
	 * @param path - the absolute path in MDFS where the dir is to be created
	 * @return a JSON header
	 */
	public String lsToNameNode(String user, String pass, String path) {
		JSONObject json = creatStandardHeader(user, pass, Config.getString("NameNode.address"), "Request", "Meta-data", "Read");
		JSONObject metadata = new JSONObject();
		try {
			metadata.put("path", path);
			json.put("Meta-data", metadata);
			return json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		
	}


	/**
	 * Creates the a JSON Header for the communication with a DataNode in the case of retrieving a file.
	 * @param file - is the meta data of a file represented as a JSON Object
	 * @return JSON Header
	 */
	public JSONObject getFromDataNode(JSONObject file) {
		JSONObject json;
		try {
			json = creatStandardHeader("", "", "", "Request", "File", "Read");
			json.put("Meta-data", file);
		} catch (JSONException e) {
			json = null;
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * Creates a JSONheader to the name node in the case of removeing a file or dir
	 * @param user user name
	 * @param pass password 
	 * @param path path to new dir
	 * @return JSON header
	 */
	public JSONObject rmToNameNode(String user, String pass, String path) {
		JSONObject json;
		try {
			json = creatStandardHeader(user, pass, Config.getString("NameNode.address"), "Request", "Meta-data", "Remove");
			JSONObject metadata = new JSONObject();
			metadata.put("path", path);
			json.put("Meta-data", metadata);
		} catch (JSONException e) {
			json = null;
			e.printStackTrace();
		}
		return json;
	}
}

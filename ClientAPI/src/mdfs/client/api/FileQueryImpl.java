package mdfs.client.api;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import mdfs.client.parser.JSONHeaderCreator;

import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.parser.FileNameOperations;
/**
 * This is a basic implementation of FileQuery where the user gains access to a MDFS - file system that is specified in mdfs/config/config.cfg
 * @author Rasmus Holm
 *
 */
public class FileQueryImpl implements FileQuery{

	private String errorMessage = null;

	private String currentPath;

	@SuppressWarnings("unused")
	private JSONObject[] currenPathData = null;

	private String user;

	private String pass;

	private SocketFactory socketFactory = new SocketFactory();

	private SocketFunctions socketFunctions = new SocketFunctions();

	private JSONHeaderCreator headerCreator = new JSONHeaderCreator();

	private FileNameOperations fileNameOperations = new FileNameOperations();
	
	
	/**
	 * This constuctor loggs in and navigats to the home dir of the user ex. "/user"
	 * @param user - the username to be used to access the MDFS file system
	 * @param pass - the password to be used to access the MDFS file system
	 */
	public FileQueryImpl(String user, String pass){
		setUser(user);
		setPass(pass);
		cd("/" + user, null);
		
	}
	
	@Override
	public boolean cd(String path, String flag) {
		
		//Retrives all Meta-Data of the dir trying to move to
		JSONObject[] pathData = ls(path, null);
		
		//If nothing is returned the dir or file dose not exist, the cd operation fails
		if(pathData == null){
			return false;
		
		//Checks if it is a dir that are tried to move to 
		}else if(pathData[0].optString("type").equals("dir")){
			//If all was successful the current path is set as the one requested. And the metametadata is stored
			currentPath = fileNameOperations.escapePath(path, pwd(), user);
			currenPathData = pathData;
			return true;
		//If it was not a dir the cd operation fails
		}else{
			return false;
		}
	}
	
	@Override
	public JSONObject[] ls(String flag) {
		//Returns the ls for the current path
		return ls(pwd(), flag);
	}

	@Override
	public JSONObject[] ls(String path, String flag) {
		//builds a absolute path from a relative or absolute path
		path = fileNameOperations.escapePath(path, pwd(), user);
		
		//Creates a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
		//Creates a request to the name node 
		String request = headerCreator.lsToNameNode(user, pass, path);
		
		//Sends request to name node
		socketFunctions.sendText(nameNodeSocket, request);
		JSONObject response;
		JSONObject[] toBeReturned = new JSONObject[1];
		try {
			//Retrieves the response from the name node
			response = new JSONObject(socketFunctions.receiveText(nameNodeSocket));
			
			try {
			nameNodeSocket.close();
			} catch (IOException e) {}
			
			//If the response contains an Error the ls operation failed and null is returned
			if(response.has("Error")){
				
					errorMessage = response.optString("Error");
					
				
				return null;
			}
			
			//File contains the metadata of the ls request to the name node
			JSONObject file = response.getJSONObject("Meta-data");
			
			//If file requested has children they are stored in the array as well
			if(file.has("Children")){
				JSONArray children = file.getJSONArray("Children");
				file.remove("Children");
				
				int size = children.length();
				toBeReturned = new JSONObject[size+1];
				for(int i = 1; i <= size; i++){
					toBeReturned[i] = children.getJSONObject(i-1);
				}
			}
			//Adds the requested file to the array
			toBeReturned[0] = file;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			nameNodeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return toBeReturned;
	}

	
	@Override
	public String pwd() {
		//returns what is stored as the currentPath since no request has to be made
		return currentPath;
	}

	
	@Override
	public boolean rm(String path, String flag) {
		boolean result = true;
		
		//builds a absolute path from a relative or absolute path
		path = fileNameOperations.escapePath(path, pwd(), user);
		
		try {
			//Handles the flag 'r' which makes the rm command recursive
			if(flag != null && flag.contains("r")){
				
				//Retrieves the children of the path that are to be removed
				JSONObject[] files = ls(path, flag);
				
				if(files == null){
					errorMessage = " No such file or directory";
					return false;
				}
				
				//Recursively removes all children before the parent are removed
				for (int i = 1; i < files.length; i++) {
					String type;
					type = files[i].getString("type");
					//If a child is a file we simply remove it
					if(type.equals("file")){
						if(!rm(files[i].getString("path"), null)){
							result = false;
						}
					//If a child is a dir we first remove all its children
					}else if(type.equals("dir")){
						if(!rm(files[i].getString("path"), flag)){
							result = false;
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//Creats a request to the name node to remove the path, and all children should now have been removed
		JSONObject request = headerCreator.rmToNameNode(user, pass, path);
		
		if(request == null){
			errorMessage = "Parameters are invalid";
			return false;
		}
		
		//Creats a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
		if(nameNodeSocket == null ){
			errorMessage = "Could not connect to NameNode";
			return false;
		} 
		
		//Send the the request to the name node
		socketFunctions.sendText(nameNodeSocket, request.toString());
		
		try {
			//Retrivse the resonse from the name node, the name node itself handels the
			//removal of the raw data from the data-nodas
			JSONObject response = new JSONObject(socketFunctions.receiveText(nameNodeSocket));
			try {
				nameNodeSocket.close();
			} catch (IOException e) {}
			
			//Checks if there is an error in the removal of the file
			if(response.has("Error")){
				errorMessage = response.getString("Error");
				
				return false;
			}
			
			//Checks if the removal was successfull and if so returns the result
			JSONObject info = response.getJSONObject("Info");
			if(info.getString("removed").equals("successful")){
				return result;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			errorMessage = "Response from NameNode was invalid.";
			return false;
		}
		//Returns false if the removal was not successful
		return false;
	}

	//Not implemented yet.
	@Override
	public InputStream get(String sourcePath, String flag) {
		return null;
	}

	/*
	 * TODO implement recursive get.
	 * (non-Javadoc)
	 * @see mdfs.client.api.FileQuery#get(java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public boolean get(String sourcePath, File targetFile, String flag) {
		//Retrives information about the requested file from the name node
		JSONObject[] file = ls(sourcePath, null);
		
		//Checks if it is a dir with children was requested and returns false if so
		//Recursiv support is not yet implemented
		if(file.length < 1) 
			return false;
		
		//If the request was for exactly one file it is fetched from one of the data nodes
		else if(file.length == 1){
			
			//Creates a request for the wanted file from a data node 
			JSONObject request = headerCreator.getFromDataNode(file[0]);
			Socket dataNodeSocket;
			try {
				//Generates a array of all the data nodes that currently are storing the requested file 
				String hosts[] = file[0].getJSONObject("Location").getJSONArray("hosts").join(" ").replace("\"", "").split(" ");
				//Creates a socket to one of the data nodes, the first one to respond 
				dataNodeSocket = socketFactory.createSocket(hosts);
				
				//if no socket could be created 
				if(dataNodeSocket == null) throw new IOException();
		
				//Changes the header to the right data node of the request
				request.put("To", dataNodeSocket.getInetAddress().getHostName());
				
				//Sends request
				socketFunctions.sendText(dataNodeSocket, request.toString());
				
				//Retrives the response as a JSONObject
				JSONObject response = new JSONObject(socketFunctions.receiveText(dataNodeSocket));
				
				//Exites and returns false if it contains an error
				if(response.has("Error")){
					errorMessage = response.optString("Error");
					return false;
				}
					
				//Recives the file from the datanode and stores on the local FS
				socketFunctions.receiveFile(dataNodeSocket, targetFile);
				try {
					dataNodeSocket.close();
				} catch (IOException e) {}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				errorMessage = "Could not create socket to DataNode";
				return false;
			}
			
			
		}else{
			
		}
		
		return true;
	}

	@Override
	public boolean put(File sourceFile, String flag) {
		return put(sourceFile, pwd() + "/" + sourceFile.getName(), flag);
	}

	@Override
	public boolean put(File sourceFile, String targetPath, String flag) {
		//builds a absolute path from a relative or absolute path
		targetPath = fileNameOperations.escapePath(targetPath, pwd(), user);
		
			
		//if it is a dir that is to be put to the FS
		if(sourceFile.isDirectory()){
			//Adding dir to file system
			mkdir(targetPath);
			boolean noError = true;
			
			//Recursively adding all files and dirs to MDFS
			if(flag.contains("r")){
				//Getting all children of the file
				File[] files = sourceFile.listFiles();
				for (File file : files) {
					//Recursively adding all children to MDFS
					if(!put(file, targetPath + "/" + file.getName(), flag)){
						errorMessage = "Error writing " + file.getAbsolutePath() + " to MDFS";
						//System.out.println(errorMessage);
						noError = false;
					}
				}				
			}
			return noError;
			
		}else{	
			
			
			
			//Creating reqest to name node to put file in MDFS
			String request = headerCreator.putToNameNode(	user, pass, targetPath, "file", 
															sourceFile.length(), (short)640, user, user, 
															Time.getTimeStamp(sourceFile.lastModified()), 
															Time.getTimeStamp(sourceFile.lastModified()));
			
			//Creates a socket to the name node
			Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
																Config.getInt("NameNode.port") );
			
			//Sends request to name node
			socketFunctions.sendText(nameNodeSocket, request);
			
			//Retrives response from name node
			String response = socketFunctions.receiveText(nameNodeSocket);			
			
			try {
				nameNodeSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
			Socket dataNodeSocket = null;
			try {
				JSONArray dataNodes = null;
				String[] hosts = null;
				JSONObject json = new JSONObject(response);
				
				//Returns false if there was a error from the name node
				if(json.has("Error")){
					return false;
					
				//If file already exists, it connects to DataNode for overwriting	
				}else if( json.getJSONObject("Meta-data").getJSONObject("Location").has("hosts") ){
					dataNodes = json.getJSONObject("Meta-data").getJSONObject("Location").getJSONArray("hosts");
					
				//New file and there for connects to a "random" available Data Node	
				}else{
					dataNodes = json.getJSONObject("Info").getJSONArray("datanodes");
					
				}
				//Builds a list of hosts that the file are to be written to
				hosts = dataNodes.join(" ").replace("\"", "").split(" ");
				//Creats a socket to the first responent of the host list
				dataNodeSocket = socketFactory.createSocket(hosts);
				
				//Returns false if a socket to the datanodes could not be created
				if(dataNodeSocket == null){
					return false;
				}
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Creates a request to the data nodes
			request = headerCreator.putToDataNode(response, dataNodeSocket.getInetAddress().getHostName());
			
			//Sends request and file to data node.
			socketFunctions.sendText(dataNodeSocket, request);
			socketFunctions.sendFile(dataNodeSocket, sourceFile);
			
			//Receives response from data node
			response = socketFunctions.receiveText(dataNodeSocket);
			
			try {
				dataNodeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}			
		
	}



	@Override
	public boolean mv(String sourcePath, String targetPath, String flag) {
		sourcePath = fileNameOperations.escapePath(sourcePath);
		targetPath = fileNameOperations.escapePath(targetPath);
		//TODO Implement mv
		return false;
	}

	@Override
	public boolean chmod(String targetPath, short octalPermission, String flag) {
		targetPath = fileNameOperations.escapePath(targetPath);
		// TODO Implement chmod
		return false;
	}

	@Override
	public boolean chown(String targetPath, String owner, String group, String flag) {
		targetPath = fileNameOperations.escapePath(targetPath);
		// TODO Implement chown
		return false;
	}


	@Override
	public void setUser(String user) {
		this.user = user;
	}


	@Override
	public void setPass(String pass) {
		this.pass = pass;
	}

	@Override
	public boolean mkdir(String targetPath) {
		//builds a absolute path from a relative or absolute path
		targetPath = fileNameOperations.escapePath(targetPath, currentPath, user);
		
		//Creates a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
					
		//Creates a request to the name node
		String request = headerCreator.mkdirToNameNode(user, pass, targetPath, (short)640, user, user);
		
		//Sends request to name node
		socketFunctions.sendText(nameNodeSocket, request);
		
		//Receives the response from the name node
		String response = socketFunctions.receiveText(nameNodeSocket);
		
		try {
			nameNodeSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			//If the response contains errors mkdir returns false;
			JSONObject json = new JSONObject(response);
			if(json.has("Error")){
				return false;
			}else{
				return true;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		
		return false;
	}

	@Override
	public String getError() {
		return errorMessage;
	}

}

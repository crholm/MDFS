package mdfs.datanode.io;

import java.io.File;
import java.io.IOException;
import java.net.Socket;




import mdfs.utils.Config;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.parser.FileNameOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Handles replication of raw data between Data Nodes.
 * @author Rasmus Holm
 *
 */
// TODO implement intelligent way of replicating to load balance

public class Replicator implements Runnable{
	JSONArray hosts = new JSONArray();
	String metadata;
	String filePath;
	
	/**
	 * Handels all replication in case of a file being overwritten. 
	 * I starts a thread that handels the replication itself.
	 * @param metadata the JSON metadata that represents the file to be replicated
	 */
	public void overwriteFile(JSONObject metadata){
		//Sets the metadata of the file to be replicated
		this.metadata = metadata.toString();
		
		try {
			//Get the location of which the old version of the file is stored
			JSONObject location = metadata.getJSONObject("Location");
			
			//Retrieves the full path on the local FS of the file to be replicated
			filePath = new FileNameOperations().translateFileNameToFullPath(location.getString("name"));
			
			//An array containing all hosts the file
			JSONArray hosted = location.getJSONArray("hosts");
			
			//Creates a reference string to exclude the current data node
			String thisHost = Config.getString("address") + ":" + Config.getString("port");
			
			//Adds all other hosts to the array of which to replicate the file to
			for(int i = 0; i < hosted.length(); i++){
				if(!thisHost.equals( hosted.getString(i) )){
					hosts.put(hosted.getString(i));
				}
			}
			
			new Thread(this).start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Handles all replication in case of a new file being written. 
	 * I starts a thread that handles the replication itself.
	 * @param metadata the JSON metadata that represents the file to be replicated
	 */
	public void newFile(JSONObject metadata){

		this.metadata = metadata.toString();
		
		try {
			//Get the location of which the old version of the file is stored
			JSONObject location = metadata.getJSONObject("Location");
			
			//Retrieves the full path on the local FS of the file to be replicated
			filePath = new FileNameOperations().translateFileNameToFullPath(location.getString("name"));
			
			//Retrieves the ratio or rather to how many data nodes the file are to be replicated if possible
			int ratio = location.getInt("ratio");			
			
			//Creates a reference string to exclude the current data node
			String thisHost = Config.getString("address") + ":" + Config.getString("port");
			
			//Get available hosts specified in the config.
			String[] dataNodesAddress = Config.getStringArray("datanode.address");
			String[] dataNodesPort = Config.getStringArray("datanode.port");
			String[] dataNodes = new String[dataNodesAddress.length];
			
			//Build a usable array for datanodes
			for (int i = 0; i < dataNodes.length; i++) {
				dataNodes[i] = dataNodesAddress[i] + ":" + dataNodesPort[i];
			}
			
			//Adds the data node to the array of which the file are to be replicated to
			for(int i = 0; i<ratio && i < dataNodes.length; i++){
				if(!thisHost.equals(dataNodes[i]))
					this.hosts.put(dataNodes[i]);
			}
			
			
			new Thread(this).start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * This thread replicates the file specified to all the specified locations
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			JSONObject metadata = new JSONObject(this.metadata);
			JSONObject json = new JSONObject();
			
			SocketFactory socketFactory = new SocketFactory();
			SocketFunctions socketFunctions = new SocketFunctions();
			Socket socket;
			File file;
			
			//Creates the message to the other data nodes
			json.put("From", Config.getString("address"));
			json.put("Stage", "Request");
			json.put("Type", "File");
			json.put("Mode", "Cascade");
			json.put("Meta-data", metadata);
			
			//Loops through and send the file to each data node the file is to be replicated to
			for(int i = 0; i < hosts.length(); i++){
				//Splitt the address into host[0] = address, host[1] = port
				String[] host = hosts.getString(i).split(":");
				json.put("To", host[0]);
				
				//Creates a file for the file to be sent.
				file = new File(filePath);
				//Creates a socket to the data node
				socket = socketFactory.createSocket(host[0], Integer.parseInt(host[1]));
				
				//Checks that a socket to the data node was created.
				if(socket != null){
					socketFunctions.sendText(socket, json.toString());
					socketFunctions.sendFile(socket, file);
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}
			
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
}

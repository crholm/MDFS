package mdfs.namenode.io;

import java.io.IOException;
import java.net.Socket;

import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.utils.Config;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * This object quries diffrent datanodes. It provides a way for the name node to tell the datanodes what to do.
 * @author Rasmus Holm
 *
 */
public class DataNodeQuerier implements Runnable{
	private SocketFactory socketFactory = new SocketFactory();
	private SocketFunctions socketFunctions = new SocketFunctions();
	private JSONObject query = null;
	private DataNodeInfoRepositoryNode[] dataNodes = null; 
	
	/**
	 * This method will remove all raw data that are deposited on the diffrent datanodes from them.
	 * @param node contains the metadata of the file that is to be removed
	 */
	public void removeData(MetaDataRepositoryNode node){
		//Fetches all the Datanodes that are currently storing the raw data of the File
		dataNodes = node.getLocations();
		
		//Builds the query that is sent to eatch datanode
		query = new JSONObject();
		try {
			query.put("From", Config.getString("address"));
			
			query.put("Stage", "Request");
			query.put("Type", "File");
			query.put("Mode", "Remove");
			
			query.put("Meta-data", node.toJSON());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//Starts the thread that will send the query
		new Thread(this).start();
	}
	
	/*
	 * The thread simply sends the query stored in this object to each datanode that are set as well 
	 */
	@Override
	public void run() {
		Socket dataNodeSocket;
		//Loops all the nodes
		for (DataNodeInfoRepositoryNode node : dataNodes) {
			try {
				
				query.put("To", node.getAddress());
				
				//Creates a socket to one datanode
				dataNodeSocket = socketFactory.createSocket(node.getAddress(), Integer.parseInt(node.getPort())); 
				if(dataNodeSocket != null){
					//Sends query
					socketFunctions.sendText(dataNodeSocket, query.toString());
					try {
						dataNodeSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
			
		}
	}
}

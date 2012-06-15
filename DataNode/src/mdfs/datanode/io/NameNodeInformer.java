package mdfs.datanode.io;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;

/**
 * This class informs the NameNode of changes to the local file system in regard to the MDFS file system.
 * Nothing is done until it is started as a thread on which the Name Node is updatede.
 * @author Rasmus Holm
 *
 */
public class NameNodeInformer implements Runnable{
	private String type;
	private String mode;
	private JSONObject info;
	private SocketFunctions socketFunctions = new SocketFunctions();
	private SocketFactory socketFactory = new SocketFactory();
	
	
	/**
	 * This method Starts a thread of it self that send info to the name node
	 * @param type the Type in which the changes were made
	 * @param mode the Mode in which the changes were made
	 * @param info JSONObeject that represents what the NameNode needs to know in regard to the changes made
	 */
	public void newDataLocation(String type, String mode, JSONObject info){
		//Supplies information to the NameNode Informer and starts it
		this.type = type;
		this.mode = mode;
		this.info = info;
		new Thread(this).start();
	}
	
	
	@Override
	public void run() {
		
		
		JSONObject json = new JSONObject();
		try {
			//Builds a message to the name node
			json.put("From", Config.getString("address"));
			json.put("To", Config.getString("NameNode.address"));
			json.put("Stage", "Info");
			json.put("Type", type);
			json.put("Mode", mode);
			json.put("Info", info);
			
			try {
				//Creates a socket to the name node
				Socket socket = socketFactory.createSocket(Config.getString("NameNode.address"), Config.getInt("NameNode.port"));
				
				Verbose.print("Informing Namenode of data location: " + json.toString(), this, Config.getInt("verbose")-2);
				
				//Sening information to the name node
				socketFunctions.sendText(socket, json.toString());
				
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		
		
	}

}

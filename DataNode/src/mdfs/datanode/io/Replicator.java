package mdfs.datanode.io;

import mdfs.utils.Config;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolLocation;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import mdfs.utils.parser.FileNameOperations;

import java.io.File;
import java.io.IOException;
import java.net.Socket;


/**
 * Handles replication of raw data between Data Nodes.
 * @author Rasmus Holm
 *
 */
// TODO implement intelligent way of replicating to load balance

public class Replicator implements Runnable{
	String hosts[];
	MDFSProtocolMetaData metadata;
	String filePath;
    String logicalPath;
    String fileName;
	
	/**
	 * Handels all replication in case of a file being overwritten. 
	 * I starts a thread that handels the replication itself.
	 * @param metadata the JSON metadata that represents the file to be replicated
	 */
	public void overwriteFile(MDFSProtocolMetaData metadata){
		//Sets the metadata of the file to be replicated
		this.metadata = metadata;

        //Get the location of which the old version of the file is stored
        MDFSProtocolLocation location = metadata.getLocation();

        //Retrieves the full path on the local FS of the file to be replicated
        fileName = location.getName();
        filePath = new FileNameOperations().translateFileNameToFullPath(fileName);


        //An array containing all hosts the file
        int size = location.getHostsSize();
        String hosted[] = new String[size];
        hosted = location.getHostsArray(hosted);

        //Creates a reference string to exclude the current data node
        String thisHost = Config.getString("address") + ":" + Config.getString("port");

        //Adds all other hosts to the array of which to replicate the file to
        hosts = new String[size-1];
        int j = 0;
        for(int i = 0; i < hosted.length; i++){
            if(!thisHost.equals(hosted[i])){
                hosts[j] = hosted[i];
                j++;
            }
        }

        new Thread(this).start();

	}
	
	
	/**
	 * Handles all replication in case of a new file being written. 
	 * I starts a thread that handles the replication itself.
	 * @param metadata the JSON metadata that represents the file to be replicated
	 */
	public void newFile(MDFSProtocolMetaData metadata){

		this.metadata = metadata;

			//Get the location of which the old version of the file is stored
			MDFSProtocolLocation location = metadata.getLocation();
			
			//Retrieves the full path on the local FS of the file to be replicated
			filePath = new FileNameOperations().translateFileNameToFullPath(location.getName());
			
			//Retrieves the ratio or rather to how many data nodes the file are to be replicated if possible
			int ratio = location.getRatio();
			
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
			
			hosts = dataNodes;
			
			
			new Thread(this).start();
		
	}
	
	/*
	 * This thread replicates the file specified to all the specified locations
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
            MDFSProtocolHeader header = new MDFSProtocolHeader();

			SocketFactory socketFactory = new SocketFactory();
			SocketFunctions socketFunctions = new SocketFunctions();
			Socket socket;
			File file;
			
			//Creates the message to the other data nodes

			header.setStage(Stage.REQUEST);
            header.setType(Type.FILE);
            header.setMode(Mode.CASCADE);
            header.setMetadata(metadata);

			//Loops through and send the file to each data node the file is to be replicated to
			for(int i = 0; i < hosts.length; i++){
                header.setInfo(new MDFSProtocolInfo());
                header.getInfo().addToken(metadata.getPath(), fileName, Mode.CASCADE, Config.getString("Token.key"));

				//Splitt the address into host[0] = address, host[1] = port

				String[] host = hosts[i].split(":");

				//Creates a file for the file to be sent.
				file = new File(filePath);

				//Creates a socket to the data node
				socket = socketFactory.createSocket(host[0], Integer.parseInt(host[1]));
				
				//Checks that a socket to the data node was created.
				if(socket != null){
					socketFunctions.sendText(socket, header.toString());
					socketFunctions.sendFile(socket, file);
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}

	}
	
	
	
}

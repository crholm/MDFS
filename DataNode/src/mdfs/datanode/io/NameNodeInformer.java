package mdfs.datanode.io;

import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;

import java.io.IOException;
import java.net.Socket;

/**
 * This class informs the NameNode of changes to the local file system in regard to the MDFS file system.
 * Nothing is done until it is started as a thread on which the Name Node is updatede.
 * @author Rasmus Holm
 *
 */
public class NameNodeInformer implements Runnable{
	private Type type;
	private Mode mode;
	private MDFSProtocolInfo info;
	private SocketFunctions socketFunctions = new SocketFunctions();
	private SocketFactory socketFactory = new SocketFactory();
	
	
	/**
	 * This method Starts a thread of it self that send info to the name node
	 * @param type the Type in which the changes were made
	 * @param mode the Mode in which the changes were made
	 * @param info JSONObeject that represents what the NameNode needs to know in regard to the changes made
	 */
	public void newDataLocation(Type type,  Mode mode, MDFSProtocolInfo info){
		//Supplies information to the NameNode Informer and starts it
		this.type = type;
		this.mode = mode;
		this.info = info;
		new Thread(this).start();
	}
	
	
	@Override
	public void run() {
		
		
		MDFSProtocolHeader header = new MDFSProtocolHeader();

        //Builds a message to the name node

        header.setStage(Stage.INFO);
        header.setType(type);
        header.setMode(mode);
        header.setInfo(info);
        info.addToken(info.getPath(), info.getName(), mode, Config.getString("Token.key"));

        try {
            //Creates a socket to the name node
            Socket socket = socketFactory.createSocket(Config.getString("NameNode.address"), Config.getInt("NameNode.port"));

            Verbose.print("Informing Namenode of data location: " + header.toString(), this, Config.getInt("verbose")-2);

            //Sening information to the name node
            socketFunctions.sendText(socket, header.toString());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

		
		
	}

}

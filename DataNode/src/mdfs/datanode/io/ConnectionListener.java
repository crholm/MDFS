package mdfs.datanode.io;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mdfs.utils.Config;
import mdfs.utils.Verbose;

/**
 * Creates a Server socket that listen to a port. When a connection is accepted a new thread of the 
 * type ConnectionSheppard is created that handles it.
 * @author Rasmus Holm
 *
 */
public class ConnectionListener {
	/**
	 * Starts a ConnectionListener at given port. When a connection is received it creates a thread of the type 
	 * {@link ConnectionSheppard} which handles the connection until until it is closed.
	 * @param port port to listen to
	 */
	public ConnectionListener(int port){
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				System.err.println("Could not listen on port: " + port);
				System.exit(1);
			}
		Verbose.print("Listening to port: " + port, this, Config.getInt("verbose")+1);
		while(true){	
			try {
				clientSocket = serverSocket.accept();
				Verbose.print("Server accepted connection", this, Config.getInt("verbose")-2);
				Thread client = new Thread(new ConnectionSheppard(clientSocket));
				client.start();
			} catch (IOException e) {
				System.err.println("Accept failed.");
			}	
		}
		
		

		
	}
}

package mdfs.namenode.io;

import mdfs.utils.Config;
import mdfs.utils.Verbose;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Creats a Server socket that listen to a port. When a connectionen is accepted a new thread of the 
 * type ConnectionSheppard is created that handles it.
 * @author Rasmus Holm
 *
 */
public class ConnectionListener implements Runnable{
	private ServerSocket serverSocket;




    public ConnectionListener(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }


    @Override
    public void run() {
        Socket clientSocket = null;

        Verbose.print("Listening to port: " + serverSocket.getLocalPort(), this, Config.getInt("verbose"));
        while(true){
            try {
                //When a connection is accepted a new Thread is started handling it
                clientSocket = (Socket)serverSocket.accept();
                Verbose.print("Server accepted connection", this, Config.getInt("verbose"));
                Thread client = new Thread(new ConnectionSheppard(clientSocket));
                client.start();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Accept failed. " + e);
            }

        }
    }
}

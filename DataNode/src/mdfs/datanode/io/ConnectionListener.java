package mdfs.datanode.io;

import mdfs.utils.Config;
import mdfs.utils.Verbose;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Creates a Server socket that listen to a port. When a connection is accepted a new thread of the 
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
        Verbose.print("Listening to port: " + serverSocket.getLocalPort(), this, Config.getInt("verbose")+1);
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

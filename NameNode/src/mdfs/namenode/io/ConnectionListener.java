package mdfs.namenode.io;

import mdfs.utils.Config;
import mdfs.utils.ThreadPool;
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
    private static ThreadPool sharedPool;



    public ConnectionListener(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
        if(sharedPool == null){
            sharedPool = new ThreadPool(5, 15, 3000, 300);
        }
    }


    @Override
    public void run() {
        Socket clientSocket;
        ConnectionSheppard job;
        Verbose.print("Listening to port: " + serverSocket.getLocalPort(), this, Config.getInt("verbose"));
        while(true){
            try {
                //When a connection is accepted a new Thread is started handling it
                clientSocket = (Socket)serverSocket.accept();
                Verbose.print("Server accepted connection", this, Config.getInt("verbose"));

                job = new ConnectionSheppard(clientSocket);

                sharedPool.execute(job);
            }catch (IOException e) {
                e.printStackTrace();
                System.err.println("Accept failed. " + e);
            }

        }
    }
}

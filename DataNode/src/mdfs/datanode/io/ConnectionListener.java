package mdfs.datanode.io;

import mdfs.utils.Config;
import mdfs.utils.ThreadPool;
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
    private static ThreadPool sharedPool;



    public ConnectionListener(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
        if(sharedPool == null){
            sharedPool = new ThreadPool(5, 15, 3000, 300);
        }
    }


    @Override
    public void run() {
        Socket clientSocket = null;
        Verbose.print("Listening to port: " + serverSocket.getLocalPort(), this, Config.getInt("verbose")+1);
        ConnectionSheppard job;
        while(true){
            try {
                clientSocket = serverSocket.accept();
                Verbose.print("Server accepted connection", this, Config.getInt("verbose")-2);

                job = new ConnectionSheppard(clientSocket);
                sharedPool.execute(job);

                //Thread client = new Thread();
                //client.start();
            } catch (IOException e) {
                System.err.println("Accept failed.");
            }
        }
    }
}

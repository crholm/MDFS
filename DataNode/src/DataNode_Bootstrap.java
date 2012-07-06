import mdfs.datanode.io.ConnectionListener;
import mdfs.utils.Config;
import mdfs.utils.io.EServerSocket;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Simply starts the DataNode. No interaction with a user is possible
 * @author Rasmus Holm
 *
 */
public class DataNode_Bootstrap {
	public static void main(String[] args){
		System.out.println("DataNode:");

        try {

            if(Config.getInt("noSSL") == 1){
                ServerSocket serverSocket = new ServerSocket(Config.getInt("port"));
                new Thread(new ConnectionListener(serverSocket)).start();
            }

            if(Config.getInt("SSL") == 1){

                ServerSocket serverSocket = new EServerSocket(Config.getInt("SSLport"));
                new Thread(new ConnectionListener(serverSocket)).start();

                /*
                SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(Config.getInt("SSLport"));
                new Thread(new ConnectionListener(sslServerSocket)).start();
                */
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

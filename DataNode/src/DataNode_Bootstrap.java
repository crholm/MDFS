import mdfs.datanode.io.ConnectionListener;
import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.Verbose;
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

        Verbose.print("DataNode", null, 1);


        Time.syncTime(Config.getString("NameNode.address"), Config.getInt("NameNode.port"));

        try {

            if(Config.getInt("unsecured") == 1){
                ServerSocket serverSocket = new ServerSocket(Config.getInt("port"));
                new Thread(new ConnectionListener(serverSocket)).start();
            }

            if(Config.getInt("Encrypted") == 1){
                ServerSocket serverSocket = new EServerSocket(Config.getInt("port")+10);
                new Thread(new ConnectionListener(serverSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

package mdfs.namenode.tests;

import mdfs.namenode.io.ConnectionListener;
import mdfs.namenode.repositories.DataNodeInfoRepository;
import mdfs.namenode.repositories.GroupDataRepository;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.UserDataRepository;
import mdfs.utils.Config;
import mdfs.utils.io.EServerSocket;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This bootstraps and starts the Name Node an no interaction from a user i available after it is started.
 * It also adds some users that can access MDFS
 * @author Rasmus Holm
 *
 */
public class NameNode_Test {
	public static void main(String[] args){
        System.out.println("Loading user Reopsitory...");
        UserDataRepository.getInstance();
        System.out.println("Loading group Reopsitory...");
        GroupDataRepository.getInstance();
        System.out.println("Loading DataNode Repository.. .");
        DataNodeInfoRepository.getInstance();
        System.out.println("Loading MetaData Repository...");
        MetaDataRepository.getInstance();



        UserDataRepository.getInstance().add("raz", "qwerty");
        UserDataRepository.getInstance().add("test1", "test1");
        UserDataRepository.getInstance().add("test2", "test2");

        System.out.println("NameNode:");

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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
	}
}



import mdfs.namenode.io.ConnectionListener;
import mdfs.namenode.repositories.DataNodeInfoRepository;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.UserDataRepository;
import mdfs.utils.Config;

/**
 * This bootstraps and starts the Name Node an no interaction from a user i available after it is started.
 * @author Rasmus Holm
 *
 */
public class NameNode_Bootstrap {
	public static void main(String[] args){
		System.out.println("Loading user Reopsitory...");
		UserDataRepository.getInstance();
		System.out.println("Loading DataNode Repository.. .");
		DataNodeInfoRepository.getInstance();
		System.out.println("Loading MetaData Repository...");
		MetaDataRepository.getInstance();
		
		System.out.println("NameNode:");
		new ConnectionListener(Config.getInt("port"));
	}
}

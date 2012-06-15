

import mdfs.datanode.io.ConnectionListener;
import mdfs.utils.Config;

/**
 * Simply starts the DataNode. No interaction with a user is possible
 * @author Rasmus Holm
 *
 */
public class DataNode_Bootstrap {
	public static void main(String[] args){
		System.out.println("DataNode:");
		new ConnectionListener(Config.getInt("port"));
	}
}

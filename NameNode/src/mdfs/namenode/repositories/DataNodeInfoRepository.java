package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.Config;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores Information about available DataNodes
 * As this is a shared recorese it is a Singelton and synchronized by {@link ReentrantLock}
 * The lock works in the manner that if the thread holing the lock would crach, the lock will still be released
 * @author Rasmus Holm
 *
 */
public class DataNodeInfoRepository {
	private ReentrantLock lock = new ReentrantLock(true);
	
	private static DataNodeInfoRepository instance = null;
	
	//listByAddress, the key is built like "address:port"
	private ConcurrentSkipListMap<String, DataNodeInfoRepositoryNode> listByAddress = new ConcurrentSkipListMap<String, DataNodeInfoRepositoryNode>();

	//listByName, the key is the name of the data node which should be uniq
	private ConcurrentSkipListMap<String, DataNodeInfoRepositoryNode> listByName = new ConcurrentSkipListMap<String, DataNodeInfoRepositoryNode>();

    /**
     * Used to gain access to the shared repository
     * @return the instance of the DataNodeInfoRepository
     */
    public static DataNodeInfoRepository getInstance(){
        if(instance == null){
            instance = new DataNodeInfoRepository();
        }
        return instance;
    }

	private DataNodeInfoRepository(){
		lock.lock();
		try{
			load();
		}finally{
			lock.unlock();
		}
	}
	
	/*
	 * Loads all data nodes info in to the repo.
	 * The method load the data nodes avalible from the local config file but stores them in the
	 * MySQL permanent storage aswell, to enable the realation between metadata and nodes when loading.
	 * 
	 * TODO: A dynamic implementation that enables datanodes to connect without the namenode having prier 
	 * 		 knowledge about it
	 */
	private void load(){
		
		//Gets information about the data nods from the config file
		String[] address = Config.getStringArray("datanode.address");
		String[] name = Config.getStringArray("datanode.name");
		String[] port = Config.getStringArray("datanode.port");

		//Adds all the nodes to the Repository
		for(int i = 0; i < address.length; i++){
			DataNodeInfoRepositoryNode node = new DataNodeInfoRepositoryNode();
			node.setAddress(address[i]);
			node.setName(name[i]);
			node.setPort(port[i]);
			listByName.put(node.getName(), node);
			listByAddress.put(node.getAddress()+":"+node.getPort(), node);
		}
		
		//Updates the MySQL database with the valuse from the config.
		for(DataNodeInfoRepositoryNode node : listByAddress.values()){
			MySQLUpdater.getInstance().update(node);
		}
	}
	

	
	/**
	 * Returns the DataNodeInfo that relates to the dns address that is given.
	 * @param address is the dns address that relates to the data node. ex. node.example.com
	 * @param port is the port that the data node is listening to
	 * @return the DataNode asked for, null if unavailable
	 */
	public DataNodeInfoRepositoryNode get(String address, String port){
		DataNodeInfoRepositoryNode node = null;
		lock.lock();
		try{
			node = listByAddress.get(address + ":" + port);
		}finally{
			lock.unlock();
		}
		return node; 		
	}
	
	/**
	 * Returns the DataNodeInfo that relates to the name that is given.
	 * @param name of the data node
	 * @return the DataNode asked for, null if unavailable
	 */
	public DataNodeInfoRepositoryNode get(String name){
		DataNodeInfoRepositoryNode node = null;
		lock.lock();
		try{
			node = listByName.get(name);
		}finally{
			lock.unlock();
		}
		return node; 
	}
	/**
	 * Return all the DataNodeInfo stored in the repository represented by a JSONArray
	 * Each node will be one string element in the JSONArray in the form of "address:port"
	 * @return all the DataNodeInfo stored in the repository represented by a JSONArray
	 */
	public JSONArray toJSONArray() {
		JSONArray array = null;
		lock.lock();
		try{
			array = new JSONArray();
			
			for (DataNodeInfoRepositoryNode node : listByAddress.values()) {
				array.put(node.getAddress() + ":" + node.getPort());
			}
		}finally{
			lock.unlock();
		}
		return array;
	}

    public LinkedList<String> toList() {
        LinkedList<String> list = null;
        lock.lock();
        try{
            list = new LinkedList<String>();

            for (DataNodeInfoRepositoryNode node : listByAddress.values()) {
                list.add(node.getAddress() + ":" + node.getPort());
            }
        }finally{
            lock.unlock();
        }
        return list;
    }
	
	
}

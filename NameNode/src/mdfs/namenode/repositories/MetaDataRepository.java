package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLFetch;
import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.FSTree;
import mdfs.utils.io.protocol.enums.MetadataType;
import mdfs.utils.parser.FileNameOperations;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A implementation of MetaDataRepository that is synchronized via {@link ReentrantLock}
 * for allowing multiple threads. When a lock is acquired all other operations on the MetaDataRepository
 * are locked. If the thread that holds the lock fails/craches, The lock will be unlocked.
 * 
 * @author Rasmus Holm
 *
 */

public class MetaDataRepository {
	
	private static MetaDataRepository instance = null;
	private ReentrantLock lock = new ReentrantLock();
	private FSTree<MetaDataRepositoryNode> repository;

	
	private MetaDataRepository(){
		lock.lock();
		try{
			load();
		}finally{
			lock.unlock();
		}
	}
	
	public static MetaDataRepository getInstance(){
		if(instance == null){
			instance = new MetaDataRepository();
		}
		return instance;
	}
	
	/**
    * Retrieves the {@link MetaDataRepositoryNode} that is asked for
    * @param key is the same as the logical path the file/node in MDFS
    * @return the MetaDataRepositoryNode that was requester, null if not exist
    */
	public MetaDataRepositoryNode get(String key) {
		MetaDataRepositoryNode node = null;
		lock.lock();
		try{
			node = repository.get(key);
		}finally{
			lock.unlock();
		}
		return node;
	}
    public MetaDataRepositoryNode getParent(String key) {
        lock.lock();
        try{
            MetaDataRepositoryNode node = get(new FileNameOperations().parentPath(key));
            return node;
        }finally {
            lock.unlock();
        }
    }
	
	public boolean hasChildren(String key) {
		lock.lock();
		boolean result = false;
		try{
			result = repository.hasChildern(key);
		}finally{
			lock.unlock();
		}
		return result;
	}
	
	/**
	 * Retrieves the children of a {@link MetaDataRepositoryNode} that is asked for
	 * @param key is the same as the logical path the file/node in MDFS
	 * @return all children of a node, null if it dose not exist
	 */
	public MetaDataRepositoryNode[] getChildren(String key) {
		MetaDataRepositoryNode[] children = null;
		lock.lock();
		try{
			int size = repository.getNumberOfChildern(key);
			children = new MetaDataRepositoryNode[size];
			children = repository.getChildernArray(key, children);
		}finally{
			lock.unlock();
		}
		return children;
	}
	
	/**
     * Removes a node from the repository as well as from permanent storage
     * @param key is the same as the logical path the file/node in MDFS
     * @return the removed node
     */
	public MetaDataRepositoryNode remove(String key) {
		MetaDataRepositoryNode node = null;
		lock.lock();
		try{
			node = repository.remove(key);
			//node == null if it did not exist in the repo
			if(node != null)
				MySQLUpdater.getInstance().deleteMetaData(key);
		}finally{
			lock.unlock();
		}
		return node;
	}

	/**
     * Adds a node to the repository as well as to permanent storage
     * @param key is the same as the logical path the file/node in MDFS
     * @param metaData is the {@link MetaDataRepositoryNode} that is to be added
     * @return true is successful, false otherwise
     */
	public boolean add(String key, MetaDataRepositoryNode metaData) {
		boolean b = false;
		lock.lock();
		try{
			b = repository.put(key, metaData);
			if(b)
				MySQLUpdater.getInstance().updateMetaData(metaData);
		}finally{
			lock.unlock();
		}
		return b;
	}

	/**
     * Replaces a node in repository
     * @param key the key of the node to be replaced
     * @param metaData the meta data that will replace the old
     * @return node that was replaces, null if node did not exist
     */
	public MetaDataRepositoryNode replace(String key, MetaDataRepositoryNode metaData) {
		MetaDataRepositoryNode node = null;
		lock.lock();
		try{
			node = repository.replace(key, metaData);
			//TODO: implement updating the SQL
			//MySQLUpdater.getInstance().updateMetaData(metaData);
		}finally{
			lock.unlock();
		}
		return node;
	}
	
	/**
	 * Adds and Array of the MetaDataRepositoryNodes to the repository 
	 * @param nodes to be added
	 * @return true is successful, false otherwise
	 */
	public boolean add(MetaDataRepositoryNode[] nodes){
		lock.lock();
		boolean result = true;
		try{
			for (MetaDataRepositoryNode node : nodes) {
				if(!add(node.getKey(), node))
					result = false;
			}
		}finally{
			lock.unlock();
		}
		return result;
	}
	
	/**
     * Load the MetaDataRepositority from permanent storage
     */
	public void load() {
		lock.lock();
		try{
			clear();
			MySQLFetch sql = new MySQLFetch();
			
			//Counts the number of rows/nodes that are to be loaded into the repository
			int size = sql.countRows("meta-data");
			int partition = 512;
			
			//Adding MetaData
			MetaDataRepositoryNode[] nodes;
			for(int i = 0; i < size/partition; i++){
				
				//Fetching a number of rows into a MetaDataRepositoryNode[]
				nodes = sql.getMetaDataReopsitoryNodes(i*partition, partition);
				
				//Adding the nodes to the repository, without the location of the raw data.
				add(nodes);
			}
			//Fetching a number of rows into a MetaDataRepositoryNode[], this is what remains after the partitioning
			nodes = sql.getMetaDataReopsitoryNodes(((int)(size/partition)) * partition, size%partition);
			
			//Adding the nodes to the repository, without the location of the raw data.
			add(nodes);
			
			
			//Adding location to MetaData, building the relation between a file and its location on a DataNode
			size = sql.countRows("meta-data_data-node");
			String[][] metaDataDataNodeRelation;
			for(int i = 0; i < size/partition; i++){
				//Fetching the relations
				metaDataDataNodeRelation = sql.getMetaDataDataNodeRelations(i*partition, partition);
				
				//Loading the relation into the repository 
				loadMetaDataDataNodeRelation(metaDataDataNodeRelation);
			}
			//Fetching the remaining relations after partitioning
			metaDataDataNodeRelation = sql.getMetaDataDataNodeRelations(((int)(size/partition)) * partition, size%partition);
			
			//Loading the relation into the repository 
			loadMetaDataDataNodeRelation(metaDataDataNodeRelation);
		}finally{
			lock.unlock();
		}
		
	
	}
	
	/*
	 * the relations looks as follow: relation[n][2]
	 * where n is the number of relation while the 2 is the relation itself
	 * ex. 	relation[123][0] = the full path that a MetaDataRepositoryNode contain
	 * 		relation[123][1] = the name of the Data Node the holds the raw data the metadata reference
	 */
	private boolean loadMetaDataDataNodeRelation(String[][] relations){
		DataNodeInfoRepository dataNodeReop = DataNodeInfoRepository.getInstance();
		DataNodeInfoRepositoryNode dataNode;
		MetaDataRepositoryNode metaNode;
		
		//Building realation from the array[][]
		for (String[] relation : relations) {
			
			//Fetching concerned metadatareponode
			metaNode = get(relation[0]);
			
			//If it exist the relation is tryied to be added
			if(metaNode != null){
				
				//fetching the datanode that stores the data
				dataNode = dataNodeReop.get(relation[1]);
				
				//If the datanode exist the relation is built
				if(dataNode != null){
					metaNode.addLocation(dataNode, false);
				}
			}
		}
		
		return true;
	}
	

	/**
     * Clears the Repositority
     */
	public void clear() {
		lock.lock();
		try{
			MetaDataRepositoryNode deafualtNode = new MetaDataRepositoryNode();
			deafualtNode.setOwner("root");
			deafualtNode.setGroup("root");
			deafualtNode.setFileType(MetadataType.DIR);
			deafualtNode.setFilePath("/");
			deafualtNode.setPermission((short)774);
			deafualtNode.setSize(0);
			repository = new FSTree<MetaDataRepositoryNode>("/", "/", deafualtNode);
		}finally{
			lock.unlock();
		}
	}


}

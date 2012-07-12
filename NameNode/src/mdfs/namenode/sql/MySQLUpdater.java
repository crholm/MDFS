package mdfs.namenode.sql;

import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This Class is a threaded Singelton that helps other classes to execute query that need no response.
 * It works in a way that a thread is intermittently executing querys to the MySQL database specified mdfs/config/config.cfg
 * The MySQL.update.rate in the config.cfg is the milliseconds that the thread sleeps before checking if any querys
 * are queued to be issued.
 * This enables other classes to just enqueue a query and not having to wait for a response from the SQL server.
 * MySQLUpdater works in a way that other classes can queue items even if MySQLUpdater is currently executing querys
 * from said queue. This helps MDFS performance since no other pars of the software have to wait on a SQL server. 
 * 
 * @author Rasmus Holm
 *
 */
public class MySQLUpdater implements Runnable{
	
	private static MySQLUpdater instance = null;
	
	private Thread thread = null; 
	
	private int updateRate = Config.getInt("MySQL.update.rate");
	
	private ConcurrentLinkedQueue<String> queue1 = new ConcurrentLinkedQueue<String>();
	private ConcurrentLinkedQueue<String> queue2 = new ConcurrentLinkedQueue<String>();
	private boolean queue1IsInUse = false;
	private MySQLDeleteInsertUpdate updater = new MySQLDeleteInsertUpdate();
	
	ReentrantLock lock = new ReentrantLock();
	
	private MySQLUpdater(){
		thread = new Thread(this);
		thread.start();
	}
	public static MySQLUpdater getInstance(){
		if(instance == null){
			instance = new MySQLUpdater();
		}
		return instance;
	}
	
	/*
	 * By using 2 queues other classes can always enqueue querys without waiting for the updating
	 * thread to be finished. This way other classes never have to wait for sql related performance issues
	 */
	@Override
	public void run() {
		Connection connection;
			
		while(true){
			//Locks the queue 1 and all new query queued in queue2
			lock.lock();
			try{
				queue1IsInUse = true;
			}finally{
				lock.unlock();
			}
			
			// Executing updates from queue 1, if any
			if(!queue1.isEmpty()){
			
				connection = new MySQLConnector().getConnection();
				String query;
				while((query = queue1.poll()) != null){
					updater.update(connection, query);
				}	
				
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			//Unlocks queue1 and and all new query queued in queue1 again
			lock.lock();
			try{
				queue1IsInUse = false;
			}finally{
				lock.unlock();
			}
			
			// // Executing updates from queue 2
			if(!queue2.isEmpty()){
				
				connection = new MySQLConnector().getConnection();
				String query;
				while((query = queue2.poll()) != null){
					updater.update(connection, query);
				}	
				
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
			
			//Sleeps a while to wait for new querys
			try {
				Thread.sleep(updateRate);
			} catch (InterruptedException e) {}
			
			
		}
		
	}
	/*
	 * enqueus a new item in the queue for querys to be updated.
	 * if queue1 is in use queue2 is used
	 */
	private void enqueue(String query){
		lock.lock();
		try{
			if(queue1IsInUse){
				queue2.add(query);
			}else{
				queue1.add(query);
			}
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Forces the MySQLUpdater to execute querys enqueued
	 */
	public void push(){
		thread.interrupt();
	}
	
	/**
	 * Updates the node in permanent storage
	 * @param node the MetaDataRepositoryNode that is to be updated in SQL
	 */
	public void updateMetaData(MetaDataRepositoryNode node){
		lock.lock();
		try{
			enqueue(createMetaDataRowSQLStatement(node));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Deletes a MetaDataRepositoryNode as stored in the MySQL database
	 * @param filePath the logical MDFS path to the node that is to be deleted from the SQL
	 */
	public void deleteMetaData(String filePath){
		lock.lock();
		try{
			enqueue(createMetaDataDeleteRowStatement(filePath));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Deletes a MetaDataRepositoryNode as stored in the MySQL database
	 * @param node The node that is to be deleted from SQL
	 */
	public void deleteMetaData(MetaDataRepositoryNode node){
		deleteMetaData(node.getFilePath());
	}
	
	/**
	 * Updates the node in permanent storage
	 * @param node the node that is to be updated
	 */
	public void updateUserData(UserDataRepositoryNode node){
		lock.lock();
		try{
			enqueue(createUserDataRowSQLStatement(node));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Removes node from permanent storage
	 * @param node the node that is to be removed
	 */
	public void removeUserData(UserDataRepositoryNode node) {
		lock.lock();
		try{
			enqueue(createUserDataDeleteRowSQLStatement(node));
		}finally{
			lock.unlock();
		}
		
	//	DELETE FROM `mdfs`.`user-data` WHERE `user-data`.`name` = \'test2\'
		
	}
	
	
	/**
	 * Updates the node in permanent storage
	 * @param node the node that is to be updated
	 */
	public void updateDataNode(DataNodeInfoRepositoryNode node) {
		lock.lock();
		try{
			enqueue(createDataNodeRowSQLStatement(node));
		}finally{
			lock.unlock();
		}
		
	}
	
	/**
	 * Updates the relation between a MetaDataRepositoryNode DataNodeInfoRepositoryNode in permanent storage
	 * @param metaData The MetaDataRepositoryNode that has relation to DataNodeInfoRepositoryNode 
	 * @param dataNode The DataNodeInfoRepositoryNode that has relation to MetaDataRepositoryNode 
	 */
	public void updateMetaDataDataNodeRelation( MetaDataRepositoryNode metaData, DataNodeInfoRepositoryNode dataNode) {
		lock.lock();
		try{
			enqueue(createMetaDataDataNodeRelation(metaData, dataNode));
		}finally{
			lock.unlock();
		}
	}
	

	/*
	 * Creates SQL statement executing update
	 */
	private String createMetaDataDataNodeRelation(MetaDataRepositoryNode metaData, DataNodeInfoRepositoryNode dataNode) {
		
		return  "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data_data-node` " +
				"(`Meta_Data_filePath`, " +
				"`Data_Node_Name`) " +
				"VALUES (" +
				"'" + metaData.getFilePath()  +  "', " +
				"'" + dataNode.getName() + "'" +
				");";
	}
	
	/*
	 * Creates SQL statement executing update
	 */
	private String createDataNodeRowSQLStatement(DataNodeInfoRepositoryNode node) {
		
		return "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "data-node` (" +
				"`name`, " +
				"`address`, " +
				"`port`" +
				") " +
				"VALUES (" +
				"'" + node.getName()  + "', " +
				"'" + node.getAddress() + "', " +
				"'" + node.getPort() + "'" +
				")" +
				"ON DUPLICATE KEY UPDATE" +
				"`name` = '" + node.getName() + "', " +
				"`address` = '" + node.getAddress() + "', " +
				"`port` = '" + node.getPort() + "' " +
				";";
	}
	
	/*
	 * Creates SQL statement executing delete
	 */
	private String createMetaDataDeleteRowStatement(String filePath){
		return 	"DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data` " +
				"WHERE `meta-data`.`filePath` = '" +  filePath  + "';" ;
	}
	
	/*
	 * Creates SQL statement executing update
	 */
	private String createUserDataRowSQLStatement(UserDataRepositoryNode node){
		return "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data` (" +
                "`uid`, " +
                "`name`, " +
				"`pwdHash`" +
				") " +
				"VALUES (" +
				"'" + node.getUid() + "', '" + node.getName() + "', '" + node.getPwdHash() + "')" +
				"ON DUPLICATE KEY UPDATE" +
                "`uid` = '" + node.getUid() + "', " +
                "`name` = '" + node.getName() + "', " +
				"`pwdHash` = '"+ node.getPwdHash() + "'" +
				";";
	}
	private String createUserDataDeleteRowSQLStatement(UserDataRepositoryNode node){
		return "DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data` " +
				"WHERE `user-data`.`name` = '" + node.getName() + "';";
	}
	
	/*
	 * Creates SQL statement executing update
	 */
	private String createMetaDataRowSQLStatement(MetaDataRepositoryNode node){
		return 	"INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data` (" + 
						 "`filePath`, " +
						 "`size`, " +
						 "`fileType`, " +
						 "`storageName`, " +
						 "`permission`, " +
						 "`owner`, " +
						 "`group`, " +
						 "`created`, " +
						 "`lastEdited` ) " +
				 "values (" +
				 			"'" + node.getFilePath() + "', " +
				 			"'" + node.getSize() + "', " +
				 			"'" + node.getFileType() + "', " +
				   		    "'" + node.getStorageName() + "', " +
				   		    "'" + node.getPermission() + "', " +
				   		    "'" + node.getOwner() + "', " +
				   		    "'" + node.getGroup() + "', " +
				   		    "'" + node.getCreated() + "', " +
				   		    "'" + node.getLastEdited() + "'" +
				   		")" +
		 		
				 "ON DUPLICATE KEY UPDATE " +   
		 				"`size` = '" + node.getSize() + "', " +
		 				"`fileType` = '" + node.getFileType() + "', " +
		 				"`storageName` = '" + node.getStorageName() + "', " +
		 				"`permission` = '" + node.getPermission() + "', " +
		 				"`owner` = '" + node.getOwner() + "', " +
		 				"`group` = '" + node.getGroup() + "', " +
		 				"`created` = '" + node.getCreated() + "', " +
		 				"`lastEdited` = '" + node.getLastEdited() + "' " +
		 				//"`lastTouched` = '" + node.getLastTouched() + "' " +
		 		";";
	}
	


}

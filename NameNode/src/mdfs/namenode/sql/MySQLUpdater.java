package mdfs.namenode.sql;

import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.GroupDataRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
	public void update(MetaDataRepositoryNode node){
		lock.lock();
		try{
			enqueue(updateMetaData(node));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Deletes a MetaDataRepositoryNode as stored in the MySQL database
     * @param node
     */
	public void remove(MetaDataRepositoryNode node){
		lock.lock();
		try{
			enqueue(removeMetaData(node.getFilePath()));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Deletes a MetaDataRepositoryNode as stored in the MySQL database
	 * @param node The node that is to be deleted from SQL
	 */

	/**
	 * Updates the node in permanent storage
     * @param node the node that is to be updated
     */
	public void update(UserDataRepositoryNode node){
		lock.lock();
		try{
			enqueue(updateUserData(node));
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Removes node from permanent storage
     * @param node the node that is to be removed
     */
	public void remove(UserDataRepositoryNode node) {
		lock.lock();
		try{
			enqueue(removeUserData(node.getUid()));
		}finally{
			lock.unlock();
		}

	}
	
	
	/**
	 * Updates the node in permanent storage
     * @param node the node that is to be updated
     */
	public void update(DataNodeInfoRepositoryNode node) {
		lock.lock();
		try{
			enqueue(updateDataNode(node));
		}finally{
			lock.unlock();
		}
		
	}


    public void update(GroupDataRepositoryNode node){
        lock.lock();
        try{
            enqueue(updateGroup(node));
        }finally{
            lock.unlock();
        }
    }
    public void remove(GroupDataRepositoryNode node){
        lock.lock();
        try{
            enqueue(removeGroup(node.getGid()));
        }finally{
            lock.unlock();
        }
    }


    public void updateRelation(){

    }


    /**
	 * Updates the relation between a MetaDataRepositoryNode DataNodeInfoRepositoryNode in permanent storage
     * @param metadata The MetaDataRepositoryNode that has relation to DataNodeInfoRepositoryNode
     * @param datanode The DataNodeInfoRepositoryNode that has relation to MetaDataRepositoryNode
     */
	public void updateRelation(MetaDataRepositoryNode metadata, DataNodeInfoRepositoryNode datanode) {
		lock.lock();
		try{
			enqueue(updateRelation_MetaNodeDataNode(metadata, datanode));
		}finally{
			lock.unlock();
		}
	}
    public void updateRelation(GroupDataRepositoryNode group, UserDataRepositoryNode user) {
        lock.lock();
        try{
            enqueue(updateRelation_GroupUser(group.getGid(), user.getUid()));
        }finally{
            lock.unlock();
        }
    }
    public void removeRelation(MetaDataRepositoryNode metadata, DataNodeInfoRepositoryNode datanode) {
        lock.lock();
        try{
            enqueue(removeRelation_MetaNodeDataNode(metadata, datanode));
        }finally{
            lock.unlock();
        }
    }


    public void removeRelation(GroupDataRepositoryNode group, UserDataRepositoryNode user){
        lock.lock();
        try{
            enqueue(removeRelation_GroupUser(group.getGid(), user.getUid()));
        }finally{
            lock.unlock();
        }
    }



    private String removeGroup(int gid) {
        return "DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "group-data` " +
                "WHERE `group-data`.`gid` = " + gid + ";";
    }

    private String updateGroup(GroupDataRepositoryNode node) {
        return "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "group-data` (" +
                "`gid`, " +
                "`name`" +
                ") " +
                "VALUES (" +
                "'" + node.getGid() + "', " +
                "'" + escape(node.getName())  +"'" +
                ")" +
                "ON DUPLICATE KEY UPDATE" +
				"`gid` = '" + node.getGid() + "', " +
				"`name` = '" + escape(node.getName()) + "' " +
                ";";
    }

    private String updateRelation_GroupUser(int gid, int uid) {
        return "INSERT IGNORE INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data_group-data` (" +
                "`uid`, " +
                "`gid`) " +
                "VALUES (" +
                "'" + uid + "', " +
                "'" + gid + "'" +
                ");";
    }
    private String removeRelation_GroupUser(int gid, int uid) {
        return "DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data_group-data` " +
                "WHERE " +
                "`user-data_group-data`.`uid` = " + uid + " " +
                "AND " +
                "`user-data_group-data`.`gid` = " + gid +  ";";
    }


    /*
	 * Creates SQL statement executing update
	 */
	private String updateRelation_MetaNodeDataNode(MetaDataRepositoryNode metaData, DataNodeInfoRepositoryNode dataNode) {
		
		return  "INSERT IGNORE INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data_data-node` " +
				"(`Meta_Data_filePath`, " +
				"`Data_Node_Name`) " +
				"VALUES (" +
				"'" + escape(metaData.getFilePath())  +  "', " +
				"'" + escape(dataNode.getName()) + "'" +
				");";
	}
    private String removeRelation_MetaNodeDataNode(MetaDataRepositoryNode metadata, DataNodeInfoRepositoryNode datanode) {
        return "DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data_data-node` " +
                "WHERE " +
                "`meta-data_data-node`.`Meta_Data_filePath` = " + escape(metadata.getFilePath()) + " " +
                "AND " +
                "`meta-data_data-node`.`Data_Node_Name` = " + escape(datanode.getName()) +  ";";
    }
	
	/*
	 * Creates SQL statement executing update
	 */
	private String updateDataNode(DataNodeInfoRepositoryNode node) {
		
		return "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "data-node` (" +
				"`name`, " +
				"`address`, " +
				"`port`" +
				") " +
				"VALUES (" +
				"'" + escape(node.getName())  + "', " +
				"'" + escape(node.getAddress()) + "', " +
				"'" + escape(node.getPort()) + "'" +
				")" +
				"ON DUPLICATE KEY UPDATE" +
				"`name` = '" + escape(node.getName()) + "', " +
				"`address` = '" + escape(node.getAddress()) + "', " +
				"`port` = '" + escape(node.getPort()) + "' " +
				";";
	}
	
	/*
	 * Creates SQL statement executing delete
	 */
	private String removeMetaData(String filePath){
		return 	"DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data` " +
				"WHERE `meta-data`.`filePath` = '" +  escape(filePath)  + "';" ;
	}
	
	/*
	 * Creates SQL statement executing update
	 */
	private String updateUserData(UserDataRepositoryNode node){
		return "INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data` (" +
                "`uid`, " +
                "`name`, " +
				"`pwdHash`" +
				") " +
				"VALUES (" +
				"'" + node.getUid() + "', '" + escape(node.getName()) + "', '" + escape(node.getPwdHash()) + "')" +
				"ON DUPLICATE KEY UPDATE" +
                "`uid` = '" + node.getUid() + "', " +
                "`name` = '" + escape(node.getName()) + "', " +
				"`pwdHash` = '"+ escape(node.getPwdHash()) + "'" +
				";";
	}
	private String removeUserData(int uid){
		return "DELETE FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data` " +
				"WHERE `user-data`.`uid` = " + uid + ";";
	}
	
	/*
	 * Creates SQL statement executing update
	 */
	private String updateMetaData(MetaDataRepositoryNode node){
		return 	"INSERT INTO `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data` (" + 
						 "`filePath`, " +
						 "`size`, " +
						 "`fileType`, " +
						 "`storageName`, " +
						 "`permission`, " +
						 "`uid`, " +
						 "`gid`, " +
						 "`created`, " +
						 "`lastEdited`," +
                         "`lastTouched` ) " +
				 "values (" +
				 			"'" + escape(node.getFilePath()) + "', " +
				 			"'" + node.getSize() + "', " +
				 			"'" + node.getFileType() + "', " +
				   		    "'" + escape(node.getStorageName()) + "', " +
				   		    "'" + node.getPermission() + "', " +
				   		    "'" + node.getUid() + "', " +
				   		    "'" + node.getGid() + "', " +
				   		    "'" + node.getCreated() + "', " +
				   		    "'" + node.getLastEdited() + "', " +
                            "'" + node.getLastTouched() + "'" +
				   		")" +
		 		
				 "ON DUPLICATE KEY UPDATE " +   
		 				"`size` = '" + node.getSize() + "', " +
		 				"`fileType` = '" + node.getFileType() + "', " +
		 				"`storageName` = '" + escape(node.getStorageName()) + "', " +
		 				"`permission` = '" + node.getPermission() + "', " +
		 				"`uid` = '" + node.getUid() + "', " +
		 				"`gid` = '" + node.getGid() + "', " +
		 				"`created` = '" + node.getCreated() + "', " +
		 				"`lastEdited` = '" + node.getLastEdited() + "', " +
		 				"`lastTouched` = '" + node.getLastTouched() + "' " +
		 		";";
	}
	



    private String escape2(String s){

        s = s.replace("\\", "\\\\");
        s = s.replace((char)0x00+"", "\\0");
        s = s.replace("'", "\\'");
        s = s.replace("\"", "\\\"");
        s = s.replace("\n", "\\n");
        s = s.replace("\b", "\\b");
        s = s.replace("\r", "\\r");
        s = s.replace("\t", "\\t");
        s = s.replace((char)26+"", "\\Z");
        s = s.replace("%", "\\%");
        s = s.replace("_", "\\_");

        return s;
    }


    //Warning: wrong if NO_BACKSLASH_ESCAPES SQL mode is enabled
    private static final HashMap<String,String> sqlTokens;
    private static Pattern sqlTokenPattern;

    static
    {
        //MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
        String[][] search_regex_replacement = new String[][]
                {
                        //search string     search regex        sql replacement regex
                        {   "\u0000"    ,       "\\x00"     ,       "\\\\0"     },
                        {   "'"         ,       "'"         ,       "\\\\'"     },
                        {   "\""        ,       "\""        ,       "\\\\\""    },
                        {   "\b"        ,       "\\x08"     ,       "\\\\b"     },
                        {   "\n"        ,       "\\n"       ,       "\\\\n"     },
                        {   "\r"        ,       "\\r"       ,       "\\\\r"     },
                        {   "\t"        ,       "\\t"       ,       "\\\\t"     },
                        {   "\u001A"    ,       "\\x1A"     ,       "\\\\Z"     },
                        {   "\\"        ,       "\\\\"      ,       "\\\\\\\\"  }
                };

        sqlTokens = new HashMap<String,String>();
        String patternStr = "";
        for (String[] srr : search_regex_replacement)
        {
            sqlTokens.put(srr[0], srr[2]);
            patternStr += (patternStr.isEmpty() ? "" : "|") + srr[1];
        }
        sqlTokenPattern = Pattern.compile('(' + patternStr + ')');
    }

    private static String escape(String s){
        return s;
    }

    private static String escape3(String s)
    {
        Matcher matcher = sqlTokenPattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while(matcher.find())
        {
            matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}

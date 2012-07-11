package mdfs.namenode.sql;

import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.Config;
import mdfs.utils.io.protocol.enums.MetadataType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

/**
 * This class are used to fetch specific element in regard to MDFS from a MySQL Database and the 
 * mdfs/config/config.cfg file
 * @author Rasmus Holm
 *
 */
public class MySQLFetch {
	ResultSet rs = null;
	Statement stmt = null;
	Connection conn = null;
	
	/**
	 * 
	 * @param query is the query that is execueted to fetch whanted elements
	 */
	public void createResultSet(String query){
		try {
			conn = new MySQLConnector().getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return the ResultSet after that it has been created
	 */
	public ResultSet getResultSet(){
		return rs;
	}
	
	/**
	 * 
	 * @param table the table that is to be counted
	 * @return the number of rows in a table
	 */
	public int countRows(String table){
		
		String query = "SELECT COUNT(*) FROM `" + Config.getString("MySQL.db") + "`.`" 
												+ Config.getString("MySQL.prefix") + table +  "`;";
		createResultSet(query);
		
		try {
			getResultSet().next();
			int count = getResultSet().getInt("COUNT(*)");
			close();
			return count;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		close();
		return 0;
		
	}
	
	/**
	 * Fetches all the UserDataRepositoryNodes that are stored in the database and parses them in to such
	 * @return an array of all the UserDataRepositort stored in SQL
	 */
	public UserDataRepositoryNode[] getUserDataRepositoryNodes(){
		//Creates the query for the operation
		String query =  "SELECT * FROM `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data` " +
						"ORDER BY `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "user-data`.`name` ASC;";
		
		//Creates the resultset and fetches it
		createResultSet(query);
		
		ResultSet result = getResultSet();
		UserDataRepositoryNode node = null;
		UserDataRepositoryNode[] nodes = null;	
		
		try {
			//Parses the result set in to a LinkedList that will contain all the UserDataReopNodes 
			LinkedList<UserDataRepositoryNode> list = new LinkedList<UserDataRepositoryNode>();
			while(result.next()){
				node = new UserDataRepositoryNode(result.getString("name"));
				node.setPwdHash(result.getString("pwdHash"));
                node.setUid(result.getInt("uid"));
				list.add(node);
			}
			
			//Converts the linked list into an array
			nodes = new UserDataRepositoryNode[list.size()];
			list.toArray(nodes);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return nodes;
	}
	
	
	/**
	 * Cloases and cleans up the connection, resultset and so on.
	 */
	public void close(){
		try {
			stmt.close();
			rs.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Fetches all the MetaDataRepositoryNode that are stored in the database and parses them in to such within the
	 * offset and length
	 * @param offset the offset in the sql
	 * @param length the length or number of tuples/MetaDataReopsitoryNodes 
	 * @return the array containing the MetaDataReopsitoryNodes 
	 */
	public MetaDataRepositoryNode[] getMetaDataReopsitoryNodes(int offset, int length){
		//Creates the query for the operation
		String query = 	"SELECT * FROM " +
						"`" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data` " +
						"ORDER BY  `meta-data`.`filePath` ASC " +
						"LIMIT "+ offset +" , " + length  + ";";
		
		//Execute query and creates the resultset and 
		createResultSet(query);
		ResultSet result = getResultSet();
		
		MetaDataRepositoryNode[] nodes = new MetaDataRepositoryNode[length];
		MetaDataRepositoryNode node = null;
		
		
		
		try {
			int i = 0;
			//Adds all the metadata into the repo nodes
			while(result.next() && i < length){
				node = new MetaDataRepositoryNode();
				
				node.setFilePath(result.getString("filePath"));
				node.setSize(result.getLong("size"));
				
				String fileType = result.getString("fileType");
				if(fileType.equals("DIR"))
					node.setFileType(MetadataType.DIR);
				else if(fileType.equals("FILE"))
					node.setFileType(MetadataType.FILE);
				
				node.setStorageName(result.getString("storageName"));
				node.setPermission(result.getShort("permission"));
				node.setOwner(result.getString("owner"));
				node.setGroup(result.getString("group"));
				node.setCreated(result.getString("created"));
				node.setLastEdited(result.getString("lastEdited"));
				node.setLastTouched(result.getString("lastTouched"));
				
				
				nodes[i] = node;
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close();
		return nodes;
	}

	/**
	 * Fetches all the MetaDataDataNodeRelations that are stored in the database and parses them in to such within the
	 * offset and length.
	 * The array returned contains the relation in the following manner
	 *  - String[][] relation = new String[length][2];
	 *  - relation[n][0] = filePath (the logical path of a file in MDFS)
	 *  - relation[n][1] = DataNode name, the name of the datanode that the raw data of the file is stored
	 *  - n = the relation itself
	 * @param offset the of set in regards to the request
	 * @param length the length or number of tuples/MetaDataReopsitoryNodesDataNodeRelation
	 * @return a String[length][2] array that contains the relation between the MetaData and DataNode.  
	 */
	public String[][] getMetaDataDataNodeRelations(int offset, int length) {
		String query = 	"SELECT * " +
						"FROM  `" + Config.getString("MySQL.db") + "`.`" + Config.getString("MySQL.prefix") + "meta-data_data-node` " +
						"ORDER BY  `meta-data_data-node`.`Meta_Data_filePath` ASC " +
						"LIMIT " + offset + " , " + length + ";";
		
		String[][] r = new String[length][2];
		createResultSet(query);
		ResultSet result = getResultSet();
		
		int i = 0;
		try {
			while(result.next() && i < length){
				r[i][0] = result.getString("Meta_Data_filePath");
				r[i][1] = result.getString("Data_Node_Name");
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		

		return r;
	}
}

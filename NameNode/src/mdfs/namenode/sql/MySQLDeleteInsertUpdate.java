package mdfs.namenode.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class simply execute generic update insert and delete querys that dose not return anything
 * @author Rasmus Holm
 *
 */
public class MySQLDeleteInsertUpdate{
	
	public MySQLDeleteInsertUpdate(){	}
	

	/**
	 * 
	 * @param connection to the database
	 * @param query the query to be executed
	 */
	public void update(Connection connection, String query){
		 Statement s;
		 try {
			s = connection.createStatement();
			//System.out.println(query);
			s.executeUpdate(query);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
			 
	}
	
	
}

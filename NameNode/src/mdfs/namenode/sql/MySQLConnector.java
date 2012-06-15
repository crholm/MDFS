package mdfs.namenode.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import mdfs.utils.Config;

/**
 * This class creates a connection to the MySQL database that is specified in the mdfs/config/config.cfg
 * @author Ramsus Holm
 *
 */
public class MySQLConnector {
	String user = Config.getString("MySQL.user");
	String pass = Config.getString("MySQL.pass");
	String db = Config.getString("MySQL.db");
	String prefix = Config.getString("MySQL.prefix");
	
	 Connection conn = null;
	
	/**
	 * Creates the connecton to the MySQL database when MySQLConnectior is created
	 */
	public MySQLConnector(){      
            try {
            	String url = "jdbc:mysql://" + Config.getString("MySQL.host") + "/" + Config.getString("MySQL.db");
                try {
					Class.forName("com.mysql.jdbc.Driver").newInstance ();
				} catch (InstantiationException e) {
					
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					
					e.printStackTrace();
				}
                
				conn = DriverManager.getConnection (url, user, pass);
				
			} catch (SQLException e) {
				System.err.println ("Cannot connect to database server");
				e.printStackTrace();
			}	
	}
	/**
	 * 
	 * @return the connection to the database
	 */
	public Connection getConnection(){
		return conn;
	}
}

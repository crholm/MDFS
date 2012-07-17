package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;

/**
 * A node that hold data regarding a DataNode
 * @author Rasmus Holm
 *
 */
public class DataNodeInfoRepositoryNode{
	private String name;
	private String address;
	private String port;
	
	/**
	 * 
	 * @return the name of the DataNode
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param name is stored as name of the DataNode
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 
	 * @return the port of the DataNode
	 */
	public String getPort() {
		return port;
	}
	/**
	 * 
	 * @param port to be set if the DataNode
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * 
	 * @return the address to the DataNode
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * 
	 * @param address to be set as a address of the DataNode
	 */
	public void setAddress(String address) {
		this.address = address;
	}

    public void commit(){
        MySQLUpdater.getInstance().update(this);
    }

}

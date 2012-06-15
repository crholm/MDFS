package mdfs.namenode.repositories;

import java.util.LinkedList;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.Config;
import mdfs.utils.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The node that contains all the meta-data and information neaded for a file or a dir in MDFS
 * @author Rasmus Holm
 *
 */
public class MetaDataRepositoryNode{
	private int replicationRatio = 0;
	private DataTypeEnum fileType = null;
	private String filePath = "";
	private long size;
	private String storageName = null;
	private LinkedList<DataNodeInfoRepositoryNode> location = new LinkedList<DataNodeInfoRepositoryNode>();
	
	private short permission = 644;
	private String owner = "";
	private String group = "";
	
	private String created = "";
	private String lastEdited = "";
	private String lastTouched = "";
	
	
	public MetaDataRepositoryNode(){
		String time = Time.getTimeStamp();
		created = time;
		lastEdited = time;
		lastTouched = time;
	}
	
	/**
	 * 
	 * @return the file type 
	 */
	public DataTypeEnum getFileType() {
		return fileType;
	}
	
	/**
	 * 
	 * @param fileType that is to be set as the fileType of the metadata
	 */
	public void setFileType(DataTypeEnum fileType) {
		this.fileType = fileType;
		if(fileType == DataTypeEnum.FILE)
			this.replicationRatio = Config.getInt("replication.ratio");
	}
	
	/**
	 * 
	 * @return the full logical path to the file in MDFS
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * 
	 * @return the key that a the meta-data should be indexed by, same as getFilePath().
	 */
	public String getKey() {
		return filePath;
	}
	
	/**
	 * 
	 * @param filePath sets the full logical path to the file in MDFS
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * 
	 * @return the size of the file
	 */
	public long getSize() {
		return size;
	}
	
	/**
	 * 
	 * @param size to be set as the files size
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	/**
	 * 
	 * @return the storage name of the file on a name node
	 */
	public String getStorageName() {
		return storageName;
	}
	
	/**
	 * 
	 * @param storageName the name to be used as storage name on the DataNodes.
	 */
	public void setStorageName(String storageName) {
		this.storageName = storageName;
	}
	
	/**
	 * Adds a storage location for the file, and updates permanent storage.
	 * @param node adds a storage location. If duplicated, it will be ignored.
	 */
	public void addLocation(DataNodeInfoRepositoryNode node){
		addLocation(node, true);
	}
	
	/**
	 * Adds a storage location for the file.
	 * @param node adds a storage location. If duplicated, it will be ignored.
	 * @param updateSQL updates permanent storage if set to True
	 */
	public void addLocation(DataNodeInfoRepositoryNode node, boolean updateSQL){
		if(!location.contains(node)){
			location.add(node);
			if(updateSQL)
				MySQLUpdater.getInstance().updateMetaDataDataNodeRelation(this, node);
		}
	}
	
	/**
	 * 
	 * @return a array of DataNodeInfoRepositoryNode that holds the file the metadate reference
	 */
	public DataNodeInfoRepositoryNode[] getLocations(){
		DataNodeInfoRepositoryNode[] nodes = new DataNodeInfoRepositoryNode[location.size()];
		return location.toArray(nodes);
	}
	
	/**
	 * Get permissions for the file.
	 * Permissions are set in a Unix fashion by a 3-letter number ex 764
	 * -the first letter, 7, is the owners permissions.
	 * -the second letter, 6, us the groups permissions.
	 * -the third letter, 4, is every once permissions.
	 * 
	 * Guide:
	 * 7	full
	 * 6	read and write
	 * 5	read and execute
	 * 4	read only
	 * 3	write and execute
	 * 2	write only
	 * 1	execute only
	 * 0	none
	 * @return permission as a short
	 */
	public short getPermission() {
		return permission;
	}
	
	/**
	 * Set permissions for the file.
	 * 
	 * Permissions are set in a Unix fashion by a 3-letter number ex 764
	 * -the first letter, 7, is the owners permissions.
	 * -the second letter, 6, us the groups permissions.
	 * -the third letter, 4, is every once permissions.
	 * 
	 * Guide:
	 * 7	full
	 * 6	read and write
	 * 5	read and execute
	 * 4	read only
	 * 3	write and execute
	 * 2	write only
	 * 1	execute only
	 * 0	none
	 */
	public void setPermission(short permission) {
		this.permission = permission;
	}
	
	/**
	 * 
	 * @return the owner of the file
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * 
	 * @param owner sets the owner of the file
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * 
	 * @return the group of the file
	 */
	public String getGroup() {
		return group;
	}
	
	/**
	 * 
	 * @param group sets the group of the file
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * 
	 * @return the date of creation for the file
	 */
	public String getCreated() {
		return created;
	}
	
	/**
	 * 
	 * @param created set the date of creation for file
	 */
	public void setCreated(String created) {
		this.created = created;
	}
	
	/**
	 * 
	 * @return the date the file was last edidted
	 */
	public String getLastEdited() {
		return lastEdited;
	}
	
	/**
	 * 
	 * @param lastEdited set when the file was last edidted
	 */
	public void setLastEdited(String lastEdited) {
		this.lastEdited = lastEdited;
	}
	
	/**
	 * 
	 * @return when the file was last touched
	 */
	public String getLastTouched() {
		return lastTouched;
	}
	
	/**
	 * 
	 * @param lastTouched set when the file was last touched
	 */
	public void setLastTouched(String lastTouched) {
		this.lastTouched = lastTouched;
	}
	
	/**
	 * 
	 * @return a JSON representation of the object
	 */
	public JSONObject toJSON(){
		JSONObject metadata = new JSONObject();
		JSONObject location = new JSONObject();
		
		try {
			
			metadata.put("path", getFilePath());
			if(getFileType() == DataTypeEnum.DIR){
				metadata.put("type", "dir");
			}else if(getFileType() == DataTypeEnum.FILE){
				metadata.put("type", "file");
			}else{
				metadata.put("type", "none");
			}
			
			metadata.put("size", getSize());
			metadata.put("permission", getPermission());
			metadata.put("owner", getOwner());
			metadata.put("group", getGroup());
			metadata.put("created", getCreated());
			metadata.put("lastEdited", getLastEdited());
			metadata.put("lastToutched", getLastTouched());
			
			
			if(getStorageName() != null);
				location.put("ratio", replicationRatio);
				location.put("name", getStorageName());
				
			/*
			 * Adds the DataNodes that hosts the files
			 */
			if(!this.location.isEmpty()){	
				JSONArray array = new JSONArray();
				
				for (DataNodeInfoRepositoryNode node : this.location) {
					array.put(node.getAddress() + ":" + node.getPort());
				}
				location.put("hosts", array);
			}
			
			/*
			 * Only files has a location at a DataNode.
			 */
			if(fileType == DataTypeEnum.FILE)
				metadata.put("Location", location);
				
			
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
		return metadata;
	}
}

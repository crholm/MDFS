package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.io.protocol.MDFSProtocolLocation;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.MetadataType;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * The node that contains all the meta-data and information neaded for a file or a dir in MDFS
 * @author Rasmus Holm
 *
 */
public class MetaDataRepositoryNode extends MDFSProtocolMetaData{
	private int replicationRatio = 0;
	private String storageName;
	private LinkedList<DataNodeInfoRepositoryNode> location = new LinkedList<DataNodeInfoRepositoryNode>();
	

	public MetaDataRepositoryNode(){
		long time = Time.currentTimeMillis();
		setCreated(time);
		setLastEdited(time);
		setLastTouched(time);
	}
	
	/**
	 * 
	 * @return the file type 
	 */
	public MetadataType getFileType() {
		return getType();
	}
	
	/**
	 * 
	 * @param fileType that is to be set as the fileType of the metadata
	 */
	public void setFileType(MetadataType fileType) {
		setType(fileType);
		if(fileType == MetadataType.FILE)
			this.replicationRatio = Config.getInt("replication.ratio");
	}
	
	/**
	 * 
	 * @return the full logical path to the file in MDFS
	 */
	public String getFilePath() {
		return getPath();
	}
	
	/**
	 * 
	 * @return the key that a the meta-data should be indexed by, same as getFilePath().
	 */
	public String getKey() {
		return getPath();
	}
	
	/**
	 * 
	 * @param filePath sets the full logical path to the file in MDFS
	 */
	public void setFilePath(String filePath) {
		setPath(filePath);
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
				MySQLUpdater.getInstance().updateRelation(this, node);
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
	
    @Override
	public int getUid(){
        if(super.getUid() != -1)
            return super.getUid();

        if(getOwner() == null)
            return -1;

        setUid(UserDataRepository.getInstance().getUid(getOwner()));

        return getUid();

    }

    @Override
    public int getGid(){
        if(super.getGid() != -1)
            return super.getGid();

        if(getGroup() == null)
            return -1;

        setGid(GroupDataRepository.getInstance().getGid(getGroup()));

        return getGid();
    }


	
	/**
	 * 
	 * @return a JSON representation of the object
	 */
    @Override
	public JSONObject toJSON(){

        if(getOwner() == null){
            setOwner(UserDataRepository.getInstance().getName(getUid()));
        }
        if(getGroup() == null){
            setGroup(GroupDataRepository.getInstance().getName(getGid()));
        }

        if(getType() == MetadataType.FILE){
            setLocation(new MDFSProtocolLocation());

            if(getStorageName() != null){
                getLocation().setRatio(replicationRatio);
                getLocation().setName(getStorageName());
            }

            if(!this.location.isEmpty()){
                for (DataNodeInfoRepositoryNode node : this.location) {
                    getLocation().addHost(node.getAddress() + ":" + node.getPort());
                }

            }
        }

		JSONObject json = super.toJSON();

        //To minimize size of MetaDataRepoisotryNode
        setLocation(null);
		setChildren(null);
		return json;
	}

    public void commit(){
        setLastTouched(Time.currentTimeMillis());
        MySQLUpdater.getInstance().update(this);
    }
}

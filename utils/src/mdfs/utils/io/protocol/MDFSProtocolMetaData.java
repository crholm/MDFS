package mdfs.utils.io.protocol;

import mdfs.utils.io.protocol.enums.MetadataType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Package: mdfs.utils.io
 * Created: 2012-06-15
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSProtocolMetaData extends MDFSProtocol{
    private String path;
    private MetadataType type;
    private long size = -1L;
    private int permission = -1;
    private String owner;
    private String group;
    private int uid = -1;
    private int gid = -1;
    private long created = -1;
    private long lastEdited = -1;
    private long lastToutched = -1;
    private MDFSProtocolLocation location;
    private LinkedList<MDFSProtocolMetaData> children;


    public MDFSProtocolMetaData(){}
    public MDFSProtocolMetaData(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }
    public MDFSProtocolMetaData(JSONObject jsonObject){

        if(jsonObject != null){
            setPath(jsonObject.optString("path", null));
            setType(jsonObject.optString("type", null));
            setSize(jsonObject.optLong("size", -1L));
            setPermission(jsonObject.optInt("permission", -1));
            setOwner(jsonObject.optString("owner", null));
            setGroup(jsonObject.optString("group", null));
            setUid(jsonObject.optInt("uid", -1));
            setGid(jsonObject.optInt("gid", -1));
            setCreated(jsonObject.optLong("created", -1));
            setLastEdited(jsonObject.optLong("lastEdited", -1));
            setLastTouched(jsonObject.optLong("lastToutched", -1));

            if(jsonObject.has("Location"))
                setLocation(new MDFSProtocolLocation(jsonObject.optJSONObject("Location")));

            if(jsonObject.has("Children")){
                JSONArray array = jsonObject.optJSONArray("Children");
                if(array != null){
                    for(int i = 0; i < array.length(); i++)
                        addChild( new MDFSProtocolMetaData( array.optJSONObject(i) ) );

                }
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();


        try {

            if(getPath() != null)
                json.put("path", getPath());

            if(getType() != null)
                json.put("type", getType());

            if(getSize() != -1L)
                json.put("size", getSize());

            if(getPermission() != -1)
                json.put("permission", getPermission());

            if(getOwner() != null)
                json.put("owner", getOwner());

            if(getGroup() != null)
                json.put("group", getGroup());

            if(getCreated() != -1)
                json.put("created", getCreated());

            if(getUid() != -1)
                json.put("uid", getUid());

            if(getGid() != -1)
                json.put("gid", getGid());

            if(getLastEdited() != -1)
                json.put("lastEdited", getLastEdited());

            if(getLastTouched() != -1)
                json.put("lastToutched", getLastTouched());

            if(getLocation() != null)
                json.put("Location", getLocation().toJSON());

            if(getChildren() != null){

                JSONArray array = new JSONArray();
                for(MDFSProtocolMetaData child : getChildren())
                    array.put(child.toJSON());
                json.put("Children", array);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MetadataType getType() {
        return type;
    }

    public void setType(MetadataType type) {
        this.type = type;
    }
    public void setType(String type) {
        try{
            this.type = MetadataType.valueOf(type.toUpperCase().trim());
        }catch (Exception e){
            this.type = null;
        }
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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
    public int getPermission() {
        return permission;
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
     * @param permission as a int
     */
    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(long lastEdited) {
        this.lastEdited = lastEdited;
    }

    public long getLastTouched() {
        return lastToutched;
    }

    public void setLastTouched(long lastTouched) {
        this.lastToutched = lastTouched;
    }

    public MDFSProtocolLocation getLocation() {
        return location;
    }

    public void setLocation(MDFSProtocolLocation location) {
        this.location = location;
    }

    public LinkedList<MDFSProtocolMetaData> getChildren() {
        return children;
    }

    public MDFSProtocolMetaData[] getChildernArray(MDFSProtocolMetaData[] children){
        this.children.toArray(children);
        return children;
    }
    public int getChildrenSize(){
        if(children == null){
            return 0;
        }
        return children.size();
    }

    public void setChildren(LinkedList<MDFSProtocolMetaData> children) {
        this.children = children;
    }

    public void addChild(MDFSProtocolMetaData child){
        LinkedList<MDFSProtocolMetaData> children = getChildren();
        if(children == null)
            children = new LinkedList<MDFSProtocolMetaData>();
        children.add(child);
        setChildren(children);
    }
}

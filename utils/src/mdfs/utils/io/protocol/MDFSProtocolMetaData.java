package mdfs.utils.io.protocol;

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
    private String type;
    private long size = -1L;
    private int permission = -1;
    private String owner;
    private String group;
    private String created;
    private String lastEdited;
    private String lastToutched;
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
            setCreated(jsonObject.optString("created", null));
            setLastEdited(jsonObject.optString("lastEdited", null));
            setLastToutched(jsonObject.optString("lastToutched", null));

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

            if(getCreated() != null)
                json.put("created", getCreated());

            if(getLastEdited() != null)
                json.put("lastEdited", getLastEdited());

            if(getLastToutched() != null)
                json.put("lastToutched", getLastToutched());

            if(getLocation() != null)
                json.put("Location", getLocation().toJSON());

            if(getChildren() != null){
                JSONArray array = new JSONArray();
                LinkedList<MDFSProtocolMetaData> children = getChildren();
                for(MDFSProtocolMetaData child : children)
                    array.put(child.toJSON());
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getPermission() {
        return permission;
    }

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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(String lastEdited) {
        this.lastEdited = lastEdited;
    }

    public String getLastToutched() {
        return lastToutched;
    }

    public void setLastToutched(String lastToutched) {
        this.lastToutched = lastToutched;
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

    public void setChildren(LinkedList<MDFSProtocolMetaData> children) {
        this.children = children;
    }

    public void addChild(MDFSProtocolMetaData child){
        LinkedList<MDFSProtocolMetaData> children = getChildren();
        if(children == null)
            children = new LinkedList<MDFSProtocolMetaData>();
        children.add(child);
    }
}

package mdfs.utils.io.protocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Package: mdfs.utils.io.protocol
 * Created: 2012-07-17
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSProtocolUserGroup extends MDFSProtocol {
    private int uid = -1;
    private String user = null;
    private int gid = -1;
    private String group = null;
    private String password = null;

    private LinkedList<MDFSProtocolUserGroup> members;


    public MDFSProtocolUserGroup(){}
    public MDFSProtocolUserGroup(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }
    public MDFSProtocolUserGroup(JSONObject json){
        if(json != null){
            setUid(json.optInt("uid", -1));
            setUser(json.optString("user", null));
            setGid(json.optInt("gid", -1));
            setGroup(json.optString("group", null));

            if(json.has("Members")){
                JSONArray array = json.optJSONArray("Members");
                if(array != null){
                    for(int i = 0; i < array.length(); i++)
                        addMember(new MDFSProtocolUserGroup(array.optJSONObject(i)));
                }
            }

        }
    }



    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            if(getUid() != -1)
                json.put("uid", getUid());

            if(getUser() != null)
                json.put("user", getUser());

            if(getPassword() != null)
                json.put("password", getPassword());

            if(getGid() != -1)
                json.put("gid", getGid());

            if(getGroup() != null)
                json.put("group", getGroup());

            if(getMembers() != null){

                JSONArray array = new JSONArray();
                for(MDFSProtocolUserGroup member : getMembers())
                    array.put(member.toJSON());
                json.put("Members", array);
            }


        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return json;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addMember(MDFSProtocolUserGroup member) {
        LinkedList<MDFSProtocolUserGroup> members = getMembers();
        if(members == null)
            members = new LinkedList<MDFSProtocolUserGroup>();
        members.add(member);
        setMembers(members);
    }
    public LinkedList<MDFSProtocolUserGroup> getMembers(){
        return members;
    }
    public void setMembers(LinkedList<MDFSProtocolUserGroup> members){
        this.members = members;
    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

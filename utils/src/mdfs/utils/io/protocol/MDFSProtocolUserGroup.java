package mdfs.utils.io.protocol;

import mdfs.utils.io.protocol.enums.Mode;
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
    private Mode action = null;

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
            setAction(json.optString("action", null));

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
    public JSONObject toJSON(){
        return toJSON(true);
    }

    public JSONObject toJSON(boolean withMembers) {
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

            if(getAction() != null)
                json.put("action", getAction().toString());

            if(getMembers() != null && withMembers){ //To prevent accidental recursive lookup.

                JSONArray array = new JSONArray();
                for(MDFSProtocolUserGroup member : getMembers())
                    array.put(member.toJSON(false));
                json.put("Members", array);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


    public Mode getAction() {
        return action;
    }

    public void setAction(Mode action) {
        this.action = action;
    }
    public void setAction(String action) {

        try{
            if(action == null)
                this.action = null;
            else
                this.action = Mode.valueOf(action.toUpperCase().trim());
        }catch (Exception e){
            this.action = null;
        }
    }

    public void addMember(MDFSProtocolUserGroup member) {
        if(members == null)
            members = new LinkedList<MDFSProtocolUserGroup>();
        members.add(member);
    }
    public LinkedList<MDFSProtocolUserGroup> getMembers(){
        if(members == null)
            members = new LinkedList<MDFSProtocolUserGroup>();
        return members;
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

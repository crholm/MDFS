package mdfs.utils.io.protocol;

import mdfs.utils.io.protocol.enums.ActionStatus;
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
public class MDFSProtocolInfo extends MDFSProtocol {
    private ActionStatus removed;
    private ActionStatus written;
    private int id = -1;
    private String cookie;
    private LinkedList<String> datanodes;

    public MDFSProtocolInfo(){}
    public MDFSProtocolInfo(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }
    public MDFSProtocolInfo(JSONObject jsonObject){
        if(jsonObject != null){

            setRemoved(jsonObject.optString("removed", null));
            setWritten(jsonObject.optString("written", null));
            setId(jsonObject.optInt("id", -1));
            setCookie(jsonObject.optString("cookie", null));

            if(jsonObject.has("datanodes")){
                JSONArray array = jsonObject.optJSONArray("datanodes");
                if(array != null){

                    for(int i = 0; i < array.length(); i++){
                        addDatanode(array.optString(i));
                    }


                }
            }

        }
    }


    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            if(getRemoved() != null)
                json.put("removed", getRemoved());

            if(getWritten() != null)
                json.put("written", getWritten());

            if(getId() != -1)
                json.put("id", getId());

            if(getCookie() != null)
                json.put("cookie", getCookie());

            if(getDatanodes() != null)
                    json.put("datanodes", new JSONArray(getDatanodes()));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


            return json;
    }



    public ActionStatus getRemoved() {
        return removed;
    }

    public void setRemoved(ActionStatus removed) {
        this.removed = removed;
    }

    public void setRemoved(String removed) {
        try{
            this.removed = ActionStatus.valueOf(removed.toUpperCase().trim());
        }catch (Exception e){
            this.removed = null;
        }
    }

    public ActionStatus getWritten() {
        return written;
    }

    public void setWritten(ActionStatus written) {
        this.written = written;
    }
    public void setWritten(String written) {
        try{
            this.written = ActionStatus.valueOf(written.toUpperCase().trim());
        }catch (Exception e){
            this.written = null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public LinkedList<String> getDatanodes() {
        return datanodes;
    }

    public void setDatanodes(LinkedList<String> datanodes) {
        this.datanodes = datanodes;
    }

    public void addDatanode(String datanode){
        LinkedList<String> datanodes = getDatanodes();
        if(datanodes == null)
            datanodes = new LinkedList<String>();
        datanodes.add(datanode);
    }


}

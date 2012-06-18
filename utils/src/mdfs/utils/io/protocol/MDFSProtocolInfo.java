package mdfs.utils.io.protocol;

import mdfs.utils.io.protocol.enums.ActionStatus;
import mdfs.utils.io.protocol.enums.Overwrite;
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
    private Overwrite overwrite;
    private int id = -1;
    private String cookie;
    private String path;
    private String name;
    private String host;
    private String port;
    private LinkedList<String> datanodes;

    public MDFSProtocolInfo(){}
    public MDFSProtocolInfo(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }


    public MDFSProtocolInfo(JSONObject jsonObject){
        if(jsonObject != null){

            setRemoved(jsonObject.optString("removed", null));
            setWritten(jsonObject.optString("written", null));
            setOverwrite(jsonObject.optString("overwrite", null));
            setId(jsonObject.optInt("id", -1));
            setCookie(jsonObject.optString("cookie", null));

            setPath(jsonObject.optString("path", null));
            setName(jsonObject.optString("name", null));
            setHost(jsonObject.optString("host", null));
            setPort(jsonObject.optString("port", null));

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

            if(getOverwrite() != null){
                json.put("overwrite", getOverwrite());
            }

            if(getId() != -1)
                json.put("id", getId());

            if(getCookie() != null)
                json.put("cookie", getCookie());

            if(getPath() != null)
               json.put("path", getPath());

            if(getName() != null)
                json.put("name", getName());

            if(getHost() != null)
                json.put("host", getHost());

            if(getPort() != null)
                json.put("port", getPort());

            if(getDatanodes() != null)
                    json.put("datanodes", new JSONArray(getDatanodes()));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


            return json;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Overwrite getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Overwrite overwrite) {
        this.overwrite = overwrite;
    }
    public void setOverwrite(String overwrite) {
        try{
        this.overwrite = Overwrite.valueOf(overwrite.toUpperCase().trim());
        }catch (Exception e){
            this.overwrite = null;
        }
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
    public String[] getDatanodesArray(String datanodes[]){
        datanodes = this.datanodes.toArray(datanodes);
        return datanodes;
    }

    public int getDatanodesSize() {
        return datanodes.size();
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

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
public class MDFSProtocolLocation extends MDFSProtocol{
    private int ratio = -1;
    private String name;
    private LinkedList<String> hosts;


    public MDFSProtocolLocation(){}
    public MDFSProtocolLocation(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }
    public MDFSProtocolLocation(JSONObject jsonObject){
        if(jsonObject != null){

            setRatio(jsonObject.optInt("ratio" , -1));
            setName(jsonObject.optString("name", null));

            if(jsonObject.has("hosts")){
                JSONArray array = jsonObject.optJSONArray("hosts");
                if(array!= null){

                    for(int i = 0; i < array.length(); i++)
                        addHost(array.optString(i));

                }
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            if(ratio != -1)
                json.put("ratio", getRatio());

            if(getName() != null)
                json.put("name", getName());

            if(getHosts() != null)
                    json.put("hosts", new JSONArray(getHosts()));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<String> getHosts() {
        return hosts;
    }

    public String[] getHostsArray(String hosts[]) {
        this.hosts.toArray(hosts);
        return hosts;
    }

    public void setHosts(LinkedList<String> hosts) {
        this.hosts = hosts;
    }
    public int getHostsSize(){
        return this.hosts.size();
    }

    public void addHost(String host){
        LinkedList<String> hosts = getHosts();
        if(hosts == null)
            hosts = new LinkedList<String>();
        hosts.add(host);
    }
}

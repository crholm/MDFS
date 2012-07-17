package mdfs.utils.io.protocol;

import mdfs.utils.Time;
import mdfs.utils.crypto.HMAC;
import mdfs.utils.crypto.digests.SHA1;
import mdfs.utils.io.protocol.enums.EventStatus;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Overwrite;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Package: mdfs.utils.io
 * Created: 2012-06-15
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSProtocolInfo extends MDFSProtocol {
    private EventStatus removed;
    private EventStatus written;
    private Overwrite overwrite;
    private int id = -1;
    private String cookie;
    private String path;
    private String name;
    private String host;
    private String port;
    private long length = -1;
    private long localTime = -1;
    private String token;
    private long tokenGenTime = -1;
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
            setLength(jsonObject.optLong("length", -1));
            setLocalTime(jsonObject.optLong("localTime", -1));

            setToken(jsonObject.optString("token", null));
            setTokenGenTime(jsonObject.optLong("tokenGenTime", -1));

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

            if(getLength() != -1)
                json.put("length", getLength());

            if(getLocalTime() != -1)
                json.put("localTime", getLocalTime());

            if(getToken() != null)
                json.put("token", getToken());

            if(getTokenGenTime() != -1)
                json.put("tokenGenTime", getTokenGenTime());


            if(getDatanodes() != null)
                    json.put("datanodes", new JSONArray(getDatanodes()));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


            return json;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenGenTime() {
        return tokenGenTime;
    }

    public void setTokenGenTime(long tokenGenTime) {
        this.tokenGenTime = tokenGenTime;
    }

    public long getLocalTime() {
        return localTime;
    }

    public void setLocalTime(long localTime) {
        this.localTime = localTime;
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

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public EventStatus getRemoved() {
        return removed;
    }

    public void setRemoved(EventStatus removed) {
        this.removed = removed;
    }

    public void setRemoved(String removed) {
        try{
            this.removed = EventStatus.valueOf(removed.toUpperCase().trim());
        }catch (Exception e){
            this.removed = null;
        }
    }

    public EventStatus getWritten() {
        return written;
    }

    public void setWritten(EventStatus written) {
        this.written = written;
    }
    public void setWritten(String written) {
        try{
            this.written = EventStatus.valueOf(written.toUpperCase().trim());
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
        if(getDatanodesSize() == 0)
            return null;
        datanodes = this.datanodes.toArray(datanodes);
        return datanodes;
    }

    public int getDatanodesSize() {
        if(datanodes == null){
            return 0;
        }
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
        setDatanodes(datanodes);
    }


    private String genToken(String msg, String key){
        try {
            HMAC mac = new HMAC(new SHA1(), key.getBytes("UTF8"));

            byte[] tmp = new byte[mac.getDigestSize()];

            mac.doFinal(msg.getBytes("UTF8"), tmp, 0);

            return new BigInteger(1, tmp).toString(16);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void addToken(String filepath, String filename, Mode mode, String tokenKey){


        long tokenGenTime = Time.currentTimeMillis();

        this.setToken(
                genToken( tokenGenTime + mode.toString() + filepath + filename, tokenKey) );

        this.setTokenGenTime(tokenGenTime);
        this.setPath(filepath);
        this.setName(filename);
    }

    public boolean authToken(Mode mode, String tokenKey, long timeWindow){
        return authToken(getPath(), getName(), mode, tokenKey, timeWindow);
    }

    public boolean authToken(String filepath, String filename, Mode mode, String tokenKey, long timeWindow){
        if(getToken() == null)
            return false;

        if(getTokenGenTime() == -1)
            return false;

        if(getTokenGenTime()+timeWindow < Time.currentTimeMillis())
            return false;

        String token =  genToken(getTokenGenTime() + mode.toString() + filepath + filename, tokenKey);

        if(getToken().equals(token))
            return true;

        return false;

    }






}

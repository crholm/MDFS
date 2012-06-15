package mdfs.utils.io.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Package: mdfs.utils.io
 * Created: 2012-06-15
 *
 * @author Rasmus Holm
 * @version 1.0
 */

public class MDFSProtocolHeader extends MDFSProtocol {

    private String from;
    private String to;

    private String stage;
    private String type;
    private String mode;

    private String error;

    private String user;
    private String pass;

    private MDFSProtocolMetaData metadata;
    private MDFSProtocolInfo info;


    public MDFSProtocolHeader(){}
    public MDFSProtocolHeader(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }
    public MDFSProtocolHeader(JSONObject jsonObject){
        if(jsonObject != null){

            setFrom(jsonObject.optString("From", null));
            setTo(jsonObject.optString("To", null));

            setStage(jsonObject.optString("Stage", null));
            setType(jsonObject.optString("Type", null));
            setMode(jsonObject.optString("Mode", null));

            setError(jsonObject.optString("Error", null));

            setUser(jsonObject.optString("User", null));
            setPass(jsonObject.optString("Pass", null));

            if(jsonObject.has("Meta-data"))
                setMetadata(new MDFSProtocolMetaData(jsonObject.optJSONObject("Meta-data")));

            if(jsonObject.has("Info"))
                setInfo(new MDFSProtocolInfo(jsonObject.optJSONObject("Meta-data")));

        }
    }



    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {

            if(getFrom() != null)
                json.put("From", getFrom());

            if(getTo() != null)
                json.put("To", getTo());

            if(getStage() != null)
                json.put("Stage", getStage());

            if(getType() != null)
                json.put("Type", getType());

            if(getMode() != null)
                json.put("Mode", getMode());

            if(getError() != null)
                json.put("Error", getError());

            if(getUser() != null)
                json.put("User", getUser());

            if(getPass() != null)
                json.put("Pass", getPass());

            if(getMetadata() != null);
            json.put("Meta-data", getMetadata().toJSON());

            if(getInfo() != null)
                json.put("Info", getInfo().toJSON());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


        return json;
    }




    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public MDFSProtocolMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MDFSProtocolMetaData metadata) {
        this.metadata = metadata;
    }

    public MDFSProtocolInfo getInfo() {
        return info;
    }

    public void setInfo(MDFSProtocolInfo info) {
        this.info = info;
    }

}

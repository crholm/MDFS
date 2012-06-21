package mdfs.utils.io.protocol;

import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
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

    private Stage stage;
    private Type type;
    private Mode mode;

    private String error;
    private int errorCode = -1;

    private String user;
    private String pass;

    private MDFSProtocolMetaData metadata;
    private MDFSProtocolInfo info;


    public static MDFSProtocolHeader createErrorHeader(Stage stage, Type type, Mode mode, String error){
        MDFSProtocolHeader header = new MDFSProtocolHeader();
        header.setStage(stage);
        header.setType(type);
        header.setMode(mode);
        header.setError(error);
        return header;
    }


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
            setErrorCode(jsonObject.optInt("ErrorCode", -1));

            setUser(jsonObject.optString("User", null));
            setPass(jsonObject.optString("Pass", null));

            if(jsonObject.has("Meta-data"))
                setMetadata(new MDFSProtocolMetaData(jsonObject.optJSONObject("Meta-data")));

            if(jsonObject.has("Info"))
                setInfo(new MDFSProtocolInfo(jsonObject.optJSONObject("Info")));

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

            if(getErrorCode() != -1){
                json.put("ErrorCode", getErrorCode());
                //json.put("Error", MDFSErrorCode.info[getErrorCode()]);
            }

            if(getUser() != null)
                json.put("User", getUser());

            if(getPass() != null)
                json.put("Pass", getPass());

            if(getMetadata() != null)
                json.put("Meta-data", getMetadata().toJSON());

            if(getInfo() != null)
                json.put("Info", getInfo().toJSON());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


        return json;
    }


    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
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

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setStage(String stage) {
        try{
            this.stage = Stage.valueOf(stage.toUpperCase().trim());
        }catch (Exception e){
            this.stage = null;
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    public void setType(String type) {
        try{
            this.type = Type.valueOf(type.toUpperCase().trim());
        }catch (Exception e){
            this.type = null;
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
    public void setMode(String mode) {
        try{
            this.mode = Mode.valueOf(mode.toUpperCase().trim());
        }catch (Exception e){
            this.mode = null;
        }
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

package mdfs.utils.io.protocol;

import org.json.JSONObject;

/**
 * Package: mdfs.utils.io
 * Created: 2012-06-15
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public abstract class  MDFSProtocol {
    @Override
    public String toString(){
        return toJSON().toString();
    }

    abstract public JSONObject toJSON();
}

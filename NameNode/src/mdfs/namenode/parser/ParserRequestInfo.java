package mdfs.namenode.parser;

import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

/**
 * Package: mdfs.datanode.parser
 * Created: 2012-07-09
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ParserRequestInfo implements Parser {

    private Mode mode;
    private Session session;
    private String errorMsg;

    public ParserRequestInfo(Mode mode){
        this.mode = mode;
    }

    /*
	 * Sets a error message and prints it
	 *
	 */
    private void setErrorMsg(String errorMsg){
        Verbose.print("Error message: " + errorMsg, this, Config.getInt("verbose") - 1);
        session.setStatus("error");
        this.errorMsg = errorMsg;
    }

    @Override
    public boolean parse(Session session) {
        this.session = session;

        switch (mode){
            case PING:
                MDFSProtocolHeader response = this.session.getRequest();
                response.setStage(Stage.RESPONSE);
                if(response.getInfo() == null)
                    response.setInfo(new MDFSProtocolInfo());
                response.getInfo().setLocalTime(Time.currentTimeMillis());
                this.session.setResponse(response);
                return true;

            default:
                //Creates a error response
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.INFO, mode, "Mode: " + mode + " is an non-valid mode"));
                setErrorMsg("None valid Mode in Header");

                return false;
        }

    }
}

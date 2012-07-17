package mdfs.namenode.parser;

import mdfs.namenode.repositories.ACL;
import mdfs.namenode.repositories.UserDataRepository;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

/**
 * Package: mdfs.namenode.parser
 * Created: 2012-07-17
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ParserRequestGroup implements Parser {

    Mode mode;
    Session session;
    UserDataRepositoryNode user;


    public ParserRequestGroup(Mode mode){
        this.mode = mode;
    }

    @Override
    public boolean parse(Session session) {
        this.session = session;

        UserDataRepository userdata = UserDataRepository.getInstance();
        user = userdata.get(session.getRequest().getUser());
        String pass = session.getRequest().getPass();

        //Checks so that user credentials are provided
        if(user == null || pass == null){
            //Creates Error Response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "In valid user or password" ));

            return false;
        }
        //Checks so that the user credentials are correct
        if(!userdata.authUser(user, pass)){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "In valid user or password"));

            return false;
        }



        switch (mode){
            case READ:
                return parseRead();
            case WRITE:
                return parseWrite();
            case REMOVE:
                return parseRemove();
            case EDIT:
                return parseEdit();
            case INFO:
                return parseInfo();


            default:
                //Creates a error response
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Mode: " + mode + " is an non-valid mode"));

                return false;
        }
    }

    private boolean parseInfo() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private boolean parseEdit() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may edit a group on the system."));
            return false;
        }


        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private boolean parseRemove() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may remove a group from the system."));
            return false;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private boolean parseWrite() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may add a group to the system."));
            return false;
        }
        return false;
    }

    private boolean parseRead() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }
}

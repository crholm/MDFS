package mdfs.namenode.parser;

import mdfs.namenode.repositories.ACL;
import mdfs.namenode.repositories.UserDataRepository;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.Time;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolUserGroup;
import mdfs.utils.io.protocol.enums.EventStatus;
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
//TODO Implement tests and API
public class ParserRequestUser implements Parser {

    Mode mode;
    Session session;
    UserDataRepositoryNode user;
    MDFSProtocolUserGroup metauser;

    public ParserRequestUser(Mode mode){
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
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.USER, mode, "In valid user or password" ));

            return false;
        }
        //Checks so that the user credentials are correct
        if(!userdata.authUser(user, pass)){
            //Creates a error response
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.USER, mode, "In valid user or password"));

            return false;
        }

        metauser = session.getRequest().getUserGroup();


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
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.USER, mode, "Mode: " + mode + " is an non-valid mode"));

                return false;
        }
    }

    //Returns all users
    private boolean parseInfo() {

        MDFSProtocolUserGroup users = new MDFSProtocolUserGroup();

        MDFSProtocolUserGroup userscontent[] = UserDataRepository.getInstance().toArray();
        for(MDFSProtocolUserGroup user : userscontent){
            users.addMember(user);
        }

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(users);
        response.getInfo().setWritten(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;
    }

    //Changes password
    private boolean parseEdit() {
        if(metauser == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        int uid = metauser.getUid();
        String username = metauser.getUser();

        if(uid == -1 && username == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "A user was not provided"));
            return false;
        }
        if(metauser.getPassword() == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "New password was not provided"));
            return false;
        }

        UserDataRepositoryNode usernode = null;

        if(uid != -1 && username != null){
            usernode = UserDataRepository.getInstance().get(uid);
            if(usernode == null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
                return false;
            }
            if(!usernode.getName().equals(username)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Username dose not match uid"));
                return false;
            }

        }else if(uid != -1){
            usernode = UserDataRepository.getInstance().get(uid);
        }else if(username != null){
            usernode = UserDataRepository.getInstance().get(username);
        }

        if(usernode == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
            return false;
        }

        if(!ACL.isSU(user) && user.getUid() != usernode.getUid()){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Operation not permitted"));
            return false;
        }

        usernode.setPassword(metauser.getPassword());
        usernode.commit();

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(usernode);
        response.getInfo().setWritten(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;

    }

    //Removes user
    private boolean parseRemove() {

        if(metauser == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }

        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Operation not permitted"));
            return false;
        }

        int uid = metauser.getUid();
        String username = metauser.getUser();

        if(uid == -1 && username == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "A user was not provided"));
            return false;
        }

        UserDataRepositoryNode usernode = null;

        if(uid != -1 && username != null){
            usernode = UserDataRepository.getInstance().get(uid);
            if(usernode == null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
                return false;
            }
            if(!usernode.getName().equals(username)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Username dose not match uid"));
                return false;
            }

        }else if(uid != -1){
            usernode = UserDataRepository.getInstance().get(uid);
        }else if(username != null){
            usernode = UserDataRepository.getInstance().get(username);
        }

        if(usernode == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
            return false;
        }

        usernode = UserDataRepository.getInstance().remove(usernode.getUid());


        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(usernode);
        response.getInfo().setRemoved(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;

    }

    //Create/writes new user.
    private boolean parseWrite() {
        if(metauser == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }

        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Operation not permitted"));
            return false;
        }

        int uid = metauser.getUid();
        String username = metauser.getUser();
        String password = metauser.getPassword();

        if(password == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group -> password was not provided"));
            return false;
        }

        if(username == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "A user was not provided"));
            return false;
        }

        UserDataRepositoryNode usernode = null;



        if(uid != -1 && username != null){

            usernode = UserDataRepository.getInstance().get(uid);

            if(usernode != null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "uid already exist"));
                return false;
            }

        }

        usernode = UserDataRepository.getInstance().get(username);
        if(usernode != null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Username already exist"));
            return false;
        }


        if(uid == -1){
            if(UserDataRepository.getInstance().add(username, password))
                usernode = UserDataRepository.getInstance().get(username);
            else{
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Failed to add user"));
                return false;
            }
        }else {
            usernode = new UserDataRepositoryNode(uid, username);
            usernode.setPassword(password);
            if(!UserDataRepository.getInstance().add(usernode)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Failed to add user"));
                return false;
            }
        }

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(usernode);
        response.getInfo().setWritten(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;

    }

    //returns all information about user and group memberships.
    private boolean parseRead() {
        if(metauser == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        int uid = metauser.getUid();
        String username = metauser.getUser();

        if(uid == -1 && username == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "A user was not provided"));
            return false;
        }

        UserDataRepositoryNode usernode = null;

        if(uid != -1 && username != null){
            usernode = UserDataRepository.getInstance().get(uid);
            if(usernode == null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
                return false;
            }
            if(!usernode.getName().equals(username)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Username dose not match uid"));
                return false;
            }

        }else if(uid != -1){
            usernode = UserDataRepository.getInstance().get(uid);
        }else if(username != null){
            usernode = UserDataRepository.getInstance().get(username);
        }

        if(usernode == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
            return false;
        }

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(usernode);

        session.setResponse(response);
        return true;

    }

    private MDFSProtocolHeader createHeader(){
        MDFSProtocolHeader header = new MDFSProtocolHeader();
        header.setStage(Stage.RESPONSE);
        header.setType(Type.GROUP);
        header.setMode(mode);

        MDFSProtocolInfo info = new MDFSProtocolInfo();

        info.setLocalTime(Time.currentTimeMillis());

        header.setInfo(info);

        return header;
    }
}

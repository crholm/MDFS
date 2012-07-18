package mdfs.namenode.parser;

import mdfs.namenode.repositories.*;
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
public class ParserRequestGroup implements Parser {

    Mode mode;
    Session session;
    UserDataRepositoryNode user;
    MDFSProtocolUserGroup metagroup;


    public ParserRequestGroup(Mode mode){
        this.mode = mode;
    }

    @Override
    public boolean parse(Session session) {
        this.session = session;

        UserDataRepository userdata = UserDataRepository.getInstance();
        user = userdata.get(session.getRequest().getUser());
        String pass = session.getRequest().getPass();

        metagroup = session.getRequest().getUserGroup();

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
        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(new MDFSProtocolUserGroup());

        GroupDataRepositoryNode[] groups = GroupDataRepository.getInstance().toArray();
        for(GroupDataRepositoryNode group : groups)
            response.getUserGroup().addMember(group);

        session.setResponse(response);
        return true;


    }

    private boolean parseEdit() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may edit a group on the system."));
            return false;
        }
        if(metagroup == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        if(metagroup.getAction() == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "the field User-group -> action was not set"));
            return false;
        }
        if(metagroup.getAction() != Mode.ADD && metagroup.getAction() != Mode.REMOVE){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "the field User-group -> action was not a valid value"));
            return false;
        }

        int uid = metagroup.getUid();
        String username = metagroup.getUser();
        int gid = metagroup.getGid();
        String groupname = metagroup.getGroup();

        //Checks that all requeried values were in request
        if((uid == -1 && username == null) || (gid == -1 && groupname == null)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "A group and user was not provided"));
                return false;
        }


        //Checking so that user exists and uid and user match if provided.
        UserDataRepositoryNode usernode;
        if((uid != -1 && username != null)){
            usernode =  UserDataRepository.getInstance().get(uid);
            if(usernode == null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
                return false;
            }
            if(!usernode.getName().equals(username)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Username and uid dose not match"));
                return false;
            }


        }else if(uid != -1){
            usernode =  UserDataRepository.getInstance().get(uid);

        }else{
            usernode =  UserDataRepository.getInstance().get(username);

        }

        if(usernode == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "User dose not exist"));
            return false;
        }



        //Checking that group exist and that gid and groupname match
        GroupDataRepositoryNode groupnode;
        if((gid != -1 && groupname != null)){
            groupnode =  GroupDataRepository.getInstance().get(gid);
            if(groupnode == null){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group dose not exist"));
                return false;
            }
            if(!groupnode.getName().equals(groupname)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group and gid dose not match"));
                return false;
            }


        }else if(gid != -1){
            groupnode =  GroupDataRepository.getInstance().get(uid);

        }else{
            groupnode =  GroupDataRepository.getInstance().get(groupname);

        }

        if(groupnode == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group dose not exist"));
            return false;
        }


        boolean success;
        MDFSProtocolHeader response = createHeader();


        if(metagroup.getAction() == Mode.ADD){
            if(groupnode.containsUser(usernode)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group already contains user"));
                return false;
            }

            success = groupnode.addUser(usernode);
            response.getInfo().setWritten(EventStatus.SUCCESSFUL);


        }else if(metagroup.getAction() == Mode.REMOVE){
            if(!groupnode.containsUser(usernode)){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group dose not contains user"));
                return false;
            }

            success = groupnode.removeUser(usernode);
            response.getInfo().setRemoved(EventStatus.SUCCESSFUL);



        }else{
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "the field User-group -> action was not a valid value"));
            return false;
        }
        if(!success){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Operation failed"));
            return false;
        }


        response.setUserGroup(groupnode);

        session.setResponse(response);
        return true;

    }

    private boolean parseRemove() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may remove a group from the system."));
            return false;
        }
        if(metagroup == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        if(metagroup.getGroup() == null && metagroup.getGid() == -1){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group -> group and User-group -> gid was not provided"));
            return false;
        }
        //Checks so that group name and gid match
        if(metagroup.getGroup() != null && metagroup.getGid() != -1){
            if(!metagroup.getGroup().equals(GroupDataRepository.getInstance().getName(metagroup.getGid()))){
                this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field group name and giu dose not match"));
                return false;
            }
        }

        GroupDataRepositoryNode group;
        if(metagroup.getGid() != -1)
            group = GroupDataRepository.getInstance().remove(metagroup.getGid());
        else
            group = GroupDataRepository.getInstance().remove(metagroup.getGroup());

        if(group == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group dose not exist"));
            return false;
        }

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(group);
        response.getInfo().setRemoved(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;

    }


    //Creates/writes a new group.
    private boolean parseWrite() {
        if(!ACL.isSU(user)){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Only su may add a group to the system."));
            return false;
        }

        if(metagroup == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        if(metagroup.getGroup() == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group -> group was not provided"));
            return false;
        }

        if(metagroup.getGid() != -1 && GroupDataRepository.getInstance().contains(metagroup.getGid()) ){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "gid already exists"));
            return false;
        }
        if(GroupDataRepository.getInstance().contains(metagroup.getGroup())){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group name already exists"));
            return false;
        }


        GroupDataRepositoryNode group;
        if(metagroup.getGid() == -1)
            group = GroupDataRepository.getInstance().add(metagroup.getGroup());
        else
            group = GroupDataRepository.getInstance().add(metagroup.getGid(), metagroup.getGroup());

        if(group == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group already exists"));
            return false;
        }

        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(group);
        response.getInfo().setWritten(EventStatus.SUCCESSFUL);

        session.setResponse(response);
        return true;
    }

    private boolean parseRead() {
        if(metagroup == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group was not provided"));
            return false;
        }
        if(metagroup.getGroup() == null && metagroup.getGid() == -1){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Field User-group -> group nor User-group -> gid was not provided"));
            return false;
        }

        GroupDataRepositoryNode group;
        if(metagroup.getGid() != -1)
            group = GroupDataRepository.getInstance().get(metagroup.getGid());
        else
            group = GroupDataRepository.getInstance().get(metagroup.getGroup());

        if(group == null){
            this.session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.GROUP, mode, "Group dose not exists"));
            return false;
        }


        MDFSProtocolHeader response = createHeader();
        response.setUserGroup(group);

        response.getInfo().setWritten(EventStatus.SUCCESSFUL);

        session.setResponse(response);

        return false;
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

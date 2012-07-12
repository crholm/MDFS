package mdfs.client.api.io;

import mdfs.utils.ArrayUtils;
import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.*;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Package: mdfs.client.api
 * Created: 2012-06-21
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSFile {

    private MDFSProtocolMetaData metadata;
    private MDFSProtocolInfo info;
    private String path;
    private String user;
    private String pass;

    public MDFSFile(String path, String user, String pass) throws IOException{
        this.path = path;
        this.pass = pass;
        this.user = user;



    }

    public MDFSFile(MDFSProtocolMetaData metadata, String user, String pass) throws IOException{
        this(metadata.getPath(), user, pass);
    }

    private void getFileData() throws IOException{
        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();


        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.READ);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        request.setMetadata(metadata);

        SocketFactory socketFactory = new SocketFactory();
        SocketFunctions socketFunctions = new SocketFunctions();


        Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"),
															Config.getInt("NameNode.port") );


        if(!  socketFunctions.sendText(nameNodeSocket, request.toString())  )
            throw new IOException("Failed to send request to NameNode");

        MDFSProtocolHeader response;



        String s =  socketFunctions.receiveText(nameNodeSocket);

        try {
            nameNodeSocket.close();
        } catch (IOException e) {}

        if(s == null)
            throw new IOException("No response could be read from NameNode");

        try {
            response = new MDFSProtocolHeader(s);
        } catch (JSONException e) {
            throw new IOException("Could not pares response from NameNode");
        }


        if(response.getMetadata() != null && response.getMetadata().getType() != null )
            this.metadata = response.getMetadata();

        this.info = response.getInfo();


    }


    public boolean canExecute(){
        if(metadata == null)
            return false;

        int permission = metadata.getPermission();
        if(permission < 0)
            return false;

        permission = permission % 778;


        int all = permission % 10;
        if(all%4%2 == 1)
            return true;

        int user = permission / 100;
        if(user%4%2 == 1)
            return true;

        int group = (permission % 100 - all) / 10 ;
        if(group%4%2 == 1)
            return true;
        //TODO Implement check that user in right group

        return false;
    }
    public boolean canRead(){
        if(metadata == null)
            return false;

        int permission = metadata.getPermission();
        if(permission < 0)
            return false;

        permission = permission % 778;

        int all = permission % 10;
        if(all>>2 == 1)
            return true;

        int user = permission / 100;
        if(user>>2 == 1)
            return true;

        int group = (permission % 100 - all) / 10 ;
        if(group >> 2 == 1)
            return true;
        //TODO Implement check that user in right group

        return false;
    }
    public boolean canWrite(){
        if(metadata == null)
            return false;

        int permission = metadata.getPermission();
        if(permission < 0)
            return false;

        permission = permission % 778;

        int all = permission % 10;
        if( (all>>1) % 2 == 1)
            return true;

        int user = permission / 100;
        if( (user>>1) % 2 == 1)
            return true;

        int group = (permission % 100 - all) / 10 ;
        if( (group >> 1) % 2 == 1)
            return true;
        //TODO Implement check that user in right group

        return false;

    }
    public int compareTo(MDFSFile file){
        return this.getPath().compareTo(file.getPath());
    }


    //TODO Implement this
    public boolean createNewFile(){
        if(exists())
            return false;

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();
        SocketFunctions socketFunctions = new SocketFunctions();
        SocketFactory socketFactory = new SocketFactory();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.WRITE);

        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        metadata.setType(MetadataType.FILE);
        metadata.setSize(-1);
        metadata.setPermission(640);
        metadata.setOwner(user);
        metadata.setGroup(user);
        metadata.setCreated(Time.currentTimeMillis());
        metadata.setLastEdited(Time.currentTimeMillis());
        metadata.setLastTouched(Time.currentTimeMillis());

        request.setMetadata(metadata);


        //Creates a socket to the name node
        Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"),
                                                            Config.getInt("NameNode.port") );

        //Sends request to name node
        if(! socketFunctions.sendText(nameNodeSocket, request.toString()))
            return false;

        //Retrives response from name node
        MDFSProtocolHeader response;
        String s = socketFunctions.receiveText(nameNodeSocket);
        if(s == null)
            return false;

        try {
            response = new MDFSProtocolHeader(s);

        } catch (JSONException e) {
            return false;
        }

        try {
            nameNodeSocket.close();
        } catch (IOException ignored) {}

        if(response.getError() != null)
            return false;

        this.metadata = response.getMetadata();
        this.info = response.getInfo();

        return true;
    }


    public boolean delete(){
        if(!exists())
            return false;

        if(hasChildren())
            return false;

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.REMOVE);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        request.setMetadata(metadata);


        SocketFactory socketFactory = new SocketFactory();
        SocketFunctions socketFunctions = new SocketFunctions();


        Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"),
                                                            Config.getInt("NameNode.port") );
        if(nameNodeSocket == null ){
            return false;
        }

        if(! socketFunctions.sendText(nameNodeSocket, request.toString()) )
            return false;


        MDFSProtocolHeader response;
        try {
            response = new MDFSProtocolHeader(socketFunctions.receiveText(nameNodeSocket));

        } catch (JSONException e) {
           return false;
        }
        try {
            nameNodeSocket.close();
        } catch (IOException ignored) {}


        if(response.getError() != null)
            return false;

        MDFSProtocolInfo info = response.getInfo();
        if(info == null)
            return false;

        if(info.getRemoved() == EventStatus.SUCCESSFUL)
            return true;

        return false;
    }

    public boolean exists(){
        if(metadata == null)
            return false;

        if(metadata.getType() == MetadataType.FILE){

            if(metadata.getLocation() == null)
                return false;

            if(metadata.getLocation().getName() == null)
                return false;

        }else if(metadata.getType() != MetadataType.DIR)
            return false;

        return true;
    }

    public  MDFSFile[] getChildren(){
        if(!exists())
            return null;

        int size = metadata.getChildrenSize();

        if(size == 0)
            return null;

        LinkedList<MDFSProtocolMetaData> metadataChildren = metadata.getChildren();

        try{
            MDFSFile file[] = new MDFSFile[size];
            int i = 0;
            for(MDFSProtocolMetaData child : metadataChildren){
                file[i] = new MDFSFile(child.getPath(), user, pass);
                i++;
            }

            return file;

        }catch (Exception e){
            return null;
        }



    }

    public boolean hasChildren(){
        if(metadata == null)
            return false;
        return metadata.getChildrenSize() > 0 ? true : false;
    }

    //TODO Implement in protocol, on NameNode and in SQL.
    public long getFreeSpace(){
        return 0;
    }

    public String getName(){
        if(metadata == null)
            return null;

        String name = metadata.getPath();
        if(name == null)
            return null;

        if(name.lastIndexOf( (int)'/' ) == name.length()-1)
            name = name.substring(0, name.length()-1);

        return name.substring(name.lastIndexOf("/")+1);
    }

    public String getParent(){
        if(metadata == null)
            return null;

        String name = metadata.getPath();
        if(name == null)
            return null;

        name = name.replace("////", "/");
        name = name.replace("///", "/");
        name = name.replace("//", "/");
        name = name.replace("/", " \n\r");
        name = name.trim();

        String path[] = name.split(" \n\r");

        if(path.length < 2)
            return null;


        return "/" + ArrayUtils.implode(path, "/", path.length-1);

    }
    public MDFSFile getParentFile(){
        String parent = getParent();
        if(parent == null)
            return null;

        try {
            return new MDFSFile(parent, user, pass);
        } catch (IOException e) {
            return null;
        }
    }
    public String getPath(){
        return this.path;
    }

    //TODO Implement in protocol, on NameNode and in SQL.
    public long getTotalSpace(){
        return 0;
    }
    public int hashCode(){
        return getPath().hashCode();
    }
    public boolean isDirectory(){
        if(!exists())
            return false;

        if(metadata.getType() == MetadataType.DIR)
            return true;

        return false;
    }
    public boolean isFile(){
        if(!exists())
            return false;

        if(metadata.getType() == MetadataType.FILE)
            return true;

        return false;
    }
    //TODO change implementation of time to hold the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
    public long lastModified(){
        return 0;
    }
    public long length(){
        if(exists())
            return metadata.getSize();
        return -1;
    }


    public boolean mkdir(){
        if(exists())
            return false;

        MDFSFile parent = getParentFile();
        if(parent == null || !parent.exists())
            return false;


        //Creates a request to the name node
        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();
        SocketFunctions socketFunctions = new SocketFunctions();
        SocketFactory socketFactory = new SocketFactory();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.WRITE);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        metadata.setType(MetadataType.DIR);
        metadata.setSize(0);
        metadata.setPermission(750);
        metadata.setOwner(user);
        metadata.setGroup(user);

        long timestamp = Time.currentTimeMillis();
        metadata.setCreated(timestamp);
        metadata.setLastEdited(timestamp);
        metadata.setLastTouched(timestamp);

        request.setMetadata(metadata);

        //Sends request to name node
        //Creates a socket to the name node
        Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"),
                                                               Config.getInt("NameNode.port") );

        if(! socketFunctions.sendText(nameNodeSocket, request.toString()))
            return false;

        //Receives the response from the name node
        MDFSProtocolHeader response;

        try {
            response = new MDFSProtocolHeader(socketFunctions.receiveText(nameNodeSocket));
        } catch (JSONException e) {
            return false;
        }

        try {
            nameNodeSocket.close();
        } catch (IOException ignored){}

        if(response.getError() != null){
           return false;
        }

        return true;
    }
    public boolean mkdirs(){
        if(exists())
            return false;

        String name = path;
        name = name.replace("////", "/");
        name = name.replace("///", "/");
        name = name.replace("//", "/");
        name = name.replace("/", " \n\r");
        name = name.trim();

        String path[] = name.split(" \n\r");

        String tPath = "";
        for(String p : path){
            tPath += "/" + p;

            try{

                MDFSFile dir = new MDFSFile(tPath, user, pass);

                if(!dir.exists()){
                    if(!dir.mkdir())
                        return false;
                }

            }catch (IOException ignored){
                return false;
            }
        }

        return true;
    }


    //TODO implement what follows
    public boolean renameTo(MDFSFile dest){
        return true;
    }
    public boolean renameTo(String dest){
        return true;
    }
    public boolean setPermission(int chmod){
        return true;
    }



    public String toString(){
        return getPath();
    }

    protected MDFSProtocolMetaData getMetadata(){
        return metadata;
    }

    protected MDFSProtocolInfo getInfo(){
        return info;
    }




}

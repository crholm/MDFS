package mdfs.client.api.io;

import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;

import java.io.File;

/**
 * Package: mdfs.client.api
 * Created: 2012-06-21
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSFile{

    public String path;

    public MDFSFile(String path, String user, String pass){


    }


    public boolean canExecute(){
        return true;
    }
    public boolean canRead(){
        return true;
    }
    public boolean canWrite(){
        return true;
    }
    public int compareTo(MDFSFile file){
        return this.getPath().compareTo(file.getPath());
    }

    public boolean createNewFile(){
        return true;
    }

    public boolean delete(){
        return true;
    }

    public boolean exists(){
        //if file == file file.getMetadata().getLocation() == null || file.getMetadata().getLocation().getName() == null || file.getMetadata().getLocation().getHostsSize() == 0
        return true;
    }

    public  MDFSFile[] getChildren(){
        return null;
    }

    public long getFreeSpace(){
        return 0;
    }

    public String getName(){
        return null;
    }

    public String getParent(){
        return null;
    }
    public MDFSFile getParentFile(){
        return null;
    }
    public String getPath(){
        return path;
    }
    public long getTotalSpace(){
        return 0;
    }
    public int hashCode(){
        return getPath().hashCode();
    }
    public boolean isDirectory(){
        return true;
    }
    public boolean isFile(){
        return true;
    }
    public long lastModified(){
        return 0;
    }
    public long length(){
        return 0;
    }
    public boolean mkdir(){
        return true;
    }
    public boolean mkdirs(){
        return true;
    }
    public boolean renameTo(File dest){
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
        return null;
    }

    protected MDFSProtocolInfo getInfo(){
        return null;
    }




}

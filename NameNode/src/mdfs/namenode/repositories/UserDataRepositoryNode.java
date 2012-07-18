package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.crypto.digests.SHA1;
import mdfs.utils.io.protocol.MDFSProtocolUserGroup;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A node that holds user data
 * @author Rasmus Holm
 *
 */
public class UserDataRepositoryNode extends MDFSProtocolUserGroup {
	private String pwdHash;

    private LinkedList<MDFSProtocolUserGroup> groups;
    private ReentrantLock lock = new ReentrantLock(true);


	/**
	 * 
	 * @param name sets user name
	 */
	public UserDataRepositoryNode(int uid, String name){
		super.setUser(name);
        super.setUid(uid);
        groups = super.getMembers();
	}


    /**
	 * 
	 * @return the key, the username
	 */
	public String getName(){
		return super.getUser();
	}

	/**
	 * 
	 * @return the hash of the password
	 */
	public String getPwdHash() {
		return pwdHash;
	}
	/**
	 * 
	 * @param pwdHash sets the hashed password
	 */
	public void setPwdHash(String pwdHash) {
		this.pwdHash = pwdHash;
	}

    @Override
    public void setPassword(String password) {
        try {
            SHA1 md = new SHA1();
            byte hash[] = new byte[md.getDigestSize()];
            md.doFinal(password.getBytes("UTF8"), hash,0);
            setPwdHash(new BigInteger(1, hash).toString(16) );

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     * @param group
     * @return
     */
    void addedToGroup(GroupDataRepositoryNode group){
        lock.lock();
        try{
            if(groups.contains(group))
                return;
            groups.add(group);
        }finally {
            lock.lock();
        }

    }

    void removedFromGroup(GroupDataRepositoryNode group) {
        lock.lock();
        try{
            groups.remove(group);
        }finally {
            lock.unlock();
        }
    }

    public GroupDataRepositoryNode[] getGroupMemberships(){
        lock.lock();
        try{
            int size = groups.size();
            GroupDataRepositoryNode nodes[] = new GroupDataRepositoryNode[size];
            nodes = groups.toArray(nodes);
            return nodes;
        }finally {
            lock.unlock();
        }
    }

    public void commit(){
        MySQLUpdater.getInstance().update(this);
    }

}

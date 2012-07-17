package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.io.protocol.MDFSProtocolUserGroup;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A node that holds user data
 * @author Rasmus Holm
 *
 */
public class UserDataRepositoryNode extends MDFSProtocolUserGroup {
	private String pwdHash;
    //TODO, same list occores in super class, Implement it with casting?
    private LinkedList<GroupDataRepositoryNode> groups = new LinkedList<GroupDataRepositoryNode>();
    private ReentrantLock lock = new ReentrantLock(true);


	/**
	 * 
	 * @param name sets user name
	 */
	public UserDataRepositoryNode(int uid, String name){
		super.setUser(name);
        super.setUid(uid);
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

    public GroupDataRepositoryNode[] getGroupMembership(){
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

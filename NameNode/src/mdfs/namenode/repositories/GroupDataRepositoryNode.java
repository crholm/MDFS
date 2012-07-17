package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.io.protocol.MDFSProtocolUserGroup;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Package: mdfs.namenode.repositories
 * Created: 2012-07-12
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class GroupDataRepositoryNode extends MDFSProtocolUserGroup{
    //TODO, same list occores in super class, Implement it with casting?
    private LinkedList<UserDataRepositoryNode> users = new LinkedList<UserDataRepositoryNode>();
    private ReentrantLock lock = new ReentrantLock(true);

    public GroupDataRepositoryNode(int gid, String name){
        super.setGid(gid);
        super.setGroup(name);
    }


    public String getName() {
        return super.getGroup();
    }

    public boolean addUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            if(users.contains(node))
                return false;
            users.add(node);
            node.addedToGroup(this);
            MySQLUpdater.getInstance().updateRelation(this, node);

            return true;
        }finally {
            lock.unlock();
        }
    }
    void loadUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            if(users.contains(node))
                return;
            users.add(node);
            node.addedToGroup(this);
        }finally {
            lock.unlock();
        }

    }

    public boolean removeUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            boolean r = users.remove(node);
            node.removedFromGroup(this);

            if(r)
                MySQLUpdater.getInstance().removeRelation(this, node);

            return r;
        }finally {
            lock.unlock();
        }
    }

    public boolean containsUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            return users.contains(node);
        }finally {
            lock.unlock();
        }

    }

    public UserDataRepositoryNode[] getGroupMembers(){
        lock.lock();
        try{
            int size = users.size();
            UserDataRepositoryNode nodes[] = new UserDataRepositoryNode[size];
            nodes = users.toArray(nodes);
            return nodes;
        }finally {
            lock.unlock();
        }
    }


    public void commit(){
        MySQLUpdater.getInstance().update(this);
    }

}

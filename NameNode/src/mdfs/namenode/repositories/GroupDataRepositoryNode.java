package mdfs.namenode.repositories;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Package: mdfs.namenode.repositories
 * Created: 2012-07-12
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class GroupDataRepositoryNode {
    private int gid;
    private String name;
    private LinkedList<UserDataRepositoryNode> users = new LinkedList<UserDataRepositoryNode>();
    private ReentrantLock lock = new ReentrantLock(true);

    public GroupDataRepositoryNode(int gid, String name){
        this.gid = gid;
        this.name = name;
    }

    public int getGid() {
        return gid;
    }


    public String getName() {
        return name;
    }

    public boolean addUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            if(users.contains(node))
                return false;
            users.add(node);
            node.addedToGroup(this);
            //TODO Implement add to SQL
            return true;
        }finally {
            lock.unlock();
        }
    }

    public boolean removeUser(UserDataRepositoryNode node){
        lock.lock();
        try{
            boolean r = users.remove(node);
            node.removedFromGroup(this);
            //TODO Implement remove from SQL
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


}

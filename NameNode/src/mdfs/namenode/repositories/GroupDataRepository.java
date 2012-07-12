package mdfs.namenode.repositories;

import mdfs.utils.SplayTree;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Package: mdfs.namenode.repositories
 * Created: 2012-07-12
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class GroupDataRepository {
    private static GroupDataRepository ourInstance = new GroupDataRepository();
    private SplayTree<Integer, GroupDataRepositoryNode> repositoryGid = new SplayTree<Integer, GroupDataRepositoryNode>();
    private SplayTree<String, GroupDataRepositoryNode> repository = new SplayTree<String, GroupDataRepositoryNode>();
    private int gidCounter = 1000000;
    private ReentrantLock lock = new ReentrantLock(true);

    public static GroupDataRepository getInstance() {
        return ourInstance;
    }

    private GroupDataRepository() { }

    /**
     *
     * @param name
     * @return new groupe, null if it already exists
     */
    public GroupDataRepositoryNode addGroup(String name){
        lock.unlock();
        try{
            GroupDataRepositoryNode node = addGroup(gidCounter, name);

            if(node != null)
                gidCounter++;

            return node;
        }finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param gid
     * @param name
     * @return new group, null if it already exists
     */
    public GroupDataRepositoryNode addGroup(int gid, String name){
        lock.lock();
        try{
            if(repository.contains(name))
                return null;
            if(repositoryGid.contains(gid))
                return null;


            GroupDataRepositoryNode node = new GroupDataRepositoryNode(gid, name);
            repository.put(node.getName(), node);
            repositoryGid.put(node.getGid(), node);
            return node;

        }finally {
            lock.unlock();
        }
    }

    public GroupDataRepositoryNode getGroup(int gid){
        lock.lock();
        try{
            return repositoryGid.get(gid);
        }finally {
            lock.unlock();
        }
    }
    public GroupDataRepositoryNode getGroup(String name){
        lock.lock();
        try{
            return repository.get(name);
        }finally {
            lock.unlock();
        }
    }

    public int getGid(String name){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repository.get(name);
            if(node == null)
                return -1;
            return node.getGid();
        }finally {
            lock.unlock();
        }
    }
    public String getName(int gid){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repositoryGid.get(gid);
            if(node == null)
                return null;
            return node.getName();
        }finally {
            lock.unlock();
        }
    }


    public GroupDataRepositoryNode removeGroup(int gid){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repositoryGid.remove(gid);

            if(node == null)
                return null;

            repository.remove(node.getName());

            return node;
        }finally {
            lock.unlock();
        }
    }
    public GroupDataRepositoryNode removeGroup(String name){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repository.remove(name);

            if(node == null)
                return null;

            repositoryGid.remove(node.getGid());

            return node;
        }finally {
            lock.unlock();
        }
    }




}

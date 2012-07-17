package mdfs.namenode.repositories;

import mdfs.namenode.sql.MySQLFetch;
import mdfs.namenode.sql.MySQLUpdater;
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
    private static GroupDataRepository instance;
    private SplayTree<Integer, GroupDataRepositoryNode> repositoryGid = new SplayTree<Integer, GroupDataRepositoryNode>();
    private SplayTree<String, GroupDataRepositoryNode> repository = new SplayTree<String, GroupDataRepositoryNode>();
    private int gidCounter = 1000000;
    private ReentrantLock lock = new ReentrantLock(true);


    public static GroupDataRepository getInstance() {
        if(instance == null)
            instance = new GroupDataRepository();
        return instance;
    }

    private GroupDataRepository() {
        lock.lock();
        try{
            load();
        }finally{
            lock.unlock();
        }
    }

    /**
     *
     *
     * @param name
     * @return new groupe, null if it already exists
     */
    public GroupDataRepositoryNode add(String name){
        lock.unlock();
        try{
            GroupDataRepositoryNode node = add(gidCounter, name);


            if(node != null)
                gidCounter++;

            return node;
        }finally {
            lock.unlock();
        }
    }

    /**
     *
     *
     * @param gid
     * @param name
     * @return new group, null if it already exists
     */
    public GroupDataRepositoryNode add(int gid, String name){
        lock.lock();
        try{
            if(repository.contains(name))
                return null;
            if(repositoryGid.contains(gid))
                return null;


            GroupDataRepositoryNode node = new GroupDataRepositoryNode(gid, name);
            repository.put(node.getName(), node);
            repositoryGid.put(node.getGid(), node);

            MySQLUpdater.getInstance().update(node);
            return node;

        }finally {
            lock.unlock();
        }
    }

    public GroupDataRepositoryNode get(int gid){
        lock.lock();
        try{
            return repositoryGid.get(gid);
        }finally {
            lock.unlock();
        }
    }
    public GroupDataRepositoryNode get(String name){
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


    public GroupDataRepositoryNode remove(int gid){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repositoryGid.remove(gid);

            if(node == null)
                return null;

            repository.remove(node.getName());

            MySQLUpdater.getInstance().remove(node);
            return node;
        }finally {
            lock.unlock();
        }
    }
    public GroupDataRepositoryNode remove(String name){
        lock.lock();
        try{
            GroupDataRepositoryNode node = repository.remove(name);

            if(node == null)
                return null;

            repositoryGid.remove(node.getGid());
            MySQLUpdater.getInstance().remove(node);
            return node;
        }finally {
            lock.unlock();
        }
    }

    public boolean contains(int gid){
        lock.lock();
        try{
            return repositoryGid.contains(gid);
        }finally {
            lock.unlock();
        }
    }
    public boolean contains(String group){
        lock.lock();
        try{
            return repository.contains(group);
        }finally {
            lock.unlock();
        }
    }

    public GroupDataRepositoryNode[] toArray(){
        lock.lock();
        try{
            GroupDataRepositoryNode[] nodes = new GroupDataRepositoryNode[repository.size()];
            int i = 0;
            for(GroupDataRepositoryNode node : repository){
                nodes[i++] = node;
            }
            return nodes;
        }finally {
            lock.unlock();
        }

    }


    public void load(){
        lock.lock();
        try{


            MySQLFetch sql = new MySQLFetch();


            int partition = 512;


            //Loading groups.
            int size = sql.countRows("group-data");
            GroupDataRepositoryNode groups[];
            for(int i = 0; i < size/partition+1; i++){

                if(i < size/partition)
                    groups = sql.getGroupDataRepositoryNodes(i*partition, partition);
                else
                    groups = sql.getGroupDataRepositoryNodes(((int)(size/partition)) * partition, size%partition);

                for(GroupDataRepositoryNode group : groups){
                    repository.put(group.getName(), group);
                    repositoryGid.put(group.getGid(), group);
                }
            }


            //Loading User Group relation.
            size = sql.countRows("user-data_group-data");
            int relation[][];
            for(int i = 0; i < size/partition+1; i++){

                if(i < size/partition)
                    relation = sql.getUserGroupRelation(i*partition, partition);
                else
                    relation = sql.getUserGroupRelation(((int)(size/partition)) * partition, size%partition);

                for(int r[] : relation){
                    GroupDataRepositoryNode group = repositoryGid.get(r[0]);
                    UserDataRepositoryNode user = UserDataRepository.getInstance().get(r[1]);
                    group.loadUser(user);
                }
            }



            //Loading user uid counter
            int count = sql.max("group-data", "gid");
            if(count > gidCounter)
                gidCounter = count;

        }finally {
            lock.unlock();
        }


    }


}

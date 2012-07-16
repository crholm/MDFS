package mdfs.namenode.repositories;


import mdfs.namenode.sql.MySQLFetch;
import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.SplayTree;
import mdfs.utils.Time;
import mdfs.utils.crypto.digests.SHA1;
import mdfs.utils.io.protocol.enums.MetadataType;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;


/**
 * UserDataRepository stores MDFS user data that correlates to the files, authentication and so on.
 * It is a Singelton since it is a shared recorce and are synchronized via {@link ReentrantLock}
 * for allowing multiple threads. When a lock is acquired all other operations on the UserDataRepository
 * are locked. If the thread that holds the lock fails/craches, The lock will be unlocked.
 * @author Rasmus Holm
 *
 */
public class UserDataRepository {
	
	
    private SplayTree<Integer, UserDataRepositoryNode> repositoryUid = new SplayTree<Integer, UserDataRepositoryNode>();
    private SplayTree<String, UserDataRepositoryNode> repository = new SplayTree<String, UserDataRepositoryNode>();
    private ReentrantLock lock = new ReentrantLock(true);
	private static UserDataRepository instance= null;
    private int uidCounter = 1000;
	
	
	private UserDataRepository(){
		lock.lock();
		try{
			load();
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * 
	 * @return the instance of the UserDataRepository
	 */
	public static UserDataRepository getInstance(){	
		if(instance == null){
			instance = new UserDataRepository();
		}
		return instance;
	}
	
	/**
	 * 1. Adds a user in the form a {@link UserDataRepositoryNode} to the Repository where the key is the username.
	 * 2. Creates a home dir for the new user.
	 * 3. Updates permanent storage with new user.
	 *
     * @param node the UserDataRepository node to add to repositori
     * @return true if successful, false if it is preexisting
	 */
	public boolean add(UserDataRepositoryNode node){
		
		lock.lock();
		boolean result = false;
		try{
			//Makes sure the user dose not exist
			if(!repository.containsKey(node.getName()) && !repositoryUid.containsKey(node.getUid()) ){
				
				//Adds user to repo
				repository.put(node.getName(), node);
                repositoryUid.put(node.getUid(), node);

                //Creating home group and adding user to it.
                GroupDataRepositoryNode group = GroupDataRepository.getInstance().add(node.getUid(), node.getName());
                group.addUser(node);
				
				//Creates the home dir for the new user
				MetaDataRepositoryNode homeDir = new MetaDataRepositoryNode();
				homeDir.setFilePath("/" + node.getName());
				homeDir.setFileType(MetadataType.DIR);


                long time = Time.currentTimeMillis();
                homeDir.setCreated(time);
				homeDir.setLastEdited(time);

				homeDir.setOwner(node.getName());
				homeDir.setGroup(node.getName());


				
				//Adds the new dir to the repo
				MetaDataRepository.getInstance().add(homeDir.getKey(), homeDir);
				
				//Writes the new user to permanent storage
				MySQLUpdater.getInstance().update(node);

				result = true;
			}
		}finally{
			lock.unlock();
		}
		
		return result;
	}


    /**
     * 1. Adds a user in the form a {@link UserDataRepositoryNode} but generated from name and pass to the Repository where the key is the username
     * 2. Creates a home dir for the new user.
     * 3. Updates permanent storage with new user.
     *
     * @param name the username of the new user
     * @param pass the cleartext password of the new user
     * @return true if successful, false if it is preexisting
     */
    public boolean add(String name, String pass){
        lock.lock();
        boolean result = false;
        try{
            //TODO Save user counter.
            UserDataRepositoryNode node = new UserDataRepositoryNode(uidCounter++, name);

            node.setPwdHash(SHA1.quick(pass.getBytes("UTF8")));
            result = add(node);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally{
            lock.unlock();
        }
        return result;
    }

	/**
	 * Removes a user from the repository.
	 *
     * @param name the name of the user to be removed
     * @return the removed users
	 */
	public UserDataRepositoryNode remove(String name){
		lock.lock();
		UserDataRepositoryNode result;
		try{
			result = repository.remove(name);
			if(result != null) {
                result = repositoryUid.remove(result.getUid());
                removeUserFromGroups(result);
				MySQLUpdater.getInstance().remove(result);
            }
		}finally{
			lock.unlock();
		}
		return result;
	}
    public UserDataRepositoryNode remove(int uid){
        lock.lock();
        UserDataRepositoryNode result;
        try{
            result = get(uid);
            result = remove(result.getName());

        }finally{
            lock.unlock();
        }
        return result;
    }

    private void removeUserFromGroups(UserDataRepositoryNode user){
        GroupDataRepositoryNode[] groups = user.getGroupMembership();
        for(GroupDataRepositoryNode group : groups)
            group.removeUser(user);
    }
	

	
	/**
	 * Fetches the user from the repository
	 *
     * @param name the username of the user that is requested
     * @return the user, null if  it dose not exist
	 */
	public UserDataRepositoryNode get(String name){
		lock.lock();
		UserDataRepositoryNode node = null;
		try{
			node = repository.get(name);
		}finally{
			lock.unlock();
		}
		return node;
	}

    public UserDataRepositoryNode get(int uid){
        lock.lock();
        UserDataRepositoryNode node = null;
        try{
            node = repositoryUid.get(uid);
        }finally{
            lock.unlock();
        }
        return node;
    }
	
	/**
	 * Authenticates a user
	 * @param name username as a String
	 * @param password clear text password
	 * @return true if valid, false if wrong password
	 */
	public boolean authUser(String name, String password){
		lock.lock();
		boolean result = false;
		try{
			UserDataRepositoryNode user = get(name);
			result = authUser(user, password);
		}finally{
			lock.unlock();
		}
		return result;
	}
    public boolean authUser(int uid, String password){
        lock.lock();
        boolean result = false;
        try{
            UserDataRepositoryNode user = get(uid);
            result = authUser(user, password);
        }finally{
            lock.unlock();
        }
        return result;
    }
	/**
	 * Authenticates a user
	 * @param user the node that the password is checked against
	 * @param password
	 * @return true if valid, false if wrong password
	 */
	public boolean authUser(UserDataRepositoryNode user, String password){
		lock.lock();
		boolean result = false;
		try{
			if(SHA1.quick(password.getBytes("UTF8")).equals(user.getPwdHash())){
				result = true;
			}
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally{
			lock.unlock();
		}
		return result;
	}
	

	/**
	 * Saves the repository to permanent storage
	 */
	public void save(){
		
	}

    public String getName(int uid) {
        lock.lock();
        try{
            UserDataRepositoryNode node = repositoryUid.get(uid);
            if(node == null)
                return null;
            return node.getName();
        }finally {
            lock.unlock();
        }
    }
    public int getUid(String name) {
        lock.lock();
        try{
            UserDataRepositoryNode node = repository.get(name);
            if(node == null)
                return -1;
            return node.getUid();
        }finally {
            lock.unlock();
        }
    }

    /**
     * Loads user data repository from permanent storage
     */
    public void load(){
        lock.lock();
        try{


            MySQLFetch sql = new MySQLFetch();

            int partition = 512;
            int size = sql.countRows("user-data");

            //Loaing userdata
            UserDataRepositoryNode[] users;
            for(int i = 0; i < size/partition+1; i++){

                if(i < size/partition)
                    users = sql.getUserDataRepositoryNodes(i*partition, partition);
                else
                    users = sql.getUserDataRepositoryNodes(((int)(size/partition)) * partition, size%partition);

                for (UserDataRepositoryNode user : users) {
                    repository.put(user.getName(), user);
                    repositoryUid.put(user.getUid(), user);
                }
            }

            //Loading user uid counter
            int count = sql.max("user-data", "uid");
            if(count > uidCounter)
                uidCounter = count;



        }finally {
            lock.unlock();
        }

    }
}


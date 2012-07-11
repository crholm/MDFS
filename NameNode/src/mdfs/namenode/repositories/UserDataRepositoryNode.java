package mdfs.namenode.repositories;

/**
 * A node that holds user data
 * @author Rasmus Holm
 *
 */
public class UserDataRepositoryNode implements Comparable<UserDataRepositoryNode>{
	private int uid;
    private String name;
	private String pwdHash;

	
	/**
	 * 
	 * @param name sets user name
	 */
	public UserDataRepositoryNode(String name){
		this.name = name;
	}

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
	 * 
	 * @return the key, the username
	 */
	public String getKey(){
		return name;
	}
	/**
	 * 
	 * @return the username
	 */
	public String getName() {
		return name;
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
	 */
	@Override
	public int compareTo(UserDataRepositoryNode o) {
		return getName().compareTo(o.getName());
	}
	
}

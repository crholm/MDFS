package mdfs.namenode.repositories;

import mdfs.utils.crypto.HashTypeEnum;
/**
 * A node that holds user data
 * @author Rasmus Holm
 *
 */
public class UserDataRepositoryNode implements Comparable<UserDataRepositoryNode>{
	private String name;
	private String pwdHash;
	private HashTypeEnum hashType = HashTypeEnum.SHA1;
	
	/**
	 * 
	 * @param name sets user name
	 */
	public UserDataRepositoryNode(String name){
		this.name = name;
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
	 * @return the type of hashing the password should be hashed with
	 */
	public HashTypeEnum getHashType() {
		return hashType;
	}
	/**
	 * 
	 * @param hashType set the type of which the password should be hashed with
	 */
	public void setHashType(HashTypeEnum hashType) {
		this.hashType = hashType;
	}
	
	/**
	 * 
	 */
	@Override
	public int compareTo(UserDataRepositoryNode o) {
		return getName().compareTo(o.getName());
	}
	
}

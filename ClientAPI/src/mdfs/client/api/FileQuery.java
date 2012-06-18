package mdfs.client.api;

import mdfs.utils.io.protocol.MDFSProtocolMetaData;

import java.io.File;
import java.io.InputStream;

/**
 * This Interface enabels the user to access a MDFS file system in a manner that are simulare to generic terminal commands.
 * @author  Rasmus Holm
 */
public interface FileQuery {
	
	/**
	 * Sets the username to be used when loging on to the MDFS file system
	 * @param user
	 */
	public void setUser(String user);
	/**
	 * Sets the password used when logging on to the MDFS filesystem
	 * @param pass
	 */
	public void setPass(String pass);
	
	/**
	 * Returns the last error in case of one
	 * @return
	 */
	public String getError();
	
	
	/**
	 * Lets you navigate the MDFS file system
	 * @param path - the relative or absolute path you want to navigate to
	 * @param flag
	 * @return
	 */
	public boolean cd(String path, String flag);
	
	/**
	 * Returns a Array of JSONObjects representing directory that are navigated too. 
	 * @param flag
	 * @return object[0] is the Meta-data of the dir and object[1..*] are all the children of object[0] if any. 
	 *  
	 */
	public MDFSProtocolMetaData[] ls(String flag);
	
	/**
	 * Returns a Array of JSONObjects representing directory that are navigated too.
	 * @param path - the relative or absolute path to the directory or file query. 
	 * @param flag
	 * @return object[0] is the Meta-data of the dir or object[1..*] are all the children of object[0] if any. 
	 * @return null if file or dir dose not exist
	 */
	public MDFSProtocolMetaData[] ls(String path, String flag);
	
	/**
	 * Returns the current path that are navigated too in MDFS
	 * @return - the current path that are navigated too in MDFS
	 */
	public String pwd();
	
	/**
	 * Removes a file or dir from MDFS. 
	 * If no flag is used it only removes files or empty dirs only.
	 * to recursively remove a dir and all its content use flag 'r'
	 * @param path - the relative or absolute path to the file to be removed.
	 * @param flag
	 * @return true if successful, false otherwise.
	 */
	public boolean rm(String path, String flag);

	
	public InputStream get(String sourcePath, String flag);
	
	/**
	 * Retries a file from MDFS and saves it to the local or mapped file system
	 * @param sourcePath - the absolute or relative path to file to be retrived
	 * @param targetFile - the File that the file from MDFS are written to
	 * @param flag
	 * @return true if successful, false otherwise.
	 */
	public boolean get(String sourcePath, File targetFile, String flag);
	
	/**
	 * Writes a source file to MDFS at relativ location and name of pwd/current path navigated too.
	 * @param sourceFile - the file to be written;
	 * @param flag 'r' recursively writes everything in the dir to filesystem, 'o' overwrites everything with same
	 * @param flag otherwise only merge if not. 
	 * @return true if successful, false otherwise
	 */
	public boolean put(File sourceFile, String flag);
	
	/**
	 * Writes a source file to MDFS at relativ location and name of pwd/current path navigated too.
	 * @param sourceFile - the file to be written;
	 * @param targetPath - the relative or absolute path to the target file in the MDFS file system.
	 * @param flag 'r' recursively writes everything in the dir to filesystem, 'o' overwrites everything with same
	 * @param flag otherwise only merge if not. 
	 * @return true if successful, false otherwise
	 */
	public boolean put(File sourceFile, String targetPath, String flag);
	
	/**
	 * Creats a dir in MDFS file system
	 * @param targetPath - the relative or absolute path to the dir to be created.
	 * @return true if successful, false otherwise
	 */
	public boolean mkdir(String targetPath);
	
	/**
	 * Moves a file or dir Only in MDFS
	 * @param sourcePath - The file or dir path to be moved in MDFS
	 * @param targetPath - The file or dir in MDFS that is the target
	 * @param flag - 'o' overwrites old files or dirs
	 * @return true if sucessfull
	 */
	public boolean mv(String sourcePath, String targetPath, String flag);
	
	/**
	 * Changes permissions on a file in the MDFS file system
	 * @param targetPath - the relative or absolute path to target file or dir
	 * @param octalPermission - new permission code
	 * @param flag
	 * @return true if successful, false otherwise
	 */
	public boolean chmod(String targetPath, short octalPermission, String flag );
	
	/**
	 * Changes the owner of a file in the MDFS file system
	 * @param targetPath - the relative or absolute path to target file or dir
	 * @param owner - the new owner of the file
	 * @param group - the new group of the file
	 * @param flag
	 * @return true if successful, false otherwise 
	 */
	public boolean chown(String targetPath, String owner, String group, String flag );
	
	
}

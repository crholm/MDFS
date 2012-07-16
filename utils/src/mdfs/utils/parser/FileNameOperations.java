package mdfs.utils.parser;

import mdfs.utils.ArrayUtils;
import mdfs.utils.Config;
/**
 * A small class that helps with File Name operations
 * @author Rasmus Holm
 *
 */
public class FileNameOperations {
	private static final String fileseparator = System.getProperty("file.separator");
	
	/**
	 * Escapes relative path to a full path
	 * ex. /tmp/example/./test/test1/.. -> /tmp/example/test
	 * @param path unescaped
	 * @return the path escaped
	 */
	public String escapePath(String path){
		String[] splitPath = path.split("/");
		String newPath = "";
		
		for (String string : splitPath) {
			if(string.length() < 1 || string.equals("."));
			else if(string.equals("..")){
				int i = newPath.lastIndexOf("/");
				if(i == 0){
					newPath = "/";
				}else{
					newPath = newPath.substring(0, newPath.lastIndexOf("/"));
				}
			}
			else 
				newPath = newPath + "/" + string;
		}
	
		return newPath;
	}

    public String parentPath(String path){
        path = path.trim();
        String dirs[] = path.split("/");
        path = "";
        for(int i = 0; i < dirs.length-1; i++){
            if(dirs[i].length() > 0)
                path += "/" + dirs[i];
        }
        return path;
    }
	
	/**
	 * Escapes relative and abstract path to a full path
	 * ex. ~/example/test -> /user/example/test
	 * ex. /user$ example/test/../test1/. -> /user/example/test1
	 * 
	 * @param path unescaped path
	 * @param currentPath the path that are currently located in
	 * @param user the user in realltiv to the path
	 * @return the path escaped
	 */
	public String escapePath(String path, String currentPath, String user){
		String newPath;
		if(path.charAt(0) == '/'){
			newPath = path;
		}else if(path.charAt(0) == '~'){
			newPath = path.replaceFirst("~", "/" + user);
		}
		else{
			newPath = currentPath + "/" + path;
		}
		
		return escapePath(newPath);
	}
	
	/**
	 * Creates a uniq logical name
	 * ex. 12-43-12-76-43-85-23-85-34-72-12448971241
	 * @return a uniq name
	 */
	public String createUniqName(){
		String name = "";
		for(int i = 0; i < 10; i++){
			name = name + Integer.toString((int)(Math.random()*100)) + "-";
		}
		name = name + Long.toString(System.currentTimeMillis());
		return name;
	}	
	
	
	/*
	 * Takes a MDFS file name and translates it in to a location at the local file system
	 * It returns an array of string which represents the path 
	 */
	private String[] basicTranslation(String fileName){
		String partialPath = fileName.replace("-", fileseparator);
		partialPath = fileseparator + partialPath;
		String[] splitPath = partialPath.split(fileseparator);		
		String[] storageLocation = Config.getStringArray("DataNode.storage.path");
		
		int index = splitPath[splitPath.length-1].hashCode();
		
		int location = Math.abs((int)(index % storageLocation.length));

		return (String[]) ArrayUtils.addAll( storageLocation[location].split(fileseparator), splitPath );
	}
	
	/**
	 * Returns the full path to a MDFS file form its abstract file name
	 * @param fileName the MDFS file name, ex. 12-34-15-63-23-55-55-34-3423513561
	 * @return full path to the file on the local file system
	 */
	public String translateFileNameToFullPath(String fileName){
		String[] fullPath = basicTranslation(fileName);
		String path = fileseparator + ArrayUtils.implode(fullPath, fileseparator);
		path = path.replace( (fileseparator+fileseparator), fileseparator);
		return path;
	}
	/**
	 * Returns the full path to the parent dir of a MDFS file on the local filesystem
	 * @param fileName the MDFS file name
	 * @return full path to the parent dir of the MDFS file on the local filesystem
	 */
	public String translateFileNameToDirPath(String fileName){
		String[] fullPath = basicTranslation(fileName);
		String path = fileseparator + ArrayUtils.implode(fullPath, fileseparator, fullPath.length-1) + fileseparator;
		path = path.replace( (fileseparator+fileseparator), fileseparator);
		return path;
	}
	
}

package mdfs.utils;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A implementation of a file structure that enable one to store generic nodes with a String key
 * The String key should be the path the node is stored at. ex. c:\example\file.txt or /tmp/example/file.txt
 * @author Rasmus Holm
 *
 * @param <E> a generic element associated with a file path
 */
public class FSTree<E> {
	
	private FSTreeNode<E> start = new FSTreeNode<E>();
	
	private String fileSeperator;
	private String startIndex;
	
	
	/**
	 * Creates the file tree with startIndex and fileSeperator
	 * ex. 	c:\example\file.txt 
	 * 		makes	startIndex = "c:\" 
	 * 				fileSeperator = "\"
	 * 
	 * @param startIndex the staring index, ex. "c:\"
	 * @param fileSeperator the file seprator, ex. "\"
	 * @param defaultElement is the element that would represent the start index, ex. "c:\"
	 */
	public FSTree(String startIndex, String fileSeperator, E defaultElement){
		start.key = startIndex;
		start.content = defaultElement;
		this.startIndex = startIndex;
		this.fileSeperator = fileSeperator;
	}
	/**
	 * Creates the file tree with startIndex and fileSeperator
	 * ex. 	c:\example\file.txt 
	 * 		makes	startIndex = "c:\" 
	 * 				fileSeperator = "\"
	 * @param startIndex the staring index, ex. "c:\"
	 * @param fileSeperator the file seprator, ex. "\"
	 */
	public FSTree(String startIndex, String fileSeperator){
		 this.startIndex = startIndex;
		 this.fileSeperator = fileSeperator;
	}
	/**
	 * Creates the file tree with startIndex = "/" and fileSeperator = "/"
	 */
	public FSTree(){
		 this.fileSeperator = "/";
		 this.startIndex = "/";
	}	
	
	/**
	 * 
	 * @return true is tree is empty otherwise false
	 */
	public boolean isEmpty(){
		return start.numberOfChildern() == 0 ? true : false; 
	}

	/*
	 * parses the path into a generic way to compare location
	 */
	private String[] parsePath(String path){
		path = path.replaceFirst(startIndex, "");
		return path.split(fileSeperator);
	}
	
	/*
	 * Navigates to the node that at path indicate
	 */
	private FSTreeNode<E> getNode(String path){
        if(path.equals(startIndex))
            return start;

		String[] exploadedPath = parsePath(path);
		FSTreeNode<E> node = start;
		String name;
		
		for(int i = 1; i <= exploadedPath.length; i++){
			name = startIndex + ArrayUtils.implode(exploadedPath, fileSeperator, i);
			
			node = node.getChild(name);
			if(node == null){
				return null;
			}
		}
		return node;
	}
	
	/**
	 * Puts a element with the key path in the tree
	 * To be able to put a new element in the tree the full path of parents files must exist
	 * @param path the path to element that is to be put into the tree
	 * @param element the element assosiated with the path
	 * @return true if successful, false otherwise
	 */
	public boolean put(String path, E element){		
		
		String[] exploadedPath = parsePath(path);
		
		FSTreeNode<E> node = start;
		String name;
		
		for(int i = 1; i < exploadedPath.length; i++){
			name = startIndex + ArrayUtils.implode(exploadedPath, fileSeperator, i);
			
			node = node.getChild(name);
			if(node == null){
				return false;
			}
		}
		if(node.getChild(path) != null){
			return false;
		}
		FSTreeNode<E> newNode = new FSTreeNode<E>();
		newNode.key = path;
		newNode.content = element;
		
		node.putChild(path, newNode);
		return true;
	}
	/**
	 * 
	 * @param path the path of the element requested
	 * @return the element associated with path, null if it dose not exist
	 */
	public E get(String path){
		
		FSTreeNode<E> node = getNode(path); 
		if(node == null){
			return null;
		}
		return getNode(path).content;		
	}

	/**
	 * 
	 * @param path the childen of this path
	 * @param e the arrau the childeren i put in to
	 * @return an array of children that a path holds, if it has none or it dose not exist it returns null
	 */
	public E[] getChildernArray(String path, E[] e){
		
		FSTreeNode<E> node = getNode(path);
		
		Collection<FSTreeNode<E>> nodes = node.getChildren();
		if(nodes == null){
			return null;
		}
		int i = 0;
		for (FSTreeNode<E> fsTreeNode : nodes) {
			e[i++] = fsTreeNode.content;
		}
		return e;
	}
	
	/**
	 * 
	 * @param path the path to the elemnt whoms childenr are requested
	 * @return the number a childen that a element has, -1 if the node dose not element 
	 */
	public int getNumberOfChildern(String path){
		FSTreeNode<E> node = getNode(path);
		if(node == null){
			return -1;
		}
		return node.numberOfChildern();
	}
		
	/**
	 * Removes a element from the file tree, all children will be removed aswell.
	 * 
	 * @param path to element
	 * @return the removed element
	 */
	public E remove(String path){
		FSTreeNode<E> node = getNode(path);
		if(node == null){
			return null;
		}
		node.parent.removeChild(path);
		node.parent = null;
		return node.content;
	}
	
	/**
	 * Replaces a element with a new one, children are not effected
	 * @param path to the element that are to be replaced
	 * @param element the new element 
	 * @return the old element that was replaced
	 */
	public E replace(String path, E element){
		FSTreeNode<E> node = getNode(path);
		if(node == null){
			return null;
		}
		E e = node.content;
		node.content = element;
		return e;
	}
	
	/**
	 * 
	 * @param path to the element whom children existent are asked
	 * @return true if the number of children > 0
	 */
	public boolean hasChildern(String path) {
		FSTreeNode<E> node = getNode(path);
		if(node == null){
			return false;
		}
		if(node.numberOfChildern() > 0){
			return true;
		}
		return false;
		
	}
	
	/*
	 * 
	 * Internal node in FSTree
	 */
	@SuppressWarnings("hiding")
	private class FSTreeNode<E>{
		private ConcurrentSkipListMap<String, FSTreeNode<E>> children = null;
	
		@SuppressWarnings("unused")
		public String key;
		public FSTreeNode<E> parent;
		public E content;
		
		public int numberOfChildern(){
			if(children == null){
				return 0;
			}else{
				return children.size();
			}
		}
		
		public FSTreeNode<E> getChild(String key){
			if(children == null){
				return null;
			}else{
				return children.get(key);
			}
		}
		
		public Collection<FSTreeNode<E>> getChildren(){
			if(children == null){
				return null;
			}
			return children.values();
		}
		
		public void putChild(String key, FSTreeNode<E> node){
			if(children == null){
				children = new ConcurrentSkipListMap<String, FSTreeNode<E>>();
			}
			node.parent = this;
			children.put(key, node);	
		}
		public void removeChild(String path){
			if(children == null){
				return;
			}
			children.remove(path);
		}
	}
	

}
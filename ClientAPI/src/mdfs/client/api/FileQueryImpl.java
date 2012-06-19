package mdfs.client.api;


import mdfs.utils.Config;
import mdfs.utils.Time;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.*;
import mdfs.utils.parser.FileNameOperations;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
/**
 * This is a basic implementation of FileQuery where the user gains access to a MDFS - file system that is specified in mdfs/config/config.cfg
 * @author Rasmus Holm
 *
 */
public class FileQueryImpl implements FileQuery{

	private String errorMessage = null;

	private String currentPath;

	@SuppressWarnings("unused")
	private MDFSProtocolMetaData[] currenPathData = null;

	private String user;

	private String pass;

	private SocketFactory socketFactory = new SocketFactory();

	private SocketFunctions socketFunctions = new SocketFunctions();

	private FileNameOperations fileNameOperations = new FileNameOperations();
	
	
	/**
	 * This constuctor loggs in and navigats to the home dir of the user ex. "/user"
	 * @param user - the username to be used to access the MDFS file system
	 * @param pass - the password to be used to access the MDFS file system
	 */
	public FileQueryImpl(String user, String pass){
		setUser(user);
		setPass(pass);
		cd("/" + user, null);
		
	}
	
	@Override
	public boolean cd(String path, String flag) {
		
		//Retrives all Meta-Data of the dir trying to move to
		MDFSProtocolMetaData[] pathData = ls(path, null);
		
		//If nothing is returned the dir or file dose not exist, the cd operation fails
		if(pathData == null){
			return false;
		
		//Checks if it is a dir that are tried to move to 
		}else if(pathData[0].getType() == MetadataType.DIR){
			//If all was successful the current path is set as the one requested. And the metametadata is stored
			currentPath = fileNameOperations.escapePath(path, pwd(), user);
			currenPathData = pathData;
			return true;
		//If it was not a dir the cd operation fails
		}else{
			return false;
		}
	}
	
	@Override
	public MDFSProtocolMetaData[] ls(String flag) {
		//Returns the ls for the current path
		return ls(pwd(), flag);
	}

	@Override
	public MDFSProtocolMetaData[] ls(String path, String flag) {
		//builds a absolute path from a relative or absolute path
		path = fileNameOperations.escapePath(path, pwd(), user);
		
		//Creates a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
		//Creates a request to the name node 

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.READ);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        request.setMetadata(metadata);


		//Sends request to name node

		socketFunctions.sendText(nameNodeSocket, request.toString());
		MDFSProtocolHeader response;
		MDFSProtocolMetaData[] toBeReturned = new MDFSProtocolMetaData[1];

        //Retrieves the response from the name node

        try {
            String s =  socketFunctions.receiveText(nameNodeSocket);
            response = new MDFSProtocolHeader(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


        try {
        nameNodeSocket.close();
        } catch (IOException e) {}

        //If the response contains an Error the ls operation failed and null is returned
        if(response.getError() != null){

                errorMessage = response.getError();

            return null;
        }

        //File contains the metadata of the ls request to the name node
        MDFSProtocolMetaData file = response.getMetadata();


        //If file requested has children they are stored in the array as well
        if(file.getChildrenSize() != 0){
            int size = file.getChildrenSize();

            MDFSProtocolMetaData[] children = new MDFSProtocolMetaData[size];
            children = file.getChildernArray(children);

            toBeReturned = new MDFSProtocolMetaData[size+1];
            System.arraycopy(children, 0, toBeReturned, 1, size);

        }

        //Adds the requested file to the array
        file.setChildren(null);
        toBeReturned[0] = file;


		try {
			nameNodeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return toBeReturned;
	}

	
	@Override
	public String pwd() {
		//returns what is stored as the currentPath since no request has to be made
		return currentPath;
	}

	
	@Override
	public boolean rm(String path, String flag) {
		boolean result = true;
		
		//builds a absolute path from a relative or absolute path
		path = fileNameOperations.escapePath(path, pwd(), user);
		

        //Handles the flag 'r' which makes the rm command recursive
        if(flag != null && flag.contains("r")){

            //Retrieves the children of the path that are to be removed
            MDFSProtocolMetaData[] files = ls(path, flag);

            if(files == null){
                errorMessage = " No such file or directory";
                return false;
            }

            //Recursively removes all children before the parent are removed
            for (int i = 1; i < files.length; i++) {
                MetadataType type;
                type = files[i].getType();
                //If a child is a file we simply remove it
                if(type == MetadataType.FILE){
                    if(!rm(files[i].getPath(), null)){
                        result = false;
                    }
                //If a child is a dir we first remove all its children
                }else if(type == MetadataType.DIR){
                    if(!rm(files[i].getPath(), flag)){
                        result = false;
                    }
                }
            }
        }

		
		//Creats a request to the name node to remove the path, and all children should now have been removed

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.REMOVE);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(path);
        request.setMetadata(metadata);


		//Creats a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
		if(nameNodeSocket == null ){
			errorMessage = "Could not connect to NameNode";
			return false;
		} 
		
		//Send the the request to the name node
		socketFunctions.sendText(nameNodeSocket, request.toString());
        MDFSProtocolHeader response;
		try {
			//Retrivse the resonse from the name node, the name node itself handels the
			//removal of the raw data from the data-nodas
            response = new MDFSProtocolHeader(socketFunctions.receiveText(nameNodeSocket));

        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = "Response from NameNode was invalid.";
            return false;
        }

        try {
            nameNodeSocket.close();
        } catch (IOException e) {}

        //Checks if there is an error in the removal of the file
        if(response.getError() != null){
            errorMessage = response.getError();
            return false;
        }

        //Checks if the removal was successfull and if so returns the result
        MDFSProtocolInfo info = response.getInfo();
        if(info.getRemoved() == EventStatus.SUCCESSFUL){
            return result;
        }
			

		//Returns false if the removal was not successful
		return false;
	}

	//Not implemented yet.
	@Override
	public InputStream get(String sourcePath, String flag) {
		return null;
	}

	/*
	 * TODO implement recursive get.
	 * (non-Javadoc)
	 * @see mdfs.client.api.FileQuery#get(java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public boolean get(String sourcePath, File targetFile, String flag) {
		//Retrives information about the requested file from the name node
		MDFSProtocolMetaData[] file = ls(sourcePath, null);
		
		//Checks if it is a dir with children was requested and returns false if so
		//Recursiv support is not yet implemented
		if(file.length < 1) 
			return false;
		
		//If the request was for exactly one file it is fetched from one of the data nodes
		else if(file.length == 1){
			
			//Creates a request for the wanted file from a data node
            //headerCreator.getFromDataNode(file[0]);
			MDFSProtocolHeader request = new MDFSProtocolHeader();
            request.setStage(Stage.REQUEST);
            request.setType(Type.FILE);
            request.setMode(Mode.READ);
            request.setMetadata(file[0]);

			Socket dataNodeSocket;
			try {
				//Generates a array of all the data nodes that currently are storing the requested file 
				String hosts[] = new String[file[0].getLocation().getHostsSize()];
                hosts = file[0].getLocation().getHostsArray(hosts);

				//Creates a socket to one of the data nodes, the first one to respond 
				dataNodeSocket = socketFactory.createSocket(hosts);
				
				//if no socket could be created 
				if(dataNodeSocket == null) throw new IOException();
		

				//Sends request
				socketFunctions.sendText(dataNodeSocket, request.toString());
				
				//Retrives the response as a MDFSProtocolHeader
				MDFSProtocolHeader response = new MDFSProtocolHeader(socketFunctions.receiveText(dataNodeSocket));
				
				//Exites and returns false if it contains an error
				if(response.getError() != null){
					errorMessage = response.getError();
					return false;
				}
					
				//Recives the file from the datanode and stores on the local FS
				socketFunctions.receiveFile(dataNodeSocket, targetFile);
				try {
					dataNodeSocket.close();
				} catch (IOException e) {}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				errorMessage = "Could not create socket to DataNode";
				return false;
			}
			
			
		}else{
			
		}
		
		return true;
	}

	@Override
	public boolean put(File sourceFile, String flag) {
		return put(sourceFile, pwd() + "/" + sourceFile.getName(), flag);
	}

	@Override
	public boolean put(File sourceFile, String targetPath, String flag) {
		//builds a absolute path from a relative or absolute path
		targetPath = fileNameOperations.escapePath(targetPath, pwd(), user);
		
			
		//if it is a dir that is to be put to the FS
		if(sourceFile.isDirectory()){
			//Adding dir to file system
			mkdir(targetPath);
			boolean noError = true;
			
			//Recursively adding all files and dirs to MDFS
			if(flag.contains("r")){
				//Getting all children of the file
				File[] files = sourceFile.listFiles();
				for (File file : files) {
					//Recursively adding all children to MDFS
					if(!put(file, targetPath + "/" + file.getName(), flag)){
						errorMessage = "Error writing " + file.getAbsolutePath() + " to MDFS";
						//System.out.println(errorMessage);
						noError = false;
					}
				}				
			}
			return noError;
			
		}else{	
			
			
			
			//Creating reqest to name node to put file in MDFS
            MDFSProtocolHeader request = new MDFSProtocolHeader();
            MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();

            request.setStage(Stage.REQUEST);
            request.setType(Type.METADATA);
            request.setMode(Mode.WRITE);

            request.setUser(this.user);
            request.setPass(this.pass);

            metadata.setPath(targetPath);
            metadata.setType(MetadataType.FILE);
            metadata.setSize(sourceFile.length());
            metadata.setPermission(640);
            metadata.setOwner(user);
            metadata.setGroup(user);
            metadata.setCreated(Time.getTimeStamp(sourceFile.lastModified()));
            metadata.setLastEdited(Time.getTimeStamp(sourceFile.lastModified()));
            metadata.setLastTouched(Time.getTimeStamp());

            request.setMetadata(metadata);


			//Creates a socket to the name node
			Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
																Config.getInt("NameNode.port") );
			
			//Sends request to name node
			socketFunctions.sendText(nameNodeSocket, request.toString());
			
			//Retrives response from name node
            MDFSProtocolHeader response = null;
            try {
                String s = socketFunctions.receiveText(nameNodeSocket);

                response = new MDFSProtocolHeader(s);

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            try {
				nameNodeSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}


			
			Socket dataNodeSocket = null;

            String[] hosts = null;

            //Returns false if there was a error from the name node
            if(response.getError() != null){
                errorMessage = response.getError();
                return false;

            //If file already exists, it connects to DataNode for overwriting
            }else if( response.getMetadata().getLocation().getHostsSize() > 0){
                hosts = new String[response.getMetadata().getLocation().getHostsSize()];
                hosts = response.getMetadata().getLocation().getHostsArray(hosts);


            //New file and there for connects to a "random" available Data Node
            }else{
                hosts = new String[response.getInfo().getDatanodesSize()];
                hosts = response.getInfo().getDatanodesArray(hosts);

            }

            //Creats a socket to the first responent of the host list
            dataNodeSocket = socketFactory.createSocket(hosts);

            //Returns false if a socket to the datanodes could not be created
            if(dataNodeSocket == null){
                return false;
            }
				
				

			
			//Creates a request to the data nodes
            MDFSProtocolHeader request2 = new MDFSProtocolHeader();
            request2.setStage(Stage.REQUEST);
            request2.setType(Type.FILE);
            request2.setMode(Mode.WRITE);
            request2.setMetadata(response.getMetadata());
            request2.setInfo(response.getInfo());


			//Sends request and file to data node.
			socketFunctions.sendText(dataNodeSocket, request2.toString());
			socketFunctions.sendFile(dataNodeSocket, sourceFile);
			
			//Receives response from data node
            try {
                MDFSProtocolHeader response2 = new MDFSProtocolHeader(socketFunctions.receiveText(dataNodeSocket));
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            try {
				dataNodeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}			
		
	}



	@Override
	public boolean mv(String sourcePath, String targetPath, String flag) {
		sourcePath = fileNameOperations.escapePath(sourcePath);
		targetPath = fileNameOperations.escapePath(targetPath);
		//TODO Implement mv
		return false;
	}

	@Override
	public boolean chmod(String targetPath, short octalPermission, String flag) {
		targetPath = fileNameOperations.escapePath(targetPath);
		// TODO Implement chmod
		return false;
	}

	@Override
	public boolean chown(String targetPath, String owner, String group, String flag) {
		targetPath = fileNameOperations.escapePath(targetPath);
		// TODO Implement chown
		return false;
	}


	@Override
	public void setUser(String user) {
		this.user = user;
	}


	@Override
	public void setPass(String pass) {
		this.pass = pass;
	}

	@Override
	public boolean mkdir(String targetPath) {
		//builds a absolute path from a relative or absolute path
		targetPath = fileNameOperations.escapePath(targetPath, currentPath, user);
		
		//Creates a socket to the name node
		Socket nameNodeSocket = socketFactory.createSocket( Config.getString("NameNode.address"), 
															Config.getInt("NameNode.port") );
					
		//Creates a request to the name node
        MDFSProtocolHeader request = new MDFSProtocolHeader();
        MDFSProtocolMetaData metadata = new MDFSProtocolMetaData();

        request.setStage(Stage.REQUEST);
        request.setType(Type.METADATA);
        request.setMode(Mode.WRITE);
        request.setUser(this.user);
        request.setPass(this.pass);

        metadata.setPath(targetPath);
        metadata.setType(MetadataType.DIR);
        metadata.setSize(0);
        metadata.setPermission(640);
        metadata.setOwner(user);
        metadata.setGroup(user);

        String timestamp = Time.getTimeStamp();
        metadata.setCreated(timestamp);
        metadata.setLastEdited(timestamp);
        metadata.setLastTouched(timestamp);

        request.setMetadata(metadata);

        //Sends request to name node
		socketFunctions.sendText(nameNodeSocket, request.toString());
		
		//Receives the response from the name node
        MDFSProtocolHeader response = null;
        try {
            response = new MDFSProtocolHeader(socketFunctions.receiveText(nameNodeSocket));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        try {
			nameNodeSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		


        if(response.getError() != null){
            errorMessage = getError();
            return false;
        }else{
            return true;
        }

	}

	@Override
	public String getError() {
		return errorMessage;
	}

}

package mdfs.namenode.parser;

import mdfs.namenode.repositories.DataNodeInfoRepository;
import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses and execute updates regarding Info for Files in the different data repository that 
 * exists on the name node
 * @author Rasmus Holm
 *
 */
public class ParserInfoFile implements Parser {
	private String mode;
	private Session session;
	public ParserInfoFile(String mode){
		this.mode = mode;
	}
	
	@Override
	public boolean parse(Session session) {
		this.session = session;
		//When the Mode = Write
		if(mode.equals("Write")){
			return parseWrite();
		}
		return false;
	}
	
	/*
	 * This one updates a MetaDataRepositoryNode with information about on what DataNodes the file is stored.
	 */
	private boolean parseWrite(){
		try {
			//Gets information from the request in to regard on what datanode the file has been stored
			JSONObject request = session.getRequest();
			JSONObject info = request.getJSONObject("Info");
			String filePath = info.getString("path");
			String host = info.getString("host");
			String port = info.getString("port");
			boolean overwrite = info.getBoolean("overwrite");
			
			
			Verbose.print("parseWrite -> File: " + filePath + " Host: " + host + " Port: " + port, this, Config.getInt("verbose")-2);
			//Fetches the MetaDataRepositoryNode that the update of information this is regarding
			MetaDataRepositoryNode node = MetaDataRepository.getInstance().get(filePath);
			if(node == null){
				return false;
			}
			
			//Finds the datanode that are the raw data now is stored on
			DataNodeInfoRepository dataNodes = DataNodeInfoRepository.getInstance();
			DataNodeInfoRepositoryNode dataNode = dataNodes.get(host, port);
			
			/*
			 * TODO: maby make sure that the correct DataNodes are informed of overwrite.
			 * 		 this is handle by the datanode themself at the moment. 
			 */
			
			//Adds the new location of the raw data to the MetaDataRepositoryNode
			if(!overwrite)
				node.addLocation(dataNode);
			
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

}

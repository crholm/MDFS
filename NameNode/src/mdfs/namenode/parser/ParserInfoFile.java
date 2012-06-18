package mdfs.namenode.parser;

import mdfs.namenode.repositories.DataNodeInfoRepository;
import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Overwrite;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

/**
 * Parses and execute updates regarding Info for Files in the different data repository that 
 * exists on the name node
 * @author Rasmus Holm
 *
 */
public class ParserInfoFile implements Parser {
	private Mode mode;
	private Session session;
	public ParserInfoFile(Mode mode){
		this.mode = mode;
	}
	
	@Override
	public boolean parse(Session session) {
		this.session = session;
		//When the Mode = Write
		if(mode == Mode.WRITE){
			return parseWrite();
		}
		return false;
	}
	
	/*
	 * This one updates a MetaDataRepositoryNode with information about on what DataNodes the file is stored.
	 */
	private boolean parseWrite(){
        //Gets information from the request in to regard on what datanode the file has been stored
        MDFSProtocolHeader request = session.getRequest();
        MDFSProtocolInfo info = request.getInfo();

        String filePath = info.getPath();
        String host = info.getHost();
        String port = info.getPort();
        boolean overwrite = info.getOverwrite() == Overwrite.TRUE ? true : false;


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

	}

}

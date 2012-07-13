package mdfs.namenode.parser;

import mdfs.namenode.repositories.DataNodeInfoRepository;
import mdfs.namenode.repositories.DataNodeInfoRepositoryNode;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolInfo;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Overwrite;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
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
		switch (mode){
            case WRITE:
            case WRITESTREAM:
                return parseWrite();
            default:
                session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, session.getRequest().getType(), mode, "Parser for mode: " + mode + " dose not exist"));
                return false;

        }

	}
	
	/*
	 * This one updates a MetaDataRepositoryNode with information about on what DataNodes the file is stored.
	 */
	private boolean parseWrite(){
        //Gets information from the request in to regard on what datanode the file has been stored
        MDFSProtocolHeader request = session.getRequest();
        MDFSProtocolInfo info = request.getInfo();

        //Checks that all fields needed are present
        if(info == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "JSON object Info dose not exist in header"));
            return false;
        }
        if(info.getPath() == null || info.getHost() == null || info.getPort() == null || info.getOverwrite() == null ){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "path, host, port and/or overwrite dose not exist in Info field"));
            return false;
        }
        if(!info.authToken(info.getName(), mode, Config.getString("Token.key"), Config.getInt("Token.window"))){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "Token failed to authenticate"));
            return false;

        }

        String filePath = info.getPath();
        String host = info.getHost();
        String port = info.getPort();
        boolean overwrite = info.getOverwrite() == Overwrite.TRUE ? true : false;


        Verbose.print("parseWrite -> File: " + filePath + " Host: " + host + " Port: " + port, this, Config.getInt("verbose")-2);
        //Fetches the MetaDataRepositoryNode that the update of information this is regarding
        MetaDataRepositoryNode node = MetaDataRepository.getInstance().get(filePath);


        //Checks that the node exist in FS
        if(node == null){
            session.setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, Type.FILE, mode, "File dose not exist in file system"));
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

        if(info.getLength() != -1){
            node.setSize(info.getLength());
            MySQLUpdater.getInstance().update(node);
        }

        return true;

	}

}

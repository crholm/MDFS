package mdfs.datanode.parser;

import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

import java.io.File;
import java.io.InputStream;

/**
 * An implementation of the interface Session that are related to the operations of the Datanode
 * @author Rasmus Holm
 *
 */
public class SessionImpl implements Session{
	private MDFSProtocolHeader request = null;
	private MDFSProtocolHeader response = null;
	private InputStream inputStreamFromRequest = null;
	private File fileForResponse = null;
	private Parser parser;
	
	@SuppressWarnings("unused")
	private String status = "none";
	private ParserFactory parserFactory = new ParserFactory();
	
	@Override
	public void setResponse(MDFSProtocolHeader response) {
		this.response = response;
		
	}

	@Override
	public MDFSProtocolHeader getRequest() {
		return request;
	}

	@Override
	public boolean setRequest(MDFSProtocolHeader request) {
		if(request == null){
			return false;
		}
		this.request = request;
		return true;
	}

	@Override
	public void setStatus(String status) {
		Verbose.print("Session Status change: " + status, this, Config.getInt("verbose"));
		this.status = status;
		
	}

	@Override
	public MDFSProtocolHeader getResponse() {
		return response;
	}

	@Override
	public InputStream getInputStreamFromRequest() {
		return inputStreamFromRequest;
	}

	@Override
	public void setInputStreamFromRequest(InputStream inputStream) {
		inputStreamFromRequest = inputStream;
	}

	@Override
	public File getFileForResponse() {
		return fileForResponse;
	}

	@Override
	public void setFileForResponse(File file) {
		fileForResponse = file;
	}

	@Override
	public boolean parseRequest() {
		//Checks so that the request contains the fields To, From, Stage, Type and Mode

		if(request != null && request.getStage() != null && request.getMode() != null && request.getType() != null){
			
			//Checks so that the field To contains the current data nods address

				
				//Creates a parser from given information 
				parser = parserFactory.getParser(request.getStage(), request.getType(), request.getMode());
				
				//Parses the request and wraps the session in it.
				if(!parser.parse(this)){
					setStatus("error - Parsing failed");
					return false;
				}

		}else{
			setStatus("error - Requets, Stage, Mode or Type is missing");
			return false;
		}
		
		return true;
	}

}

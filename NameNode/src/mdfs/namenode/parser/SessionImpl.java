package mdfs.namenode.parser;

import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

import java.io.File;
import java.io.InputStream;


/**
 * Session is the object that contains request and respose for MDFS comunication protcol.
 * It contains all nessesary information to handel a request and dose so by parsing and executing
 * @author Rasmus Holm
 *
 */
public class SessionImpl implements Session{
	private MDFSProtocolHeader request = null;
	private MDFSProtocolHeader response = null;
	private Parser parser;
	
	@SuppressWarnings("unused")
	private String status = "none";
	private ParserFactory parserFactory = new ParserFactory();
	
	/**
	 * Sets the JSON response that the session holds.
     * @param response
     */
	public void setResponse(MDFSProtocolHeader response){
		this.response = response; 
	}
	
	/**
	 * Enables other Objects to read and edit the request as a JSONObject
	 * @return the request in present form as a JSONObject 
	 */
	public MDFSProtocolHeader getRequest(){
		return request;
	}
	
	/**
	 * Adds a JSON string as request for the session
	 *
     * @param request@return false if parsing fails or if a request is already added, true if sucessfull in parsing request to JSONObject
	 */
	public boolean setRequest(MDFSProtocolHeader request){
		this.request = request;

		return true;
	}
	

	/**
	 * Parses a added JSON Request via a object Parser
	 * @return false - if reqest is not set, or failing to parse request accordingly to MDFS Communication Protocol
	 */
	public boolean parseRequest() {
		//Checks so that the request contains the fields To, From, Stage, Type and Mode
		if(request.getStage() != null && request.getMode() != null && request.getType() != null){
			

            //Creates a parser from given information
            parser = parserFactory.getParser(request.getStage(), request.getType(), request.getMode());

            //Parses the request and wraps the session in it.
            if(parser == null){
                setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, request.getType(), request.getMode(), " Parser for arguments did not exist"));
                setStatus("error - Parser for arguments did not exist");
                return false;
            }

            if(!parser.parse(this)){
                if(getResponse() == null)
                    setResponse(MDFSProtocolHeader.createErrorHeader(request.getStage(), request.getType(), request.getMode(), "Parsing failed"));
                setStatus("error - Parsing failed");
                return false;
            }

        }else{
            setResponse(MDFSProtocolHeader.createErrorHeader(Stage.RESPONSE, null, null, "Stage, Type and/or Mode was not provided"));
            setStatus("error - Stage, Type and/or Mode was not provided");
            return false;
        }

		
		return true;
	}
	
	/**
	 * Sets and edits the satus for the session
	 * @param status
	 */
	public void setStatus(String status){
		Verbose.print("Session Status change: " + status, this, Config.getInt("verbose")-5);
		this.status = status;
	}
	
	/**
	 * 
	 * @return the response to request as a JSON String, null if request has not been parsed.
	 */
	public MDFSProtocolHeader getResponse(){
		return response;
	}
	
	@Override
	public InputStream getInputStreamFromRequest() {
		return null;
	}
	@Override
	public void setInputStreamFromRequest(InputStream inputStream) {

	}
	@Override
	public File getFileForResponse() {
		return null;
	}
	@Override
	public void setFileForResponse(File file) {

	}
	
	
	
	
}

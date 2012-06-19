package mdfs.utils.parser;

import mdfs.utils.io.protocol.MDFSProtocolHeader;
import org.json.JSONException;

import java.io.File;
import java.io.InputStream;

/**
 * Session is the object that contains request and respose for MDFS comunication protcol.
 * It contains all nessesary information to handel a request and dose so by parsing and executing
 * @author Rasmus Holm
 *
 */
public interface Session {
	/**
	 * Parses a added JSON Request via a object Parser
	 * @return false - if reqest is not set, or failing to parse request accordingly to MDFS Communication Protocol
	 * @throws JSONException
	 */
	public boolean parseRequest();
	
	/**
	 * Sets the JSON response that the session holds.
     * @param response the response in JSON format
     */
	public void setResponse(MDFSProtocolHeader response);
	/**
	 * Enables other Objects to read and edit the request as a JSONObject
	 * @return the request in present form as a JSONObject 
	 */
	public MDFSProtocolHeader getRequest();
	
	/**
	 * Adds a JSON string as request for the session
	 *
     * @param request@return false if parsing fails or if a request is already added, true if successful in parsing request to JSONObject
	 */
	public boolean setRequest(MDFSProtocolHeader request);

	/**
	 * 
	 * @return the inputstream on which a binary file can be read
	 */
	public InputStream getInputStreamFromRequest();
	public void setInputStreamFromRequest(InputStream inputStream);
	public File getFileForResponse();
	public void setFileForResponse(File file);
	
	/**
	 * 
	 * @return the response to request as a JSON String, null if request has not been parsed.
	 */
	public MDFSProtocolHeader getResponse();

	/**
	 * Sets and edits the satus for the session
	 * @param status
	 */
	public void setStatus(String status);
}

package mdfs.namenode.parser;

import java.io.File;
import java.io.InputStream;

import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Session is the object that contains request and respose for MDFS comunication protcol.
 * It contains all nessesary information to handel a request and dose so by parsing and executing
 * @author Rasmus Holm
 *
 */
public class SessionImpl implements Session{
	private JSONObject request = null;
	private JSONObject response = null;
	private Parser parser;
	
	@SuppressWarnings("unused")
	private String status = "none";
	private ParserFactory parserFactory = new ParserFactory();
	
	/**
	 * Sets the JSON response that the session holds.
	 * @param response
	 */
	public void setJsonResponse(JSONObject response){
		this.response = response; 
	}
	
	/**
	 * Enables other Objects to read and edit the request as a JSONObject
	 * @return the request in present form as a JSONObject 
	 */
	public JSONObject getJsonRequest(){
		return request;
	}
	
	/**
	 * Adds a JSON string as request for the session
	 * @param jsonString - A string that is parsed into a JSONObject
	 * @return false if parsing fails or if a request is already added, true if sucessfull in parsing request to JSONObject
	 */
	public boolean addJsonRequest(String jsonString){
		if(request != null){
			return false;
		}
		try {
			request = new JSONObject(jsonString);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}
	

	/**
	 * Parses a added JSON Request via a object Parser
	 * @return false - if reqest is not set, or failing to parse request accordingly to MDFS Communication Protocol
	 * @throws JSONException
	 */
	public boolean parseRequest() throws JSONException { 
		//Checks so that the request contains the fields To, From, Stage, Type and Mode
		if(request != null && request.has("To") && request.has("From") && request.has("Stage") && request.has("Mode") && request.has("Type")){
			
			//Checks so that the field To contains the current name nods address
			if( request.getString("To").equals(Config.getString("address"))){
				
				//Creates a parser from given information 
				parser = parserFactory.getParser(request.getString("Stage"), request.getString("Type"), request.getString("Mode"));
				
				//Parses the request and wraps the session in it.
				if(!parser.parse(this)){
					setStatus("error");
					return false;
				}
			}else{
				setStatus("error");
				return false;
			}
		}else{
			setStatus("error");
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
	public String getResponse(){
		if(response == null){
			return null;
		}
		return response.toString();
	}
	
	@Override
	public InputStream getInputStreamFromRequest() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setInputStreamFromRequest(InputStream inputStream) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public File getFileForResponse() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setFileForResponse(File file) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}

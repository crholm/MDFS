package mdfs.datanode.parser;
import java.io.File;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;


import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.parser.Parser;
import mdfs.utils.parser.Session;

/**
 * An implementation of the interface Session that are related to the operations of the Datanode
 * @author Rasmus Holm
 *
 */
public class SessionImpl implements Session{
	private JSONObject request = null;
	private JSONObject response = null;
	private InputStream inputStreamFromRequest = null;
	private File fileForResponse = null;
	private Parser parser;
	
	@SuppressWarnings("unused")
	private String status = "none";
	private ParserFactory parserFactory = new ParserFactory();
	
	@Override
	public void setJsonResponse(JSONObject response) {
		this.response = response;
		
	}

	@Override
	public JSONObject getJsonRequest() {
		return request;
	}

	@Override
	public boolean addJsonRequest(String jsonString) {
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

	@Override
	public void setStatus(String status) {
		Verbose.print("Session Status change: " + status, this, Config.getInt("verbose"));
		this.status = status;
		
	}

	@Override
	public String getResponse() {
		if(response == null){
			return null;
		}
		return response.toString();
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
	public boolean parseRequest() throws JSONException {
		//Checks so that the request contains the fields To, From, Stage, Type and Mode
		if(request != null && request.has("To") && request.has("From") && request.has("Stage") && request.has("Mode") && request.has("Type")){
			
			//Checks so that the field To contains the current data nods address
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

}

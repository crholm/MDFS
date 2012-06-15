package mdfs.namenode.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import mdfs.namenode.parser.SessionImpl;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.parser.Session;

import org.json.JSONException;



/**
 * ConnectionShepperd handels a connection from start to end in regard to the MDFS comunication protocol
 * Nothing is done until it is started as a thread
 * @author Rasmus Holm
 *
 */
public class ConnectionSheppard implements Runnable{
	String request = "";
	Socket connection;
	InputStream in;
	OutputStream out;
	String tmpFileName;
	
	//The session is wrapping both the request, the parser and its response
	Session session = new SessionImpl();
	SocketFunctions socketFunctions = new SocketFunctions();
	
	/**
	 * Creats a instant of a ConnectionSheppard that handles a connection from start to end.
	 * Nothing is done until it is started as a thread.
	 * @param connection is the socket which it is handling
	 */
	public ConnectionSheppard(Socket connection){
		this.connection = connection;
		try {
			this.in = connection.getInputStream();
			this.out = connection.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Receives the request via a socket
	 * Create a session and parse it for a response
	 */
	private void getRequest() throws IOException{
		
		this.request = socketFunctions.receiveText(connection);
	
		try {
			
			Verbose.print("Parsing request", this, Config.getInt("verbose")-6);
			
			session.addJsonRequest(this.request);
			session.parseRequest();
			
			Verbose.print("Parsing done.", this, Config.getInt("verbose")-6);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} 		
	}
	

	private void sendResponse(){
		
		if(session.getResponse() != null)
			socketFunctions.sendText(connection, session.getResponse());
	}
	
	
	@Override
	public void run(){
		try {
			getRequest();
			sendResponse();
			in.close();
			out.close();
			connection.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}

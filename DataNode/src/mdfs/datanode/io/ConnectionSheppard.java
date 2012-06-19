package mdfs.datanode.io;

import mdfs.datanode.parser.SessionImpl;
import mdfs.utils.Config;
import mdfs.utils.Verbose;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.parser.Session;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



/**
 * ConnectionShepperd handels a connection from start to end in regard to the MDFS comunication protocol
 * Nothing is done until it is started as a thread
 * @author Rasmus Holm
 *
 */
public class ConnectionSheppard implements Runnable{
	String request;
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
	 * Receives the request [and file] via a socket
	 * Create a session and parse it for a response
	 */
	private void getRequest() throws IOException{
		
		this.request = socketFunctions.receiveText(in);
	
		try {
			Verbose.print("Parsing request...", this, Config.getInt("verbose")-2);

           System.out.print("String request ->" + this.request);

            MDFSProtocolHeader h = new MDFSProtocolHeader(this.request);

            System.out.println("Header request ->" + h);

           	session.setRequest(h);
			session.setInputStreamFromRequest(in);
			
			session.parseRequest();
			
			Verbose.print("Parsing done.", this, Config.getInt("verbose")-2);
		
			
		} catch (JSONException e) {
			Verbose.print("Error parsing request", this, Config.getInt("verbose")+1);
			e.printStackTrace();
		} 
		
	}
	
	/*
	 * Sends both text header and files to the recipient if any.
	 */
	private void sendResponse(){
		
		//Sends response if any
		if(session.getResponse() != null){
			Verbose.print("Sending response...", this, Config.getInt("verbose")-2);
			socketFunctions.sendText(connection, session.getResponse().toString());
			
			//Sends file if any
			if(session.getFileForResponse() != null)
				socketFunctions.sendFile(connection, session.getFileForResponse());
			
			Verbose.print("Response sent.", this, Config.getInt("verbose")-2);
		}
	}

	@Override
	public void run(){
		try {
			//The cycle which all communication takes
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

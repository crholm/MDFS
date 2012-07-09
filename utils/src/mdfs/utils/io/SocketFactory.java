package mdfs.utils.io;

import mdfs.utils.Config;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * A factory that creates Socket for use in MDFS 
 * @author Rasmus Holm
 *
 */
public class SocketFactory {

	/**
	 * Creates and returns a socket with given parametes
	 * @param host - the domain name for socket recipant, ex. node.example.com
	 * @param port the port of the host
	 * @return Socket if successful, otherwise null
	 */
	public Socket createSocket(String host, int port){
		if(Config.getInt("UseEncryption") == 1){

            try {
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port+10);
                return socket;

            } catch (IOException e){
                e.printStackTrace();
                return null;
            }


        }else if(Config.getInt("UseEncryption") == 2){

            try {
                return new ESocket(host, port+20);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }else{

            try {
                return new Socket(host, port);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
	}
	
	/**
	 * Creates a socket to the first host that responds to request
	 * @param hosts is an array of hosts, each string represents a host and are supose to be in the format "address:port", ex. 127.0.0.1:80
	 * @return a socket to first responding host
	 */
	public Socket createSocket(String[] hosts) {
        if(hosts == null){
            return null;
        }
		for (String string : hosts) {
			String[] host = string.split(":");
			
			Socket socket = createSocket(host[0], Integer.valueOf(host[1]));
			if(socket != null)
				return socket;
		}
		return null;
	}
	
}

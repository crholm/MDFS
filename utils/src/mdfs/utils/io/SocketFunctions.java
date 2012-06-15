package mdfs.utils.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.net.Socket;
import java.nio.charset.Charset;

import mdfs.utils.ArrayUtils;


/**
 * Handels writing and reading between sockets.
 * SocketFunctions is mapped to a low level protocol for transfering text and binary files
 * @author Rasmus Holm
 *
 */
public class SocketFunctions {
	
	private static Charset charset = Charset.forName("UTF-8");
	private static int chunkSize = 2048;
	
	/**
	 * Sends text via Socket and are supposed to be read by receiveText() to be read correctly
	 * @param socket - that text will be sent on
	 * @param text the text to be sent
	 * @return true if transmission was successful
	 */
	public boolean sendText(Socket socket, String text){
		try {
			return sendText(socket.getOutputStream(), text);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Sends text via OutputStream and are supposed to be read by receiveText() to be read correctly
	 * @param outputStream that the text will be sent on
	 * @param text - the text to be sent
	 * @return true if transmission was successful
	 */
	public boolean sendText(OutputStream outputStream, String text){
		
		//Converts text to a byte array to before it is sent
		byte[] b = (text+"\n").getBytes(charset);
		
		//turns the length of b into a byte array to be sent as a overhead. How many bytes to receive 
		byte[] a = ArrayUtils.intToByteArray(b.length);
		
		
		
		//Writes the bytes to the outputstream
		try {
			outputStream.write(a);
			outputStream.write(b);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;		
	}
	
	/**
	 * Receives the text from Socket that are sent from sendText()
	 * @param socket that the text is to be received on
	 * @return text received, if fails null
	 */
	public String receiveText(Socket socket){
		try {
			return receiveText(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Receives the text from InputStream that are sent from sendText()
	 * @param inputStream that the text is sent on
	 * @return text received, if fails null
	 */
	public String receiveText(InputStream inputStream){
		try {
			
			//Reads the first 4 byte as a int, which is used to know how many bytes of text is expected to receive 
			byte[] b = new byte[4];
			inputStream.read(b, 0, 4);
			int length = ArrayUtils.byteArrayToInt(b);
			
			
			//Receiving text in to response
			b = new byte[length];
			for(int i = 0; i < length/chunkSize; i++){				
				inputStream.read(b, i*chunkSize, chunkSize);
			}
			inputStream.read(b, chunkSize*((int)(length/chunkSize)), length%chunkSize);
			
			return new String(b, charset);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			
		return null;
	}
	
	/**
	 * Send a file in binary format via a socket and are suppose to be received by receiveFile()
	 * @param out - the socket on which the file are to be sent on
	 * @param sourceFile - The file that are to be sent.
	 * @return true if successful
	 */
	public boolean sendFile(Socket out, File sourceFile) {
		try {
			return sendFile(out.getOutputStream(), sourceFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Send a file in binary format as a OutputStream and are suppose to be received by receiveFile()
	 * @param out - the Outputstream on which the file are sent
	 * @param sourceFile - The file that are to be sent.
	 * @return true if successful
	 */
	public boolean sendFile(OutputStream out, File sourceFile) {
		try {
			
			//Calculating the length of the file and turning it in to a 8 byte overhead;
			long length = sourceFile.length();
			byte[] b = ArrayUtils.longToByteArray(length);
			out.write(b);
			
			
			InputStream in = new FileInputStream(sourceFile);
			byte buf[]=new byte[chunkSize];
		
			//Writing file
			int len;
			while((len=in.read(buf, 0, chunkSize))>0)
				out.write(buf,0,len);
			in.close();
			out.flush();
			
		
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Receives a binary file sent from sendFile() via a Socket and writes it to filesystem 
	 * @param in - Socket on which the file are sent and received
	 * @param targetFile - the file on local or remote filesystem that the file are written to
	 * @return true if successful
	 */
	public boolean receiveFile(Socket in, File targetFile){
		try {
			return receiveFile(in.getInputStream(), targetFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		}
	}
	/**
	 * Receives a binary file sent from sendFile() via a InputStream and writes it to filesystem 
	 * @param in - InputStream on which the file are sent and received
	 * @param targetFile - the file on local or remote filesystem that the file are written to
	 * @return true if successful
	 */
	public boolean receiveFile(InputStream in, File targetFile){
		try {
			
			//Reading 8 bytes overhead from stream in to a long that is how many bytes is expected
			byte[] b = new byte[8];
			in.read(b, 0, 8);
			long length = ArrayUtils.byteArrayToLong(b);
			
			
			OutputStream out = new FileOutputStream(targetFile, false);
			
			byte buf[]=new byte[chunkSize];
			int len;
			
			//Receiving file
			for(int i = 0; i < length/chunkSize; i++){
				len = in.read(buf, 0, chunkSize);
				out.write(buf,0,len);
			}
			len = in.read(buf, 0, (int)(length%chunkSize));
			out.write(buf,0,len);
			
			return true;
		} catch (IOException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
}

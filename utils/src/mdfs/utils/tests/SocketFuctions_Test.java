package mdfs.utils.tests;

import mdfs.utils.io.SocketFunctions;

import java.io.*;

public class SocketFuctions_Test {
	public static void main(String[] args){
		SocketFunctions socketFunctions = new SocketFunctions();
		File toBeSent = new File("/home/kungar/workspace/TDDD12/lab1/company_data.sql");
		File target = new File("/tmp/target.sql");
		
		OutputStream out;
		try {
			out = new FileOutputStream(target);
			socketFunctions.sendFile(out, toBeSent);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		
		InputStream in;
		try {
			in = new FileInputStream(toBeSent);
			File targetFile = new File("/tmp/target2.sql");
			socketFunctions.receiveFile(in, targetFile);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		
		
		
	}
}

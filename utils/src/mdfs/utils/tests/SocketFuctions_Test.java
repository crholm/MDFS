package mdfs.utils.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mdfs.utils.io.SocketFunctions;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		InputStream in;
		try {
			in = new FileInputStream(toBeSent);
			File targetFile = new File("/tmp/target2.sql");
			socketFunctions.receiveFile(in, targetFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}

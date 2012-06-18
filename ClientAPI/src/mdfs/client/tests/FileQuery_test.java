package mdfs.client.tests;

import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;

import java.io.File;

public class FileQuery_test {


	FileQuery fq = new FileQueryImpl("raz", "qwerty");


	public void put(){
		
		File file = new File("/home/kungar/workspace/TDDD12/lab1/Lab1");
	
		System.out.println("Making dir /raz/dir1 -->" + fq.mkdir("/raz/dir1"));
		System.out.println("Making dir /raz/dir1 -->" + fq.mkdir("/raz/dir11"));
		System.out.println("Making dir /raz/dir1/dir2 -->" + fq.mkdir("/raz/dir1/dir2"));
		fq.put(file, "/raz/dir1/dir2/Lab1", null);
		fq.put(file, "/raz/dir1/dir2/Lab2", null);
		
		file = new File("/home/kungar/workspace/TDDD12");
		fq.put(file, "/raz/TDDD12", "r");
		
	}
	
	public void cd() {
		System.out.println();
		System.out.println("cd /raz  --> " + fq.cd("/raz", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd /raz/dir1  --> " + fq.cd("/raz/dir1", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd /raz/dir1/dir2  --> " + fq.cd("/raz/dir1/dir2", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd /raz/dir1/dir2/Lab1  --> " + fq.cd("/raz/dir1/dir2/Lab1", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd ~/dir11  --> " + fq.cd("~/dir11", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd ..  --> " + fq.cd("..", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd /raz/dir1/dir2  --> " + fq.cd("/raz/dir1/dir2", null));
		System.out.println("pwd: " + fq.pwd());
		System.out.println("cd ../././.././dir11/. --> " + fq.cd("../././.././dir11/.", null));
		System.out.println("pwd: " + fq.pwd());
	}
	
	public void ls(){
		System.out.println();
		
		MDFSProtocolMetaData[] files = fq.ls("/raz/dir1/dir2", null);
		for (MDFSProtocolMetaData metadata : files) {
			System.out.println("File --> " + metadata);
		}
		
		System.out.println();
		files = fq.ls("/raz/dir1/dir2/Lab1", null);
		for (MDFSProtocolMetaData metadata : files) {
			System.out.println("Singel File --> " + metadata);
		}
		
	}
	
	private void get() {
		File file = new File("/home/kungar/workspace/mdfs-output/test2");
		fq.get("/raz/dir1/dir2/Lab1", file, null);
		
	}
	
	private void rm() {
		System.out.println("Removing /raz/dir1/dir2/Lab1 -->" + fq.rm("/raz/dir1/dir2/Lab1", null));
		System.out.println("Removing /raz/dir1/dir2/Lab2 -->" + fq.rm("/raz/dir1/dir2/Lab2", null));
		System.out.println("Removing /raz/dir1/dir2 -->" + fq.rm("/raz/dir1/dir2", null));
		System.out.println("Removing dir /raz/TDDD12 -->" +fq.rm("/raz/TDDD12", "r") );
		
		
	}

	

	private void testWrite1000(){
		String defaultPath = "/raz/test1000";
		System.out.println("\n\nCreating dir -> " + defaultPath);
		fq.mkdir(defaultPath);
		File f = new File("/tmp/toucher");
		long time = System.currentTimeMillis();
		for(int i = 0; i < 100; i++){
			
			System.out.println("Creating dir -> " + defaultPath + "/" + i);
			fq.mkdir(defaultPath + "/" + i);
			System.out.println("Writing 100 files...");
			
			for(int j = 0; j< 100; j++){
				fq.put(f, defaultPath + "/" + i + "/" + j, null);
			}
			System.out.println("done.");
			
		}
		long time2 = System.currentTimeMillis();
		System.out.println("Time spent -> " + (time2 - time)/1000 + "s + " + (time2-time)%1000 + "ms" );
		
		
	}
	
	private void testWrite1000Parallel(){
		ThreadedPut.startTime = System.currentTimeMillis();
		
		String defaultPath = "/raz/test1000parallel";
		fq.mkdir(defaultPath);
		ThreadedPut tp;
		System.out.println("Starting 10 threads");
		for(int i = 0; i < 10; i++){
			tp = new ThreadedPut(defaultPath, i);
			new Thread(tp).start();
		}
		System.out.println("all threads are started");
		
		 
	}
	
	public static void main(String[] args){
		FileQuery_test test = new FileQuery_test();
		
		test.put();
		test.ls();
		test.cd();
		test.get();
		test.rm();
		
		
		//test.testWrite1000Parallel();
	}


	
}

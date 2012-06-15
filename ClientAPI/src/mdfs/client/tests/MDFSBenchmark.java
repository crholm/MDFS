package mdfs.client.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;

public class MDFSBenchmark implements Runnable{
	public static long end;
	public static int threadsRunning = 0;
	
	
	FileQuery fq = new FileQueryImpl("test1", "test1");
	String path;
	String file;
	int offset;
	
	public MDFSBenchmark(String path, String file, int offset) {
		this.path = path;
		this.file = file;
		this.offset = offset;
	}
	
	public static void main(String[] args){
		String path = "/tmp/mdfs.test.file";
		File file = new File(path);
		if(!file.exists()){
			try {
				file.createNewFile();
				FileOutputStream out = new FileOutputStream(file);
				out.write("a".getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		FileQuery f = new FileQueryImpl("test1", "test1");
		f.mkdir("/test1/benchmark");
		System.out.println("Writing 10000 files with 10 threads:");
		long start = System.currentTimeMillis();
		
		for(int i = 0; i<10; i++){
			new Thread(new MDFSBenchmark("/test1/benchmark", path, i)).start();
			threadsRunning++;
		}
		
		while(threadsRunning != 0){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("Time used: " + (end-start)/1000 + "s " + (end-start)%1000 + "ms\n" );
		
		System.out.println("Removing 10000 files, no threads");
		start = System.currentTimeMillis();
		f.rm("/test1/benchmark", "r");
		long stop = System.currentTimeMillis();
		System.out.println("Time used: " + (stop-start)/1000 + "s " + (stop-start)%1000 + "ms\n");
	}


	@Override
	public void run() {
		for(int i = 0; i < 10; i++){
			int dir = offset*10+i;
			fq.mkdir(path+"/"+dir);
			for(int j = 0; j < 100; j++){
				fq.put(new File(file), path+"/"+dir+"/"+j, null);
			}
		}
		end = System.currentTimeMillis();
		threadsRunning--;
	}
}

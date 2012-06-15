package mdfs.client.tests;

import java.io.File;

import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;

public class ThreadedPut implements Runnable{
	public static long startTime;
	public static long stopTime;
	
	private FileQuery fq = new FileQueryImpl("raz", "qwerty");
	String defaultPath;
	
	int size;
	
	public ThreadedPut(String defaultPath, int size) {
		this.defaultPath = defaultPath;
		this.size = size;
	}

	@Override
	public void run() {
		
		File f = new File("/tmp/toucher");
			
		fq.mkdir(defaultPath);
		
		
		
		for(int i = 0; i<10; i++){
			int name = i+size*10;
			fq.mkdir(defaultPath + "/" + name);
			
			for(int j = 0; j< 100; j++){
				fq.put(f, defaultPath + "/" + name + "/"+ j, null);
			}
			
			
		}
		
		stopTime = System.currentTimeMillis();
		System.out.println("done. time so far -> " + (stopTime - startTime)/1000 + "s + " +  (stopTime - startTime)%1000 + "ms");
			
	
		
	}

}

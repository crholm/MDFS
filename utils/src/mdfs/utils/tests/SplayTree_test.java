package mdfs.utils.tests;

import mdfs.utils.SplayTree;

public class SplayTree_test {
	public static void main(String[] args){
		SplayTree<String, String> st = new SplayTree<String, String>();
		
		
		
		for(int i = 0; i < 1000000; i ++){
			st.insert(Integer.toString(i), ">"+Integer.toString(i)+"<");
		}
		
		
		for(int i = 0; i < 10; i++){
			String user =  Integer.toString((int)(Math.random()*999999));
			long time;
			String data;
			
			System.out.println("1 try:");
			time = System.currentTimeMillis();
			data = st.find(user);
			time = System.currentTimeMillis() - time;
			System.out.println(" User: " + user);
			System.out.println(" Data: " + data);
			System.out.println(" Time: " + time);
			
			System.out.println("2 try:");
			time = System.currentTimeMillis();
			data = st.find(user);
			time = System.currentTimeMillis() - time;
			System.out.println(" User: " + user);
			System.out.println(" Data: " + data);
			System.out.println(" Time: " + time);
			
			
			
			
		}
		
		
	}
}

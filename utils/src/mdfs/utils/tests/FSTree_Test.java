package mdfs.utils.tests;


import mdfs.utils.FSTree;

public class FSTree_Test {
	
	public FSTree_Test(){
		FSTree<String> md = new FSTree<String>("/", "/");
		
		String d;
		
		for(int i = 0; i < 3; i++){
			d = new String();
			d = ("/" + Integer.toString(i));
			md.put(d, d);
			
			for(int j = 0; j < 3; j++){
				d = new String();
				d = ("/" + Integer.toString(i) + "/" + Integer.toString(j));
				md.put(d, d);
				
				for(int n = 0; n < 3; n++){
					d = new String();
					d = ("/" + Integer.toString(i) + "/" + Integer.toString(j)+ "/" + Integer.toString(n) );
					md.put(d, d);
				}
			}
		}
		
		
		System.out.println(md.isEmpty());
		
		
		for(int i = 0; i < 10; i++){
			String key = "/" + Integer.toString((int)(Math.random()*3)) + 
							"/" + Integer.toString((int)(Math.random()*3)) + 
								"/" + Integer.toString((int)(Math.random()*3));
		
			System.out.println("Searchin for :" + key);
			System.out.println("Found: " + md.get(key) + "\n");
		}
	}
	
	
	
	
	
	
	public static void main(String[] args){
		System.out.println("sad");
		new FSTree_Test();
		/*
		FSTree<String> tree = new FSTree<String>("/", "/");
	
		tree.put("/raz", "/raz");
		tree.put("/raz", "/raz2");
		tree.put("/raz/1", "/raz/1");
		tree.put("/raz/2", "/raz/2");
		tree.put("/raz/3", "/raz/3");
		tree.put("/raz/3", "/raz/3");
		tree.put("/raz/3", "/raz/4");
		
		tree.put("/raz1", "/raz1");
		tree.put("/raz1/1", "/raz1/1");
		tree.put("/raz1/1", "/raz1/1");
		tree.put("/raz1/1", "/raz1/1");
		tree.put("/raz1/1/1", "/raz1/1/1");
		tree.put("/raz1/1/2", "/raz1/1/2");
		
		tree.put("/raz3", "/raz3");
		
		tree.put("/raz3/4/5", "/raz3/4/5");
		
		System.out.println(tree.get("/raz"));
		System.out.println(tree.get("/raz/2"));
		System.out.println(tree.get("/raz/3"));
		System.out.println(tree.get("/raz1/1/2"));
		System.out.println("-->"  + tree.get("/raz3/4/5"));
		
		*/
		
			
		
		/*
		String[] a = new String[4];
		a = tree.getChildernArray("/raz", a);
		for (String string : a) {
			System.out.println(string);
		}
		*/
		
	}
}

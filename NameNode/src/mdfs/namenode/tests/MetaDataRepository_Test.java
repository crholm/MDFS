package mdfs.namenode.tests;

import mdfs.namenode.repositories.*;


public class MetaDataRepository_Test {
	public static void main(String args[]){
		MetaDataRepository md = MetaDataRepository.getInstance();
		System.out.println("Building meta data...");
		MetaDataRepositoryNode d = new MetaDataRepositoryNode();
		
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				for(int n = 0; n < 3; n++){
					d = new MetaDataRepositoryNode();
					d.setFilePath("/" + Integer.toString(i) + "/" + Integer.toString(j)+ "/" + Integer.toString(n));
					md.add(d.getKey(), d);
				}
			}
		}
		
		for(int i = 0; i < 10; i++){
			String key = "/" + Integer.toString((int)(Math.random()*3)) + 
							"/" + Integer.toString((int)(Math.random()*3)) + 
								"/" + Integer.toString((int)(Math.random()*3));
		
			System.out.println("Searchin for :" + key);
			System.out.println("Found: " + md.get(key).getKey() + "\n");
		}
		
		
		/*
		
		for(int i = 0; i < 1000000; i++){
			MetaDataRepositoryNode d = new MetaDataRepositoryNode();
			d.setFilePath(Integer.toString(i));
			md.add(d.getKey(), d);
		}
		System.out.println("done.");
		
		for(int i = 0; i < 10; i++){
			String key = Integer.toString((int)(1+Math.random()*999990));
			System.out.println("Searchin for :" + key);
			System.out.println("Found: " + md.get(key).getKey() + "\n");
		}
		*/
		
		
	}
}

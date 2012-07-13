package mdfs.namenode.tests;

import mdfs.namenode.repositories.UserDataRepository;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.crypto.digests.SHA1;

public class UserDataRepository_Test {
	
	public static void main(String args[]){
		UserDataRepository ud = UserDataRepository.getInstance();
		System.out.println("Building user data...");
		for(int i = 0; i < 10000; i++){
			UserDataRepositoryNode d = new UserDataRepositoryNode(i, Integer.toString(i));
			d.setPwdHash(SHA1.quick(Integer.toString(i).getBytes()));
			ud.add(d);
		}
		System.out.println("done.");
		
		for(int i = 0; i < 10; i++){
			String key = Integer.toString((int)(1+Math.random()*9990));
			System.out.println("Searchin for :" + key);
			UserDataRepositoryNode d = ud.get(key);
			System.out.println("Found user hash: " + d.getPwdHash());
			System.out.println("Have correct password: " + ud.authUser(key, key));
			System.out.println("Have correct password: " + ud.authUser(d, key) + "\n");
		}
		
		
		
	}
}

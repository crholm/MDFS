package mdfs.namenode.tests;

import mdfs.namenode.parser.SessionImpl;
import mdfs.namenode.repositories.MetaDataRepository;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepository;

public class Session_Test {
	public static void main(String[] args){		
		MetaDataRepository metaRepo = MetaDataRepository.getInstance();
		UserDataRepository userRepo = UserDataRepository.getInstance();
		System.out.println("Trying writing:");
		
		System.out.println("Adding user: " + 
		userRepo.addUser("raz", "qwerty"));
		SessionImpl session = new SessionImpl();
		/*
        System.out.println("Adding Json string: " +

        session.setRequest(getJsonTestWrite()));
		
		try {
			session.parseRequest();
		} catch (JSONException e) {

			e.printStackTrace();
		}
		*/
		
		MetaDataRepositoryNode node = metaRepo.get("raz/info.txt");
		System.out.println("Node json data:\n" + node.toJSON());
		System.out.println("Response json data:\n" + session.getResponse());
		
		
		System.out.println("\nTrying retreving info:");
		session = new SessionImpl();
		/*
        System.out.println("Adding Json string: " +
		session.setRequest(getJsonTestInfo()));
		try {
			session.parseRequest();
		} catch (JSONException e) {

			e.printStackTrace();
		}
		System.out.println("Response json data:\n" + session.getResponse());
		 */
	}
	
	public static String getJsonTestWrite(){
		return "{ \"From\": \"test.island.liu.se\", \"To\": \"lucy.island.liu.se\", \"Stage\": \"Request\", \"Type\": \"Meta-data\", \"Mode\": \"Write\", \"User\": \"raz\", \"Pass\": \"qwerty\", \"Meta-data\":{    \"path\": \"raz/info.txt\",    \"type\": \"file\",    \"size\": 2024,    \"permission\": 764,    \"owner\": \"raz\",    \"group\": \"raz\",    \"created\": \"2011-09-10 13:27:02\",    \"lastEdited\": \"2011-09-11 11:09:00\",    \"lastToutched\": \"2011-09-13 16:17:34\"    } }";
	}
	public static String getJsonTestInfo(){
		return "{ \"From\": \"test.island.liu.se\", \"To\": \"lucy.island.liu.se\", \"Stage\": \"Request\", \"Type\": \"Meta-data\", \"Mode\": \"Info\", \"User\": \"raz\", \"Pass\": \"qwerty\", \"Meta-data\":{    \"path\": \"raz/info.txt\"  } }";
	}
}

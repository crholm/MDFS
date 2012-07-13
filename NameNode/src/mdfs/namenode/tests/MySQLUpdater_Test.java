package mdfs.namenode.tests;


import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.namenode.sql.MySQLUpdater;
import mdfs.utils.Time;
import mdfs.utils.io.protocol.enums.MetadataType;

public class MySQLUpdater_Test {
	public static void main(String[] args){
		MetaDataRepositoryNode node = new MetaDataRepositoryNode();
		node.setFilePath("test/node1");
		node.setSize(123);
		node.setFileType(MetadataType.DIR);
		node.setStorageName("124-124-124-1551332-23-6244724.v1");
		node.setPermission((short)666);
		node.setOwner("test1");
		node.setGroup("test1");
		node.setCreated(Time.currentTimeMillis());
		node.setLastEdited(Time.currentTimeMillis());
		
		MetaDataRepositoryNode node2 = new MetaDataRepositoryNode();
		node2.setFilePath("test/node2");
		node2.setSize(123);
		node2.setFileType(MetadataType.DIR);
		node2.setStorageName("124-124-124-1551332-23-6244724.v1");
		node2.setPermission((short)666);
		node2.setOwner("test1");
		node2.setGroup("test1");
		node2.setCreated(Time.currentTimeMillis());
		node2.setLastEdited(Time.currentTimeMillis());
		
		MySQLUpdater sql = MySQLUpdater.getInstance();
		
		sql.update(node2);
		sql.update(node);

		
		UserDataRepositoryNode user = new UserDataRepositoryNode(82, "raz");
		user.setPwdHash("dsfoiuh320y9ewsmfpu29fun+28uf+98mfu2ff,siu +943ufj");
		sql.update(user);
		
		
		
	
		
	}
}

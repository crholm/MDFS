package mdfs.namenode.tests;


import mdfs.namenode.repositories.DataTypeEnum;
import mdfs.namenode.repositories.MetaDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepositoryNode;

import mdfs.namenode.sql.MySQLUpdater;

public class MySQLUpdater_Test {
	public static void main(String[] args){
		MetaDataRepositoryNode node = new MetaDataRepositoryNode();
		node.setFilePath("test/node1");
		node.setSize(123);
		node.setFileType(DataTypeEnum.DIR);
		node.setStorageName("124-124-124-1551332-23-6244724.v1");
		node.setPermission((short)666);
		node.setOwner("test1");
		node.setGroup("test1");
		node.setCreated("2012-03-20 12:05:42");
		node.setLastEdited("2012-03-20 12:05:42");
		
		MetaDataRepositoryNode node2 = new MetaDataRepositoryNode();
		node2.setFilePath("test/node2");
		node2.setSize(123);
		node2.setFileType(DataTypeEnum.DIR);
		node2.setStorageName("124-124-124-1551332-23-6244724.v1");
		node2.setPermission((short)666);
		node2.setOwner("test1");
		node2.setGroup("test1");
		node2.setCreated("2012-03-20 12:05:42");
		node2.setLastEdited("2012-03-20 12:05:42");
		
		MySQLUpdater sql = MySQLUpdater.getInstance();
		
		sql.updateMetaData(node2);
		sql.updateMetaData(node);

		
		UserDataRepositoryNode user = new UserDataRepositoryNode("raz");
		user.setPwdHash("dsfoiuh320y9ewsmfpu29fun+28uf+98mfu2ff,siu +943ufj");
		sql.updateUserData(user);
		
		
		
	
		
	}
}

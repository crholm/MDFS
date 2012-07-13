package mdfs.namenode.tests.junit;

import mdfs.namenode.repositories.*;
import mdfs.utils.Config;
import mdfs.utils.io.protocol.enums.MetadataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RepositoriesModuleTest {
	
	DataNodeInfoRepository dataNodeInfoRepository;
	MetaDataRepository metaDataRepository;
	UserDataRepository userDataRepository;
	
	@Before
	public void setUp() throws Exception {
		dataNodeInfoRepository = DataNodeInfoRepository.getInstance();
		metaDataRepository = MetaDataRepository.getInstance();
		userDataRepository = UserDataRepository.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoading() {
		assertNotNull(dataNodeInfoRepository);
		assertNotNull(metaDataRepository);
		assertNotNull(userDataRepository);
	}
	
	@Test
	public void testUserDataRepository() {
		UserDataRepositoryNode node;
		//Adding users
		assertTrue(userDataRepository.add("moduelTest", "moduelTest"));
		assertTrue(!userDataRepository.add("moduelTest", "moduelTest"));
		
		//Getting user
		assertTrue((node = userDataRepository.get("moduelTest")) != null);
		assertTrue(userDataRepository.get("1234567890") == null);
		
		//Adding same user
		assertTrue(!userDataRepository.add(node));
		
		//Testing authentication
		assertTrue(userDataRepository.authUser("moduelTest", "moduelTest"));
		assertTrue(!userDataRepository.authUser("moduelTest", "moduelTest1"));
		assertTrue(userDataRepository.authUser(node, "moduelTest"));
		assertTrue(!userDataRepository.authUser(node, "moduelTest1"));
		
		//Testing removing user
		assertTrue(userDataRepository.remove("moduelTest") != null);
		assertTrue(userDataRepository.remove("moduelTest") == null);
	}
	
	@Test
	public void testDataNodeInfoRepository() {
	
		
		String[] datanodeName = Config.getStringArray("datanode.name");
		String[] datanodeAddress = Config.getStringArray("datanode.address");
		String[] datanodePort = Config.getStringArray("datanode.port");
		
		assertNotNull(datanodeName);
		assertNotNull(datanodeAddress);
		assertNotNull(datanodePort);
		
		for (String name : datanodeName) {
			assertTrue(dataNodeInfoRepository.get(name) != null);
			assertTrue(dataNodeInfoRepository.get(name+"qwerty") == null);
		}
		
		for(int i = 0; i < datanodeAddress.length; i++){
			assertTrue(dataNodeInfoRepository.get(datanodeAddress[i], datanodePort[i]) != null);
			assertTrue(dataNodeInfoRepository.get(datanodeAddress[i], datanodePort[i]+"qwerty") == null);
			assertTrue(dataNodeInfoRepository.get(datanodeAddress[i], datanodePort[i]).getName().equals(datanodeName[i]));
		}
		
	}
	
	
	@Test
	public void testMetaDataRepository() {
		MetaDataRepositoryNode node1 = new MetaDataRepositoryNode();
		node1.setFilePath("/metaRepoTest");
		node1.setFileType(MetadataType.DIR);
				
		MetaDataRepositoryNode node2 = new MetaDataRepositoryNode();
		node2.setFilePath("/metaRepoTest/dir");
		node2.setFileType(MetadataType.DIR);
		
		assertTrue(!metaDataRepository.add(node2.getKey(), node2));
		assertTrue(metaDataRepository.add(node1.getKey(), node1));
		assertTrue(metaDataRepository.add(node2.getKey(), node2));
		
		for(int i = 0; i < 10; i++){
			MetaDataRepositoryNode node = new MetaDataRepositoryNode();
			node.setFilePath("/metaRepoTest/" + i);
			node.setFileType(MetadataType.FILE);
			node.setSize(10);
			assertTrue(metaDataRepository.add(node.getKey(), node));
			assertTrue(!metaDataRepository.add(node.getKey(), node));
			
			assertTrue(metaDataRepository.get("/metaRepoTest/" + i) == node);
		}
		
		MetaDataRepositoryNode[] nodes = new MetaDataRepositoryNode[10];
		for(int i = 0; i < 10; i++){
			MetaDataRepositoryNode node = new MetaDataRepositoryNode();
			node.setFilePath("/metaRepoTest/dir/" + i);
			node.setFileType(MetadataType.FILE);
			node.setSize(10);
			nodes[i] = node;
		}
		assertTrue(metaDataRepository.add(nodes, false));
		
		nodes = metaDataRepository.getChildren("/metaRepoTest/dir");
		assertTrue(nodes.length == 10);
		
		
		for (MetaDataRepositoryNode node : nodes) {
			assertTrue(metaDataRepository.remove(node.getKey()) == node);
		}
		MetaDataRepositoryNode node3 = new MetaDataRepositoryNode();
		node3.setFilePath("/metaRepoTest/dir");
		node3.setFileType(MetadataType.FILE);
		node3.setSize(10);
		
		assertTrue(metaDataRepository.replace(node3.getKey(), node3) == node2);
		
		assertTrue(metaDataRepository.remove(node3.getKey()) == node3);
		assertTrue(metaDataRepository.remove(node1.getKey()) == node1);
		
	}
	
	

}

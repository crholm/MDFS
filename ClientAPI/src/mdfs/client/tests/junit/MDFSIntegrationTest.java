package mdfs.client.tests.junit;

import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;
import mdfs.utils.HashTypeEnum;
import mdfs.utils.Hashing;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MDFSIntegrationTest {

	@Test
	public void testFileQueryImpl() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertNotNull(fq);
		
	}

	@Test
	public void testMkdir() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertTrue(fq.mkdir("rm"));
		assertTrue(fq.mkdir("/test1/rm1"));

        for(int i = 0; i < 10; i++){
            assertTrue(fq.mkdir("/test1/" + i));
        }
	}
	
	@Test
	public void testCd() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertTrue(fq.cd(".", null));
		assertTrue(fq.cd("/test1", null));
		assertTrue(fq.cd("~", null));
		assertTrue(fq.cd("~/1", null));
		assertTrue(fq.cd("~/1/../2", null));
		assertTrue(fq.cd(".././3", null));
	}

	@Test
	public void testLsString() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		
		fq.cd("/test1", null);
		assertTrue(fq.ls(null) != null);
		fq.cd("/test1/1", null);
		assertTrue(fq.ls(null) != null);
		fq.cd("/test1/2", null);
		assertTrue(fq.ls(null) != null);
		fq.cd("/test1/DoNotExist", null);
		assertTrue(fq.ls(null) != null);
		
	}

	@Test
	public void testLsStringString() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertTrue(fq.ls("/test1", null) != null);
		assertTrue(fq.ls("/test1/", null) != null);
		assertTrue(fq.ls("/test1/0", null) != null);
		assertTrue(fq.ls("/test1/1", null) != null);
		assertTrue(fq.ls("/test1/DoNotExist", null) == null);
		
	}

	@Test
	public void testPwd() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertTrue(fq.pwd().equals("/test1"));
		String path;
		
		for(int i = 0; i < 10; i++){
			path = "/test1/" + i;
			fq.cd(path, null);
			assertTrue(fq.pwd().equals(path));
		}
		
	}

	@Test
	public void testRm() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		assertTrue(fq.mkdir("rm"));
		assertTrue(fq.mkdir("/test1/rm1"));
		assertTrue(fq.mkdir("/test1/rm1/1"));
		assertTrue(fq.mkdir("/test1/rm1/2"));
		assertTrue(fq.mkdir("/test1/rm1/3"));
		
		assertTrue(!fq.rm("/test1/rm1", null));
		assertTrue(fq.rm("/test1/rm1/1", null));
		assertTrue(fq.rm("/test1/rm1", "r"));
		assertTrue(fq.rm("/test1/rm", "r"));

        for(int i = 0; i < 10; i++){
            assertTrue(fq.rm("/test1/" + i, "r"));
        }
		
	}


	@Test
	public void testGetStringFileString() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		
		String testStringHash = Hashing.hash(HashTypeEnum.SHA1, testString);
		try {
			File file = File.createTempFile("source", ".junitTest");
			FileOutputStream out = new FileOutputStream(file);
			
			byte[] b = testString.getBytes();
			int len = b.length;
			out.write(b);
			
			
			assertTrue(fq.put(file,"/test1/testFile1", null));
		
			File file2 =  File.createTempFile("new", ".junitTest");

            Thread.sleep(200);

			assertTrue(fq.get("/test1/testFile1", file2, null));
			
			
			FileInputStream in = new FileInputStream(file2);
			
			
			b = new byte[len];
			in.read(b);
			
			String newFileContent = new String(b);
			String newFileContentHash = Hashing.hash(HashTypeEnum.SHA1, newFileContent);
			
			assertTrue( testStringHash.equals(newFileContentHash)   );

            assertTrue(fq.rm("/test1/testFile1", null));
			file2.delete();
			file.delete();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

	@Test
	public void testPutFileString() {
		FileQuery fq = new FileQueryImpl("test1", "test1");
		
		try {
			File file = File.createTempFile("source", ".junitTest");
			FileOutputStream out = new FileOutputStream(file);
			
			byte[] b = testString.getBytes();
			out.write(b);
			
			
			assertTrue(fq.put(file,"/test1/testFile2", null));
			assertTrue(fq.put(file, null));
			assertTrue(fq.rm("/test1/testFile2", null));
			assertTrue(fq.rm("/test1/" + file.getName(), null));
			
			file.delete();
		}catch (Exception e) {
		}
	}

	@Test
	public void testPutFileStringString() {
		testPutFileString();
	}

	
	private static String testString = 	"qwertyuiopåasdfghjklödksläfmnå09234q4åewf'q234itg'qokg'v,q3q034fqgk'kareva'kpergmv'9maa" +
							"äaeirgå4opgmW0PTOJG94 MNG430 q	t3450it q+4a'ptw' 'j4O'PTJ J4JTY RJ gk0t4kg04KG P'OEKf'2P3 UT'3JTR 4" +
							"rå8H4 EWJF + J4TK4	J	9J43T+J	43+TJ0 +4J	3T 4JT J4ÅO'GEWHgå9hrbu qn3å0 4mgnq39n go  4t30 zxcvbn -" +
							"äaeirgå4opgmW0PTOJG94 MNG430 q	t3450it q+4a'ptw' 'j4O'PTJ J4JTY RJ gk0t4kg04KG P'OEKf'2P3 UT'3JTR 4" +
							"äaeirgå4opgmW0PTOJG94 MNG430 q	t3450it q+4a'ptw' 'j4O'PTJ J4JTY RJ gk0t4kg04KG P'OEKf'2P3 UT'3JTR 4" +
							"rå8H4 EWJF + J4TK4	J	9J43T+J	43+TJ0 +4J	3T 4JT J4ÅO'GEWHgå9hrbu qn3å0 4mgnq39n go  4t30 zxcvbn -" +
							"rå8H4 EWJF + J4TK4	J	9J43T+J	43+TJ0 +4J	3T 4JT J4ÅO'GEWHgå9hrbu qn3å0 4mgnq39n go  4t30 zxcvbn -";
}

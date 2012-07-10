package mdfs.utils.tests.junit;

import static org.junit.Assert.*;

import mdfs.utils.crypto.HashTypeEnum;
import mdfs.utils.crypto.Hashing;

import org.junit.Test;

public class HashingTest {

	private String hash(HashTypeEnum type, String test){
		return Hashing.hash(type, test);
	}
	
	@Test
	public void testHashMD5() {
		assertTrue(hash(HashTypeEnum.MD5, "qwerty").equals("d8578edf8458ce06fbc5bb76a58c5ca4"));
	}
	@Test
	public void testHashSHA1() {
		assertTrue(hash(HashTypeEnum.SHA1, "qwerty").equals("b1b3773a05c0ed0176787a4f1574ff0075f7521e"));
	}

}

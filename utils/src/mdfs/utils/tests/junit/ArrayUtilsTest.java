package mdfs.utils.tests.junit;

import static org.junit.Assert.*;

import mdfs.utils.ArrayUtils;

import org.junit.Test;

public class ArrayUtilsTest {

	@Test
	public void testAddAllStringArrayArray() {
		String[] s1 = {"1", "2"};
		String[] s2 = {"3", "4"};
		String[] s3 = {"5", "6"};
		
		String[] sAll = ArrayUtils.addAll(s1, s2, s3);

		assertTrue(sAll[0].equals(s1[0]));
		assertTrue(sAll[1].equals(s1[1]));
		assertTrue(sAll[2].equals(s2[0]));
		assertTrue(sAll[3].equals(s2[1]));
		assertTrue(sAll[4].equals(s3[0]));
		assertTrue(sAll[5].equals(s3[1]));
	}

	@Test
	public void testAddAllIntArrayArray() {
		int[] s1 = {1, 2};
		int[] s2 = {3, 4};
		int[] s3 = {5, 6};
		
		int[] sAll = ArrayUtils.addAll(s1, s2, s3);

		assertTrue(sAll[0] == s1[0]);
		assertTrue(sAll[1] == s1[1]);
		assertTrue(sAll[2] == s2[0]);
		assertTrue(sAll[3] == s2[1]);
		assertTrue(sAll[4] == s3[0]);
		assertTrue(sAll[5] == s3[1]);
	}

	@Test
	public void testImplodeStringArrayString() {
		String test = "Fo Bar and some cherry on top";
		
		String[] s = test.split(" ");
		assertTrue(  ArrayUtils.implode(s, " ").equals(test));
		
		s = test.split("o");
		assertTrue(  ArrayUtils.implode(s, "o").equals(test));
		
		s = test.split("r");
		assertTrue(  ArrayUtils.implode(s, "r").equals(test));
	}

	@Test
	public void testImplodeStringArrayStringInt() {
		String test = "Fo Bar and some cherry on top";
		
		String[] s = test.split(" ");
		assertTrue(  ArrayUtils.implode(s, " ", 3).equals("Fo Bar and"));
				
	}

	@Test
	public void testImplodeCharArray() {
		String test = "Fo Bar and some cherry on top";
		char[] c = test.toCharArray();
		assertTrue(ArrayUtils.implode(c).equals(test));
	}

	@Test
	public void testImplodeCharArrayInt() {
		String test = "Fo Bar and some cherry on top";
		char[] c = test.toCharArray();
		for(int i = 0; i<10; i++)
			assertTrue(ArrayUtils.implode(c, i).equals(test.subSequence(0, i)));
	}

	@Test
	public void testImplodeCharArrayString() {
		String test = "Fo Bar and some cherry on top";
		char[] c = test.toCharArray();
		assertTrue(ArrayUtils.implode(c, " ").equals(test.replace("", " ").trim()));
	}

	@Test
	public void testImplodeCharArrayStringInt() {
		String test = "Fo Bar and some cherry on top";
		char[] c = test.toCharArray();
		for(int i = 1; i<10; i++)
			assertTrue(  ArrayUtils.implode(c, " ", i).equals(  test.replace("", " ").trim().subSequence(0, i*2-1)  )   );
	}

	@Test
	public void testByteArrayToInt() {
		byte[] b = ArrayUtils.intToByteArray(12345);
		assertTrue(ArrayUtils.byteArrayToInt(b) == 12345);
	}

	@Test
	public void testByteArrayToLong() {
		byte[] b = ArrayUtils.longToByteArray(12345678L);
		assertTrue(ArrayUtils.byteArrayToLong(b) == 12345678L);
	}

	@Test
	public void testLongToByteArray() {
		byte[] b = ArrayUtils.longToByteArray(12345678L);
		assertTrue(ArrayUtils.byteArrayToLong(b) == 12345678L);
	}

	@Test
	public void testIntToByteArray() {
		byte[] b = ArrayUtils.intToByteArray(12345);
		assertTrue(ArrayUtils.byteArrayToInt(b) == 12345);
	}

}

package mdfs.utils;

/**
 * Usefull tools when dealing with Arrays.
 * @author Rasmus Holm
 *
 */
public class ArrayUtils{
	
	/**
	 * Add all object arrays in to one large array
	 * @param s all arrays that is to be added
	 * @return one array containing all the arrays provided
	 */
	public static Object[] addAll(Object[]... s ){
		int size = 0;
		for (Object[] objects : s) {
			size += objects.length;
		}
		
		Object[] sum = new Object[size];
		
		int offset = 0;
		for (Object[] array : s) {
			System.arraycopy(array, 0, sum, offset, array.length);
			offset += array.length;
		}
		return sum;
	}
	
	/**
	 * Add all String array in to one large array
	 * @param s all arrays that is to be added
	 * @return one array containing all the arrays provided
	 */
	public static String[] addAll(String[]... s ){
		int size = 0;
		for (String[] strings : s) {
			size += strings.length;
		}
		String[] sum = new String[size];
		
		int offset = 0;
		for (String[] array : s) {
			System.arraycopy(array, 0, sum, offset, array.length);
			offset += array.length;
		}
		
		return sum;
	}
	
	/**
	 * Add all int array in to one large array
	 * @param n all arrays that is to be added
	 * @return one array containing all the arrays provided
	 */
	public static int[] addAll(int[]... n ){
		int size = 0;
		for (int[] i : n) {
			size += i.length;
		}
		
		int[] sum = new int[size];
		
		int offset = 0;
		for (int[] array : n) {
			System.arraycopy(array, 0, sum, offset, array.length);
			offset += array.length;
		}
		
		return sum;
	}
	
	
	/**
	 * Imploads a String array in to one String
	 * @param array that is to be imploaded
	 * @param glue is the string that will glue the arrays together in the new String
	 * @return a String that is the imploaded array
	 */
	public static String implode(String[] array, String glue) {
	  return implode(array, glue, array.length);
	}
	
	/**
	 * Imploads a String array in to one String
	 * @param array the array that is to be imploded
	 * @param glue is the string that will glue the arrays together in the new String
	 * @param limit the maximum number of String from the array that is to be imploaded
	 * @return a String that is the imploaded array
	 */
	public static String implode(String[] array, String glue, int limit) {
	    String out = "";
	    for(int i=0; i<array.length && i<limit; i++) {
	        if(i!=0) { out += glue; }
	        out += array[i];
	    }
	    return out;
	}
	
	/**
	 * Imploads a char array in to one String
	 * @param array the array that is to be imploded
	 * @return a String that is the imploaded array
	 */
	public static String implode(char[] array) {
		  return implode(array, "", array.length);
	}
	/**
	 * Imploads a char array in to one String
	 * @param array the array that is to be imploded
	 * @param limit maximum number of char that is to be imploaded to a string
	 * @return a String that is the imploaded array
	 */
	public static String implode(char[] array, int limit) {
		  return implode(array, "", limit);
	}
	/**
	 * Imploads a char array in to one String
	 * @param array the array that is to be imploded
	 * @param glue is the String that will glue the chars into one string
	 * @return a String that is the imploaded array
	 */
	public static String implode(char[] array, String glue) {
		  return implode(array, glue, array.length);
		}
		
	/**
	 * Imploads a char array in to one String
	 * @param array the array that is to be imploded
	 * @param glue is the String that will glue the chars into one string
	 * @param limit maximum number of char that is to be imploaded to a string
	 * @return a String that is the imploaded array
	 */
	public static String implode(char[] array, String glue, int limit) {
	    String out = "";
	    for(int i=0; i<array.length && i<limit; i++) {
	        if(i!=0) { out += glue; }
	        out += array[i];
	    }
	    return out;
	}
	
	
	/**
	 * Turns a byte array of 4 into a int 
	 * @param b the bytes that is to be converted to an int
	 * @return the int the byte array represents 
	 */
	public static int byteArrayToInt(byte[] b){
		int intNum = 0;
		for(int i =0; i < 4; i++){ 
			intNum <<= 8;
			intNum ^= (int)b[i] & 0xFF;
		}
		return intNum;
	}
	/**
	 * Turns a byte array of 8 into a long
	 * @param b the bytes that is to be converted to a long
	 * @return the long the bytes represents
	 */
	public static long byteArrayToLong(byte[] b){
		long longNum = 0;
		for(int i =0; i < 8; i++){ 
			longNum <<= 8;
			longNum ^= (long)b[i] & 0xFF;
		}
		return longNum;
	}
	
	/**
	 * Turns a long in to a byte array of 8
	 * @param l the long that is to be turned in to a byte array
	 * @return the byte array representing the provided long
	 */
	public static byte[] longToByteArray(long l){
		byte[] a = new byte[8];
		for(int i = 0; i<8; i++){
			 a[7 - i] = (byte)(l >>> (i * 8));
		}
		return a;
	}
	
	/**
	 * Turns a int in to a byte array of 4
	 * @param l the int that is to be turned in to a byte array
	 * @return the byte array representing the provided int
	 */
	public static byte[] intToByteArray(int l){
		byte[] a = new byte[4];
		for(int i = 0; i<4; i++){
			 a[3 - i] = (byte)(l >>> (i * 8));
		}
		return a;
	}
	
}

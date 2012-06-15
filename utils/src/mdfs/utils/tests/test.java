package mdfs.utils.tests;


import mdfs.utils.ArrayUtils;
import mdfs.utils.Time;



public class test {
	public static void main(String[] args){
		
		System.out.println(Time.getTimeStamp());
		
		
		Integer[] i = {1,2,3,4,5};
		Integer[] i2 = {1,2,3,4,5};
		Integer[] i3 = (Integer[]) ArrayUtils.addAll(i, i2);
		
		for (Object j : i3) {
			System.out.println((Integer)j);
		}
	
		
		
		
		System.out.println(System.getProperty("java.class.path"));
		
		int number = 1234567;
		long numberL = 9223372036854775803L;
		
		
		
		byte[] b = ArrayUtils.intToByteArray(number);
		System.out.println(number +  " ---> " + ArrayUtils.byteArrayToInt(b));
		
		b = ArrayUtils.longToByteArray((long)numberL);
		System.out.println(numberL +  " ---> " + ArrayUtils.byteArrayToLong(b));
		
	
	}
	
	public int byteToInt(byte[] b){
		
		return 1;
	}
}

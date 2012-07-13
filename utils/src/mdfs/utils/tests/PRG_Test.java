package mdfs.utils.tests;

import mdfs.utils.crypto.PRG;
import mdfs.utils.crypto.engines.HC128;
import mdfs.utils.crypto.engines.Salsa20;
import mdfs.utils.crypto.engines.SosemanukFast;
import mdfs.utils.crypto.engines.SosemanukSlow;

import java.math.BigInteger;
import java.util.Random;

public class PRG_Test {
	
	static BigInteger key = new BigInteger(128, new Random());
	static BigInteger iv = new BigInteger(128, new Random());
	static String m0 = "Deployment of STS can take different forms depending on communication requirements and the level of prior communication between parties. The data described in STS Setup may be shared prior to the beginning of a session to lessen the impact of the session's establishment.";
	static int iterations = 50000;
	static int mb = 100;
	static byte m1[] = new byte[1000*1000*mb]; //10mb
	
	public static void main(String args[]){
		
		testSmallManyPRG(new SosemanukFast());
		testSmallManyPRG(new SosemanukSlow());
		testSmallManyPRG(new HC128());
		//testSmallManyPRG(new HC256());
		testSmallManyPRG(new Salsa20());
		
		testOneLargePRG(new SosemanukFast());
		testOneLargePRG(new SosemanukSlow());
		testOneLargePRG(new HC128());
		//testOneLargePRG(new HC256());
	    testOneLargePRG(new Salsa20());
		
				
	}
	
	static void testSmallManyPRG(PRG prg0){
		byte c0[] = new byte[m0.getBytes().length];
	
		byte aws[] = null;
		
		long time = System.currentTimeMillis();
		
		for(int i = 0; i < iterations; i++){

			prg0 = prg0.getNewPRG();
			prg0.init(key.toByteArray(), iv.toByteArray());
			prg0.processBytes(m0.getBytes(), 0, m0.getBytes().length, c0, 0);
			
			
			prg0 = prg0.getNewPRG();
			prg0.init(key.toByteArray(), iv.toByteArray());
			prg0.processBytes(c0, 0, c0.length);
			
			aws = c0;
		}
		
		time = System.currentTimeMillis() - time;
		
		System.out.print(prg0.getAlgorithmName() + "\tSmall\t");
		System.out.println(8*2*(double)((int)((double)iterations*(double)c0.length/(double)time)/(double)1000) + "mbit/s\t\t" + new String(aws));
		
	}
	
	static void testOneLargePRG(PRG prg0){
		byte c1[] = new byte[m1.length];
		long time = System.currentTimeMillis();
		
		//Encrypt
		prg0 = prg0.getNewPRG();
		prg0.init(key.toByteArray(), iv.toByteArray());
		prg0.processBytes(m1, 0, m1.length, c1, 0);
		
		//Decrypt
		byte m1c[] = new byte[c1.length];
		
		prg0 = prg0.getNewPRG();
		prg0.init(key.toByteArray(), iv.toByteArray());
		prg0.processBytes(c1, 0, c1.length, m1c, 0);
		
		time = System.currentTimeMillis() - time;
		
		System.out.print(prg0.getAlgorithmName() + "\tLarge\t");
		System.out.println(8*2*mb*1000/time + "mbit/s");
	
	}
	
	
	
	
	
}

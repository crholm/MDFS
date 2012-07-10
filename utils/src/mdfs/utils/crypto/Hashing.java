package mdfs.utils.crypto;


import java.io.UnsupportedEncodingException; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 

/**
 * A simple implementation allowing hashing if String to MD5 or SHA-1
 * @author Rasmus Holm
 *
 */
public class Hashing {

	private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    }
	
	/**
	 * Hashes a String with {@link HashTypeEnum}
	 * @param hash the type of hash function to be used
	 * @param string the string that is to be hashed
	 * @return hashed String 
	 */
	public static String hash(HashTypeEnum hash, String string){
		MessageDigest md = null;
		byte[] hashed;
		try {
			switch(hash){
			case SHA1:
				md = MessageDigest.getInstance("SHA-1");
			    hashed = new byte[40];
				break;
			case MD5:
				md = MessageDigest.getInstance("MD5");
				hashed = new byte[32];
				break;
			

			default:
				return null;
			}
			
			md.update(string.getBytes("UTF8"), 0, string.length());
			hashed = md.digest();
		    return convertToHex(hashed);
			
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}

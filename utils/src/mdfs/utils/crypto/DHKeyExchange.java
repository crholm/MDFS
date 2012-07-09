package mdfs.utils.crypto;

import mdfs.utils.crypto.utils.Prims;

import java.math.BigInteger;
import java.util.Random;

/**
 * Package: mdfs.utils.crypto
 * Created: 2012-07-09
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class DHKeyExchange {
    private BigInteger s;

    private BigInteger p;
    private BigInteger a;
    private BigInteger A;
    private BigInteger B;

    private static final byte two[] = {2};

    private BigInteger g = new BigInteger(two);


    public void createPublicPrime(int keyLength){
        this.p = Prims.getPrime(keyLength);
    }
    public void setPublicPrime(BigInteger p){
        this.p = p;
    }
    public BigInteger getPublicPrime(){
        return p;
    }


    public BigInteger getPublicBase(){
        return g;
    }
    public void setPublicBase(BigInteger g){
        this.g = g;
    }



    public void createPrivateKey(int keyLength){
        this.a = new BigInteger(keyLength, new Random());
    }
    public void setPrivateKey(BigInteger a){
        this.a = a;
    }
    public BigInteger getPrivateKey(){
        return a;
    }


    public boolean createPublicKey(){
        if(g == null || a == null || p == null)
            return false;

        A = g.modPow(a, p);
        return true;
    }
    public BigInteger getPublicKey(){
        return A;
    }




    public void setForeignPublicKey(BigInteger publicKey){
        this.B = publicKey;
    }
    public BigInteger getForeignPublicKey(){
        return B;
    }




    public boolean createSharedSecretKey(){
        if(B == null || a == null || p == null)
            return false;

        this.s = B.modPow(a, p);
        return true;
    }
    public BigInteger getSharedSecretKey(){
        return s;
    }


}

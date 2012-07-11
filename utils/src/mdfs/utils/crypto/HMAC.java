package mdfs.utils.crypto;

/**
 * Package: mdfs.utils.crypto
 * Created: 2012-07-11
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class HMAC{

    private boolean init = false;
    private byte key[];
    private byte keyBlock[];
    private MessageDigest md;
    private int blocksize;
    byte oKeyPad[];
    byte iKeyPad[];


    public HMAC(){}
    public HMAC(MessageDigest md, byte key[]){
        init(md, key);
    }

    public void init(MessageDigest md, byte key[]){
        if(this.init){
            reset();
            return;
        }
        this.md = md;
        this.key = key;

        this.blocksize = md.getByteLength();
        reset();
        this.init = true;
    }


    public void reset(){
        md.reset();


        oKeyPad = new byte[blocksize];
        iKeyPad = new byte[blocksize];

        keyBlock = key;
        if(keyBlock.length > blocksize){
            keyBlock = new byte[md.getDigestSize()];
            md.doFinal(key, keyBlock, 0);
        }

        oKeyPad = new byte[blocksize];
        iKeyPad = new byte[blocksize];

        for(int i = 0; i < keyBlock.length; i++){
            oKeyPad[i] = (byte)(0x5c ^ keyBlock[i]);
            iKeyPad[i] = (byte)(0x36 ^ keyBlock[i]);
        }
        for(int i = 0; i < blocksize - keyBlock.length; i++){
            oKeyPad[i+keyBlock.length] = 0x5c;
            iKeyPad[i+keyBlock.length] = 0x36;
        }

        update(iKeyPad);
    }

    public void update(byte in){
        md.update(in);
    }
    public void update(byte[] in){
        md.update(in, 0, in.length);
    }
    public void update(byte[] in, int inOff, int len){
        md.update(in, inOff, len);
    }


    public int doFinal(byte out[], int off){

        int i = md.doFinal(out, off);

        md.update(oKeyPad, 0, blocksize);
        md.update(out, off, i);

        i = md.doFinal(out, off);
        reset();
        return i;
    }

    public int doFinal(byte in[], byte out[], int outOff){
        update(in);
        return doFinal(out, outOff);
    }

    public int getDigestSize()
    {
        return md.getDigestSize();
    }



    public byte[] special(byte keyBlock1[], byte msgBlock[]){
        int blocksize = 64;
        md.reset();

        if(keyBlock1.length > blocksize){
                md.update(keyBlock1, 0, keyBlock1.length);
                byte[] tmp = new byte[md.getDigestSize()];
                md.doFinal(tmp, 0);
                keyBlock1 = tmp;
        }

        byte oKeyPad[] = new byte[blocksize];
        byte iKeyPad[] = new byte[blocksize];

        for(int i = 0; i < keyBlock1.length; i++){
            oKeyPad[i] = (byte)(0x5c ^ keyBlock1[i]);
            iKeyPad[i] = (byte)(0x36 ^ keyBlock1[i]);
        }
        for(int i = 0; i < blocksize - keyBlock1.length; i++){
            oKeyPad[i+keyBlock1.length] = 0x5c;
            iKeyPad[i+keyBlock1.length] = 0x36;
        }

        byte[] innerblock = new byte[md.getDigestSize()];

        md.update(iKeyPad,0,iKeyPad.length);
        md.update(msgBlock,0,msgBlock.length);
        int i = md.doFinal(innerblock, 0);

        byte[] outerblock = new byte[md.getDigestSize()];


        md.update(oKeyPad,0,oKeyPad.length);
        md.update(innerblock,0,i);
        i = md.doFinal(outerblock, 0);



        return outerblock;
    }

}

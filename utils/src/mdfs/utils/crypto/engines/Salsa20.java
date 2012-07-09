package mdfs.utils.crypto.engines;

import mdfs.utils.crypto.PRG;

public class Salsa20 implements PRG {
    /** Constants */
    private final static int STATE_SIZE = 32; // 16, 32 bit ints = 64 bytes

    public static byte[] toByteArray(String string)
    {
        byte[] bytes = new byte[string.length()];

        for (int i = 0; i != bytes.length; i++)
        {
            char ch = string.charAt(i);

            bytes[i] = (byte)ch;
        }

        return bytes;
    }
    
    private final static byte[]
        sigma = toByteArray("expand 32-byte k"),
        tau   = toByteArray("expand 16-byte k");

    /*
     * variables to hold the state of the engine
     * during encryption and decryption
     */
    private int         index = 0;
    private int[]       engineState = new int[STATE_SIZE]; // state
    private int[]       x = new int[STATE_SIZE] ; // internal buffer
    private byte[]      keyStream   = new byte[STATE_SIZE * 4], // expanded state, 64 bytes
                        workingKey  = null,
                        workingIV   = null;
    private boolean     initialised = false;

    /*
     * internal counter
     */
    private int cW0, cW1, cW2;

    /**
     * initialise a Salsa20 cipher.
     *
     *
     */
    public void init(byte[] key, byte[] iv)
    {
        /* 
        * Salsa20 encryption and decryption is completely
        * symmetrical, so the 'forEncryption' is 
        * irrelevant. (Like 90% of stream ciphers)
        */

        if (iv == null){
            iv = new byte[8];
        }else if(iv.length != 8){
        	byte b[] = new byte[8];
        	int i = iv.length < 8 ? iv.length : 8;
        	
        	System.arraycopy(iv, 0, b, 0, i);
        }

        

       

        workingKey = key;
        workingIV = iv;

        setKey(workingKey, workingIV);
    }
    @Override
    public int getIVSize() {
        return 8;
    }

    @Override
    public int getKeySize() {
        return 32;
    }

    public String getAlgorithmName()
    {
        return "Salsa20";
    }

    public byte processByte(byte in)
    {
        
        return (byte)(getByte()^in);
    }

    @Override
	public byte getByte() {
    	if (limitExceeded())
        {
            throw new IndexOutOfBoundsException("2^70 byte limit per IV; Change IV");
        }

        if (index == 0)
        {
			generateKeyStream(keyStream);

            if (++engineState[8] == 0)
            {
                ++engineState[9];
            }
        }

        byte out = (byte)(keyStream[index]);
        index = (index + 1) & 63;

        return out;
	}





    @Override
    public void processBytes(byte[] buf, int off, int len) {

        if (!initialised)
        {
            throw new IllegalStateException(getAlgorithmName()+" not initialised");
        }

        if ((off + len) > buf.length)
        {
            throw new IndexOutOfBoundsException("input buffer too short");
        }


        if (limitExceeded(len))
        {
            throw new IndexOutOfBoundsException("2^70 byte limit per IV would be exceeded; Change IV");
        }

        for (int i = 0; i < len; i++)
        {
            if (index == 0)
            {
                generateKeyStream(keyStream);

                if (++engineState[8] == 0)
                {
                    ++engineState[9];
                }
            }

            buf[i+off] = (byte)(keyStream[index]^buf[i+off]);
            index = (index + 1) & 63;
        }
    }

    public void processBytes(
        byte[]     in, 
        int     inOff, 
        int     len, 
        byte[]     out, 
        int     outOff)
    {
        if (!initialised)
        {
            throw new IllegalStateException(getAlgorithmName()+" not initialised");
        }

        if ((inOff + len) > in.length)
        {
            throw new IndexOutOfBoundsException("input buffer too short");
        }

        if ((outOff + len) > out.length)
        {
            throw new IndexOutOfBoundsException("output buffer too short");
        }

        if (limitExceeded(len))
        {
            throw new IndexOutOfBoundsException("2^70 byte limit per IV would be exceeded; Change IV");
        }

        for (int i = 0; i < len; i++)
        {
            if (index == 0)
            {
				generateKeyStream(keyStream);

                if (++engineState[8] == 0)
                {
                    ++engineState[9];
                }
            }

            out[i+outOff] = (byte)(keyStream[index]^in[i+inOff]);
            index = (index + 1) & 63;
        }
    }

    public void reset()
    {
        setKey(workingKey, workingIV);
    }

    private int littleEndianToInt(byte[] bs, int off){
        int n = bs[  off] & 0xff;
        n |= (bs[++off] & 0xff) << 8;
        n |= (bs[++off] & 0xff) << 16;
        n |= bs[++off] << 24;
        return n;
    }
    
    private void intToLittleEndian(int n, byte[] bs, int off)
    {
        bs[  off] = (byte)(n       );
        bs[++off] = (byte)(n >>>  8);
        bs[++off] = (byte)(n >>> 16);
        bs[++off] = (byte)(n >>> 24);
    }

	private void intToLittleEndian(int[] ns, byte[] bs, int off)
	{
		for (int i = 0; i < ns.length; ++i)
		{
			intToLittleEndian(ns[i], bs, off);
			off += 4;
		}
	}
    
    // Private implementation

    private void setKey(byte[] keyBytes, byte[] ivBytes)
    {
        workingKey = keyBytes;
        workingIV  = ivBytes;

        index = 0;
        resetCounter();
        int offset = 0;
        byte[] constants;

        // Key
        engineState[1] = littleEndianToInt(workingKey, 0);
        engineState[2] = littleEndianToInt(workingKey, 4);
        engineState[3] = littleEndianToInt(workingKey, 8);
        engineState[4] = littleEndianToInt(workingKey, 12);

        if (workingKey.length == 32)
        {
            constants = sigma;
            offset = 16;
        }
        else
        {
            constants = tau;
        }

        engineState[11] = littleEndianToInt(workingKey, offset);
        engineState[12] = littleEndianToInt(workingKey, offset+4);
        engineState[13] = littleEndianToInt(workingKey, offset+8);
        engineState[14] = littleEndianToInt(workingKey, offset+12);
        engineState[0 ] = littleEndianToInt(constants, 0);
        engineState[5 ] = littleEndianToInt(constants, 4);
        engineState[10] = littleEndianToInt(constants, 8);
        engineState[15] = littleEndianToInt(constants, 12);

        // IV
        engineState[6] = littleEndianToInt(workingIV, 0);
        engineState[7] = littleEndianToInt(workingIV, 4);
        engineState[8] = engineState[9] = 0;

        initialised = true;
    }

	private void generateKeyStream(byte[] output)
	{
		salsaCore(20, engineState, x);
		intToLittleEndian(x, output, 0);
	}

    /**
     * Salsa20 function
     *
     * @param   input   input data
     *
     * @return  keystream
     */    
    public static void salsaCore(int rounds, int[] input, int[] x)
    {
        System.arraycopy(input, 0, x, 0, input.length);

		for (int i = rounds; i > 0; i -= 2)
        {
            x[ 4] ^= rotl((x[ 0]+x[12]), 7);
            x[ 8] ^= rotl((x[ 4]+x[ 0]), 9);
            x[12] ^= rotl((x[ 8]+x[ 4]),13);
            x[ 0] ^= rotl((x[12]+x[ 8]),18);
            x[ 9] ^= rotl((x[ 5]+x[ 1]), 7);
            x[13] ^= rotl((x[ 9]+x[ 5]), 9);
            x[ 1] ^= rotl((x[13]+x[ 9]),13);
            x[ 5] ^= rotl((x[ 1]+x[13]),18);
            x[14] ^= rotl((x[10]+x[ 6]), 7);
            x[ 2] ^= rotl((x[14]+x[10]), 9);
            x[ 6] ^= rotl((x[ 2]+x[14]),13);
            x[10] ^= rotl((x[ 6]+x[ 2]),18);
            x[ 3] ^= rotl((x[15]+x[11]), 7);
            x[ 7] ^= rotl((x[ 3]+x[15]), 9);
            x[11] ^= rotl((x[ 7]+x[ 3]),13);
            x[15] ^= rotl((x[11]+x[ 7]),18);
            x[ 1] ^= rotl((x[ 0]+x[ 3]), 7);
            x[ 2] ^= rotl((x[ 1]+x[ 0]), 9);
            x[ 3] ^= rotl((x[ 2]+x[ 1]),13);
            x[ 0] ^= rotl((x[ 3]+x[ 2]),18);
            x[ 6] ^= rotl((x[ 5]+x[ 4]), 7);
            x[ 7] ^= rotl((x[ 6]+x[ 5]), 9);
            x[ 4] ^= rotl((x[ 7]+x[ 6]),13);
            x[ 5] ^= rotl((x[ 4]+x[ 7]),18);
            x[11] ^= rotl((x[10]+x[ 9]), 7);
            x[ 8] ^= rotl((x[11]+x[10]), 9);
            x[ 9] ^= rotl((x[ 8]+x[11]),13);
            x[10] ^= rotl((x[ 9]+x[ 8]),18);
            x[12] ^= rotl((x[15]+x[14]), 7);
            x[13] ^= rotl((x[12]+x[15]), 9);
            x[14] ^= rotl((x[13]+x[12]),13);
            x[15] ^= rotl((x[14]+x[13]),18);
        }

		for (int i = 0; i < STATE_SIZE; ++i)
		{
			x[i] += input[i];
		}
    }

    /**
     * Rotate left
     *
     * @param   x   value to rotate
     * @param   y   amount to rotate x
     *
     * @return  rotated x
     */
    private static int rotl(int x, int y)
    {
        return (x << y) | (x >>> -y);
    }

    private void resetCounter()
    {
        cW0 = 0;
        cW1 = 0;
        cW2 = 0;
    }

    private boolean limitExceeded()
    {
        if (++cW0 == 0)
        {
            if (++cW1 == 0)
            {
                return (++cW2 & 0x20) != 0;          // 2^(32 + 32 + 6)
            }
        }

        return false;
    }

    /*
     * this relies on the fact len will always be positive.
     */
    private boolean limitExceeded(int len)
    {
		cW0 += len;
		if (cW0 < len && cW0 >= 0)
        {
            if (++cW1 == 0)
            {
                return (++cW2 & 0x20) != 0;          // 2^(32 + 32 + 6)
            }
        }

        return false;
    }

	

	@Override
	public PRG getNewPRG() {
		return new Salsa20();
	}
}

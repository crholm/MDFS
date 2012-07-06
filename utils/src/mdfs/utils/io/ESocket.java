package mdfs.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Package: mdfs.utils.io
 * Created: 2012-07-06
 * Encrypted Socket with encrypted Key exchange.
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ESocket extends Socket {
    private BigInteger p;

    private BigInteger a;
    private BigInteger b;

    private BigInteger A;
    private BigInteger B;

    private BigInteger s;

    private PRG inPRG;
    private PRG outPRG;

    private EInputStream inputStream;
    private EOutputStream outputStream;



    private static final byte two[] = {2};
    private static final BigInteger g = new BigInteger(two);


    protected ESocket() {
        super();
    }

    public ESocket(String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        wait4Handshake();
    }

    public ESocket(InetAddress address, int port) throws IOException {
        super(address, port);
        wait4Handshake();
    }

    public ESocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
        wait4Handshake();
    }

    public ESocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        super(address, port, localAddr, localPort);
        wait4Handshake();
    }


    @Override
    public InputStream getInputStream() throws IOException {
        if(inputStream == null)
           throw new IOException();
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if(outputStream == null)
            throw new IOException();
        return outputStream;
    }







    // Initiates a DH key exchange
    protected void initHandshake() throws IOException {
        p = Prims.get1024prim();

        a = new BigInteger(350, new Random());
        A = g.modPow(a, p);

        InputStream in = super.getInputStream();
        OutputStream out = super.getOutputStream();

        byte pArray[] = p.toByteArray();
        byte pLen[] = intToByteArray(pArray.length);

        byte AArray[] = A.toByteArray();
        byte ALen[] = intToByteArray(AArray.length);


        out.write(pLen);
        out.write(pArray);
        out.write(ALen);
        out.write(AArray);
        out.flush();


        byte BLen[] = new byte[4];
        in.read(BLen);

        byte BArray[] = new byte[byteArrayToInt(BLen)];
        in.read(BArray);

        B = new BigInteger(BArray);

        s = B.modPow(a, p);

        initPRG();

        inputStream = new EInputStream(in, inPRG);
        outputStream = new EOutputStream(out, outPRG);


    }

    // Waits for ServerSocket to Initiate a DH key exchange
    private void wait4Handshake() throws IOException {
        b = new BigInteger(350, new Random());

        InputStream in = super.getInputStream();
        OutputStream out = super.getOutputStream();

        byte pLen[] = new byte[4];
        in.read(pLen);

        byte pArray[] = new byte[byteArrayToInt(pLen)];
        in.read(pArray);

        byte ALen[] = new byte[4];
        in.read(ALen);

        byte AArray[] = new byte[byteArrayToInt(ALen)];
        in.read(AArray);

        p = new BigInteger(pArray);

        B = g.modPow(b, p);

        byte BArray[] = B.toByteArray();
        byte BLen[] = intToByteArray(BArray.length);

        out.write(BLen);
        out.write(BArray);
        out.flush();

        A = new BigInteger(AArray);

        s = A.modPow(b, p);

        initPRG();

        //Opposite PRG on client side then Server side due that one shoule corespond to one specific stream
        inputStream = new EInputStream(in, outPRG);
        outputStream = new EOutputStream(out, inPRG);

    }


    //Splits the shared secret s into to two separate secrets for corresponding streams
    private void initPRG(){

        byte sArray[] = s.toByteArray();
        inPRG = new PRG();
        byte inKey[] = new byte[32];
        byte inIV[] = new byte[16];

        outPRG = new PRG();
        byte outKey[] = new byte[32];
        byte outIV[] = new byte[16];

        for(int i = 0; i < 32 && i < sArray.length/2; i++){
            inKey[i] = sArray[i*2];
            outKey[i] = sArray[i*2+1];
            inIV[i%16] = outKey[i];
            outIV[i%16] = inKey[i];
        }

        inPRG.setKey(inKey);
        inPRG.setIV(inIV);
        outPRG.setKey(outKey);
        outPRG.setIV(outIV);
    }


    public static byte[] intToByteArray(int l){
        byte[] a = new byte[4];
        for(int i = 0; i<4; i++){
            a[3 - i] = (byte)(l >>> (i * 8));
        }
        return a;
    }

    public static int byteArrayToInt(byte[] b){
        int intNum = 0;
        for(int i =0; i < 4; i++){
            intNum <<= 8;
            intNum ^= (int)b[i] & 0xFF;
        }
        return intNum;
    }


}

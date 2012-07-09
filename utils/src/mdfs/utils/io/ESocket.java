package mdfs.utils.io;

import mdfs.utils.crypto.DHKeyExchange;
import mdfs.utils.crypto.PRG;
import mdfs.utils.crypto.engines.Salsa20;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Package: mdfs.utils.io
 * Created: 2012-07-06
 * Encrypted Socket with encrypted Key exchange.
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ESocket extends Socket {
    private byte[] s;

    private DHKeyExchange dh = new DHKeyExchange();
    private PRG inPRG = new Salsa20();
    private PRG outPRG = new Salsa20();
    private int ivLen = inPRG.getIVSize();
    private int keyLen = inPRG.getKeySize();

    private EInputStream inputStream;
    private EOutputStream outputStream;


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
        if(inputStream == null || super.isInputShutdown())
           throw new IOException();
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if(outputStream == null || super.isOutputShutdown())
            throw new IOException();
        return outputStream;
    }







    // Initiates a DH key exchange
    protected void initHandshake(int keyLength) throws IOException {

        dh.createPublicPrime(keyLength);
        dh.createPrivateKey(256);
        dh.createPublicKey();

        InputStream in = super.getInputStream();
        OutputStream out = super.getOutputStream();

        byte pArray[] = dh.getPublicPrime().toByteArray();
        byte pLen[] = intToByteArray(pArray.length);

        byte AArray[] = dh.getPublicKey().toByteArray();
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

        dh.setForeignPublicKey(new BigInteger(BArray));
        dh.createSharedSecretKey();

        s = dh.getSharedSecretKey().toByteArray();

        initPRG();

        inputStream = new EInputStream(in, inPRG);
        outputStream = new EOutputStream(out, outPRG);


    }

    // Waits for ServerSocket to Initiate a DH key exchange
    private void wait4Handshake() throws IOException {

        dh.createPrivateKey(256);

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

        dh.setPublicPrime(new BigInteger(pArray));
        dh.createPublicKey();

        byte BArray[] = dh.getPublicKey().toByteArray();
        byte BLen[] = intToByteArray(BArray.length);

        out.write(BLen);
        out.write(BArray);
        out.flush();


        dh.setForeignPublicKey(new BigInteger(AArray));
        dh.createSharedSecretKey();

        s = dh.getSharedSecretKey().toByteArray();

        initPRG();

        //Opposite PRG on client side then Server side due that one shoule corespond to one specific stream
        inputStream = new EInputStream(in, outPRG);
        outputStream = new EOutputStream(out, inPRG);

    }


    //Splits the shared secret s into to two separate secrets for corresponding streams
    private void initPRG(){

        byte inKey[] = new byte[keyLen];
        byte inIV[] = new byte[ivLen];

        byte outKey[] = new byte[keyLen];
        byte outIV[] = new byte[ivLen];

        for(int i = 0; i < ivLen && i < s.length/2; i++){
            inKey[i] = s[i*2];
            outKey[i] = s[i*2+1];

            if(i < ivLen){
                inIV[i] = s[s.length-1-i*2];
                outIV[i] = s[s.length-2-i*2];
            }
        }

        inPRG.init(inKey, inIV);
        outPRG.init(outKey, outIV);
    }


    public byte[] intToByteArray(int l){
        byte[] a = new byte[4];
        for(int i = 0; i<4; i++){
            a[3 - i] = (byte)(l >>> (i * 8));
        }
        return a;
    }

    public int byteArrayToInt(byte[] b){
        int intNum = 0;
        for(int i =0; i < 4; i++){
            intNum <<= 8;
            intNum ^= (int)b[i] & 0xFF;
        }
        return intNum;
    }


}

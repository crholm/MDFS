package mdfs.utils.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Package: mdfs.utils.io
 * Created: 2012-07-06
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class EServerSocket extends ServerSocket {

    private int publicKeyLength = 768;
    private int privateKeyLength = 256;

    public EServerSocket() throws IOException {
        super();
    }

    public EServerSocket(int port) throws IOException {
        super(port);
    }

    public EServerSocket(int port, int publicKeyLength, int privateKeyLength) throws IOException {
        super(port);
        this.privateKeyLength = privateKeyLength;
        this.publicKeyLength = publicKeyLength;
    }

    public EServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    public EServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        super(port, backlog, bindAddr);
    }


    public int getPublicKeyLength() {
        return publicKeyLength;
    }

    public void setPublicKeyLength(int keyLenght) {
        this.publicKeyLength = keyLenght;
    }

    @Override
    public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");

        ESocket esocket = new ESocket(privateKeyLength);
        implAccept(esocket);

        esocket.initHandshake(publicKeyLength);

        return esocket;
    }

}

package mdfs.client.api.io;

import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSErrorCode;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Package: mdfs.client.api.io
 * Created: 2012-06-21
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSFileInputStream extends InputStream {

    private MDFSFile file;

    private InputStream in;
    private Socket socket;

    private long length;
    private long readBytes = 0;


    public MDFSFileInputStream(MDFSFile file) throws IOException {
        this.file = file;

        //Checks so that it is a file that is trying to be retrieved
        if(!file.exists() || file.isDirectory())
            throw new FileNotFoundException(MDFSErrorCode.info[MDFSErrorCode.ENOENT]);


        //Fetching all DataNodes hosting file;
        int size = file.getMetadata().getLocation().getHostsSize();

        if(size == 0)
            throw new FileNotFoundException(MDFSErrorCode.info[MDFSErrorCode.ENOENT]);

        String hosts[] = new String[size];
        hosts = file.getMetadata().getLocation().getHostsArray(hosts);


        //Connecting to DataNode
        SocketFactory socketFactory = new SocketFactory();
        socket = socketFactory.createSocket(hosts);
        OutputStream out = socket.getOutputStream();
        in = socket.getInputStream();


        SocketFunctions socketFunctions = new SocketFunctions();

        //Creating request to DataNode
        MDFSProtocolHeader request = new MDFSProtocolHeader();
        request.setStage(Stage.REQUEST);
        request.setType(Type.FILE);
        request.setMode(Mode.READ);
        request.setMetadata(file.getMetadata());
        request.setInfo(file.getInfo());

        //Sending Request
        if(!socketFunctions.sendText(socket, request.toString()))
            throw new IOException(MDFSErrorCode.info[MDFSErrorCode.EIO] + ", sending request failed");

        out.close();

        //Retriving Response header
        String responseString = socketFunctions.receiveText(in);
        if(responseString == null)
            throw new IOException(MDFSErrorCode.info[MDFSErrorCode.EIO] + ", reading response failed");


        //TODO Implement check that file requested is file in inputstream and no errors.
        try {
            MDFSProtocolHeader response = new MDFSProtocolHeader(responseString);
        } catch (JSONException e) {
            throw new IOException(MDFSErrorCode.info[MDFSErrorCode.EIO] + ", parsing response header was not in JSON format");
        }

        length = socketFunctions.prepareFileInputStream(in);


    }
    public MDFSFileInputStream(String path, String user, String pass) throws  IOException{
        this(new MDFSFile(path, user, pass));
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        if(length-readBytes <= 0)
            return -1;

        int i = in.read(b, off, len);

        if(i > 0)
            readBytes += i;

        return i;
    }


    @Override
    public int read(byte[] b) throws IOException {

        if(length-readBytes <= 0)
            return -1;

        int i = in.read(b);

        if(i > 0)
            readBytes += i;

        return i;
    }

    @Override
    public int read() throws IOException {

        //Might not be a good ide... (one if per byte..)
        if(length-readBytes <= 0)
            return -1;

        int i = in.read();

        if(i != -1)
            readBytes++;

        return i;
    }

    @Override
    public void close() throws IOException{
        in.close();
        socket.close();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }
}

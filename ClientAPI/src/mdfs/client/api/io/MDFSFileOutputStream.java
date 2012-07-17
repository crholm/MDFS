package mdfs.client.api.io;

import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSErrorCode;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;

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
public class MDFSFileOutputStream extends OutputStream {
    MDFSFile file;
    OutputStream out;
    String response;
    Socket socket;
    SocketFunctions socketFunctions;


    public MDFSFileOutputStream(MDFSFile file) throws IOException {
        this.file = file;

        //Checks so that it is a file that is trying to be retrieved
        if(!file.exists() || file.isDirectory())
            throw new FileNotFoundException(MDFSErrorCode.info[MDFSErrorCode.ENOENT]);


        //Fetching all DataNodes hosting file;
        int size;
        String hosts[];
        //If file already are written once to MDFS.
        if(file.getMetadata().getLocation().getHosts() == null){
            size = file.getInfo().getDatanodesSize();
            hosts = new String[size];
            hosts = file.getInfo().getDatanodesArray(hosts);

        //If it is a new file.
        }else{
            size = file.getMetadata().getLocation().getHostsSize();
            hosts = new String[size];
            hosts = file.getMetadata().getLocation().getHostsArray(hosts);
        }

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        request.setStage(Stage.REQUEST);
        request.setType(Type.FILE);
        request.setMode(Mode.WRITESTREAM);
        request.setMetadata(file.getMetadata());
        request.setInfo(file.getInfo());
        //request.getMetadata().setSize(-1);    //not for now

        SocketFactory socketFactory = new SocketFactory();
        socketFunctions = new SocketFunctions();

        socket = socketFactory.createSocket(hosts);
        out = socket.getOutputStream();

        socketFunctions.sendText(out, request.toString());

    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException{
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException{
        out.write(b);
    }

    @Override
    public void write(int i) throws IOException {
        out.write(i);
    }

    @Override
    public void close() throws IOException{
        out.close();

        InputStream in = socket.getInputStream();

        response = socketFunctions.receiveText(in);
        in.close();

        socket.close();

    }

    @Override
    public void flush() throws IOException{
        out.flush();
    }
}

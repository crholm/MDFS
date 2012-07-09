package mdfs.utils.io;

import mdfs.utils.crypto.PRG;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Package: mdfs.utils.io
 * Created: 2012-07-06
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class EOutputStream extends OutputStream {

    OutputStream out;
    PRG prg;

    protected EOutputStream(OutputStream out, PRG prg){
        this.out = out;
        this.prg = prg;
    }

    @Override
    public void write(int i) throws IOException {
        byte b = prg.processByte((byte)i);
        out.write(b);
    }
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if(len+off > b.length)
            throw new IOException();

        byte stream[] = new byte[len];
        prg.processBytes(b, off, len, stream, 0);

        out.write(stream, 0, len);

    }
    @Override
    public void flush() throws IOException {
        out.flush();
    }
    @Override
    public void close() throws IOException {
        out.close();
    }
}

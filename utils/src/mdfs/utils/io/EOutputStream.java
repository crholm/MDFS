package mdfs.utils.io;

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
        byte s[] = new byte[1];
        prg.makeStream(s, 0, 1);
        out.write(i^s[0]);
    }
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte stream[] = new byte[len];
        prg.makeStream(stream, 0, len);
        for(int i = 0; i < len; i++){
            stream[i] ^= b[i+off];
        }
        out.write(stream);

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

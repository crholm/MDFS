package mdfs.utils.io;

import mdfs.utils.crypto.PRG;

import java.io.IOException;
import java.io.InputStream;

/**
 * Package: mdfs.utils.io.protocol
 * Created: 2012-07-06
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class EInputStream extends InputStream {

    InputStream in;
    PRG prg;

    protected EInputStream(InputStream in, PRG prg){
        this.in = in;
        this.prg = prg;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        return prg.processByte((byte)b);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = in.read(b, off, len);

        prg.processBytes(b, off, i);

        return i;
    }

    @Override
    public boolean markSupported(){
        return false;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public long skip(long n) throws IOException {
        long i = in.skip(n);

        long j = i;

        byte buf[] = new byte[2048];

        while(j-2048 > 0){
            prg.processBytes(buf, 0, 2048);
            j -= 2048;
        }
        prg.processBytes(buf, 0, (int)j);

        return i;
    }

}

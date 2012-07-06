package mdfs.utils.io;

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
        byte s[] = new byte[1];
        prg.makeStream(s, 0, 1);
        return b ^ s[0];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = in.read(b, off, len);

        if(i > 0){
            byte stream[] = new byte[i];
            prg.makeStream(stream, 0, i);
            for(int j = 0; j < i; j++)
                b[j+off] ^= stream[j];
        }

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
            prg.makeStream(buf, 0, 2048);
            j -= 2048;
        }
        prg.makeStream(buf, 0, (int)j);

        return i;
    }

}

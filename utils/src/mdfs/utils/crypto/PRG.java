package mdfs.utils.crypto;

public interface PRG {
	public String getAlgorithmName();
	public void init(byte[] key, byte[] iv);
	public byte getByte();
	public void processBytes(byte[] in, int inOff, int len, byte[] out, int outOff);
    public void processBytes(byte[] buf, int off, int len);
	public byte processByte(byte in);

    public PRG getNewPRG();
    public int getIVSize();
    public int getKeySize();

}

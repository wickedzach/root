package gavin.utilities;

import gavin.Util;

import java.io.IOException;
import java.io.InputStream;

public class RandomInputStream extends InputStream {
	private int total;
	private int count;

	public RandomInputStream(int total) {
		this.total = total;
		this.count = 0;
	}

	@Override
	public int read() throws IOException {
		if (count >= total)
			return -1;
		count++;
		return Util.RAND.nextInt();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (count >= total)
			return -1;
		byte[] tmp = new byte[Math.min(available(), len)];
		Util.RAND.nextBytes(tmp);
		System.arraycopy(tmp, 0, b, off, tmp.length);
		count += tmp.length;
		return tmp.length;
	}

	@Override
	public int available() throws IOException {
		return total - count;
	}

	@Override
	public synchronized void reset() throws IOException {
		count = 0;
	}
}
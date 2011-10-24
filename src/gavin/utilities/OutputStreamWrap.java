package gavin.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * None-Thread Safe
 * 
 * @author gavin
 */
public class OutputStreamWrap extends OutputStream {
	private OutputStream out;

	public OutputStreamWrap(OutputStream out) {
		// avoid wrap to much
		this.out = out instanceof OutputStreamWrap ? ((OutputStreamWrap) out).out : out;
	}

	public OutputStreamWrap(PrintStream out) {
		super();
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	/**
	 * make code shorter
	 * 
	 * @param b
	 * @return
	 * @throws IOException
	 * @see gavin.utilities.OutputStreamWrap#write(int)
	 */
	public OutputStreamWrap w(int b) throws IOException {
		out.write(b);
		return this;
	}

	/**
	 * make code shorter
	 * 
	 * @param b
	 * @return
	 * @throws IOException
	 * @see gavin.utilities.OutputStreamWrap#write(byte[])
	 */
	public OutputStreamWrap w(byte[] b) throws IOException {
		out.write(b);
		return this;
	}

	/**
	 * make code shorter
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see gavin.utilities.OutputStreamWrap#write(byte[], int, int)
	 */
	public OutputStreamWrap w(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		return this;
	}

	// sample
	public static void main(String[] args) throws IOException {
		OutputStreamWrap out = new OutputStreamWrap(System.out);
		out.w("Hello ".getBytes()).w("World!!".getBytes()).flush();
	}
}
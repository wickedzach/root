package gavin.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputStreamWrap extends OutputStream {
	private OutputStream out;

	public OutputStreamWrap(OutputStream out) {
		this.out = out instanceof OutputStreamWrap ? ((OutputStreamWrap) out).out : out;
	}

	public OutputStreamWrap(PrintStream out) {
		super();
		this.out = null;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	public OutputStreamWrap w(int b) throws IOException {
		out.write(b);
		return this;
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	public OutputStreamWrap w(byte[] b) throws IOException {
		out.write(b);
		return this;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	public OutputStreamWrap w(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		return this;
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
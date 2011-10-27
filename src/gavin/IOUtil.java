package gavin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtil {
	public static final OutputStream BLACK_HOLE = new OutputStream() {
		@Override
		public void write(int b) throws IOException {}

		@Override
		public void write(byte[] b) throws IOException {}

		@Override
		public void write(byte[] b, int o, int l) throws IOException {}
	};

	/**
	 * Use default buffer size 8192 to copy stream
	 * 
	 * @param input
	 *            source stream
	 * @param output
	 *            destination stream
	 * @throws IOException
	 * @see gavin.IOUtil#copy(InputStream, OutputStream, bufferSize)
	 */
	public final static void copy(InputStream input, OutputStream output) throws IOException {
		copy(input, output, 8192);
	}

	/**
	 * Copy stream with specified buffer size and close them automatically
	 * 
	 * @param input
	 * @param output
	 * @param bufferSize
	 * @throws IOException
	 */
	public final static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		int len;
		byte[] buffer = new byte[bufferSize];
		synchronized (input) {
			synchronized (output) {
				while ((len = input.read(buffer)) != -1) {
					output.write(buffer, 0, len);
				}
				output.flush();
				output.close();
			}
			input.close();
		}
	}

	public final static String read(File file) throws IOException {
		return read(file, Util.encoding());
	}

	public final static String read(File file, String encoding) throws IOException {
		return read(new FileInputStream(file), encoding);
	}

	public final static String read(InputStream input) throws IOException {
		return read(input, Util.encoding());
	}

	public final static String read(InputStream input, String encoding) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtil.copy(input, output);
		return new String(output.toByteArray(), encoding);
	}
}
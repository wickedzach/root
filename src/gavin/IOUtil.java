package gavin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
	public static void copy(InputStream input, OutputStream output) throws IOException {
		copy(input, output, 8192);
	}

	public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
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

	public static String read(File file) throws IOException {
		return read(file, Util.getEncoding());
	}

	public static String read(File file, String encoding) throws IOException {
		return read(new FileInputStream(file), encoding);
	}

	public static String read(InputStream input) throws IOException {
		return read(input, Util.getEncoding());
	}

	public static String read(InputStream input, String encoding) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtil.copy(input, output);
		return new String(output.toByteArray(), encoding);
	}
}
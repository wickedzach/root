package gavin.virtualbox;

import gavin.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.UUID;

public class Toolkit {
	private static final int SKIP = 0x188;

	public static UUID getUUID(File vdi) throws IOException {
		RandomAccessFile file = new RandomAccessFile(vdi, "r");
		file.skipBytes(SKIP);
		long msb = convert(file.readLong());
		long lsb = file.readLong();
		file.close();
		return new UUID(msb, lsb);
	}

	public static void setUUID(File vdi, UUID uuid) throws IOException {
		RandomAccessFile file = new RandomAccessFile(vdi, "rw");
		file.seek(SKIP);
		file.writeLong(convert(uuid.getMostSignificantBits()));
		file.seek(SKIP + 8);
		file.writeLong(uuid.getLeastSignificantBits());
		file.close();
	}

	private static long convert(long l) {
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (l >>> 8 * (7 - i));
		}
		// 3210-54-76
		return (long) ((long) (0xff & b[3]) << 56 | (long) (0xff & b[2]) << 48 | (long) (0xff & b[1]) << 40 | (long) (0xff & b[0]) << 32 | (long) (0xff & b[5]) << 24 | (long) (0xff & b[4]) << 16 | (long) (0xff & b[7]) << 8 | (long) (0xff & b[6]) << 0);
	}

	public static void clone(File src, File dst) throws IOException {
		UUID uuid = dst.exists() ? getUUID(dst) : UUID.randomUUID();
		InputStream input = new FileInputStream(src);
		OutputStream output = new FileOutputStream(dst);
		IOUtil.copy(input, output);
		input.close();
		output.flush();
		output.close();
		setUUID(dst, uuid);
	}
}
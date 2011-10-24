package gavin;

import gavin.utilities.RandomInputStream;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Util {
	private static char[] hex = "0123456789ABCDEF".toCharArray();

	public static String getEncoding() {
		return Charset.defaultCharset().name();
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() > 0;
	}

	public static String absoluteURL(String targetURL, String relativeURL) {
		if (relativeURL.startsWith("/")) {
			return targetURL.replaceAll("^([a-zA-Z]{3,4}://[^/]+)/.*$", "$1" + relativeURL);
		} else if (relativeURL.startsWith("../")) {
			targetURL = targetURL.substring(0, targetURL.lastIndexOf('/'));
			String file = "/" + relativeURL.replaceAll("\\.\\./", "");
			while (relativeURL.startsWith("../")) {
				targetURL = targetURL.substring(0, targetURL.lastIndexOf('/'));
				relativeURL = relativeURL.replaceFirst("\\.\\./", "");
			}
			return targetURL + file;
		} else {
			return targetURL.substring(0, targetURL.lastIndexOf('/') + 1) + relativeURL;
		}
	}

	public static void show(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public static Enumeration<String> enumeration(String text, Pattern pattern) {
		return new MatcherEnumeration(pattern.matcher(text));
	}

	private static class MatcherEnumeration implements Enumeration<String> {
		private Matcher matcher;

		public MatcherEnumeration(Matcher matcher) {
			this.matcher = matcher;
		}

		@Override
		public boolean hasMoreElements() {
			return matcher.find();
		}

		@Override
		public String nextElement() {
			return matcher.group(1);
		}
	}

	public static void transform(File srcImage, File detImage) throws IOException {
		String extention = FileUtil.getExtention(detImage).toLowerCase();
		Collection<String> extentions = Arrays.asList(ImageIO.getReaderFileSuffixes());
		if (extentions.contains(extention)) {
			ImageIO.write(ImageIO.read(srcImage), extention, detImage);
		} else {
			throw new RuntimeException("unknown format:" + extention);
		}
		extentions = null;
	}

	public static BufferedImage captureScreen() throws AWTException {
		Robot robot = new Robot();
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		return robot.createScreenCapture(screenRect);
	}

	public static String unicode(String string) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (char c : string.toCharArray()) {
			try {
				bos.write(unicode(c));
			} catch (IOException e) {
			}
		}
		return new String(bos.toByteArray());
	}

	public static byte[] unicode(char c) {
		if (c < Byte.MAX_VALUE)
			return new byte[] { (byte) c };
		byte[] bytes = { 92, 117, 0, 0, 0, 0 };
		bytes[2] = (byte) hex[c >> 12 & 0xf];
		bytes[3] = (byte) hex[c >> 8 & 0xf];
		bytes[4] = (byte) hex[c >> 4 & 0xf];
		bytes[5] = (byte) hex[c & 0xf];
		return bytes;
	}

	public static void hex(InputStream in, OutputStream out) throws IOException {
		int len;
		byte[] buf = new byte[8192];
		PrintStream w = new PrintStream(out);
		while ((len = in.read(buf)) != -1) {
			for (int i = 0; i < len; i++) {
				w.append(hex[buf[i] >> 4 & 0xf]).append(hex[buf[i] & 0xf]);
			}
		}
	}

	public static DecimalFormat getFormat(int number) {
		byte[] tmp = new byte[number];
		Arrays.fill(tmp, (byte) '0');
		return new DecimalFormat(new String(tmp));
	}

	public static void invoke(String method, Object... objects) {
		if (objects == null || objects.length == 0)
			return;
		for (Object object : objects) {
			if (object == null)
				return;
			try {
				object.getClass().getMethod(method).invoke(object);
			} catch (Throwable e) {
			}
		}
	}

	public static void close(Object... objects) {
		invoke("close", objects);
	}

	// sample
	public static void main(String[] args) throws IOException {
		InputStream input;
		//
		input = new FileInputStream(new File("src/gavin/Util.java"));
		hex(input, System.out.append("0x"));
		close(input);
		//
		System.out.println();
		//
		input = new RandomInputStream(16);
		for (int i = 0; i < 16; i++) {
			hex(input, System.out);
			System.out.println();
			input.reset();
		}
		close(input);
	}
}
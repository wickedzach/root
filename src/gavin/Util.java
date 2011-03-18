package gavin;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Util {
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

	public static void call(Collection<Callback> callbacks, Object... parameters) {
		Iterator<Callback> iterator = callbacks.iterator();
		while (iterator.hasNext()) {
			iterator.next().call(parameters);
		}
	}

	public static BufferedImage captureScreen() throws AWTException {
		Robot robot = new Robot();
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		return robot.createScreenCapture(screenRect);
	}

	public static void main(String[] args) throws IOException, AWTException {
		ImageIO.write(captureScreen(), "PNG", new File("C:\\Documents and Settings\\gavin\\桌面\\sss.png"));
	}
}
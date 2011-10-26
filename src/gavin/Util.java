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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * check less
 * 
 * @author gavin
 */
public class Util {
	public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	public static final Random RAND = new SecureRandom();
	public static final String LABEL_CLOSE = "close".intern();
	public static final String LABEL_UTF8 = "UTF-8".intern();
	public static final String LABEL_MD5 = "md5".intern();
	public static final String LABEL_SHA512 = "sha-512".intern();
	public static final String EMPTY = "".intern();

	public static String getEncoding() {
		return Charset.defaultCharset().name();
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
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
			} catch (IOException e) {}
		}
		return new String(bos.toByteArray());
	}

	public static byte[] unicode(char c) {
		if (c < Byte.MAX_VALUE)
			return new byte[] { (byte) c };
		byte[] bytes = { 92, 117, 0, 0, 0, 0 };
		bytes[2] = (byte) HEX[c >> 12 & 0xf];
		bytes[3] = (byte) HEX[c >> 8 & 0xf];
		bytes[4] = (byte) HEX[c >> 4 & 0xf];
		bytes[5] = (byte) HEX[c & 0xf];
		return bytes;
	}

	public static void hex(InputStream in, OutputStream out) throws IOException {
		int len;
		byte[] buf = new byte[8192];
		PrintStream w = new PrintStream(out);
		while ((len = in.read(buf)) != -1) {
			for (int i = 0; i < len; i++) {
				w.append(HEX[buf[i] >> 4 & 0xf]).append(HEX[buf[i] & 0xf]);
			}
		}
	}

	public static final char[] hex(byte[] bs) {
		char[] c = new char[bs.length << 1];
		for (int i = 0, j = 0; i < bs.length; i++) {
			c[j++] = HEX[bs[i] >>> 4 & 0xf];
			c[j++] = HEX[bs[i] & 0xf];
		}
		return c;
	}

	public static final byte[] unhex(char[] hex) {
		byte[] b = new byte[hex.length >>> 1];
		for (int i = 0, j = 0, m, n; i < b.length; i++) {
			m = hex[j++];
			n = hex[j++];
			b[i] = (byte) ((((m > 0x60 ? m - 87 : m - 48) << 4) | (n > 0x60 ? n - 87 : n - 48)) & 0xff);
		}
		return b;
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
			} catch (Throwable e) {}
		}
	}

	public static void close(Object... objects) {
		invoke(LABEL_CLOSE, objects);
	}

	public static final byte[] digest(String algorithm, String str) {
		if (isEmpty(str)) {
			return digest(algorithm, (byte[]) null);
		}
		try {
			return digest(algorithm, str.getBytes(LABEL_UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final byte[] digest(String algorithm, byte[] bs) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			if (bs == null) {
				return md.digest();
			}
			return md.digest(bs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final byte[] md5(String str) {
		return digest(LABEL_MD5, str);
	}

	public static final byte[] md5(byte[] bs) {
		return digest(LABEL_MD5, bs);
	}

	public static final byte[] sha512(String str) {
		return digest(LABEL_SHA512, str);
	}

	public static final byte[] sha512(byte[] bs) {
		return digest(LABEL_SHA512, bs);
	}

	public static final byte[] uuid() {
		byte[] b = new byte[16];
		RAND.nextBytes(b);
		b[6] &= 0x0f; /* clear version */
		b[6] |= 0x40; /* set to version 4 */
		b[8] &= 0x3f; /* clear variant */
		b[8] |= 0x80; /* set to IETF variant */
		return b;
	}

	private static Pattern normalizeP0 = Pattern.compile("%([a-f][0-9a-f]|[0-9a-f][a-f])");
	private static Pattern normalizeP1 = Pattern.compile("%(2[DE]|3[0-9]|[46][1-9]|5[0-9AF]|7[AE])");

	@SuppressWarnings("unchecked")
	public static String normalize(String url) throws MalformedURLException {
		URL u = new URL(url);
		StringBuilder s = new StringBuilder(url.length());
		// Converting the scheme and host to lower case
		String protocol = u.getProtocol();
		if (protocol.equals("https")) { // Limiting protocols
			protocol = "http";
		}
		s.append(protocol).append("://");
		//
		String userInfo = u.getUserInfo();
		if (!isEmpty(userInfo)) {
			s.append(userInfo).append('@');
		}
		// Converting the scheme and host to lower case
		String host = u.getHost();
		s.append(host.toLowerCase());
		// Removing the default port
		int port = u.getPort();
		if (port != -1 && port != u.getDefaultPort()) {
			s.append(':').append(port);
		}
		//
		String path = u.getPath();
		Matcher matcher;
		// Capitalizing letters in escape sequences
		if ((matcher = normalizeP0.matcher(path)).find()) {
			String group;
			Map<String, String> map = new HashMap<String, String>();
			map.put(group = matcher.group(), group.toUpperCase());
			while (matcher.find()) {
				map.put(group = matcher.group(), group.toUpperCase());
			}
			group = null;
			for (Entry<String, String> m : map.entrySet()) {
				path = path.replace(m.getKey(), m.getValue());
			}
			map = null;
		}
		// Decoding percent-encoded octets of unreserved characters
		if ((matcher = normalizeP1.matcher(path)).find()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(matcher.group(), String.valueOf((char) Integer.parseInt(matcher.group(1), 0x10)));
			while (matcher.find()) {
				map.put(matcher.group(), String.valueOf((char) Integer.parseInt(matcher.group(1), 0x10)));
			}
			for (Entry<String, String> m : map.entrySet()) {
				path = path.replace(m.getKey(), m.getValue());
			}
			map = null;
		}
		matcher = null;
		// Adding trailing
		if (isEmpty(path)) {
			path = "/";
		}
		// Removing dot-segments. Removing duplicate slashes
		path = path.replace("/./", "/").replaceAll("(/[^/]+)?/\\.\\./", "/").replaceAll("(/)/{1,999}", "$1");
		// XXX Removing directory index
		// String[][] def = { { "default", "index", "welcome" }, { "asp", "jsp", "php", "htm", "html" } };
		// for (int i = 0; i < def[0].length; i++) {
		// for (int j = 0; j < def[1].length; j++) {
		// StringBuilder append = new StringBuilder(def[0][i]).append('.').append(def[1][j]).append('$');
		// path = path.replaceFirst(append.toString(), EMPTY);
		// }
		// }
		s.append(path);
		// Removing IP. Check if the IP address is the same as its domain name. Example:
		// http://208.77.188.166/ → http://www.example.com/
		// Removing the "?" when the query string is empty
		String query = u.getQuery();
		if (!isEmpty(query)) {
			// Decoding XML entry
			query = query.replace("&amp;", "&");
			// Sorting the variables of active pages
			String[] split = query.split("&");
			String k, v;
			Map<String, Object> map = new TreeMap<String, Object>();
			for (String str : split) {
				int index = str.indexOf('=');
				k = str.substring(0, index++);
				v = str.substring(index, str.length());
				// Standardizing character encoding
				String encoding = getEncoding();
				try {
					k = URLDecoder.decode(k, encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				try {
					k = URLEncoder.encode(k, encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				try {
					v = URLDecoder.decode(v, encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				try {
					v = URLEncoder.encode(v, encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (map.containsKey(k)) {
					if (map.get(k) instanceof String) {
						Collection<String> a = new ArrayList<String>(split.length);
						a.add((String) map.put(k, a));
						a.add(v);
					} else {
						((Collection<String>) map.get(k)).add(v);
					}
				} else {
					map.put(k, v);
				}
			}
			k = v = null;
			s.append('?');
			//
			Iterator<Entry<String, Object>> i0 = map.entrySet().iterator();
			Entry<String, Object> e;
			if ((e = i0.next()).getValue() instanceof String) {
				s.append(e.getKey()).append('=').append(e.getValue());
			} else {
				Iterator<String> i1 = ((Collection<String>) e.getValue()).iterator();
				s.append(e.getKey()).append('=').append(i1.next());
				while (i1.hasNext()) {
					s.append('&').append(e.getKey()).append('=').append(i1.next());
				}
				i1 = null;
			}
			while (i0.hasNext()) {
				if ((e = i0.next()).getValue() instanceof String) {
					s.append('&').append(e.getKey()).append('=').append(e.getValue());
				} else {
					Iterator<String> i1 = ((Collection<String>) e.getValue()).iterator();
					while (i1.hasNext()) {
						s.append('&').append(e.getKey()).append('=').append(i1.next());
					}
					i1 = null;
				}
			}
			map = null;
			// Removing arbitrary querystring variables. An active page may expect certain variables to appear in the querystring; all unexpected variables should be removed. Example:
			// http://www.example.com/display?id=123&fakefoo=fakebar → http://www.example.com/display?id=123
			// Removing default querystring variables. A default value in the querystring will render identically whether it is there or not. When a default value appears in the querystring, it can be removed. Example:
			// http://www.example.com/display?id=&sort=ascending → http://www.example.com/display
		}
		return s.toString();
	}

	// sample
	public static void main(String[] args) throws IOException {
		//
		Collection<String> links = new ArrayList<String>();
		links.add("http://www.google.com.tw/");
		links.add("http://www.google.com.tw");
		links.add("http://www.google.com.tw:80");
		links.add("https://www.google.com.tw:443");
		links.add("http://www.google.com.tw/index.html");
		links.add("http://www.google.com.tw/index.html?_=1%2011");
		links.add("http://www.google.com.tw/index.html?_=111#sdsd");
		links.add("hTTp://omg:123@www.Google.com.tw/abc/efg/../index.html?_=111&x='%20'~#sdsd");
		links.add("hTTp://omg:123@www.Google.com.tw");
		links.add("http://www.example.com/../a/b//////////../c/./d/index.html");
		links.add("HTTP://www.Example.com/");
		links.add("http://www.example.com/index?aa=123&bb=y&cc=&amp;dd=&bb=x");
		links.add("http://www.example.com/display?lang=en&article=fred");
		links.add("http://www.example.com/../a/b/../c/./d.html");
		links.add("http://www.example.com/a%c2%b1b");
		links.add("http://www.example.com/%7Eusername/");
		links.add("http://www.example.com");
		links.add("https://www.example.com/");
		links.add("http://www.example.com/foo//bar.html");
		links.add("http://www.example.com/display?lang=en&article=fred");
		links.add("http://www.example.com/display?");
		links.add("http://www.example.com/display?category=foo/bar+baz");
		links.add("http://www.example.com/page.jsp?var[1]=foo&var[0]=bar");
		links.add("http://www.google.com.tw/search?q=url+normalize+java&hl=zh-TW&client=ubuntu&hs=qgg&channel=cs&prmd=imvns&ei=uKSmTr63Ls-VmQWw1-y7Dw&start=10&sa=N&biw=1066&bih=738&%E4%B8%AD%E6%96%87=%EF%BC%8B");
		for (int i = 0; i < 10; i++) {
			links.addAll(links);
		}
		//
		long cost = System.currentTimeMillis();
		for (String link : links) {
			// System.out.println(link + " → " + normalize(link));
			normalize(link);
		}
		cost = System.currentTimeMillis() - cost;
		System.out.printf("test %d links cost %dms avg %fms%n", links.size(), cost, (double) cost / links.size());
		System.exit(0);
		//
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
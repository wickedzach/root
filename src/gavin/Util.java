package gavin;

import gavin.utilities.MatcherEnumeration;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
public final class Util {

	// lazy

	private static Random rand = null;

	public static final Random rand() {
		if (rand != null)
			return rand;
		return rand = new SecureRandom();
	}

	private static String encoding = null;

	public static final String encoding() {
		if (encoding != null)
			return encoding;
		return encoding = Charset.defaultCharset().name();
	}

	private static Robot robot = null;

	public static final Robot robot() throws AWTException {
		if (robot != null)
			return robot;
		return robot = new Robot();
	}

	// label
	public static final String LABEL_CLOSE = "close".intern();
	public static final String LABEL_UTF8 = "UTF-8".intern();
	public static final String LABEL_MD5 = "md5".intern();
	public static final String LABEL_SHA512 = "sha-512".intern();
	// constant
	public static final String EMPTY = "".intern();
	public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static final boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static final String noEmpty(String str, String def) {
		return isEmpty(str) ? def : str;
	}

	// jdbc4 only
	public static final Connection conn(String url, String usr, String pwd) {
		try {
			return DriverManager.getConnection(url, usr, pwd);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	public static final Enumeration<String> enumeration(String text, Pattern pattern, int group) {
		return new MatcherEnumeration(pattern.matcher(text), group);
	}

	public static final DecimalFormat getFormat(int number) {
		byte[] tmp = new byte[number];
		Arrays.fill(tmp, (byte) '0');
		return new DecimalFormat(new String(tmp));
	}

	public static final void invoke(String method, Object... objects) {
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

	public static final void close(Object... objects) {
		invoke(LABEL_CLOSE, objects);
	}

	// handle digest

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

	// handle swing UI

	public static final void show(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	// handle image

	/**
	 * transform image via difference format
	 */
	public static final void transform(File srcImage, File detImage) throws IOException {
		String extention = FileUtil.getExtention(detImage).toLowerCase();
		Collection<String> extentions = Arrays.asList(ImageIO.getReaderFileSuffixes());
		if (extentions.contains(extention)) {
			ImageIO.write(ImageIO.read(srcImage), extention, detImage);
		} else {
			throw new RuntimeException("unknown format:" + extention);
		}
		extentions = null;
	}

	public static final BufferedImage captureScreen() throws AWTException {
		return robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
	}

	// handle URL

	public static final String absoluteURL(String targetURL, String relativeURL) {
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

	private static final Pattern normalizeP0 = Pattern.compile("%([a-f][0-9a-f]|[0-9a-f][a-f])");
	private static final Pattern normalizeP1 = Pattern.compile("%(2[DE]|3[0-9]|[46][1-9]|5[0-9AF]|7[AE])");

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
		// XXX Don't Do This or It was configured by site
		// Removing directory index
		// String[][] def = { { "default", "index", "welcome" }, { "asp", "jsp", "php", "htm", "html" } };
		// for (int i = 0; i < def[0].length; i++) {
		// for (int j = 0; j < def[1].length; j++) {
		// StringBuilder append = new StringBuilder(def[0][i]).append('.').append(def[1][j]).append('$');
		// path = path.replaceFirst(append.toString(), EMPTY);
		// }
		// }
		s.append(path);
		// TODO
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
				String encoding = encoding();
				try {
					k = URLDecoder.decode(k, encoding);
				} catch (UnsupportedEncodingException e) {}
				try {
					k = URLEncoder.encode(k, encoding);
				} catch (UnsupportedEncodingException e) {}
				try {
					v = URLDecoder.decode(v, encoding);
				} catch (UnsupportedEncodingException e) {}
				try {
					v = URLEncoder.encode(v, encoding);
				} catch (UnsupportedEncodingException e) {}
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
			//
			// XXX Don't Do This or It was configured by site
			// Removing arbitrary query string variables. An active page may expect certain variables to appear in the querystring; all unexpected variables should be removed. Example:
			// http://www.example.com/display?id=123&fakefoo=fakebar → http://www.example.com/display?id=123
			//
			// XXX Don't Do This or It was configured by site
			// Removing default query string variables. A default value in the querystring will render identically whether it is there or not. When a default value appears in the querystring, it can be removed. Example:
			// http://www.example.com/display?id=&sort=ascending → http://www.example.com/display
		}
		return s.toString();
	}

	public static final String unicode(String string) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (char c : string.toCharArray()) {
			try {
				bos.write(unicode(c));
			} catch (IOException e) {}
		}
		return new String(bos.toByteArray());
	}

	public static final byte[] unicode(char c) {
		if (c < Byte.MAX_VALUE)
			return new byte[] { (byte) c };
		byte[] bytes = { 92, 117, 0, 0, 0, 0 };
		bytes[2] = (byte) HEX[c >> 12 & 0xf];
		bytes[3] = (byte) HEX[c >> 8 & 0xf];
		bytes[4] = (byte) HEX[c >> 4 & 0xf];
		bytes[5] = (byte) HEX[c & 0xf];
		return bytes;
	}

	public static final void hex(InputStream in, OutputStream out) throws IOException {
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

	public static final byte[] uuid() {
		byte[] b = new byte[16];
		rand.nextBytes(b);
		b[6] &= 0x0f; /* clear version */
		b[6] |= 0x40; /* set to version 4 */
		b[8] &= 0x3f; /* clear variant */
		b[8] |= 0x80; /* set to IETF variant */
		return b;
	}

	//

	private static final char[] tempalte = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?".toCharArray();

	public static final char[] generate(int len) {
		return generate(len, tempalte);
	}

	public static final char[] generate(int len, char[] chars) {
		char[] res = new char[len];
		for (int i = 0, j = chars.length; i < res.length; i++) {
			// java.lang.Math.random() is faster than java.util.Random.nextInt(int)
			res[i] = chars[(int) Math.floor(Math.random() * j)];
		}
		return res;
	}

	public static final String FMT_DATE = "yyyy-MM-dd";
	public static final String FMT_TIME = "HH:mm:ss";
	public static final String FMT_DATETIME = new StringBuffer(FMT_DATE).append(' ').append(FMT_TIME).toString();

	public static SimpleDateFormat SDF_DATE = new SimpleDateFormat(FMT_DATE);
	public static SimpleDateFormat SDF_TIME = new SimpleDateFormat(FMT_TIME);
	public static SimpleDateFormat SDF_DATETIME = new SimpleDateFormat(FMT_DATETIME);

	private static final Date _DATE = new Date(-1);
	private static final Date _TIME = new Date(-1);
	private static final Date _DATETIME = new Date(-1);

	private static String cacheDATE = null;
	private static String cacheTIME = null;
	private static String cacheDATETIME = null;

	public static final String date(Date date) {
		return SDF_DATE.format(date);
	}

	public static final String time(Date time) {
		return SDF_TIME.format(time);
	}

	public static final String datetime(Date datetime) {
		return SDF_DATETIME.format(datetime);
	}

	public static final String toady() { // return today
		if (cacheDATE != null && System.currentTimeMillis() / 86400000 == _DATE.getTime() / 86400000) {
			return cacheDATE;
		}
		_DATE.setTime(System.currentTimeMillis());
		return cacheDATE = date(_DATE);
	}

	public static final String time() { // return current time
		if (cacheTIME != null && System.currentTimeMillis() - _TIME.getTime() < 1000) {
			return cacheTIME;
		}
		_TIME.setTime(System.currentTimeMillis());
		return cacheTIME = time(_TIME);
	}

	public static final String now() { // return current date time
		if (cacheDATETIME != null && System.currentTimeMillis() - _DATETIME.getTime() < 1000) {
			return cacheDATETIME;
		}
		_DATETIME.setTime(System.currentTimeMillis());
		return cacheDATETIME = datetime(_DATETIME);
	}

	public static final void delay(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}
}
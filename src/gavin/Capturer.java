package gavin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class Capturer {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public static void main(String[] args) throws Exception {
		String _0 = "http://www.gratisweb.com/bah038/pics/2975/vip1.html";
		String _1 = "http://www.gratisweb.com/bah038/pics/2975/vip2.html";
		String _2 = "http://www.gratisweb.com/bah038/pics/2975/vip3.html";
		String _3 = "http://www.gratisweb.com/bah038/pics/2975/vip4.html";

		capture(_0, _1, _2, _3);
		// capture(_0, _1);
		// capture(_0);

		// Class.forName("org.sqlite.JDBC");
		// Connection conn = DriverManager.getConnection("jdbc:sqlite:capturer.db");
		// Statement stat = conn.createStatement();
		//
		// String root = "http://blog-imgs-45.fc2.com/s/w/e/sweetmm/snzb.html";
		// x = new Url2File(new File("C:\\Documents and Settings\\gavin\\桌面\\新資料夾"));
		// visit(root, null, stat, INDEX_LINK0);
		//
		// stat.close();
		// conn.close();
	}

	public static void visit(String url, String refer, Statement stat, Pattern pattern) throws Exception {
		URL u = new URL(url);
		URLConnection c = u.openConnection();
		Object[] object = SQLUtil.scalar(stat, 2, "SELECT id,modifyTime FROM link WHERE url='" + url + "'");
		boolean exists = object != null;
		String accessTime = sdf.format(new Date());
		c.connect();
		String modifyTime = sdf.format(new Date(c.getLastModified()));
		int contentLength = c.getContentLength();
		String contentEncoding = c.getContentEncoding();
		String contentType = c.getContentType();
		int urlId;
		if (exists) {
			urlId = (Integer) object[0];
			if (modifyTime.equals(object[1])) {
				return;
			} else {
				stat.executeUpdate("UPDATE link SET accessTime='" + accessTime + "',modifyTime='" + object[1] + "',contentLength=" + contentLength + ",contentEncoding='" + contentEncoding + "',contentType='" + contentType + "' WHERE id=" + urlId);
			}
		} else {
			String sql = "INSERT INTO link(url,contentLength,contentEncoding,contentType,createTime,accessTime,modifyTime)";
			sql += "VALUES('" + url + "'," + contentLength + ",'" + contentEncoding + "','" + contentType + "','" + accessTime + "','" + accessTime + "','" + modifyTime + "')";
			stat.executeUpdate(sql);
			object = SQLUtil.scalar(stat, 1, "SELECT id FROM link WHERE url='" + url + "'");
			urlId = (Integer) object[0];
			sql = "INSERT INTO refer(referId,urlId,amount)";
			if (refer == null) {
				sql += "VALUES(NULL,'" + url + "',1)";
			} else {
				sql += "SELECT id,'" + url + "',1 FROM link WHERE url='" + refer + "'";
			}
			stat.executeUpdate(sql);
		}

		String html = IOUtil.read(c.getInputStream());
		String link;
		Enumeration<String> e = Util.enumeration(html, IMAGE_LINK);
		while (e.hasMoreElements()) {
			link = e.nextElement();
			// retrive(link);
		}
		// new thread to retrive

		e = Util.enumeration(html, pattern);
		while (e.hasMoreElements()) {
			link = e.nextElement();
			// visit(link);
			// visit index link
		}
		// INSERT INTO refer(referId,urlId,amount)
		// SELECT referId,urlId,SUM(amount) FROM refer WHERE amount=1 GROUP BY referId,urlId
		// DELETE FROM refer GROUP BY referId,urlId HAVING amount<>MAX(amount)
	}

	private static Pattern INDEX_LINK0 = Pattern.compile("<a href=\"([^\"]*vip\\d\\.html)\"><font color=\"#FFFF00\">[^<]+</font>[^\\(]+\\(pics\\)</a><br>");
	private static Pattern INDEX_LINK1 = Pattern.compile("<a class=\"font16\" href=\"(vip\\d\\.html)\" target=\"_top\">.*</a>", Pattern.CASE_INSENSITIVE);

	private static Pattern IMAGE_LINK = Pattern.compile("<li><a href=\"images/(\\d{3})\\.jpg\"><img[^>]*></a></li>", Pattern.CASE_INSENSITIVE);

	private static void capture(String... url) throws IOException {
		Enumeration<String> e;
		String html, head, link;
		int max = 512000;
		// StringBuilder s = new StringBuilder();
		System.out.println("var u,s='.jpg';");
		for (int i = 0; i < url.length; i++) {
			// s.append(head = "javascript:var u='" + url[i].substring(0, url[i].lastIndexOf('/') + 1) + "';function x(u){window.open(u);}");
			System.out.print("u='" + url[i].substring(0, url[i].lastIndexOf('/') + 1) + "images/';");
			html = IOUtil.read(new URL(url[i]).openStream(), "BIG5");
			e = Util.enumeration(html, IMAGE_LINK);
			while (e.hasMoreElements()) {
				link = e.nextElement();
				// if ((s.length() + link.length()) > max) {
				// System.out.println(s.toString());
				// s.setLength(head.length());
				// }
				// s.append("x(u+'" + link + "');");
				System.out.print("load(u+'" + link + "'+s);");
				if (link.endsWith("077.jpg")) {
					link = "images/078.jpg;";
					// if ((s.length() + link.length()) > max) {
					// System.out.println(s.toString());
					// s.setLength(head.length());
					// }
					// s.append("x(u+'" + link + "');");
					System.out.print("load(u+'" + link + "'+s);");
				}
			}
			System.out.println();
			// if (s.length() > head.length()) {
			// System.out.println(s.toString());
			// }
			// s.setLength(0);
			// find the way to avoid duplicate visit
			// e = Util.enumeration(html, INDEX_LINK);
			// while (e.hasMoreElements()) {
			// capture(e.nextElement());
			// }
		}
	}

	private static Url2File x;

	public static class Url2File {
		public File dir;

		public Url2File(File dir) {
			this.dir = dir;
		}

		public String transform(String url) {
			return new File(dir, url.replaceAll("^.*/(\\d{4})/images/(\\d{3}\\.jpg)$", "$1$2")).getAbsolutePath();
		}
	}
}
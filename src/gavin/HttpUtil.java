package gavin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class HttpUtil {
	public static Collection<String> follow(String url, String referer, String agent, int steps) throws IOException {
		Collection<String> result = new ArrayList<>();
		result.add(url);
		String tmp;
		while (steps-- > 0 && (tmp = follow(url, referer, agent)) != null && !result.contains(tmp)) {
			referer = url;
			result.add(url = tmp);
		}
		return result;
	}

	public static final String CRLF = "\r\n";

	public static String follow(String url, String referer, String agent) throws IOException {
		URL u = new URL(url);
		String host = u.getHost();
		int port = u.getPort();
		if (port == -1) port = u.getDefaultPort();
		//
		Socket socket = new Socket(host, port);
		//
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.append("HEAD ").append(u.getFile()).append(" HTTP/1.1").append(CRLF);
		out.append("Host: ").append(host).append(CRLF);
		out.append("Connection:close").append(CRLF);
		if (referer != null) out.append("Referer: ").append(referer).append(CRLF);
		if (agent != null) out.append("User-Agent: ").append(agent).append(CRLF);
		out.append(CRLF).flush();
		//
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line = reader.readLine();
		if (line != null && line.matches("^HTTP/\\d\\.\\d \\d{3} .*$")) {
			int status = Integer.parseInt(line.replaceFirst("^HTTP/\\d\\.\\d (\\d{3}) .*$", "$1"));
			if (status == 302 || status == 303 || status == 301) {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("Location: ")) {
						// return line.replaceFirst("^Location: (.{1,255})$", "$1");
						return line.replaceFirst("^Location: (.+)$", "$1");
					}
				}
			}
		}
		//
		socket.close();
		return null;
	}
}
package gavin;

import static gavin.Util.*;
import gavin.utilities.RandomInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class TestUtil {

	@Test
	public void testHexInputStreamOutputStream() throws IOException {
		InputStream input = new RandomInputStream(16);
		for (int i = 0; i < 16; i++) {
			hex(input, System.out);
			System.out.println();
			input.reset();
		}
		close(input);
	}

	@Test
	public void testNormalize() throws MalformedURLException {
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
		for (String link : links) {
			System.out.println(link + " → " + normalize(link));
		}
	}

}

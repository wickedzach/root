package gavin;

import static gavin.Util.*;
import static org.junit.Assert.*;
import gavin.utilities.RandomInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
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
		//
		for (String link : links) {
			System.out.println(link + " → " + normalize(link));
		}
	}

	@Test
	public void testToday() throws IOException {
		String toady = toady();
		delay(1000);
		assertTrue(toady == toady());
		delay(1000);
		assertTrue(toady == toady());
	}

	@Test
	public void testTime() throws IOException {
		String time = time();
		delay(500);
		assertTrue(time == time());
		delay(500);
		assertFalse(time == time());
		delay(500);
		assertFalse(time == time());
	}

	@Test
	public void testNow() throws IOException {
		String now = now();
		delay(500);
		assertTrue(now == now());
		delay(500);
		assertFalse(now == now());
		delay(500);
		assertFalse(now == now());
	}

	@Test
	public void testGenerate() throws IOException {
		int round = 10, length = 12;
		StringBuilder s = new StringBuilder();
		char[] cs;
		//
		s.append("0123456789");
		cs = s.toString().toCharArray();
		System.out.printf("cs %d%s%n", cs.length, Arrays.toString(cs));
		for (int i = 0; i < round; i++) {
			System.out.println(generate(length, cs));
		}
		cs = null;
		//
		s.append("abcdefghijklmnopqrstuvwxyz");
		cs = s.toString().toCharArray();
		System.out.printf("cs %d%s%n", cs.length, Arrays.toString(cs));
		for (int i = 0; i < round; i++) {
			System.out.println(generate(length, cs));
		}
		cs = null;
		//
		s.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		cs = s.toString().toCharArray();
		System.out.printf("cs %d%s%n", cs.length, Arrays.toString(cs));
		for (int i = 0; i < round; i++) {
			System.out.println(generate(length, cs));
		}
		cs = null;
		//
		s.append("0123456789");
		cs = s.toString().toCharArray();
		System.out.printf("cs %d%s%n", cs.length, Arrays.toString(cs));
		for (int i = 0; i < round; i++) {
			System.out.println(generate(length, cs));
		}
		cs = null;
	}
}

package gavin.web;

import gavin.IOUtil;
import gavin.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultFilter implements Filter {
	private ServletContext application;
	private String encoding;

	@Override
	public void init(FilterConfig config) throws ServletException {
		application = config.getServletContext();
		encoding = config.getInitParameter("encoding");
		if (Util.isEmpty(encoding)) {
			encoding = Util.encoding();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		req.setCharacterEncoding(encoding);
		res.setCharacterEncoding(encoding);
		//
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		// avoid cache
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1L);
		//
		long cost = System.currentTimeMillis();
		chain.doFilter(req, res);
		cost = System.currentTimeMillis() - cost;
		//
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream output = new PrintStream(buffer);
		IOUtil.copy(request.getInputStream(), output);
		output.append("\ncost " + cost + " milliseconds").flush();
		application.log(new String(buffer.toByteArray(), encoding));
	}
}
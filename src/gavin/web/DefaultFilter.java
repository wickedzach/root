package gavin.web;

import gavin.Util;

import java.io.IOException;

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
	private ServletContext context;
	private String encoding;

	@Override
	public void init(FilterConfig config) throws ServletException {
		context = config.getServletContext();
		encoding = config.getInitParameter("encoding");
		if (Util.isEmpty(encoding)) {
			encoding = System.getProperty("file.encoding", "UTF-8");
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		long start = System.currentTimeMillis();

		req.setCharacterEncoding(encoding);
		res.setCharacterEncoding(encoding);

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1L);

		// response.sendRedirect(response.encodeRedirectURL("/"));
		// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		chain.doFilter(req, res);

		context.log("Request process in " + (System.currentTimeMillis() - start) + " milliseconds");
	}
}
package vmlinux.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <web-app>
...
  <filter>
    <filter-name>Set Character Encoding</filter-name>
    <filter-class>vmlinux.web.SetCharacterEncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>UTF-8</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>Set Character Encoding</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
...
</web-app>
 * @author vmlinux
 *
 */
public class SetCharacterEncodingFilter implements Filter
{
	protected String encoding = null;

	protected FilterConfig filterConfig = null;

	protected boolean ignore = true;

	@Override
	public void destroy()
	{
		this.encoding = null;
		this.filterConfig = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException
	{
		// Conditionally select and set the character encoding to be used
		if (ignore || (request.getCharacterEncoding() == null))
		{
			String encoding = selectEncoding(request);
			if (encoding != null)
			{
				request.setCharacterEncoding(encoding); // Overrides
														// the name of the
														// character encoding
														// used in the body of
														// this request. This
														// method must be called
														// prior to reading
														// request parameters or
														// reading input using
														// getReader().
			}
		}

		// Pass control on to the next filter
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		this.encoding = filterConfig.getInitParameter("encoding");
		String value = filterConfig.getInitParameter("ignore");
		if (value == null)
		{
			this.ignore = true;
		}
		else if (value.equalsIgnoreCase("true"))
		{
			this.ignore = true;
		}
		else if (value.equalsIgnoreCase("yes"))
		{
			this.ignore = true;
		}
		else
		{
			this.ignore = false;
		}
	}

	protected String selectEncoding(ServletRequest request)
	{
		return (this.encoding);
	}
}

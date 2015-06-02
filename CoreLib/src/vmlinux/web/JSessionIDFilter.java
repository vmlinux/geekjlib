package vmlinux.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JSessionIDFilter implements Filter
{
	public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper 
	{
		public HttpServletRequestWrapper(HttpServletRequest arg0) 
		{
			super(arg0);
		}
		
		public HttpSession getSession(boolean create) 
		{
			//return new HttpSessionSidWrapper(this.sid, super.getSession(create));
			//return null;
			if(create)
			{
				return super.getSession(true);
			}
			else
			{
				return null;
			}
		}
		
		public HttpSession getSession() 
		{
			//return new HttpSessionSidWrapper(this.sid, super.getSession());
			//return null;
			return super.getSession();
		}
	}
	
	@Override
	public void destroy()
	{

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException
	{
		filterChain.doFilter(new HttpServletRequestWrapper((HttpServletRequest)request),response);

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{

	}

}

package vmlinux.util;

import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings({"deprecation","unchecked"})
public class WebGetter
{
	String url;
	String method;
	HttpURLConnection conn;
	HashMap params;
	HashMap headers;
	String encoding="utf-8";
	URL realURL;
	InputStream inputStream;
	String content;
	
	public String getEncoding()
	{
		return encoding;
	}
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
	public String getURL()
	{
		return url;
	}
	public URL getRealURL()
	{
		return realURL;
	}
	public String getContentType()
	{
		return content;
	}
	public WebGetter(String url,String method)
	{
		if(url.startsWith("http://"))
			this.url=url;
		else
			this.url="http://"+url;
		this.method=method.toUpperCase();
		this.headers=new HashMap();
	}
	public void addParameter(String name,String value)
	{
		if(params==null)
			params=new HashMap();
		params.put(name,value);
	}
	public void addHeader(String name,String value)
	{
		headers.put(name,value);
	}
	public int start() throws Exception
	{
		int code;
		code = ( "POST".equals(method) ) ? doPost() : doGet();
		content=conn.getContentType();
		encoding=null;
		return code;
	}
	public byte[] getResponseData() throws IOException
	{
		if(encoding==null)
		{
			String ce=conn.getContentEncoding();
			if(ce==null)
			{
				String[] ct=conn.getContentType().toLowerCase().split("charset\\s*=\\s*");
				encoding = ( ct.length == 2 ) ? ct[1] : "unknown";
			}
			else
				encoding=ce;
		}
		inputStream=conn.getInputStream();
		realURL=conn.getURL();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] buffer=new byte[2048];
		int size=0;
		while((size=inputStream.read(buffer))>0)
		{
			baos.write(buffer,0,size);
		}
		baos.flush();
		baos.close();
		return baos.toByteArray();
	}
	public void close()
	{
		if(inputStream!=null)
		{
			try
			{
				inputStream.close();
			}
			catch(Exception ex)
			{
				
			}
		}
		if(conn!=null)
			conn.disconnect();
	}
	public int doPost() throws Exception
	{
		realURL=new URL(url);
		conn=(HttpURLConnection)realURL.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type",
				"application/x-www-form-urlencoded; charset="+encoding);
		fillHeader(conn);
		if(params!=null)
		{
			conn.setDoOutput(true);
			OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
			try
			{
				Iterator it=params.entrySet().iterator();
				Map.Entry e;
				while(it.hasNext())
				{
					e=(Map.Entry)it.next();
					osw.write((String)e.getKey());
					osw.write('=');
					osw.write(URLEncoder.encode((String)e.getValue(),encoding));
					if(it.hasNext())
						osw.write('&');
				}
				osw.flush();
			}
			finally
			{
				osw.close();
			}
		}
		return conn.getResponseCode();
	}
	public int doGet() throws Exception
	{
		if(params!=null)
		{
			StringBuffer sb=new StringBuffer(url);
			if(url.indexOf('?')>0)
			{
				if(!url.endsWith("?"))
					sb.append('&');
			}
			else
				sb.append('?');
			Iterator it=params.entrySet().iterator();
			Map.Entry e;
			while(it.hasNext())
			{
				e=(Map.Entry)it.next();
				sb.append((String)e.getKey());
				sb.append('=');
				sb.append(URLEncoder.encode((String)e.getValue(),encoding));
				if(it.hasNext())
					sb.append('&');
			}
			this.url=sb.toString();
		}
		realURL=new URL(url);
		conn=(HttpURLConnection)realURL.openConnection();
		conn.setRequestMethod(method);
		fillHeader(conn);
		return conn.getResponseCode();
	}
	protected void fillHeader(HttpURLConnection connection)
	{
		Iterator it=headers.entrySet().iterator();
		Map.Entry e;
		while(it.hasNext())
		{
			e=(Map.Entry)it.next();
			connection.setRequestProperty((String)e.getKey(),(String)e.getValue());
		}
	}
}

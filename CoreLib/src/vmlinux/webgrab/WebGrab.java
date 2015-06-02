package vmlinux.webgrab;

import java.io.IOException;

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.NVPair;

public abstract class WebGrab
{
	protected String host;
	protected int port;
	protected int tmout;

	public WebGrab(String host,int port)
	{
		this.host=host;
		this.port=port;
		this.tmout=0;
	}
	
	public WebGrab(String host,int port,int tm)
	{
		this.host=host;
		this.port=port;
		this.tmout=tm;
	}

	protected HTTPConnection getWebConnection()
	{
		System.setProperty("HTTPClient.cookies.hosts.accept",host);
		HTTPConnection conn=new HTTPConnection(host,port);
		conn.setAllowUserInteraction(false);
		if(tmout>0)
		{
			conn.setTimeout(tmout);
		}
		return conn;
	}

	protected HTTPResponse getFromPath(String path,NVPair[] param)
	{
		HTTPConnection conn=getWebConnection();
		try
		{
			HTTPResponse resp=conn.Get(path, param);
			if(resp.getStatusCode()==200)
			{
				return resp;
			}
			throw new RuntimeException("http get status is "+resp.getStatusCode());
		}
		catch(ModuleException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	protected HTTPResponse getFromPath(HTTPConnection conn,String path,NVPair[] param)
	{
		try
		{
			HTTPResponse resp=conn.Get(path, param);
			if(resp.getStatusCode()==200)
			{
				return resp;
			}
			throw new RuntimeException("http get status is "+resp.getStatusCode());
		}
		catch(ModuleException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected HTTPResponse postToPage(String path,NVPair[] param)
	{
		HTTPConnection conn=getWebConnection();
		return postToPath(conn,path,param);
	}

	protected HTTPResponse postToPage(String path,String str)
	{
		HTTPConnection conn=getWebConnection();
		return postToPath(conn,path,str);
	}
	
	protected HTTPResponse postToPath(HTTPConnection conn,String path,NVPair[] param)
	{
		try
		{
			HTTPResponse resp=conn.Post(path, param);
			if(resp.getStatusCode()==200)
			{
				/*
				byte[] buf=resp.getData();
				System.out.println("POST "+path);
				if(param!=null)
				{
					for(int i=0;i<param.length;++i)
					{
						NVPair p=param[i];
						if(p!=null)
						{
							System.out.println(p.getName()+" = "+p.getValue());
						}
					}
				}
				System.out.println(new String(buf,"gb2312"));
				System.out.println("=========================");
				*/
				return resp;
			}
			throw new RuntimeException("http post status is "+resp.getStatusCode());
		}
		catch(ModuleException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected HTTPResponse postToPath(HTTPConnection conn,String path,String str)
	{
		try
		{
			HTTPResponse resp=conn.Post(path, str);
			if(resp.getStatusCode()==200)
			{
				return resp;
			}
			else
			{
				System.out.println("ERROR "+new String(resp.getData()));
			}
			throw new RuntimeException("http post status is "+resp.getStatusCode());
		}
		catch(ModuleException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

}

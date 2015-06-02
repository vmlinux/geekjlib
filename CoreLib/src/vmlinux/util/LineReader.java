package vmlinux.util;

import java.io.*;

public class LineReader
{
	/*
	private InputStream input;
	private byte[] buffer;
	private int length;
	private int pos;
	
	public LineReader(InputStream input)
	{
		this.input=input;
		buffer=new byte[20480];
		length=0;
		pos=0;
	}
	
	protected boolean appendToCR(StringBuffer sb)
	{
		int begin=pos;
		for(;pos<length;++pos)
		{
			byte b=buffer[pos];
			if(b==13)
			{
				sb.append(new String(buffer,begin,pos-begin));
				pos++;
				if(pos<length && buffer[pos]==10)
					pos++;
				return true;
			}
		}
		sb.append(new String(buffer,begin,pos-begin));
		return false;
	}
	
	public String readLine() throws IOException
	{
		StringBuffer sb=new StringBuffer();
		if(pos<length)
		{
			if(appendToCR(sb))
				return sb.toString();
		}
		do
		{
			pos=0;
			length=input.read(buffer);
			if(length<1)
				return null;
		}while(!appendToCR(sb));
		return sb.toString();
	}
	*/

	private BufferedReader br;
	
	public LineReader(InputStream input)
	{
		br=new BufferedReader(new InputStreamReader(input));
	}
	
	public LineReader(InputStream input,String encoding)
	{
		try
		{
			br=new BufferedReader(new InputStreamReader(input,encoding));
		}
		catch(UnsupportedEncodingException ex)
		{
			
		}
	}
	
	public String readLine() throws IOException
	{
		return br.readLine();
	}
	
	public void close() throws IOException
	{
		br.close();
	}
}

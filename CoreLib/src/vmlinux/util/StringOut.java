package vmlinux.util;

import java.io.PrintStream;

public class StringOut extends PrintStream
{
	protected StringBuffer sb=new StringBuffer();
	
	public StringOut()
	{
		super(System.out);
	}
	
	public void formatln(String fmt,Object...objects)
	{
		println(StringUtil.format(fmt, objects));
	}
	
	public void println(String s)
	{
		sb.append(s);
		sb.append("\r\n");
	}
	
	public void println()
	{
		sb.append("\r\n");
	}
	
	public void print(String s)
	{
		sb.append(s);
	}
	
	public String toString()
	{
		return sb.toString();
	}
}

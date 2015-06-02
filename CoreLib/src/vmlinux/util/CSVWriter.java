package vmlinux.util;

import java.io.*;

public class CSVWriter extends PrintWriter
{
	private boolean startOfLine;
	private boolean zerofile;
	
	public CSVWriter(String file) throws IOException
	{
		super(new OutputStreamWriter(new FileOutputStream(file),"gb2312"));
		startOfLine=true;
		zerofile=true;
	}
	
	public CSVWriter(String file,boolean append) throws IOException
	{
		super(new OutputStreamWriter(new FileOutputStream(file,append),"gb2312"));
		startOfLine=true;
		zerofile=new File(file).length()==0;
	}
	
	public CSVWriter(OutputStream output) throws IOException
	{
		super(new OutputStreamWriter(output,"gb2312"));
		startOfLine=true;
		zerofile=true;
	}
	
	public CSVWriter(Writer writer)
	{
		super(writer);
		startOfLine=true;
		zerofile=true;
	}
	
	public boolean isZeroFile()
	{
		return zerofile;
	}
	
	public void writeString(String val)
	{
		if(startOfLine)
		{
			startOfLine=false;
		}
		else
		{
			print(",");
		}
		if(val==null)
		{
			val="";
		}
		else
		{
			val=StringUtil.replacePlain(val.replaceAll("\r", "\\r").replaceAll("\n", "\\n").replaceAll(",", "，"), "\"", "\"\"");
		}
		if(val.matches("^\\d{12,}$"))
			print("\"'"+val+"\"");
		else
			print("\""+val+"\"");
	}
	
	public void writeLine(String line)
	{
		if(!startOfLine)
		{
			writeNext();
		}
		println(line);
	}
	
	public void writeNext()
	{
		startOfLine=true;
		println();
	}
	
	public void close()
	{
		try
		{
			out.flush();
			out.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}

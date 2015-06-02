package vmlinux.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public abstract class DumpUtil
{
	public static void dump(HttpServletRequest request)
	{
		System.out.print("[method]");
		System.out.println(request.getMethod());
		System.out.print("[uri]");
		System.out.println(request.getRequestURI());
		Enumeration enu=request.getHeaderNames();
		String name;
		while(enu.hasMoreElements())
		{
			name=(String)enu.nextElement();
			System.out.print("[header]");
			System.out.print(name);
			System.out.print("\t=\t");
			System.out.println(request.getHeader(name));
		}
		enu=request.getParameterNames();
		while(enu.hasMoreElements())
		{
			name=(String)enu.nextElement();
			System.out.print("[param]");
			System.out.print(name);
			System.out.print("\t=\t");
			System.out.println(request.getParameter(name));
		}
	}
	public static void dumpMap(Map map)
	{
		dump(map.entrySet());
	}
	public static void dumpMapKeys(Map map)
	{
		dump(map.keySet());
	}
	public static void dumpWithIndex(Iterator i)
	{
		Object o;
		int c=0;
		while(i.hasNext())
		{
			o=i.next();
			System.out.print(c+" - ");
			if(o instanceof String)
				System.out.println((String)o);
			else if(o instanceof Map.Entry)
				dumpMapEntry((Map.Entry)o);
			else
				System.out.println(o.toString());
			++c;
		}
	}
	public static void dumpWithIndex(Set set)
	{
		dumpWithIndex(set.iterator());
	}
	public static void dump(Iterator i)
	{
		Object o;
		while(i.hasNext())
		{
			o=i.next();
			if(o instanceof String)
				System.out.println((String)o);
			else if(o instanceof Map.Entry)
				dumpMapEntry((Map.Entry)o);
			else
				System.out.println(o.toString());
		}
	}
	public static void dump(Set set)
	{
		dump(set.iterator());
	}
	public static void dumpMapEntry(Map.Entry e)
	{
		if(e.getKey() instanceof String)
			System.out.print((String)e.getKey());
		else
			System.out.print(e.getKey());
		System.out.print("=");
		if(e.getValue() instanceof String)
			System.out.println((String)e.getValue());
		else
			System.out.println(e.getValue());
	}
	public static void dumpXML(Element doc,String file)
	{
		FileOutputStream fos=null;
		try
		{
			fos=new FileOutputStream(file);
			dumpXML(doc,fos);
		}
		catch(IOException ex)
		{
			System.err.println("[err]DumpUtil::dumpXML:"+ex);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}
	public static void dumpXML(Document doc,String file)
	{
		FileOutputStream fos=null;
		try
		{
			fos=new FileOutputStream(file);
			dumpXML(doc,fos);
		}
		catch(IOException ex)
		{
			System.err.println("[err]DumpUtil::dumpXML:"+ex);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}
	public static void dumpXML(Element doc,OutputStream stream)
	{
		try
		{
			XMLOutputter outputter=new XMLOutputter(Format.getPrettyFormat());
			outputter.output(doc, stream);
		}
		catch(IOException ex)
		{
			System.err.println("[err]DumpUtil::dumpXML:"+ex);
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}
	public static void dumpXML(Document doc,OutputStream stream)
	{
		try
		{
			XMLOutputter outputter=new XMLOutputter(Format.getPrettyFormat());
			outputter.output(doc, stream);
		}
		catch(IOException ex)
		{
			System.err.println("[err]DumpUtil::dumpXML:"+ex);
		}
		finally
		{
			if (stream != null && stream!=System.out)
			{
				try
				{
					stream.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}
}

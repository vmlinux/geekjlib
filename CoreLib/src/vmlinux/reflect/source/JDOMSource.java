package vmlinux.reflect.source;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import vmlinux.reflect.*;
import vmlinux.reflect.serializer.JDOMSerializer;
import vmlinux.util.StringUtil;

@SuppressWarnings("unchecked")
public class JDOMSource extends XObjectSourceBase
{
	public static Object batchBuild(List list, String xclass)
	{
		try
		{
			return batchBuild(list,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::batchBuild:"+ex);
		}
		return null;
	}
	public static Object batchBuild(List list, Class xclass)
	{
		int len=list.size();
		Object result=Array.newInstance(xclass, len);
		for(int i=0;i<len;++i)
		{
			Array.set(result, i, buildObject((Element)list.get(i),xclass));
		}
		return result;
	}
	public static Object buildObject(String file,String xclass)
	{
		SAXBuilder sb=new SAXBuilder();
		try
		{
			Element e=sb.build(new File(file)).getRootElement();
			return buildObject(e, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(String file,Class xclass)
	{
		SAXBuilder sb=new SAXBuilder();
		try
		{
			Element e=sb.build(new File(file)).getRootElement();
			return buildObject(e, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(Element e, String xclass)
	{
		try
		{
			return buildObject(e,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(Element e, Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		JDOMSource source=new JDOMSource(e);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObjectFromString(String str, Class xclass)
	{
		SAXBuilder sb=new SAXBuilder();
		StringReader sr=new StringReader(str);
		try
		{
			Element e=sb.build(sr).getRootElement();
			return buildObject(e,xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSource::buildObjectFromString:"+ex);
		}
		return null;
	}
	
	protected Element source;
	
	public JDOMSource(Element src)
	{
		this.source=src;
	}

	/*
	protected String getData(String name)
	{
		return source==null?null:source.getChildText(name);
	}
	/*/
	protected String getData(String name)
	{
		if(source==null)
		{
			return null;
		}
		else
		{
			String lname=StringUtil.trim(name.replace('$', '.'), '_');
			String data=
				name.equals("_")?source.getText():
				(name.startsWith("__")&&name.endsWith("__"))?source.getChildText(name.substring(2,name.length()-2)):
				name.startsWith("_")?source.getAttributeValue(lname):
					source.getChildText(lname);
			return data;
		}
	}
	/**/
	protected String getData(Element e)
	{
		return e==null?null:e.getText();
	}
	
	public Object get(String name)
	{
		return getData(name);
	}

	public boolean getBoolean(String name)
	{
		String data=getData(name);
		return getBooleanInner(data);
	}

	public Date getDate(String name)
	{
		String data=getData(name);
		return getDateInner(data);
	}

	public double getDouble(String name)
	{
		String data=getData(name);
		return getDoubleInner(data);
	}

	public float getFloat(String name)
	{
		String data=getData(name);
		return getFloatInner(data);
	}

	public int getInt32(String name)
	{
		String data=getData(name);
		return getInt32Inner(data);
	}

	public long getLong(String name)
	{
		String data=getData(name);
		return getLongInner(data);
	}
	
	public byte getByte(String name)
	{
		String data=getData(name);
		return getByteInner(data);
	}
	
	public byte[] getByteArray(String name)
	{
		String data=getData(name);
		return getByteArrayInner(data);
	}

	public Object getObject(String name, Class xclass) throws IllegalAccessException, InstantiationException
	{
		String lname=StringUtil.trim(name, '_');
		Element child=source.getChild(lname);
		return getObject(child, xclass);
	}
	protected Object getObject(Element e, Class xclass) throws IllegalAccessException, InstantiationException
	{
		if(e==null)
			return null;
		XObjectSource src=new JDOMSource(e);
		return getObjectInner(src,xclass);
	}

	public String getString(String name)
	{
		String d=getData(name);
		if("".equals(d) && source!=null && source.getChild(name)!=null)
		{
			String s=JDOMSerializer.serializeObjectToString("", source.getChild(name));
			d=s.replaceAll("<[^>]+>", "");
		}
		return d;
	}

	public Object getArray(String name, Class xclass)
	{
		String lname=StringUtil.trim(name, '_');
		List children=source.getChildren(lname);
		int length=children.size();
		if(length==0)
			return null;
		Object array=null;
		Iterator i=children.iterator();
		Element e;
		String data;
		if(xclass.equals(int.class))
		{
			int[] iarray=new int[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				iarray[p]=getInt32Inner(data);
			}
			array=iarray;
		}
		else if(xclass.equals(boolean.class))
		{
			boolean[] barray=new boolean[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				barray[p]=getBooleanInner(data);
			}
			array=barray;
		}
		else if(xclass.equals(String.class))
		{
			String[] sarray=new String[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				sarray[p]=getStringInner(data);
			}
			array=sarray;
		}
		else if(xclass.equals(long.class))
		{
			long[] larray=new long[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				larray[p]=getLongInner(data);
			}
			array=larray;
		}
		else if(xclass.equals(Date.class))
		{
			Date[] darray=new Date[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				darray[p]=getDateInner(data);
			}
			array=darray;
		}
		else if(xclass.equals(double.class))
		{
			double[] dbarray=new double[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				dbarray[p]=getDoubleInner(data);
			}
			array=dbarray;
		}
		else if(xclass.equals(float.class))
		{
			float[] farray=new float[length];
			for(int p=0;i.hasNext();++p)
			{
				e=(Element)i.next();
				data=getData(e);
				farray[p]=getFloatInner(data);
			}
			array=farray;
		}
		else if(XObjectBuilder.isXObject(xclass))
		{
			try
			{
				array=Array.newInstance(xclass, length);
				for(int p=0;i.hasNext();++p)
				{
					e=(Element)i.next();
					Array.set(array, p, getObject(e,xclass));
				}
			}
			catch(Exception ex)
			{
				System.err.println("[err]XObjectJDOMSource::XObject Array:"+ex);
			}
		}
		return array;
	}
	
	public XDynamic getDynamic()
	{
		XDynamic dyn=new XDynamic();
		Iterator i=source.getAttributes().iterator();
		while(i.hasNext())
		{
			Attribute att=(Attribute)i.next();
			dyn.put(att.getName(),att.getValue());
		}
		return dyn;
	}
}

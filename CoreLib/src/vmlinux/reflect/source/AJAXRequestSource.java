package vmlinux.reflect.source;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import vmlinux.reflect.*;
import vmlinux.util.StringUtil;

@SuppressWarnings(value={"unchecked","fallthrough"})
public class AJAXRequestSource extends XObjectSourceBase
{
	public static Object buildObject(HttpServletRequest request, String xclass)
	{
		try
		{
			return buildObject(request,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]AJAXRequestSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(HttpServletRequest request, Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		AJAXRequestSource source=new AJAXRequestSource(request);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]AJAXRequestSource::buildObject:"+ex);
		}
		return null;
	}
	
	protected HttpServletRequest source;
	protected String prefix;
	
	public AJAXRequestSource(HttpServletRequest request)
	{
		this.source=request;
		this.prefix="";
	}
	protected AJAXRequestSource(String prefix,HttpServletRequest request)
	{
		this.source=request;
		this.prefix=prefix;
	}
	
	protected String getData(String name)
	{
		String lname=StringUtil.replacePlain(StringUtil.trim(name, '_'), "_", "-");
		//NOTE: no prefix added to header name
		String data=name.startsWith("_")?
				source.getHeader(lname):source.getParameter(prefix+lname);
		return data;
	}

	public Object get(String name)
	{
		return getData(name);
	}
	public String getDecodedParam(String name)
	{
		String param=source.getParameter(name);
		return decodeParam(param);
	}
	public String decodeParam(String value)
	{
		if(value!=null)
		{
			try
			{
				return StringUtil.unescape(value);
			}
			catch(Exception ex)
			{
				System.out.println("[err]AJAXRequestSource::decodeParam:"+ex);
			}
		}
		return null;
	}

	public Object getArray(String name, Class aclass)
	{
		String data=getData(name);
		if(data==null)
		{
			String lname=StringUtil.trim(name, '_');
			data=getData(lname+".0");
			if(data!=null)
			{
				List list=new ArrayList();
				String value=(String)data;
				int index=value==""?0:1;	//skip exist mark tag
				if(aclass.equals(int.class))
				{
					while(value!=null)
					{
						list.add(new Integer(getInt32Inner(value)));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(boolean.class))
				{
					while(value!=null)
					{
						list.add(new Boolean(getBooleanInner(value)));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(String.class))
				{
					while(value!=null)
					{
						list.add(getStringInner(value));
						value=(String)getDecodedParam(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(long.class))
				{
					while(value!=null)
					{
						list.add(new Long(getLongInner(value)));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(Date.class))
				{
					while(value!=null)
					{
						list.add(getDateInner(value));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(double.class))
				{
					while(value!=null)
					{
						list.add(new Double(getDoubleInner(value)));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(float.class))
				{
					while(value!=null)
					{
						list.add(new Float(getFloatInner(value)));
						value=(String)source.getParameter(prefix+lname+"."+index);
						index++;
					}
				}
				else if(XObjectBuilder.isXObject(aclass))
				{
					try
					{
						Object o=null;
						while(value!=null)
						{
							o=getObject(lname+"."+index,aclass);
							if(o==null)
								break;
							list.add(o);
							value=(String)source.getParameter(prefix+lname+"."+index);
							index++;
						}
					}
					catch(Exception ex)
					{
						System.err.println("[err]MapSource::XObject Array:"+ex);
					}
				}
				return list.toArray();
			}
			return null;
		}
		else
		{
			return getArrayX(data,aclass);
		}
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

	public Object getObject(String name, Class xclass)
			throws IllegalAccessException, InstantiationException
	{
		String lname=StringUtil.trim(name, '_');
		String oprefix=prefix+lname+".";
		//check key set to confirm no infinite loop
		Enumeration e=source.getParameterNames();
		while(e.hasMoreElements())
		{
			if(((String)e.nextElement()).startsWith(oprefix))
			{
				XObjectSource src=new AJAXRequestSource(oprefix,source);
				return getObjectInner(src,xclass);
			}
		}
		//no need to check header?
		return null;
	}

	public String getString(String name)
	{
		return decodeParam(getData(name));
	}

	public XDynamic getDynamic()
	{
		return null;
	}
}

package vmlinux.reflect.source;

import java.text.SimpleDateFormat;
import java.util.*;

import vmlinux.reflect.*;
import vmlinux.util.*;

@SuppressWarnings("unchecked")
public class CSVSource extends XObjectSourceBase
{
	public static Object buildObject(CSVReader request, String xclass)
	{
		try
		{
			return buildObject(request,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectRequestSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(CSVReader request, Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		CSVSource source=new CSVSource(request);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectRequestSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(CSVReader request, Class xclass, SimpleDateFormat fmt)
	{
		XObjectBuilder builder=new XObjectBuilder();
		CSVSource source=new CSVSource(request);
		source.setDateFormat(fmt);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectRequestSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildArray(CSVReader request, Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		ArrayList list=new ArrayList();
		CSVSource source=new CSVSource(request);
		try
		{
			while(request.next())
			{
				Object o=builder.build(source, xclass);
				list.add(o);
			}
			return list.toArray();
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectRequestSource::buildArray:"+ex);
		}
		return null;
	}
	
	protected CSVReader source;
	protected String prefix;
	
	public CSVSource(CSVReader request)
	{
		this.source=request;
		this.prefix="";
	}
	protected CSVSource(String prefix,CSVReader request)
	{
		this.source=request;
		this.prefix=prefix;
	}
	
	protected String getData(String name)
	{
		String lname=StringUtil.trim(name, '_');
		//NOTE: no prefix added to header name
		String data=source.getData(prefix+lname);
		return data;
	}
	
	public Object get(String name)
	{
		return getData(name);
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
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(boolean.class))
				{
					while(value!=null)
					{
						list.add(new Boolean(getBooleanInner(value)));
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(String.class))
				{
					while(value!=null)
					{
						list.add(getStringInner(value));
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(long.class))
				{
					while(value!=null)
					{
						list.add(new Long(getLongInner(value)));
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(Date.class))
				{
					while(value!=null)
					{
						list.add(getDateInner(value));
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(double.class))
				{
					while(value!=null)
					{
						list.add(new Double(getDoubleInner(value)));
						value=(String)source.getData(prefix+lname+"."+index);
						index++;
					}
				}
				else if(aclass.equals(float.class))
				{
					while(value!=null)
					{
						list.add(new Float(getFloatInner(value)));
						value=(String)source.getData(prefix+lname+"."+index);
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
							value=(String)source.getData(prefix+lname+"."+index);
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
			else if("_".equals(name))
			{
				List list=new ArrayList();
				try
				{
					while(source.next())
					{
						Object o=this.getObjectInner(this, aclass);
						list.add(o);
					}
					return list.toArray();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
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
		int n=source.columns();
		for(int i=0;i<n;++i)
		{
			if(source.getHeader(i).startsWith(oprefix))
			{
				XObjectSource src=new CSVSource(oprefix,source);
				return getObjectInner(src,xclass);
			}
		}
		return null;
	}

	public String getString(String name)
	{
		return getData(name);
	}

	public XDynamic getDynamic()
	{
		return null;
	}
}

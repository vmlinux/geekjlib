package vmlinux.reflect.source;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import vmlinux.reflect.*;
import vmlinux.util.StringUtil;

@SuppressWarnings("unchecked")
public class MapSource extends XObjectSourceBase
{
	public static Object buildObject(Map map, String xclass)
	{
		try
		{
			return buildObject(map,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectMapSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(String prefix,Map map,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		MapSource source=new MapSource(prefix,map);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectMapSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(Map map, Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		MapSource source=new MapSource(map);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectMapSource::buildObject:"+ex);
		}
		return null;
	}
	
	protected Map source;
	protected String prefix;
	
	public MapSource(Map map)
	{
		this.source=map;
		this.prefix="";
	}
	protected MapSource(String prefix,Map map)
	{
		this.source=map;
		this.prefix=prefix;
	}
	
	protected Object getData(String name)
	{
		String lname=StringUtil.trim(name, '_');
		return source.get(prefix+lname);
	}
	
	public Object get(String name)
	{
		return getData(name);
	}

	public Object getArray(String name, Class aclass)
	{
		Object data=getData(name);
		if(data==null)
		{
			String lname=StringUtil.trim(name, '_');
			String dot=".";
			data=getData(lname+dot+"0");
			if(data==null)
			{
				if(aclass.isArray())
				{
					List list=new ArrayList();
					for(int i=0;;++i)
					{
						Object item=getArray(lname+dot+i,aclass.getComponentType());
						if(item==null)
							break;
						list.add(item);
					}
					return list.toArray();
				}
				else
				{
					dot="";	//this error correction may cause bug
					data=getData(lname+dot+"0");
				}
			}
			if(data!=null)
			{
				List list=new ArrayList();
				if(Map.class.isAssignableFrom(data.getClass()))	//build from map object
				{
					try
					{
						Map m=(Map)data;
						int index=1;
						while(m!=null)
						{
							list.add(builder.build(new MapSource((Map)data), aclass));
							m=(Map)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					catch(InstantiationException ex)
					{
						ex.printStackTrace();
					}
					catch(IllegalAccessException ex)
					{
						ex.printStackTrace();
					}
				}
				else
				{
					String value=(String)data;
					int index=value==""?0:1;	//skip exist mark flag
					if(aclass.equals(int.class))
					{
						while(value!=null)
						{
							list.add(new Integer(getInt32Inner(value)));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(boolean.class))
					{
						while(value!=null)
						{
							list.add(new Boolean(getBooleanInner(value)));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(String.class))
					{
						while(value!=null)
						{
							list.add(getStringInner(value));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(long.class))
					{
						while(value!=null)
						{
							list.add(new Long(getLongInner(value)));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(Date.class))
					{
						while(value!=null)
						{
							list.add(getDateInner(value));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(double.class))
					{
						while(value!=null)
						{
							list.add(new Double(getDoubleInner(value)));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(aclass.equals(float.class))
					{
						while(value!=null)
						{
							list.add(new Float(getFloatInner(value)));
							value=(String)source.get(prefix+lname+dot+index);
							index++;
						}
					}
					else if(XObjectBuilder.isXObject(aclass))
					{
						index=0;
						try
						{
							Object o=null;
							while(value!=null)
							{
								o=getObject(lname+dot+index,aclass);
								if(o==null)
									break;
								list.add(o);
								value=(String)source.get(prefix+lname+dot+index);
								index++;
							}
						}
						catch(Exception ex)
						{
							System.err.println("[err]MapSource::XObject Array:"+ex);
						}
					}
				}
				return list.toArray();
			}
			return null;
		}
		else
		{
			return getArrayX(data, aclass);
		}
	}

	public boolean getBoolean(String name)
	{
		Object data=getData(name);
		return getBooleanX(data);
	}

	public Date getDate(String name)
	{
		Object data=getData(name);
		return getDateX(data);
	}

	public double getDouble(String name)
	{
		Object data=getData(name);
		return getDoubleX(data);
	}

	public float getFloat(String name)
	{
		Object data=getData(name);
		return getFloatX(data);
	}

	public int getInt32(String name)
	{
		Object data=getData(name);
		return getInt32X(data);
	}

	public long getLong(String name)
	{
		Object data=getData(name);
		return getLongX(data);
	}
	
	public byte getByte(String name)
	{
		Object data=getData(name);
		return getByteX(data);
	}
	
	public byte[] getByteArray(String name)
	{
		Object data=getData(name);
		return getByteArrayX(data);
	}

	public Object getObject(String name, Class xclass)
			throws IllegalAccessException, InstantiationException
	{
		String lname=StringUtil.trim(name, '_');
		Object data=getData(lname);
		if(data!=null && xclass.equals(data.getClass()))
		{
			return data;
		}
		if(data!=null && Map.class.isAssignableFrom(data.getClass()))
		{
			return builder.build(new MapSource((Map)data), xclass);
		}
		//check key set to confirm no infinite loop
		String oprefix=prefix+lname+".";
		//dealing object with single _ field
		boolean istobj=false;
		//Class tclass=null;
		Field tfield=null;
		String tname=prefix+lname;
		try
		{
			tfield=xclass.getField("_");
			istobj=tfield!=null;
			/*
			if(istobj)
				tclass=tfield.getType();
			*/
		}
		catch(Exception ex)
		{
			//ignore error
		}
		
		Iterator i=source.keySet().iterator();
		while(i.hasNext())
		{
			String keyname=(String)i.next();
			if(keyname.startsWith(oprefix))
			{
				XObjectSource src=new MapSource(oprefix,source);
				return getObjectInner(src,xclass);
			}
			else if(istobj && keyname.equals(tname))//_ field
			{
				Object obj=XObjectBuilder.createObject(xclass, this);
				tfield.set(obj, getX(get(name),tfield.getType()));
				return obj;
			}
		}
		return null;
	}

	public String getString(String name)
	{
		Object data=getData(name);
		return getStringX(data);
	}

	public XDynamic getDynamic()
	{
		return null;
	}
}

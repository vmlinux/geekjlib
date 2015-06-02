package vmlinux.reflect.serializer;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSerializer;
import vmlinux.reflect.XObjectSerializerBase;

//object to map
@SuppressWarnings({"deprecation","unchecked"})
public class MapSerializer extends XObjectSerializerBase
{
	public static Map serializeObject(Object o)
	{
		XObjectSerializer serializer=new MapSerializer();
		try
		{
			return (Map)serializer.serialize("map",o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectMapSerializer::serializeObject:"+ex);
		}
		return null;
	}
	public static Map serializeObject(Map map,Object o)
	{
		MapSerializer serializer=new MapSerializer();
		try
		{
			serializer.serialize(map,o,"");
			return map;
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectMapSerializer::serializeObject:"+ex);
		}
		return null;
	}
	
	public Object serialize(String name,Object o) throws IllegalAccessException
	{
		Map map=new HashMap();
		return serialize(map,o,"");
	}
	protected Map serialize(Map map,Object o,String prefix) throws IllegalAccessException
	{
		if(o==null)
			return map;
		//long ticket=System.currentTimeMillis();
		Class oclass=o.getClass();
		Field[] fields=oclass.getDeclaredFields();
		Field field;
		Class fclass;
		String fname;
		Object value=null;
		Object fo;
		Object x=null;
		for(int i=0;i<fields.length;++i)
		{
			field=fields[i];
			field.setAccessible(true);
			/*
			fclass=field.getType();
			/*/
			//serialize the real class :)
			fo=field.get(o);
			fclass=fo==null?field.getType():fo.getClass();
			/**/
			fname=field.getName();
			//Note: skip inner parent pointer for inner class
			if(fname.indexOf("$")>=0 || fname.endsWith("_"))	
				continue;
			value=null;
			if(fclass.equals(int.class))
			{
				value=new Integer(field.getInt(o));
			}
			else if(fclass.equals(boolean.class))
			{
				value=new Boolean(field.getBoolean(o));
			}
			else if(fclass.equals(String.class))
			{
				value=field.get(o);
			}
			else if(fclass.equals(long.class))
			{
				value=new Long(field.getLong(o));
			}
			else if(fclass.equals(Date.class))
			{
				value=field.get(o);
			}
			else if(fclass.equals(double.class))
			{
				value=new Double(field.getDouble(o));
			}
			else if(fclass.equals(float.class))
			{
				value=new Float(field.getFloat(o));
			}
			else if(fclass.equals(byte.class))
			{
				value=new Byte(field.getByte(o));
			}
			else if(XObjectBuilder.isXObject(fclass))
			{
				value=field.get(o);
				serialize(map,value,prefix+fname+".");
			}
			else if(fclass.isArray())
			{
				value=field.get(o);
			}
			else
			{
				if((x=field.get(o))!=null)
					value=x.toString();
			}
			if(value!=null)
			{
				map.put(prefix+fname, value);
			}
		}
		//System.out.println("serialize time "+(System.currentTimeMillis()-ticket));
		return map;
	}
}

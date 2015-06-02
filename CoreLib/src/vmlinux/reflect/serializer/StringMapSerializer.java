package vmlinux.reflect.serializer;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSerializer;
import vmlinux.reflect.XObjectSerializerBase;

//object to string map
@SuppressWarnings("unchecked")
public class StringMapSerializer extends XObjectSerializerBase
{
	public static Map serializeObject(Object o)
	{
		XObjectSerializer serializer=new StringMapSerializer();
		try
		{
			return (Map)serializer.serialize("map",o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectStringMapSerializer::serializeObject:"+ex);
		}
		return null;
	}
	public static Map serializeObject(Map map,Object o)
	{
		StringMapSerializer serializer=new StringMapSerializer();
		try
		{
			serializer.serialize(map,o,"");
			return map;
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectStringMapSerializer::serializeObject:"+ex);
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
		String value=null;
		Object fo;
		Class tclass;
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
				value=formatInt32Inner(field.getInt(o));
			}
			else if(fclass.equals(boolean.class))
			{
				value=formatBooleanInner(field.getBoolean(o));
			}
			else if(fclass.equals(String.class))
			{
				value=formatStringInner((String)field.get(o));
			}
			else if(fclass.equals(long.class))
			{
				value=formatLongInner(field.getLong(o));
			}
			else if(fclass.equals(Date.class))
			{
				value=formatDateInner((Date)field.get(o));
			}
			else if(fclass.equals(double.class))
			{
				value=formatDoubleInner(field.getDouble(o));
			}
			else if(fclass.equals(float.class))
			{
				value=formatFloatInner(field.getFloat(o));
			}
			else if(fclass.equals(byte.class))
			{
				value=formatByteInner(field.getByte(o));
			}
			else if(XObjectBuilder.isXObject(fclass))
			{
				serialize(map,field.get(o),prefix+fname+".");
			}
			else if(fclass.isArray())
			{
				Object ao=field.get(o);
				tclass=fclass.getComponentType();
				if(tclass.equals(byte.class))
					value=formatByteArrayInner((byte[])ao);
				else if(tclass.equals(int.class))
					value=formatInt32ArrayInner((int[])ao);
				else if(tclass.equals(boolean.class))
					value=formatBooleanArrayInner((boolean[])ao);
				else if(tclass.equals(long.class))
					value=formatLongArrayInner((long[])ao);
				else if(tclass.equals(double.class))
					value=formatDoubleArrayInner((double[])ao);
				else
					map.put(prefix+fname, ao);
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

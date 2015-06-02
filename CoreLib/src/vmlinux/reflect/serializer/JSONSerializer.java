package vmlinux.reflect.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Date;

import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSerializer;
import vmlinux.reflect.XObjectSerializerBase;
import vmlinux.util.StringUtil;

public class JSONSerializer extends XObjectSerializerBase
{
	public static String serializeObject(Object o)
	{
		XObjectSerializer serializer=new JSONSerializer();
		try
		{
			return (String)serializer.serialize("json",o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSONSerializer::serializeObject:"+ex);
		}
		return null;
	}

	public static String serializeObject(String name,Object o)
	{
		XObjectSerializer serializer=new JSONSerializer();
		try
		{
			return (String)serializer.serialize(name,o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSONSerializer::serializeObject:"+ex);
		}
		return null;
	}

	public Object serialize(String name, Object o) throws IllegalAccessException
	{
		StringBuffer data=new StringBuffer();
		serialize(data,o);
		return data.toString();
	}

	protected void serialize(StringBuffer data,Object o) throws IllegalAccessException
	{
		if(o==null)
			return;
		Class oclass=o.getClass();
		if(oclass.isArray())
		{
			outputArray(data,o,oclass.getComponentType(),"");
		}
		else if(XObjectBuilder.isXObject(oclass))
		{
			outputObject(data,o,oclass,"");
		}
	}
	
	protected void outputObject(StringBuffer data,Object obj,Class fclass,String fname) throws IllegalAccessException
	{
		if(obj==null)
			return;
		if(data.length()>0 && data.charAt(data.length()-1)==',' && data.charAt(data.length()-2)=='{')
			data.deleteCharAt(data.length()-1);
		if(fname.length()>0)
		{
			data.append(fname);
			data.append(":");
		}
		data.append("{");
		Field[] fields=fclass.getFields();//fclass.getDeclaredFields();
		outputObjectFields(data,obj,fields);
		if(data.charAt(data.length()-1)==',')
			data.deleteCharAt(data.length()-1);
		data.append("}");
	}
	protected void outputObjectFields(StringBuffer data,Object inst,Field[] fields) throws IllegalAccessException
	{
		Field field;
		Class fclass;
		String fname;
		for(int i=0;i<fields.length;++i)
		{
			field=fields[i];
			fname=field.getName();
			fclass=field.getType();
			if(fname.indexOf("$")>=0 || fname.endsWith("_"))	
				continue;
			if(i>0 && data.charAt(data.length()-1)!=',')
				data.append(",");
			field.setAccessible(true);
			if(fclass.isArray())
			{
				outputArray(data,field.get(inst),fclass.getComponentType(),fname);
			}
			else if(XObjectBuilder.isXObject(fclass))
			{
				outputObject(data,field.get(inst),fclass,fname);
			}
			else
			{
				outputBasic(data,inst,field,fname);
			}
		}
		if(data.charAt(data.length()-1)==',')
			data.deleteCharAt(data.length()-1);
	}
	protected void outputArray(StringBuffer data,Object container,Class aclass,String fname) throws IllegalAccessException
	{
		if(container==null)
			return;
		if(data.length()>0 && data.charAt(data.length()-1)==',' && data.charAt(data.length()-2)=='{')
			data.deleteCharAt(data.length()-1);
		if(fname.length()>0)
		{
			data.append(fname);
			data.append(":");
		}
		data.append("[");
		if(aclass.isArray())
		{
			Object o;
			int len=Array.getLength(container);
			for(int i=0;i<len;++i)
			{
				if(i>0 && data.charAt(data.length()-1)!=',')
					data.append(",");
				o=Array.get(container, i);
				outputArray(data,o,aclass.getComponentType(),"");
			}
		}
		else if(aclass.equals(int.class))
		{
			data.append(formatInt32ArrayInner((int[])container));
		}
		else if(aclass.equals(boolean.class))
		{
			data.append(formatBooleanArrayInner((boolean[])container));
		}
		else if(aclass.equals(String.class))
		{
			data.append(formatStringArrayInner((String[])container));
		}
		else if(aclass.equals(long.class))
		{
			data.append(formatLongArrayInner((long[])container));
		}
		else if(aclass.equals(byte.class))
		{
			data.append(formatByteArrayInner((byte[])container));
		}
		else if(aclass.equals(double.class))
		{
			data.append(formatDoubleArrayInner((double[])container));
		}
		else if(aclass.equals(float.class))
		{
			data.append(formatFloatArrayInner((float[])container));
		}
		else if(aclass.equals(Date.class))
		{
			data.append(formatDateArrayInner((Date[])container));
		}
		else if(XObjectBuilder.isXObject(aclass))
		{
			Object o;
			int len=Array.getLength(container);
			for(int i=0;i<len;++i)
			{
				if(i>0 && data.charAt(data.length()-1)!=',')
					data.append(",");
				o=Array.get(container, i);
				outputObject(data,o,aclass,"");
			}
		}
		data.append("]");
	}
	protected void outputBasic(StringBuffer data,Object parent,Field field,String fname) throws IllegalAccessException
	{
		if(parent==null)
			return;
		if(data.length()>0 && data.charAt(data.length()-1)==',' && data.charAt(data.length()-2)=='{')
			data.deleteCharAt(data.length()-1);
		Class fclass=field.getType();
		if(fname.length()>0)
		{
			data.append(fname);
			data.append(":");
		}
		if(fclass.equals(int.class))
		{
			data.append(formatInt32Inner(field.getInt(parent)));
		}
		else if(fclass.equals(boolean.class))
		{
			data.append(formatBooleanInner(field.getBoolean(parent)));
		}
		else if(fclass.equals(String.class))
		{
			Object o=field.get(parent);
			if(o!=null)
			{
				data.append("\"");
				String s=formatStringInner((String)o);
				data.append(StringUtil.replacePlain(
						StringUtil.replacePlain(s.replace('"', '\''), "\r", "\\r"),"\n","\\n"));
				data.append("\"");
			}
			else
			{
				data.append("\"\"");
			}
		}
		else if(fclass.equals(long.class))
		{
			data.append(formatLongInner(field.getLong(parent)));
		}
		else if(fclass.equals(Date.class))
		{
			Object o=field.get(parent);
			if(o!=null)
			{
				data.append("\"");
				data.append(formatDateInner((Date)o));
				data.append("\"");
			}
			else
			{
				data.append("\"\"");
			}
		}
		else if(fclass.equals(double.class))
		{
			data.append(formatDoubleInner(field.getDouble(parent)));
		}
		else if(fclass.equals(float.class))
		{
			data.append(formatFloatInner(field.getFloat(parent)));
		}
		else if(fclass.equals(byte.class))
		{
			data.append(formatByteInner(field.getByte(parent)));
		}
		else
		{
			//ignore other types
		}
	}
}

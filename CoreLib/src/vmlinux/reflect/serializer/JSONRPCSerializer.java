package vmlinux.reflect.serializer;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import vmlinux.reflect.XObject;
import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSerializerBase;
import vmlinux.util.StringUtil;

public class JSONRPCSerializer extends XObjectSerializerBase
{
	public static String serializeObject(String name,Object o,Map rpcsvc)
	{
		JSONRPCSerializer serializer=new JSONRPCSerializer();
		try
		{
			serializer.setServices(rpcsvc);
			return (String)serializer.serialize(name,o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSONRPCSerializer::serializeObject:"+ex);
		}
		return null;
	}
	
	protected Map services;
	public void setServices(Map services)
	{
		this.services=services;
	}
	public Object serialize(String name, Object o) throws IllegalAccessException
	{
		StringBuffer data=new StringBuffer();
		serialize(data,o,name);
		return data.toString();
	}

	protected void serialize(StringBuffer data,Object o,String name) throws IllegalAccessException
	{
		if(o==null)
			return;
		Class oclass=o.getClass();
		if(oclass.isArray())
		{
			outputArray(data,o,oclass.getComponentType(),"");
		}
		else if(o instanceof XObject)
		{
			outputObject(data,o,oclass,"");
		}
		else
		{
			outputSimple(data,o);
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
		boolean hasService=false;
		if(services!=null)
		{
			Object svc=services.get(fclass);
			if(svc!=null)
			{
				data.append("$:'");
				data.append((String)svc);
				data.append("',");
				hasService=true;
			}
		}
		//TODO: implement get all fields of class
		//getDeclaredFields only return all fields of current class
		//getFields only return public fields
		Field[] fields=fclass.getFields();
		//deal special single _ field
		if(fields.length==1 && "_".equals(fields[0].getName()))
		{
			data.deleteCharAt(data.length()-1);
			Field field=fields[0];
			Class clazz=field.getType();
			field.setAccessible(true);
			if(clazz.isArray())
			{
				outputArray(data,field.get(obj),clazz.getComponentType(),"");
			}
			else if(XObjectBuilder.isXObject(clazz))
			{
				outputObject(data,field.get(obj),clazz,"");
			}
			else
			{
				outputBasic(data,obj,field,"");
			}
		}
		else
		{
			outputObjectFields(data,obj,fields);
			if(hasService)
			{
				if(fields.length>0 && data.charAt(data.length()-1)!=',')
					data.append(",");
				Method[] methods=fclass.getMethods();
				outputObjectMethods(data,obj,methods);
			}
			data.append("}");
		}
	}
	protected void outputObjectFields(StringBuffer data,Object inst,Field[] fields) throws IllegalAccessException
	{
		Field field;
		Class fclass;
		String fname;
		int count=0;
		for(int i=0;i<fields.length;++i)
		{
			field=fields[i];
			fname=field.getName();
			fclass=field.getType();
			if(fname.indexOf("$")>=0 || fname.endsWith("_"))	
				continue;
			if(count>0 && data.charAt(data.length()-1)!=',')
				data.append(",");
			count++;
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
	protected void outputObjectMethods(StringBuffer data,Object inst,Method[] methods) throws IllegalAccessException
	{
		Method method;
		String mname;
		for(int i=0;i<methods.length;++i)
		{
			method=methods[i];
			mname=method.getName();
			if(mname.endsWith("_") || method.getDeclaringClass().equals(Object.class))
				continue;
			if(i>0 && data.charAt(data.length()-1)!=',')
				data.append(",");
			data.append(mname);
			data.append(":$C('"+mname+"')");
		}
		if(data.charAt(data.length()-1)==',')
			data.deleteCharAt(data.length()-1);
	}
	protected void outputArray(StringBuffer data,Object container,Class aclass,String fname) throws IllegalAccessException
	{
		if(data.length()>0 && data.charAt(data.length()-1)==',' && data.charAt(data.length()-2)=='{')
			data.deleteCharAt(data.length()-1);
		if(fname.length()>0)
		{
			data.append(fname);
			data.append(":");
		}
		if(container==null)
		{
			data.append("null");
			return;
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
			//data.append(formatStringArrayInner((String[])container));
			String[] sa=(String[])container;
			for(int j=0;j<sa.length;++j)
			{
				if(j>0 && data.charAt(data.length()-1)!=',')
					data.append(",");
				data.append("\"");
				writeString(data,sa[j]);
				data.append("\"");
			}
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
	protected void writeString(StringBuffer sb,String s)
	{
		String str=formatStringInner(s);
		sb.append(StringUtil.replacePlain(
			StringUtil.replacePlain(
					StringUtil.replacePlain(str, "\r", "\\r")
					, "\n", "\\n")
				,"\"","\\\""));
	}
	protected void outputSimple(StringBuffer data,Object o) throws IllegalAccessException
	{
		if(o==null)
			return;
		Class oclass=o.getClass();
		if(oclass.equals(Integer.class))
		{
			data.append(formatInt32Inner((Integer)o));
		}
		else if(oclass.equals(String.class))
		{
			data.append("\"");
			writeString(data,(String)o);
			data.append("\"");
		}
		else if(oclass.equals(Boolean.class))
		{
			data.append(formatBooleanInner((Boolean)o));
		}
		else if(oclass.equals(Long.class))
		{
			data.append(formatLongInner((Long)o));
		}
		else if(oclass.equals(Double.class))
		{
			data.append(formatDoubleInner((Double)o));
		}
		else if(oclass.equals(Float.class))
		{
			data.append(formatFloatInner((Float)o));
		}
		else if(oclass.equals(Date.class))
		{
			data.append("\"");
			data.append(formatDateInner((Date)o));
			data.append("\"");
		}
		else if(oclass.equals(Byte.class))
		{
			data.append(formatByteInner((Byte)o));
		}
		else
		{
			//try output object
			outputObject(data,o,oclass,"");
		}

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
				writeString(data,(String)o);
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
			data.append("\"");
			data.append(field.get(parent).toString());
			data.append("\"");
		}
	}
	
}

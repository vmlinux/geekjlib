package vmlinux.reflect;

import java.math.*;
import java.text.*;
import java.util.*;

import vmlinux.codec.Base64;
import vmlinux.util.StringUtil;

@SuppressWarnings({"deprecation","unchecked"})
public abstract class XObjectSourceBase implements XObjectSource
{
	public static final SimpleDateFormat format1=new SimpleDateFormat("yyyy-M-d H:m:s");
	public static final SimpleDateFormat format2=new SimpleDateFormat("yyyy-M-d");
	public static final SimpleDateFormat format3=new SimpleDateFormat("H:m:s");
	protected XObjectBuilder builder;
	protected Object container;
	protected SimpleDateFormat myformat;
	
	public void setBuilder(XObjectBuilder xob)
	{
		this.builder=xob;
	}
	//Note: set conteiner object to solve inner class problem
	public void setContainer(Object o)
	{
		container=o;
	}
	//Note: make sure to set container when implementing getObject
	public Object containerObject()
	{
		return container;
	}
	
	public void setDateFormat(SimpleDateFormat fmt)
	{
		myformat=fmt;
	}
	
	//get value from string
	protected boolean getBooleanInner(String data)
	{
		return data==null||data.length()==0?false:
			("true".equalsIgnoreCase(data)||"on".equalsIgnoreCase(data));
	}
	protected Date getDateInner(String data)
	{
		if(data!=null && data.length()>0)
		{
			if(myformat==null)
			{
				try
				{
					return format1.parse(data);
				}
				catch(ParseException ex)
				{
					try
					{
						return format2.parse(data);
					}
					catch(ParseException ex2)
					{
						throw new RuntimeException(ex);
					}
				}
			}
			else
			{
				try
				{
					return myformat.parse(data);
				}
				catch(ParseException ex)
				{
					throw new RuntimeException(ex);
				}
			}
		}
		return null;
	}
	protected double getDoubleInner(String data)
	{
		return data==null||data.length()==0?0.0D:Double.parseDouble(data);
	}
	protected float getFloatInner(String data)
	{
		return data==null||data.length()==0?0.0F:Float.parseFloat(data);
	}
	protected int getInt32Inner(String data)
	{
		return data==null||data.length()==0?0:(int)Double.parseDouble(data);
	}
	protected long getLongInner(String data)
	{
		return data==null||data.length()==0?0L:(long)Double.parseDouble(data);
	}
	protected byte getByteInner(String data)
	{
		return data==null||data.length()==0?0:Byte.parseByte(data);
	}
	protected String getStringInner(String data)
	{
		return data;
	}
	protected byte[] getByteArrayInner(String data)
	{
		return data==null||data.length()==0?null:Base64.decode(data);
	}
	protected int[] getInt32ArrayInner(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		String value;
		int len=list.size();//?? check here
		int[] result=new int[len];
		for(int i=0;i<len;++i)
		{
			value=(String)list.get(i);
			result[i]=getInt32Inner(value);
		}
		return result;
	}
	protected boolean[] getBooleanArrayInner(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		String value;
		int len=list.size();//?? check here
		boolean[] result=new boolean[len];
		for(int i=0;i<len;++i)
		{
			value=(String)list.get(i);
			result[i]=getBooleanInner(value);
		}
		return result;
	}
	protected long[] getLongArrayInner(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		String value;
		int len=list.size();//?? check here
		long[] result=new long[len];
		for(int i=0;i<len;++i)
		{
			value=(String)list.get(i);
			result[i]=getLongInner(value);
		}
		return result;
	}
	protected double[] getDoubleArrayInner(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		String value;
		int len=list.size();//?? check here
		double[] result=new double[len];
		for(int i=0;i<len;++i)
		{
			value=(String)list.get(i);
			result[i]=getDoubleInner(value);
		}
		return result;
	}
	protected float[] getFloatArrayInner(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		String value;
		int len=list.size();//?? check here
		float[] result=new float[len];
		for(int i=0;i<len;++i)
		{
			value=(String)list.get(i);
			result[i]=getFloatInner(value);
		}
		return result;
	}

	protected Object getObjectInner(XObjectSource src,Class xclass) throws IllegalAccessException, InstantiationException
	{
		if(src==null)
		{
			return null;
		}
		else if(XDynamic.class.equals(xclass))
		{
			return src.getDynamic();
		}
		else
		{
			//Note: deliver container object
			src.setContainer(this.containerObject());
			return builder.build(src, xclass);
		}
	}

	//get value from object
	protected boolean getBooleanX(Object data)
	{
		if(data instanceof String)
			return getBooleanInner((String)data);
		else if(data instanceof Boolean)
			return ((Boolean)data).booleanValue();
		return false;
	}
	protected int getInt32X(Object data)
	{
		if(data instanceof String)
			return getInt32Inner((String)data);
		else if(data instanceof Integer)
			return ((Integer)data).intValue();
		else if(data instanceof BigDecimal)
			return ((BigDecimal)data).intValue();
		else if(data instanceof BigInteger)
			return ((BigInteger)data).intValue();
		else if(data instanceof Byte)
			return ((Byte)data).intValue();
		return 0;
	}
	protected Date getDateX(Object data)
	{
		if(data instanceof String)
		{
			if(((String) data).matches("^[1234567890]+$"))
			{
				return new Date(Long.valueOf((String)data));
			}
			return getDateInner((String)data);
		}
		else if(data instanceof Date)
			return (Date)data;
		else if(data instanceof java.sql.Timestamp)
			return new Date(((java.sql.Timestamp)data).getTime());
		else if(data instanceof java.sql.Date)
			return new Date(((java.sql.Date)data).getTime());
		else if(data instanceof java.sql.Time)
			return new Date(((java.sql.Time)data).getTime());
		else if(data instanceof oracle.sql.TIMESTAMP)
		{
			oracle.sql.TIMESTAMP x=(oracle.sql.TIMESTAMP)data;
			if(x.isConvertibleTo(java.sql.Timestamp.class))
			{
				try
				{
					return new Date(x.timestampValue().getTime());
				}
				catch(Exception ex)
				{
					//ex.printStackTrace();
				}
			}
		}
		else if(data instanceof Long)
			return new Date(((Long)data).longValue());
		return null;
	}
	protected double getDoubleX(Object data)
	{
		if(data instanceof String)
			return getDoubleInner((String)data);
		else if(data instanceof Double)
			return ((Double)data).doubleValue();
		else if(data instanceof BigDecimal)
			return ((BigDecimal)data).doubleValue();
		else if(data instanceof BigInteger)
			return ((BigInteger)data).doubleValue();
		else if(data instanceof Float)
			return ((Float)data).doubleValue();
		else if(data instanceof Integer)
			return ((Integer)data).doubleValue();
		else if(data instanceof Byte)
			return ((Byte)data).doubleValue();
		return 0.0D;
	}
	protected float getFloatX(Object data)
	{
		if(data instanceof String)
			return getFloatInner((String)data);
		else if(data instanceof Float)
			return ((Float)data).floatValue();
		else if(data instanceof BigDecimal)
			return ((BigDecimal)data).floatValue();
		else if(data instanceof BigInteger)
			return ((BigInteger)data).floatValue();
		else if(data instanceof Integer)
			return ((Integer)data).floatValue();
		else if(data instanceof Byte)
			return ((Byte)data).floatValue();
		return 0.0F;
	}
	protected long getLongX(Object data)
	{
		if(data instanceof String)
			return getLongInner((String)data);
		else if(data instanceof Long)
			return ((Long)data).longValue();
		else if(data instanceof BigDecimal)
			return ((BigDecimal)data).longValue();
		else if(data instanceof BigInteger)
			return ((BigInteger)data).longValue();
		else if(data instanceof Integer)
			return ((Integer)data).longValue();
		else if(data instanceof Byte)
			return ((Byte)data).longValue();
		else if(data instanceof Date)
			return ((Date)data).getTime();
		return 0L;
	}
	protected byte getByteX(Object data)
	{
		if(data instanceof String)
			return getByteInner((String)data);
		else if(data instanceof Byte)
			return ((Byte)data).byteValue();
		return 0;
	}
	protected String getStringX(Object data)
	{
		if(data instanceof String)
			return getStringInner((String)data);
		else if(data!=null)
			return data.toString();
		return null;
	}
	protected Object getArrayX(Object data, Class aclass)
	{
		if(data!=null)
		{
			Class dclass=data.getClass();
			if(dclass.isArray() && 
					aclass.isAssignableFrom(dclass.getComponentType()))
				return data;
			else if(dclass.equals(String.class))
			{
				String str=(String)data;
				if(aclass.equals(int.class))
				{
					return getInt32ArrayInner(str);
				}
				else if(aclass.equals(boolean.class))
				{
					return getBooleanArrayInner(str);
				}
				else if(aclass.equals(long.class))
				{
					return getLongArrayInner(str);
				}
				else if(aclass.equals(double.class))
				{
					return getDoubleArrayInner(str);
				}
				else if(aclass.equals(float.class))
				{
					return getFloatArrayInner(str);
				}
			}
		}
		return null;
	}
	protected byte[] getByteArrayX(Object data)
	{
		if(data instanceof String)
			return getByteArrayInner((String)data);
		else if(data!=null)
		{
			Class dclass=data.getClass();
			if(dclass.isArray() && byte.class.isAssignableFrom(dclass.getComponentType()))
				return (byte[])data;
		}
		return null;
	}
	
	protected Object getX(Object data,Class clazz)
	{
		if(clazz.equals(boolean.class)
			||clazz.equals(Boolean.class))
		{
			return new Boolean(getBooleanX(data));
		}
		else if(clazz.equals(int.class)
				||clazz.equals(Integer.class))
		{
			return new Integer(getInt32X(data));
		}
		else if(clazz.equals(Date.class))
		{
			return getDateX(data);
		}
		else if(clazz.equals(double.class)
				||clazz.equals(Double.class))
		{
			return new Double(getDoubleX(data));
		}
		else if(clazz.equals(float.class)
				||clazz.equals(Float.class))
		{
			return new Float(getFloatX(data));
		}
		else if(clazz.equals(long.class)
				||clazz.equals(Long.class))
		{
			return new Long(getLongX(data));
		}
		else if(clazz.equals(byte.class)
				||clazz.equals(Byte.class))
		{
			return new Byte(getByteX(data));
		}
		else if(clazz.equals(String.class))
		{
			return getStringX(data);
		}
		else if(clazz.isArray() 
				&& byte.class.isAssignableFrom(clazz.getComponentType()))
		{
			return getByteArrayX(data);
		}
		else if(clazz.isArray())
		{
			return getArrayX(data,clazz.getComponentType());
		}
		return null;
	}
}

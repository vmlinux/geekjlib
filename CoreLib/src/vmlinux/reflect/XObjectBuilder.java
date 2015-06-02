package vmlinux.reflect;


import java.lang.reflect.*;
import java.util.Date;
import java.util.List;

//import vmlinux.util.ReflectUtil;

/*
 * Author: vmlinux( contact: vmlinuxx@gmail.com )
 * Update: 2006-09-20
 * 
 * XObjectBuilder summary:
 * 	supported base types: byte, boolean, int, long, float, double
 * 	supported object types: String, Date, XObject+
 * 	supported array types: boolean[], int[], long[], float[], double[], 
 * 							String[], Date[], XObject+[]
 * 	supported special types: byte[]
 * 
 * XObjectSource summary:
 * 	JDOMSource: full support (get variable from attribute if variable is started with '_')
 * 	MapSource: full support
 * 	RequestSource: partial array support (boolean[], int[], long[], float[], double[])
 * 	SqlSource: full support ( not yet tested )
 * 	JSONSource: full support
 * 
 * XObjectSerializer summary:
 * 	JDOMSerializer: full support ( variable ended with '_' will be ignored )
 * 	MapSerializer: full support
 * 	StringMapSerializer: partial support for String[], Date[], XObject+[]
 * 	JSONSerializer: full support
 */
@SuppressWarnings("unchecked")
public class XObjectBuilder
{
	public static Object transform(Class xclass,XObjectSource src,XObjectSerializer tgt)
	{
		XObjectBuilder builder=new XObjectBuilder();
		try
		{
			Object obj=builder.build(src, xclass);
			return tgt.serialize("XO", obj);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectBuilder::transform:"+ex);
		}
		return null;
	}
	
	public static boolean isXObject(Class fclass)
	{
		return XObject.class.isAssignableFrom(fclass);
	}
	public static boolean isList(Class fclass)
	{
		return List.class.isAssignableFrom(fclass);
	}
	public static Object createObject(Class oclass,XObjectSource source) throws InstantiationException, IllegalAccessException
	{
		Object inst=null;
		//*
		try
		{
			int mod=oclass.getModifiers();
			//Note: mod==0 if xclass is inner class
			Constructor constructor=oclass.getDeclaredConstructor(mod==0?new Class[]{source.containerObject().getClass()}:null);
			constructor.setAccessible(true);
			inst=constructor.newInstance(mod==0?new Object[]{source.containerObject()}:null);
		}
		catch(Exception ex)
		{
			//inst=oclass.newInstance();
			//ex.printStackTrace();
		}
		
		if(inst==null)
		{
			try
			{
				inst=oclass.newInstance();
			}
			catch(InstantiationException ex)
			{
				ex.printStackTrace();
				throw ex;
			}
		}
		
		/*/
		try
		{
			inst=Unsafe.getUnsafe().allocateInstance(oclass);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		/**/
		return inst;
	}
	public static void dumpModifier(Class xclass)
	{
		int mod=xclass.getModifiers();
		System.out.println("Modifier of "+xclass+" : "+mod);
		System.out.println("  is abstract?     : "+Modifier.isAbstract(mod));
		System.out.println("  is final?        : "+Modifier.isFinal(mod));
		System.out.println("  is interface?    : "+Modifier.isInterface(mod));
		System.out.println("  is native?       : "+Modifier.isNative(mod));
		System.out.println("  is private?      : "+Modifier.isPrivate(mod));
		System.out.println("  is protected?    : "+Modifier.isProtected(mod));
		System.out.println("  is public?       : "+Modifier.isPublic(mod));
		System.out.println("  is static?       : "+Modifier.isStatic(mod));
		System.out.println("  is strict?       : "+Modifier.isStrict(mod));
		System.out.println("  is synchronized? : "+Modifier.isSynchronized(mod));
		System.out.println("  is transient?    : "+Modifier.isTransient(mod));
		System.out.println("  is volatile?     : "+Modifier.isVolatile(mod));
	}

	public XObjectBuilder()
	{
		
	}
	public Object build(XObjectSource source,Class xclass) throws IllegalAccessException, InstantiationException
	{
		if(source==null)
			return null;
		//performace test
		//long ticket=System.currentTimeMillis();
		source.setBuilder(this);
		Object inst=null;
		if(xclass.equals(int.class) || xclass.equals(Integer.class))
		{
			inst=source.getInt32("");
		}
		else if(xclass.equals(boolean.class) || xclass.equals(Boolean.class))
		{
			inst=source.getBoolean("");
		}
		else if(xclass.equals(long.class) || xclass.equals(Long.class))
		{
			inst=source.getLong("");
		}
		else if(xclass.equals(String.class))
		{
			inst=source.getString("");
		}
		else if(xclass.equals(double.class) || xclass.equals(Double.class))
		{
			inst=source.getDouble("");
		}
		else if(xclass.equals(java.util.Date.class))
		{
			inst=source.getDate("");
		}
		else if(xclass.equals(short.class) || xclass.equals(Short.class))
		{
			inst=source.getInt32("");
		}
		else if(xclass.equals(float.class) || xclass.equals(Float.class))
		{
			inst=source.getFloat("");
		}
		else if(xclass.isArray())
		{
			inst=source.getArray("", xclass.getComponentType());
		}
		if(inst!=null)
		{
			return inst;
		}
		inst=createObject(xclass,source);
		//FIXME: get all declared fields of self and super classes
		//getDeclaredFields only get fields of current class
		//getFields only get public fields
		Field[] fields=xclass.getFields();
		Field field;
		Class fclass;
		String fname;
		Class aclass;
		int mod;
		for(int i=0;i<fields.length;++i)
		{
			field=fields[i];
			field.setAccessible(true);
			mod=field.getModifiers();
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod))
				continue;
			fclass=field.getType();
			fname=field.getName();
			//System.err.println("[debug] current field "+fname);
			if(fclass.equals(int.class))
			{
				field.setInt(inst, source.getInt32(fname));
			}
			else if(fclass.equals(boolean.class))
			{
				field.setBoolean(inst, source.getBoolean(fname));
			}
			else if(fclass.equals(String.class))
			{
				//default value of string field is kept
				String t=source.getString(fname);
				if(t!=null)
				{
					field.set(inst, t);
				}
			}
			else if(fclass.equals(long.class))
			{
				field.setLong(inst, source.getLong(fname));
			}
			else if(fclass.equals(Date.class))
			{
				//default value of date field is kept
				Date t=source.getDate(fname);
				if(t!=null)
				{
					field.set(inst, t);
				}
			}
			else if(fclass.equals(double.class))
			{
				field.setDouble(inst, source.getDouble(fname));
			}
			else if(fclass.equals(float.class))
			{
				field.setFloat(inst, source.getFloat(fname));
			}
			else if(isXObject(fclass))
			{
				source.setContainer(inst);
				field.set(inst, source.getObject(fname, fclass));
			}
			else if(fclass.isArray())
			{
				source.setContainer(inst);
				aclass=fclass.getComponentType();
				if(aclass.equals(byte.class))
				{
					field.set(inst, source.getByteArray(fname));
				}
				else
				{
					Object x=source.getArray(fname, aclass);
					if(x!=null)
					{
						if(fclass.isAssignableFrom(x.getClass()))
						{
							field.set(inst,	x);
						}
						else if(x.getClass().isArray())
						{
							int len=Array.getLength(x);
							Object farray=Array.newInstance(aclass, len);
							Object o=null;
							for(int j=0;j<len;++j)
							{
								o=Array.get(x, j);
								if(o!=null)
								{
									if(aclass.isArray())
									{
										//Object iarray=Array.newInstance(aclass.getComponentType(), Array.getLength(o));
										Object iarray=copyArray(aclass,o);
										Array.set(farray, j, iarray);
									}
									else
									{
										Array.set(farray, j, o);
									}
								}
							}
							field.set(inst, farray);
						}
					}
				}
			}
		}
		//System.out.println("build time "+(System.currentTimeMillis()-ticket));
		return inst;
	}
	
	public Object copyArray(Class aclass,Object arrfrom) throws IllegalAccessException,InstantiationException
	{
		int len=Array.getLength(arrfrom);
		Object tarray=Array.newInstance(aclass.getComponentType(), len);
		for(int i=0;i<len;++i)
		{
			Object o=Array.get(arrfrom, i);
			if(o!=null)
			{
				if(o.getClass().isArray())
				{
					Array.set(tarray, i, copyArray(o.getClass(),o));
				}
				else
				{
					Array.set(tarray, i, o);
				}
			}
		}
		return tarray;
	}
}

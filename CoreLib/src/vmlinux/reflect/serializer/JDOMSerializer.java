package vmlinux.reflect.serializer;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Date;

import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import vmlinux.reflect.XObject;
import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSerializerBase;

//object to JDOM
public class JDOMSerializer extends XObjectSerializerBase
{
	public static Element serializeObject(String name,Object o)
	{
		JDOMSerializer serializer=new JDOMSerializer();
		try
		{
			Element elem=new Element(name);
			return serializer.serialize(elem, o);
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectJDOMSerializer::serializeObject:"+ex);
		}
		return null;
	}
	public static String serializeObjectToString(String name,Object o)
	{
		Element e=(o!=null && Element.class.isAssignableFrom(o.getClass()))?
				(Element)o:serializeObject(name, o);
		if(e!=null)
		{
			try
			{
				XMLOutputter outputter=new XMLOutputter();
				StringWriter sw=new StringWriter();
				outputter.output(e, sw);
				return sw.toString();
			}
			catch(Exception ex)
			{
				System.err.println("[err]XObjectJDOMSerializer::serializeObjectToString:"+ex);
			}
		}
		return null;
	}
	public JDOMSerializer()
	{
		
	}
	public Object serialize(String name,Object o) throws IllegalAccessException
	{
		Element elem=new Element(name);
		return serialize(elem,o);
	}
	protected Element serialize(Element elem,Object o) throws IllegalAccessException
	{
		if(o==null)
			return elem;
		//long ticket=System.currentTimeMillis();
		Class oclass=o.getClass();
		//FIXME: get all declared fields of self and super classes
		//getDeclaredFields only get fields of current class
		//getFields only get public fields
		Field[] fields=oclass.getFields();
		Field field;
		Class fclass;
		String fname;
		Class aclass;
		Element e;
		String value=null;
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
			if(!fname.equals("_") && 
					(fname.indexOf("$")>=0 || fname.endsWith("_")))	
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
			else if(fclass.isArray())
			{
				Object ao=field.get(o);
				if(ao!=null)
				{
					aclass=fclass.getComponentType();
					if(aclass.equals(byte.class))
					{
						value=formatByteArrayInner((byte[])ao);
					}
					else if(aclass.equals(int.class))
					{
						int[] iarray=(int[])ao;
						for(int p=0;p<iarray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatInt32Inner(iarray[p])));
						}
					}
					else if(aclass.equals(boolean.class))
					{
						boolean[] barray=(boolean[])ao;
						for(int p=0;p<barray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatBooleanInner(barray[p])));
						}
					}
					else if(aclass.equals(String.class))
					{
						String[] sarray=(String[])ao;
						for(int p=0;p<sarray.length;++p)
						{
							Element el=new Element(fname);
							String t=formatStringInner(sarray[p]);
							if(t.indexOf("<")>=0)
							{
								el.addContent(new CDATA(t));
							}
							else
							{
								el.setText(t);
							}
							elem.addContent(el);
						}
					}
					else if(aclass.equals(long.class))
					{
						long[] larray=(long[])ao;
						for(int p=0;p<larray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatLongInner(larray[p])));
						}
					}
					else if(aclass.equals(Date.class))
					{
						Date[] darray=(Date[])ao;
						for(int p=0;p<darray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatDateInner(darray[p])));
						}
					}
					else if(aclass.equals(double.class))
					{
						double[] dbarray=(double[])ao;
						for(int p=0;p<dbarray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatDoubleInner(dbarray[p])));
						}
					}
					else if(aclass.equals(float.class))
					{
						float[] farray=(float[])ao;
						for(int p=0;p<farray.length;++p)
						{
							elem.addContent(new Element(fname).setText(
									formatFloatInner(farray[p])));
						}
					}
					else if(XObjectBuilder.isXObject(aclass))
					{
						Object[] oarray=(Object[])ao;
						for(int p=0;p<oarray.length;++p)
						{
							e=new Element(fname);
							elem.addContent(serialize(e,oarray[p]));
						}
					}
					else if(aclass.isArray())
					{
						//TODO: implement array inside array
					}
				}
			}
			else
			{
				x=field.get(o);
				if(x instanceof XObject)
				{
					e=new Element(fname);
					elem.addContent(serialize(e,x));
				}
				else if(x!=null)
					value=x.toString();
			}
			if(value!=null)
			{
				if(fname.equals("_"))
				{
					if(value.indexOf("<")>=0)
					{
						elem.addContent(new CDATA(value));
					}
					else
					{
						elem.setText(value);
					}
				}
				else if(fname.startsWith("__")&&fname.endsWith("__"))
				{
					e=new Element(fname.substring(2,fname.length()-2),value);
					elem.addContent(e);
				}
				else if(fname.startsWith("_"))
				{
					elem.setAttribute(fname.substring(1), value);
				}
				else
				{
					e=new Element(fname);
					if(value.indexOf("<")>=0)
					{
						e.addContent(new CDATA(value));
					}
					else
					{
						e.setText(value);
					}
					elem.addContent(e);
				}
			}
		}
		//System.out.println("serialize time "+(System.currentTimeMillis()-ticket));
		return elem;
	}
}

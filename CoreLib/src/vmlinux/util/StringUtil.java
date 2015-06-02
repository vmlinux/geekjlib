package vmlinux.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vmlinux.codec.*;
import vmlinux.reflect.XDynamic;

@SuppressWarnings({"deprecation","unchecked"})
public abstract class StringUtil
{
	public static String getGUID()
	{
		java.util.UUID id=java.util.UUID.randomUUID();	// java 5
		return id.toString();
	}
	
	public static String fixedNumber(int n,int l)
	{
		if(l>16)
		{
			throw new RuntimeException("format error exceed max length 16<"+l);
		}
		String s=""+n;
		if(l<=s.length())
		{
			return s.substring(0,l);
		}
		else
		{
			return "0000000000000000".substring(0,l-s.length())+s;
		}
	}
	
	static SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	public static String getDateID()
	{
		StringBuffer sb=new StringBuffer(sdf.format(new Date()));
		java.util.Random rnd=new Random();
		int r=rnd.nextInt(8999)+1000;
		sb.append(r);
		return sb.toString();
	}
	
	public static String readFile(String file)
	{
		try
		{
			FileInputStream fis=new FileInputStream(file);
			byte[] buf=new byte[fis.available()];
			fis.read(buf);
			fis.close();
			return new String(buf);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return "";
	}

	public static String replacePlain(String src,String[] pair)
	{
		if(src==null || pair==null || pair.length==0)
			return src;
		String t=src;
		for(int i=0;i<pair.length;i+=2)
		{
			t=replacePlain(t,pair[i],pair[i+1]);
		}
		return t;
	}

	public static String replacePlain(String src,String[] toreplace,String[] replaceas)
	{
		if(src==null || toreplace==null || replaceas==null || toreplace.length==0)
			return src;
		String t=src;
		for(int i=0;i<replaceas.length;++i)
		{
			t=replacePlain(t,toreplace[i],replaceas[i]);
		}
		return t;
	}

	public static String replacePlain(String src,String toreplace,String replaceas)
	{
		if(src==null || toreplace==null || replaceas==null || toreplace.length()==0)
			return src;
		int p=0;
		int q=0;
		int l=toreplace.length();
		StringBuffer sb=new StringBuffer(src.length());
		while((p=src.indexOf(toreplace,q))>=0)
		{
			sb.append(src.substring(q, p));
			sb.append(replaceas);
			q=p+l;
		}
		if(q<src.length())
			sb.append(src.substring(q));
		return sb.toString();
	}
	
	public static String replaceCharPlain(String src,char[] chararray,String replaceas)
	{
		if(src==null || chararray==null || replaceas==null || 
				chararray.length==0)
			return src;
		String result=src;
		for(int i=0;i<chararray.length;++i)
		{
			result=replacePlain(result,""+chararray[i],replaceas);
		}
		return result;
	}
	
	public static String replaceCharPlain(String src,char[] chararray,String[] replaceas)
	{
		if(src==null || chararray==null || replaceas==null || 
				chararray.length==0)
			return src;
		//*
		String result=src;
		for(int i=0;i<chararray.length;++i)
		{
			result=replacePlain(result,""+chararray[i],replaceas[i]);
		}
		return result;
		/*/
		Arrays.sort(chararray);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<src.length();++i)
		{
			char c=src.charAt(i);
			int p=Arrays.binarySearch(chararray, c);
			if(p>=0)
			{
				sb.append(replaceas[p]);
			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
		*/
	}

	public static String replaceChar(String src,char[] chararray,String[] replaceas)
	{
		if(src==null || chararray==null || replaceas==null || 
				chararray.length==0)
			return src;
		//if replaceas.length<chararray.length() throw exception
		int p=0;
		char c;
		Arrays.sort(chararray);
		StringBuffer sb=new StringBuffer(src.length());
		for(int i=0;i<src.length();++i)
		{
			c=src.charAt(i);
			p=Arrays.binarySearch(chararray, c);
			if(p>=0)
				sb.append(replaceas[p]);
			else
				sb.append(c);
		}
		return sb.toString();
	}
	//realy fast???
	//use only for many many chars to replace
	public static String replaceCharFast(String src,char[] chararray,String[] replaceas)
	{
		if(src==null || chararray==null || replaceas==null || 
				chararray.length==0)
			return src;
		Map map=new HashMap(chararray.length);
		for(int i=0;i<chararray.length;++i)
		{
			map.put(new Character(chararray[i]), replaceas[i]);
		}
		Character c;
		Object s;
		StringBuffer sb=new StringBuffer(src.length());
		for(int i=0;i<src.length();++i)
		{
			c=new Character(src.charAt(i));
			s=map.get(c);
			if(s!=null)
				sb.append((String)s);
			else
				sb.append(c);
		}
		return sb.toString();
	}
	public static List splitPlain(String src,String boundary)
	{
		if(src==null || boundary==null || boundary.length()==0)
			return null;
		List result=new ArrayList();
		int p=0;
		int q=0;
		int l=boundary.length();
		while((p=src.indexOf(boundary,q))>=0)
		{
			result.add(src.substring(q,p));
			q=p+l;
		}
		if(q<src.length())
			result.add(src.substring(q));
		return result;
	}
	public static void splitPlain(String src,String boundary,IStringCase docase)
	{
		if(src==null || boundary==null || boundary.length()==0)
			return;
		int p=0;
		int q=0;
		int l=boundary.length();
		while((p=src.indexOf(boundary,q))>=0)
		{
			docase.doCase(src.substring(q,p));
			q=p+l;
		}
		if(q<src.length())
			docase.doCase(src.substring(q));
	}
	public static int countSequence(String src,String sequence)
	{
		if(src==null || sequence==null || sequence.length()==0)
			return 0;
		int p=0;
		int q=0;
		int l=sequence.length();
		int result=0;
		while((p=src.indexOf(sequence,q))>=0)
		{
			result++;
			q=p+l;
		}
		return result;
	}
	public static String trim(String src,char totrim)
	{
		if(src==null)
			return null;
		int len=src.length();
		int s=0;
		for(;s<len;++s)
		{
			if(totrim!=src.charAt(s))
				break;
		}
		int e=len-1;
		for(;e>s;--e)
		{
			if(totrim!=src.charAt(e))
				break;
		}
		return src.substring(s, e+1);
	}
	public static String afterLast(String src,String boundary)
	{
		if(src==null)
			return null;
		int p=src.lastIndexOf(boundary);
		return p>=0?src.substring(p+1):src;
	}
	public static String afterFirst(String src,String boundary)
	{
		if(src==null)
			return null;
		int p=src.indexOf(boundary);
		return p>=0?src.substring(p+1):src;
	}
	public static String beforeFirst(String src,String boundary)
	{
		if(src==null)
			return null;
		int p=src.indexOf(boundary);
		return p>=0?src.substring(0,p):src;
	}
	public static String beforeLast(String src,String boundary)
	{
		if(src==null)
			return null;
		int p=src.lastIndexOf(boundary);
		return p>=0?src.substring(0,p):src;
	}
	public static String concat(String[] values,String seperator,String leftDecorator,String rightDecorator)
	{
		return concat(values,seperator,leftDecorator,rightDecorator,values.length);
	}
	public static String concat(String[] values,String seperator,String leftDecorator,String rightDecorator,int n)
	{
		if(values==null)
			return null;
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<n;++i)
		{
			if(i>0)
				sb.append(seperator);
			sb.append(leftDecorator);
			sb.append(values[i]);
			sb.append(rightDecorator);
		}
		return sb.toString();
	}
	public static String concatQuote(String[] values,String seperator)
	{
		if(values==null)
			return null;
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<values.length;++i)
		{
			if(i>0)
				sb.append(seperator);
			sb.append("\"");
			sb.append(replacePlain(values[i], "\"", "\\\""));
			sb.append("\"");
		}
		return sb.toString();
	}
	public static String stringBetween(String src,String startTag,String endTag)
	{
		if(src==null)
			return null;
		int p=src.indexOf(startTag);
		if(p>=0)
		{
			p+=startTag.length();
			int q=src.indexOf(endTag, p);
			if(q>=0)
			{
				return src.substring(p,q);
			}
			else
			{
				return src.substring(p);
			}
		}
		return "";
	}
	public static String stringBetweenLast(String src,String startTag,String endTag)
	{
		if(src==null)
			return null;
		int p=src.lastIndexOf(startTag);
		if(p>=0)
		{
			p+=startTag.length();
			int q=src.indexOf(endTag, p);
			if(q>=0)
			{
				return src.substring(p,q);
			}
			else
			{
				return src.substring(p);
			}
		}
		return "";
	}
	//unescape utf-8 string
	public static String unescape(String str) throws UnsupportedEncodingException
	{
		char c;
		StringBuffer result=new StringBuffer(str.length());
		byte[] buff=new byte[4];
		int b=0;
		String code;
		try
		{
			for(int i=0;i<str.length();++i)
			{
				c=str.charAt(i);
				if(c=='%')
				{
					code=str.substring(i+1, i+3);
					try
					{
						b=Integer.parseInt(code,16);
					}
					catch(Exception ex)
					{
						result.append(c);
						continue;
					}
					if(0<b && b<128)
					{
						result.append((char)b);
						i+=2;
					}
					else if(128<=b && b<192)
					{
						//not utf-8
					}
					else if(192<=b && b<224)
					{
						buff[0]=(byte)b;
						buff[1]=(byte)Integer.parseInt(str.substring(i+4,i+6),16);
						result.append(new String(buff,0,2,"utf-8"));
						i+=5;
					}
					else if(224<=b && b<240)
					{
						buff[0]=(byte)b;
						buff[1]=(byte)Integer.parseInt(str.substring(i+4,i+6),16);
						buff[2]=(byte)Integer.parseInt(str.substring(i+7,i+9),16);
						result.append(new String(buff,0,3,"utf-8"));
						i+=8;
					}
					else if(240<=b && b<248)
					{
						buff[0]=(byte)b;
						buff[1]=(byte)Integer.parseInt(str.substring(i+4,i+6),16);
						buff[2]=(byte)Integer.parseInt(str.substring(i+7,i+9),16);
						buff[3]=(byte)Integer.parseInt(str.substring(i+10,i+12),16);
						result.append(new String(buff,"utf-8"));
						i+=11;
					}
					else
					{
						//ignore
					}
				}
				else
				{
					result.append(c);
				}
			}
		}
		catch(NumberFormatException ex)
		{
			System.out.println("[warn]Can't complete unescape string: "+str);
		}
		return result.toString();
	}
	public static String decodeEntity(String html)
	{
		int l=html.length();
		StringBuffer result=new StringBuffer(l);
		char c;
		String entity;
		int pos;
		
		for(int i=0;i<l;++i)
		{
			c=html.charAt(i);
			if(c=='&')
			{
				pos=html.indexOf(';',i);
				if(pos>0)
				{
					entity=html.substring(i, pos+1);
					i=pos;
				}
				else
				{
					entity=html.substring(i);
					i=html.length();
				}
				if(entity.equals("&quot;"))
					result.append("\"");
				else if(entity.equals("&amp;"))
					result.append("&");
				else if(entity.equals("&lt;"))
					result.append("<");
				else if(entity.equals("&gt;"))
					result.append(">");
				else if(entity.equals("&#44;"))
					result.append(",");
				else if(entity.equals("&#91;"))
					result.append("[");
				else if(entity.equals("&#93;"))
					result.append("]");
				else if(entity.equals("&#123;"))
					result.append("{");
				else if(entity.equals("&#125;"))
					result.append("}");
				else
					result.append(entity);
			}
			else if(c=='%' && i+2<l && html.substring(i+1,i+3).matches("[123456789ABCDEFabcdef][0123456789ABCDEFabcdef]"))
			{
				String s=html.substring(i+1,i+3);
				result.append((char)Integer.valueOf(s, 16).intValue());
				i+=2;
			}
			else if(c=='%' && i+5<l && html.substring(i+1,i+6).matches("[uU][123456789ABCDEFabcdef][0123456789ABCDEFabcdef]{3}"))
			{
				String s=html.substring(i+2,i+6);
				result.append((char)Integer.valueOf(s, 16).intValue());
				i+=5;
			}
			else
			{
				result.append(c);
			}
		}
		return result.toString();
	}
	
	public static String md5Digest(String str)
	{
		byte[] digest=null;//new byte[32];
		try
		{
			MessageDigest md5=MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			digest=md5.digest();
		}
		catch(Exception ex)
		{
			
		}
		return Convert.toByteHexString(digest);
	}
	
	public static String sha1Digest(String str)
	{
		byte[] digest=null;//new byte[40];
		try
		{
			MessageDigest sha1=MessageDigest.getInstance("SHA-1");
			sha1.update(str.getBytes());
			digest=sha1.digest();
		}
		catch(Exception ex)
		{
			
		}
		return Convert.toByteHexString(digest);
	}

	/**
	 * subroute for format
	 * @param n
	 * @param param
	 * @return
	 */
	private static String format_param_int(Integer n,String param)
	{
		if(param==null)
			return n.toString();
		if("H".equals(param))
		{
			return Integer.toHexString(n).toUpperCase();
		}
		else if("h".equals(param))
		{
			return Integer.toHexString(n).toLowerCase();
		}
		else if("o".equalsIgnoreCase(param))
		{
			return Integer.toOctalString(n);
		}
		else if("b".equalsIgnoreCase(param))
		{
			return Integer.toBinaryString(n);
		}
		else if(param.startsWith("%"))
		{
			return String.format(param, n);
		}
		else
		{
			return n.toString();
		}
	}
	
	/**
	 * subroute for format
	 * @param d
	 * @param param
	 * @return
	 */
	private static String format_param_double(Double d,String param)
	{
		if(param==null)
		{
			return d.toString();
		}
		else
		{
			return String.format(param, d);
		}
	}
	
	/**
	 * subroute for format
	 * @param dt
	 * @param param
	 * @return
	 */
	private static String format_param_date(java.util.Date dt,String param)
	{
		if(param==null)
		{
			return dt.toString();
		}
		if(param.startsWith("%"))
		{
			return String.format(param, dt);
		}
		else if(dt==null)
		{
			return "";
		}
		else
		{
			return new SimpleDateFormat(param).format(dt);
		}
	}
	
	/**
	 * subroute for format
	 * @param s
	 * @param param
	 * @return
	 */
	private static String format_param_string(String s,String param)
	{
		if(param==null)
		{
			return s==null?"":s;
		}
		if("md5".equalsIgnoreCase(param))
		{
			return md5Digest(s);
		}
		else if("sha1".equalsIgnoreCase(param))
		{
			return sha1Digest(s);
		}
		else if("base64".equalsIgnoreCase(param))
		{
			return Base64.encode(s.getBytes());
		}
		else if("debase64".equalsIgnoreCase(param))
		{
			return new String(Base64.decode(s));
		}
		else if("base64z".equalsIgnoreCase(param))
		{
			LZW lzw=new LZW();
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try
			{
				lzw.compress(new ByteArrayInputStream(s.getBytes()), bos);
			}
			catch(Exception ex)
			{
				
			}
			byte[] buf=bos.toByteArray();
			return Base64.encode(buf);
		}
		else if("debase64z".equalsIgnoreCase(param))
		{
			LZW lzw=new LZW();
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try
			{
				lzw.decompress(new ByteArrayInputStream(Base64.decode(s)), bos);
			}
			catch(Exception ex)
			{
				
			}
			return new String(bos.toByteArray());
		}
		else if("url".equalsIgnoreCase(param))
		{
			try
			{
				return java.net.URLEncoder.encode(s,"utf-8");
			}
			catch(Exception ex)
			{
				
			}
			return s;
		}
		else if("deuni".equalsIgnoreCase(param))
		{
			return decodeUnicode(s);
		}
		else if(param!=null && param.matches("s\\d+(,\\d+)?"))
		{
			String[] l=param.split(",",2);
			int spos=Integer.valueOf(l[0].substring(1));
			int len=s.length();
			if(spos>=len)
			{
				return "";
			}
			else if(l.length>1)
			{
				int epos=Integer.valueOf(l[1]);
				if(epos>=len)
				{
					return s.substring(spos);
				}
				else
				{
					return s.substring(spos, epos);
				}
			}
			else
			{
				return s.substring(spos);
			}
		}
		else if(param!=null && param.matches("(d[\\+\\-\\*\\/]\\d+)?(,\\d+(\\.\\d+)?[df])?"))
		{
			String[] l=param.split(",",2);
			String cal=l[0];
			String fmt=l.length>1?l[1]:"d";
			long n=Long.valueOf(s);
			if(cal.length()>=3)
			{
				long d=Long.valueOf(cal.substring(2));
				char c=cal.charAt(1);
				if(c=='+')
				{
					n+=d;
				}
				else if(c=='-')
				{
					n-=d;
				}
				else if(c=='*')
				{
					n*=d;
				}
				else if(c=='/')
				{
					n/=d;
				}
				s=""+n;
			}
			if(fmt.endsWith("d"))
			{
				String f=fmt.substring(0, fmt.length()-1);
				l=f.split("\\.",2);
				int len=Integer.valueOf(l[0]);
				for(int i=s.length();i<len;++i)
				{
					s="0"+s;
				}
			}
			return s;
		}
		else if(s==null)
		{
			return "";
		}
		else
		{
			return s;
		}
	}
	
	/**
	 * subroute for format
	 * @param o
	 * @param field
	 * @param param
	 * @return
	 */
	private static String format_param_obj(Object o,String field,String param,String param2)
	{
		Class o_class=o.getClass();
		try
		{
			String f_output=null;
			if(field==null)
			{
				Method m=null;
				if(param==null)
				{
					m=o_class.getMethod("toString", new Class[]{});
					if(m!=null)
					{
						f_output=(String)m.invoke(o, new Object[]{});
					}
				}
				else
				{
					m=o_class.getMethod("toString", new Class[]{String.class});
					if(m!=null)
					{
						f_output=(String)m.invoke(o, new Object[]{param});
					}
				}
			}
			else
			{
				Field o_field=o_class.getField(field);
				Class f_class=o_field.getType();
				Object v=o_field.get(o);
				if(int.class.equals(f_class) || Integer.class.equals(f_class))
				{
					f_output=format_param_int((Integer)v,param);
				}
				else if(String.class.equals(f_class))
				{
					f_output=format_param_string((String)v,param);
				}
				else if(double.class.equals(f_class) || Double.class.equals(f_class))
				{
					f_output=format_param_double((Double)v,param);
				}
				else if(java.util.Date.class.equals(f_class))
				{
					f_output=format_param_date((java.util.Date)v,param);
				}
				else if(float.class.equals(f_class) || Float.class.equals(f_class))
				{
					f_output=format_param_double(((Float)v).doubleValue(),param);
				}
				else if(byte.class.equals(f_class) || Byte.class.equals(f_class))
				{
					f_output=format_param_int(((Byte)v).intValue(),param);
				}
				else if(Map.class.isAssignableFrom(f_class))
				{
					f_output=format_param_map((Map)v,param,param2);
				}
				else
				{
					f_output=format_param(param, new Object[]{v});
					/*
					f_output="[o:]";
					Method m=null;
					if(param==null)
					{
						m=f_class.getMethod("toString", new Class[]{});
						if(m!=null)
						{
							f_output=(String)m.invoke(o, new Object[]{});
						}
					}
					else
					{
						m=f_class.getMethod("toString", new Class[]{String.class});
						if(m!=null)
						{
							f_output=(String)m.invoke(o, new Object[]{param});
						}
					}
					*/
				}
			}
			return f_output;
		}
		catch(Exception ex)
		{
			return "[e:"+ex.getMessage()+"]";
		}
	}
	
	private static String format_param_map(Map map,String field,String param)
	{
		try
		{
			String f_output=null;
			if(field==null)
			{
				return "[e:map key not specified]";
			}
			else
			{
				Object v=map.get(field);
				Class f_class=v.getClass();
				if(int.class.equals(f_class) || Integer.class.equals(f_class))
				{
					f_output=format_param_int((Integer)v,param);
				}
				else if(String.class.equals(f_class))
				{
					f_output=format_param_string((String)v,param);
				}
				else if(double.class.equals(f_class) || Double.class.equals(f_class))
				{
					f_output=format_param_double((Double)v,param);
				}
				else if(java.util.Date.class.equals(f_class))
				{
					f_output=format_param_date((java.util.Date)v,param);
				}
				else if(float.class.equals(f_class) || Float.class.equals(f_class))
				{
					f_output=format_param_double(((Float)v).doubleValue(),param);
				}
				else if(byte.class.equals(f_class) || Byte.class.equals(f_class))
				{
					f_output=format_param_int(((Byte)v).intValue(),param);
				}
				else if(Map.class.isAssignableFrom(f_class))
				{
					f_output=format_param_map((Map)v,field,param);
				}
				else
				{
					f_output=format_param(param, new Object[]{v});
				}
			}
			return f_output;
		}
		catch(Exception ex)
		{
			return "[e:"+ex.getMessage()+"]";
		}
	}
	
	/**
	 * subroute for format
	 * @param name
	 * @param params
	 * @return
	 */
	private static String format_param(String name,Object[] params)
	{
		if(name==null && params!=null && params.length>0)
		{
			return format_param_obj(params[0], null, null, null);
		}
		String[] p=name.split(":",2);
		String[] n=p[0].split("\\.",2);
		String o_param=null;
		String o_param2=null;
		int o_index=0;
		String o_field=null;
		Object o=null;
		if(p.length>1)
		{
			o_param=p[1];
		}
		if(n.length>1)
		{
			if(!n[0].matches("^\\d+$"))
			{
				o_field=n[0];
				o=params[0];
				o_param2=o_param;
				o_param=n[1];
			}
			else
			{
				o_field=n[1];
				o_index=Integer.valueOf(n[0]);
				o=params[o_index];
			}
		}
		else
		{
			if(n[0].matches("^\\d+$"))
			{
				o_index=Integer.valueOf(n[0]);
			}
			else
			{
				o_field=n[0];
			}
			o=params[o_index];
		}
		if(o==null)
		{
			return "";
		}
		Class o_class=o.getClass();
		String o_output=null;
		if(int.class.equals(o_class) || Integer.class.equals(o_class))
		{
			o_output=format_param_int((Integer)o,o_param);
		}
		else if(String.class.equals(o_class))
		{
			o_output=format_param_string((String)o,o_param);
		}
		else if(double.class.equals(o_class) || Double.class.equals(o_class))
		{
			o_output=format_param_double((Double)o,o_param);
		}
		else if(java.util.Date.class.equals(o_class))
		{
			o_output=format_param_date((java.util.Date)o,o_param);
		}
		else if(float.class.equals(o_class) || Float.class.equals(o_class))
		{
			o_output=format_param_double(((Float)o).doubleValue(),o_param);
		}
		else if(byte.class.equals(o_class) || Byte.class.equals(o_class))
		{
			o_output=format_param_int(((Byte)o).intValue(),o_param);
		}
		else if(Map.class.isAssignableFrom(o_class))
		{
			o_output=format_param_map((Map)o,o_field,o_param);
		}
		else
		{
			o_output=format_param_obj(o,o_field,o_param,o_param2);
		}
		return o_output;
	}
	
	/**
	 * format and write system.out
	 * @param fmt
	 * @param params
	 * @return
	 */
	public static void formatOutln(String fmt,Object... params)
	{
		System.out.println(StringUtil.format(fmt, params));
	}
	
	/**
	 * format and write system.out
	 * @param fmt
	 * @param params
	 * @return
	 */
	public static void formatOut(String fmt,Object... params)
	{
		System.out.print(StringUtil.format(fmt, params));
	}
	
	/**
	 * object format function
	 * @param fmt
	 * @param params
	 * @return
	 */
	public static String format(String fmt,Object... params)
	{
		StringBuffer sb=new StringBuffer();	//{0} {1.attr} {1.attr:H}
		char c;
		int s=0;	//0:out {}; 1:in {}
		int l=fmt.length();
		StringBuffer n=new StringBuffer();
		String name;
		for(int i=0;i<l;++i)
		{
			c=fmt.charAt(i);
			if(s==0)
			{
				if(c=='{')
				{
					if(i<l-1 && fmt.charAt(i+1)=='{')
					{
						//skip
						sb.append('{');
						++i;
					}
					else
					{
						s=1;
					}
				}
				else if(c=='}')
				{
					sb.append('}');
					if(i<l-1 && fmt.charAt(i+1)=='}')
					{
						//skip
						++i;
					}
				}
				else
				{
					sb.append(c);
				}
			}
			else if(s==1)
			{
				if(c=='}')
				{
					if(i<l-1 && fmt.charAt(i+1)=='}')
					{
						//skip
						n.append('}');
						++i;
					}
					else
					{
						name=n.toString();
						//System.out.println("name="+name);
						sb.append(format_param(name,params));
						n.delete(0, n.length());
						s=0;
					}
				}
				else if(c=='{')
				{
					n.append('{');
					if(i<l-1 && fmt.charAt(i+1)=='{')
					{
						//skip
						++i;
					}
				}
				else
				{
					n.append(c);
				}
			}
		}
		return sb.toString();
	}
	
	public static int getInteger(String s,int n)
	{
		try
		{
			return Integer.valueOf(s);
		}
		catch(Exception ex)
		{
			
		}
		return n;
	}
	
	public static String getString(String s,String d)
	{
		return s==null?d:s;
	}
	
	public static String getSystemProperty(String n)
	{
		String s=System.getenv(n);
		if(s==null)
		{
			s=System.getProperty(n);
		}
		return s;
	}
	
	public static String decodeString(String s)
	{
		try
		{
			String s1=new String(s.getBytes("ISO8859-1"),"utf-8");
			String s2=new String(s.getBytes("ISO8859-1"),"gb2312");
			String s3=new String(s1.getBytes("utf-8"),"ISO8859-1");
			//String s4=new String(s1.getBytes("gb2312"),"ISO8859-1");
			if(s.equals(s3))
			{
				return s1;
			}
			return s2;
		}
		catch(Exception ex)
		{
			
		}
		return null;
	}
	
	public static String toCnNumber(String s)
	{
		return toCnNumber(s,"s");
	}
	
	public static String toCnNumber(String s,String t)
	{
		final XDynamic numMap=new XDynamic((
				"0,零,1,壹,2,贰,3,叁,4,肆,5,伍,6,陆,7,柒,8,捌,9,玖"
				+",s,拾,b,佰,k,仟,w,萬,y,亿,wy,万亿"
				+",s0,零,s1,一,s2,二,s3,三,s4,四,s5,五,s6,六,s7,七,s8,八,s9,九"
				+",ss,十,sb,百,sk,千,sw,万,sy,亿,swy,万亿"
				).split(","));
		
		if(numMap.getProperty(t+"0")==null)
		{
			throw new RuntimeException("Invalid template name "+t);
		}
		StringBuffer sb=new StringBuffer();
		int l=s.length();
		int cz=0;
		for(int i=0;i<l;++i)
		{
			char c=s.charAt(i);
			String u="";
			int m=(l-i-1)%4;
			if(m==0)
			{
				int a=(l-i-1)/4;
				if(a==1)
				{
					u="w";
				}
				else if(a==2)
				{
					u="y";
				}
				else if(a==3)
				{
					u="wy";
				}
				else if(a>0)
				{
					u="?";
				}
			}
			else if(m==3)
			{
				u="k";
			}
			else if(m==2)
			{
				u="b";
			}
			else if(m==1)
			{
				u="s";
			}
			if(c=='0')
			{
				if(m==0 && u.length()>0 && cz<3)
				{
					sb.append(numMap.getProperty(t+u));
				}
				cz++;
			}
			else
			{
				if(c=='1' && "s".equals(t) && "s".equals(u))
				{
					if(cz>0)
					{
						sb.append(numMap.getProperty(t+"0"));
					}
					sb.append(numMap.getProperty(t+u));
				}
				else
				{
					if(cz>0)
					{
						sb.append(numMap.getProperty(t+"0"));
					}
					sb.append(numMap.getProperty(t+c));
					if(u.length()>0)
					{
						sb.append(numMap.getProperty(t+u));
					}
				}
				cz=0;
			}
		}
		if(cz>0 && sb.length()==0)
		{
			sb.append(numMap.getProperty(t+"0"));
		}
		return sb.toString();
	}
	
	public static long parseCnNumber(String s)
	{
		final XDynamic charMap=new XDynamic(
				("1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,0,0"
				+",一,1,二,2,三,3,四,4,五,5,六,6,七,7,八,8,九,9,十,s"
				+",壹,1,贰,2,叁,3,肆,4,伍,5,陆,6,柒,7,捌,8,玖,9,拾,s"
				+",零,0,百,b,千,k,万,w,亿,y,佰,b,仟,k,萬,w,两,2"
				).split(","));
		
		long num=0;
		StringBuffer n=new StringBuffer();	//number
		int i=0;
		for(;i<s.length();++i)
		{
			char c=s.charAt(i);
			if(c>='0' && c<='9')
			{
				n.append(c);
			}
			else
			{
				break;
			}
		}
		if(n.length()>0)
		{
			num=Long.parseLong(n.toString());
			n.delete(0, n.length());
		}
		int u=0;
		for(;i<s.length();++i)
		{
			char c=charMap.getProperty(s.substring(i,i+1)).charAt(0);
			if(c>='0' && c<='9')
			{
				n.append(c);
			}
			else
			{
				int a=(n.toString().matches("\\d+"))?Integer.valueOf(n.toString()):0;
				u=1;
				if(c=='b')
				{
					u=100;
				}
				else if(c=='k')
				{
					u=1000;
				}
				else if(c=='w')
				{
					u=10000;
				}
				else if(c=='y')
				{
					u=100000000;
				}
				else if(c=='s')
				{
					u=10;
				}
				else
				{
					throw new NumberFormatException("Invalid char "+c);
				}
				if(u>1 && a==0)
				{
					a=1;
				}
				if(n.length()==0 && num>0)
				{
					num*=u;
				}
				else
				{
					num+=a*u;
					n.delete(0, n.length());
				}
			}
		}
		if(n.length()>0)
		{
			if(u==0)	//no any unit
			{
				num=(n.toString().matches("\\d+"))?Long.valueOf(n.toString()):0;
			}
			else
			{
				int l=n.length();
				while(l>0)
				{
					u/=10;
					l--;
					if(u<1)
					{
						throw new NumberFormatException("Invalid number after last unit "+n.toString());
					}
				};
				long a=(n.toString().matches("\\d+"))?Long.valueOf(n.toString()):0;
				num+=a*u;
			}
		}

		return num;
	}
	
	static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
	public static String decodeUnicode(String s)
	{
		Matcher m = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find())
		{
			m.appendReplacement(sb,
					Character.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
}

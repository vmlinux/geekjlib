package vmlinux.util;

import java.io.*;
import java.util.*;

public class CSVReader extends LineReader implements Map
{
	private String line;
	private String headline;
	private int lineno;
	private String[] headers;
	private Hashtable<String, Integer> headerHash;
	private String[] datas;
	private char delimiter=',';
	
	public CSVReader(String file) throws IOException
	{
		super(new FileInputStream(file),"gb2312");
		lineno=0;
	}
	
	public CSVReader(InputStream input)
	{
		super(input,"gb2312");
		lineno=0;
	}
	
	public void setTabDelimited()
	{
		delimiter='\t';
	}
	
	public CSVReader(InputStream input,String head)
	{
		super(input,"gb2312");
		lineno=1;
		line=head;
		headers=parseLine();
		headerHash=new Hashtable<String, Integer>();
		for(int i=0;i<headers.length;++i)
		{
			headerHash.put(headers[i], new Integer(i));
		}
	}
	
	
	public String getHeader(int i)
	{
		if(headers!=null && i<headers.length)
			return headers[i];
		return null;
	}
	
	public boolean changeHeader(String s1,String s2)
	{
		if(headers!=null && headerHash!=null && !s1.equals(s2))
		{
			Integer n=headerHash.get(s1);
			if(n!=null)
			{
				headerHash.put(s2, n);
				headers[n]=s2;
				//headerHash.remove(s1);
				return true;
			}
		}
		return false;
	}
	
	public boolean setHeaders(String[] s)
	{
		headers=new String[s.length];
		for(int i=0;i<s.length;++i)
		{
			headerHash.put(s[i], i);
			headers[i]=s[i];
		}
		return s.length>0 && s.length==columns();
	}
	
	public void toSqlHeaders()
	{
		if(headers!=null)
		{
			headerHash.clear();
			for(int i=0;i<headers.length;++i)
			{
				String h=headers[i];
				String hh=h.replaceAll("[\\(（][^\\)）]*[\\)）]", "");
				hh=hh.replaceAll("[\\.\\+\\-\\*\\|\\(\\)\\?/,=;'\"]", "");
				int o=1;
				String ho=hh.length()>9?hh.substring(0, 9):hh;
				while(headerHash.get(ho)!=null)
				{
					ho=hh+o;
					++o;
				}
				headers[i]=ho;
				headerHash.put(ho, i);
			}
		}
	}
	
	public int columns()
	{
		if(headers!=null)
			return headers.length;
		else
			return 0;
	}
	
	public int rows()
	{
		return lineno;
	}
	
	public String getData(String name)
	{
		if(headerHash!=null)
		{
			Integer n=headerHash.get(name);
			if(n!=null)
				return getData(n);
		}
		return null;
	}
	
	public String getData(int n)
	{
		if(line!=null && datas==null)
		{
			datas=parseLine();
		}
		if(n<datas.length)
		{
			return datas[n]==null?null:datas[n].trim();
		}
		return null;
	}
	
	public Hashtable<String, String> getRow()
	{
		Hashtable<String, String> row=new Hashtable<String, String>();
		if(line!=null && datas==null)
		{
			datas=parseLine();
		}
		if(datas.length>0)
		{
			for(int i=0;i<datas.length;++i)
			{
				row.put(headers[i], datas[i]==null?"":datas[i].trim());
			}
		}
		return row;
	}
	
	protected String[] parseLine()
	{
		if(headers!=null)
		{
			if(line.indexOf("\"")<0)
			{
				return line.split(""+delimiter,headers.length);
			}
			String[] data=new String[headers.length];
			boolean indata=false;
			StringBuffer sb=new StringBuffer();
			int col=0;
			for(int i=0;i<line.length();++i)
			{
				char c=line.charAt(i);
				if(indata)
				{
					if(c=='"')
					{
						indata=false;

						//data[col++]=StringUtil.replacePlain(sb.toString(),"\\\'","\"");
						//sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
				else
				{
					if(c=='"')
					{
						indata=true;
					}
					else if(c==delimiter)
					{
						data[col++]=StringUtil.replacePlain(sb.toString(),"\\\'","\"");
						sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
			}
			if(sb.length()>0 && col<data.length)
			{
				data[col++]=StringUtil.replacePlain(sb.toString(),"\\\'","\"");
			}
			return data;
		}
		else
		{
			if(line.indexOf("\"")<0)
			{
				return line.split(""+delimiter);
			}
			ArrayList<String> data=new ArrayList<String>();
			boolean indata=false;
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<line.length();++i)
			{
				char c=line.charAt(i);
				if(indata)
				{
					if(c=='"')
					{
						indata=false;

						//data.add(StringUtil.replacePlain(sb.toString(),"\\\'","\""));
						//sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
				else
				{
					if(c=='"')
					{
						indata=true;
					}
					else if(c==delimiter)
					{
						data.add(StringUtil.replacePlain(sb.toString().trim(),"\\\'","\""));
						sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
			}
			if(sb.length()>0)
			{
				data.add(StringUtil.replacePlain(sb.toString().trim(),"\\\'","\""));
			}
			String[] arr=new String[data.size()];
			for(int i=0;i<data.size();++i)
			{
				arr[i]=data.get(i);
			}
			return arr;
		}

	}
	
	public String headLine()
	{
		return headline;
	}

	public String currentLine()
	{
		return line;
	}

	public boolean next() throws IOException
	{
		line=super.readLine();
		if(lineno==0)
		{
			headline=line;
			headers=parseLine();
			headerHash=new Hashtable<String, Integer>();
			for(int i=0;i<headers.length;++i)
			{
				headerHash.put(headers[i], new Integer(i));
			}
			lineno++;
			return next();
		}
		else
		{
			datas=null;
			if(line!=null)
			{
				lineno++;
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	@Override
	public int size()
	{
		return headerHash.size();
	}

	@Override
	public boolean isEmpty()
	{
		return headerHash.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return headerHash.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public Object get(Object key)
	{
		return getData((String)key);
	}

	@Override
	public Object put(Object key, Object value)
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public Object remove(Object key)
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public void putAll(Map m)
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public void clear()
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public Set keySet()
	{
		return headerHash.keySet();
	}

	@Override
	public Collection values()
	{
		throw new RuntimeException("not implemented");
	}

	@Override
	public Set entrySet()
	{
		throw new RuntimeException("not implemented");
	}

}

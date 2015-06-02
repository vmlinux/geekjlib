package vmlinux.util;

import java.util.*;

public class ExcelReader
{
	private ExcelSheet sheet;
	private String[] headers;
	private Hashtable<String, Integer> headerHash;
	private int lineno=1;
	
	public ExcelReader(String file)
	{
		ExcelSheet sh=new ExcelSheet(file, "gb2312");
		sh.setSheet(0);
		init(sh);
	}
	
	public ExcelReader(ExcelSheet sh)
	{
		init(sh);
	}
	
	public ExcelReader(String file,String sheet)
	{
		ExcelSheet sh=new ExcelSheet(file, "gb2312");
		sh.setSheet(sheet);
		init(sh);
	}

	public ExcelReader(String file,int sheet)
	{
		ExcelSheet sh=new ExcelSheet(file, "gb2312");
		sh.setSheet(sheet);
		init(sh);
	}

	protected void init(ExcelSheet sheet)
	{
		this.sheet=sheet;
		String v=null;
		int c=0;
		ArrayList<String> arr=new ArrayList<String>();
		while((v=sheet.getCellString(c,0))!=null && v.length()>0)
		{
			arr.add(v);
			c++;
		}
		headers=new String[arr.size()];
		headerHash=new Hashtable<String, Integer>();
		for(int i=0;i<headers.length;++i)
		{
			String name=arr.get(i);
			headers[i]=name;
			headerHash.put(name, new Integer(i));
		}
		lineno=0;
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
				return true;
			}
		}
		return false;
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
		if(n<headers.length)
		{
			return sheet.getCellString(n,lineno);
		}
		return null;
	}

	public boolean next()
	{
		lineno++;
		if(sheet!=null)
		{
			for(int i=0;i<headers.length;++i)
			{
				String v=sheet.getCellString(i,lineno);
				if(v!=null && v.length()>0)
					return true;
			}
			return false;
		}
		return false;
	}
	
	public void close()
	{
		sheet.close();
	}
}

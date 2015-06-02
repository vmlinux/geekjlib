package vmlinux.util;

import java.io.*;
import java.util.*;

public class TextConfig
{
	Hashtable<String, String> items;
	
	public TextConfig(String file,String encoding) throws IOException
	{
		items=new Hashtable<String, String>();
		FileInputStream fis=new FileInputStream(file);
		byte[] buf=new byte[fis.available()];
		fis.read(buf);
		String text=new String(buf,encoding);
		parseString(text);
	}
	
	public TextConfig(InputStream is) throws IOException
	{
		items=new Hashtable<String, String>();
		byte[] buf=new byte[is.available()];
		is.read(buf);
		String text=new String(buf);
		parseString(text);
	}
	
	public void parseString(String text)
	{
		String[] lines=text.split("\n");
		for(int i=0;i<lines.length;++i)
		{
			String line=lines[i].trim();
			if(line.startsWith("#")
					|| line.startsWith("//")
					|| line.startsWith("--"))
			{
				continue;
			}
			String[] pair=line.split("=",2);
			if(pair[0].length()>0)
			{
				items.put(pair[0].trim().toLowerCase(), (pair[1]==null)?null:pair[1].trim());
			}
		}
	}
	
	public TextConfig()
	{
		items=new Hashtable<String, String>();
	}
	
	public String getOption(String name)
	{
		return items.get(name.toLowerCase());
	}
	
	public void setOption(String name,String value)
	{
		items.put(name, value);
	}
	
	public Hashtable<String, String> getItems()
	{
		return items;
	}
}

package vmlinux.reflect;

import java.util.*;

import vmlinux.reflect.serializer.*;

@SuppressWarnings("unchecked")
public class XDynamic extends Hashtable implements XObject
{
	static final long serialVersionUID=20090416090000L;
	
	public XDynamic()
	{
	}
	
	public XDynamic(String[] str)
	{
		addAll(str);
	}
	
	public XDynamic(XObject o)
	{
		Map m=MapSerializer.serializeObject(o);
		this.putAll(m);
	}
	
	public String getProperty(String name)
	{
		return (String)this.get(name);
	}
	
	public void setProperty(String name,Object value)
	{
		//System.out.println("[ "+name+" ]="+value);
		this.put(name, value);
	}
	
	public int addAll(String[] str)
	{
		for(int i=0;i<str.length-1;i+=2)
		{
			String key=str[i];
			String val=str[i+1];
			put(key,val);
		}
		return str.length>>1;
	}
}

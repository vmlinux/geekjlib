package vmlinux.reflect.source;

import java.util.HashMap;

import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSource;
import vmlinux.util.JSONParser;

@SuppressWarnings("unchecked")
public class JSONSource extends MapSource
{
	public static Object buildObject(String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new JSONSource(src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSONSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(String prefix,String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new JSONSource(prefix,src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSONSource::buildObject:"+ex);
		}
		return null;
	}
	public JSONSource(String prefix,String jsonstr)
	{
		super(prefix,new HashMap());
		parseJSONString(jsonstr);
	}
	public JSONSource(String jsonstr)
	{
		super(new HashMap());
		parseJSONString(jsonstr);
	}
	
	protected void parseJSONString(String str)
	{
		JSONParser parser=new JSONParser(16);
		parser.parse(str, this.source).entrySet();
	}
}

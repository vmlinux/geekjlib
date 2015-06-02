package vmlinux.reflect.source;

import java.util.HashMap;

import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSource;
import vmlinux.util.JSParser;

@SuppressWarnings("unchecked")
public class JSSource extends MapSource
{
	public static Object buildObject(String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new JSSource(src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(String prefix,String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new JSSource(prefix,src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]JSSource::buildObject:"+ex);
		}
		return null;
	}

	public JSSource(String prefix,String jsstr)
	{
		super(prefix,new HashMap());
		parseJSString(jsstr);
	}
	
	public JSSource(String jsstr)
	{
		super(new HashMap());
		parseJSString(jsstr);
	}
	
	protected void parseJSString(String str)
	{
		JSParser parser=new JSParser();
		parser.parse(str,this.source).entrySet();
	}
}

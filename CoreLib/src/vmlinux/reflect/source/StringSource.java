package vmlinux.reflect.source;

import java.util.HashMap;
import java.util.List;

import vmlinux.codec.Parameter;
import vmlinux.reflect.XObjectBuilder;
import vmlinux.reflect.XObjectSource;
import vmlinux.util.IStringCase;
import vmlinux.util.StringUtil;

@SuppressWarnings("unchecked")
public class StringSource extends MapSource
{
	public static Object buildObject(String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new StringSource(src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]StringSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(String prefix,String src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		XObjectSource source=new StringSource(prefix,src);
		try
		{
			return builder.build(source, xclass);
		}
		catch(Exception ex)
		{
			System.err.println("[err]StringSource::buildObject:"+ex);
		}
		return null;
	}
	
	class SplitCase implements IStringCase
	{
		public void doCase(String data)
		{
			List pair=StringUtil.splitPlain(data, "=");
			if(pair.size()==2)
				source.put(pair.get(0), Parameter.decode((String)pair.get(1)));
			else
				source.put(pair.get(0), "");
		}
	}
	public StringSource(String src)
	{
		super(new HashMap());
		StringUtil.splitPlain(src, "&", new SplitCase());
	}
	public StringSource(String prefix,String src)
	{
		super(prefix,new HashMap());
		StringUtil.splitPlain(src, "&", new SplitCase());
	}
}

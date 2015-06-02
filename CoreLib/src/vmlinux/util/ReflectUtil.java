package vmlinux.util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation","unchecked"})
public abstract class ReflectUtil
{
	public static boolean isClassOf(Class fclass,Class clazz)
	{
		Class[] classes=fclass.getInterfaces();
		for(int i=0;i<classes.length;++i)
		{
			if(clazz.equals(classes[i]))
				return true;
		}
		return false;
	}
	
	public static boolean isTypeOf(Class fclass,Class clazz)
	{
		return clazz.isAssignableFrom(fclass);
	}
	
	public static boolean isAbstract(Class clazz)
	{
		return Modifier.isAbstract(clazz.getModifiers());
	}
	
	public static List getClasses(String pkgname) throws ClassNotFoundException
	{
		String path=StringUtil.replacePlain(pkgname, ".", "/");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url=loader.getResource(path);
		if(url==null)	//try again in some version
			url=loader.getResource("/"+path);
		if(url!=null)
		{
			File file=new File(URLDecoder.decode(url.getFile()));
			if(file.exists() && file.isDirectory())
			{
				String[] files=file.list();
				List classes=new ArrayList(files.length);
				String fname;
				for(int i=0;i<files.length;++i)
				{
					fname=files[i];
					if(fname.endsWith(".class"))
					{
						classes.add(Class.forName(
								pkgname+"."+
								fname.substring(0,fname.length()-6)));
					}
				}
				return classes;
			}
		}
		return null;
	}
	public static List getClassNames(String pkgname)
	{
		String path=StringUtil.replacePlain(pkgname, ".", "/");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url=loader.getResource(path);
		if(url==null)	//try again in some version
			url=loader.getResource("/"+path);
		if(url!=null)
		{
			File file=new File(URLDecoder.decode(url.getFile()));
			if(file.exists() && file.isDirectory())
			{
				String[] files=file.list();
				List classes=new ArrayList(files.length);
				String fname;
				for(int i=0;i<files.length;++i)
				{
					fname=files[i];
					if(fname.endsWith(".class"))
					{
						classes.add(fname.substring(0, fname.length()-6));
					}
				}
				return classes;
			}
		}
		return null;
	}
}

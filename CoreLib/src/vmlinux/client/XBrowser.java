package vmlinux.client;

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import vmlinux.reflect.XObject;
import vmlinux.reflect.serializer.JSONSerializer;
import vmlinux.reflect.source.JSONSource;
import vmlinux.util.ReflectUtil;

public class XBrowser implements LocationListener
{
	public static interface JavaHandler
	{
		
	}
	public static class JavaCaller extends BrowserFunction
	{
		protected JavaHandler handler;
		
		public JavaCaller(Browser b,String name,JavaHandler h)
		{
			super(b,name);
			this.handler=h;
		}

		@Override
		public Object function(Object[] arguments)
		{
			if(handler==null || arguments.length==0)
			{
				return super.function(arguments);
			}
			String fun=(String)arguments[0];
			Class clazz=handler.getClass();
			System.out.println("[call]: "+fun);
			Method[] methods=clazz.getMethods();
			for(int i=0;i<methods.length;++i)
			{
				Method m=methods[i];
				Class[] pc=m.getParameterTypes();
				if(fun.equals(m.getName()) && pc.length==arguments.length-1)
				{
					try
					{
						Object[] p=new Object[pc.length];
						for(int j=0;j<pc.length;++j)
						{
							String s=(String)arguments[j+1];
							System.out.println("[param]: "+s);
							if(ReflectUtil.isTypeOf(pc[j], XObject.class))
							{
								p[j]=JSONSource.buildObject(s, pc[j]);
							}
							else
							{
								p[j]=s;
							}
						}
						Object ret=m.invoke(handler, p);
						String result=null;
						if(ReflectUtil.isTypeOf(ret.getClass(), XObject.class))
						{
							result=JSONSerializer.serializeObject(ret);
						}
						else if(ret.getClass().isArray() && ReflectUtil.isTypeOf(ret.getClass().getComponentType(), XObject.class))
						{
							result=JSONSerializer.serializeObject(ret);	
						}
						else
						{
							result=ret.toString();
						}
						System.out.println("[result]: "+result);
						return result;
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			return null;
		}
	}
	
	protected Display display;
	protected Shell shell;
	protected Browser browser;
	protected JavaHandler handler;
	
	public XBrowser(String title,int w,int h)
	{
		display=new Display();
		shell=new Shell(display);
		shell.setText(title);
		shell.setSize(w,h);
		shell.setLayout(new FillLayout());
		browser=new Browser(shell, SWT.FILL);
		browser.addLocationListener(this);
	}
	
	public void setImages(String sfile,String bfile)
	{
		Image simg=new Image(display, sfile);
		Image bimg=new Image(display, bfile);
		shell.setImages(new Image[]{simg,bimg});
	}
	
	public void setHandler(JavaHandler h)
	{
		handler=h;
	}
	
	public void show()
	{
		new JavaCaller(browser, "java", handler);
		shell.open();

		while (!shell.isDisposed())
		{ 
			if (!display.readAndDispatch()) 
				display.sleep(); 
		} 
		display.dispose(); 
	}
	
	public void loadUrl(String url)
	{
		browser.setUrl(url);
	}
	
	@Override
	public void changed(LocationEvent e)
	{
		e.doit=onLocationChanging(e.location);
	}

	@Override
	public void changing(LocationEvent e)
	{
		e.doit=onLocationChanged(e.location);
	}
	
	protected boolean onLocationChanging(String loc)
	{
		return true;
	}
	
	protected boolean onLocationChanged(String loc)
	{
		return true;
	}

}

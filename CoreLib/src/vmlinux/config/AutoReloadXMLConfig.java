package vmlinux.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

@SuppressWarnings({"deprecation","unchecked"})
public class AutoReloadXMLConfig implements Runnable
{
	class XMLConfigItem
	{
		public Document doc;
		public long ticket;
		public File file;
		
		public XMLConfigItem(Document doc,long ticket,File file)
		{
			this.doc=doc;
			this.ticket=ticket;
			this.file=file;
		}
	}
	
	protected Thread daemon;
	protected Map configs;
	protected SAXBuilder builder;
	protected XMLOutputter outputter;
	
	public AutoReloadXMLConfig()
	{
		builder=new SAXBuilder();
		outputter=new XMLOutputter();
		configs=new HashMap();
		daemon=new Thread(this);
		daemon.start();
	}
	
	public Document LoadConfig(String file,Document defaultdoc) throws JDOMException, IOException
	{
		Document doc=getConfig(file);
		if(doc==null)
		{
			File f=new File(file);
			long ticket;
			if(f.exists())
			{
				doc=builder.build(file);
				ticket=f.lastModified();
			}
			else
			{
				doc=defaultdoc;
				ticket=System.currentTimeMillis();
			}
			if(doc!=null)
			{
				System.out.println("register config "+file);
				synchronized(configs)
				{
					configs.put(file, new XMLConfigItem(doc,ticket,f));
				}
			}
		}
		return doc;
	}
	
	public Document getConfig(String file)
	{
		Object o=configs.get(file);
		if(o!=null)
		{
			XMLConfigItem i=(XMLConfigItem)o;
			return i.doc;
		}
		return null;
	}
	
	public void saveConfig(String file)
	{
		Object o=configs.get(file);
		if(o!=null)
		{
			XMLConfigItem i=(XMLConfigItem)o;
			FileOutputStream fos=null;
			try
			{
				fos=new FileOutputStream(i.file,false);
				outputter.output(i.doc, fos);
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				if(fos!=null)
				{
					try
					{
						fos.close();
					}
					catch(IOException ex)
					{
						
					}
				}
			}
			i.ticket=i.file.lastModified();
		}		
	}
	
	protected void watchChanges()
	{
		XMLConfigItem i;
		Iterator it=configs.entrySet().iterator();
		Entry e;
		Document doc;
		while(it.hasNext())
		{
			e=(Entry)it.next();
			i=(XMLConfigItem)e.getValue();
			if(i.ticket<i.file.lastModified())
			{
				System.out.println("reloading config "+i.file.getName());
				try
				{
					doc=builder.build(i.file);
					synchronized(i)
					{
						i.doc=doc;
					}
					i.ticket=i.file.lastModified();
				}
				catch(JDOMException ex)
				{
					ex.printStackTrace();
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
	public void run()
	{
		try
		{
			while(true)
			{
				Thread.sleep(180000);
				watchChanges();
			}
		}
		catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}

}

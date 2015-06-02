package vmlinux.util;

import java.util.ArrayList;

import vmlinux.reflect.XObject;
import vmlinux.reflect.serializer.JDOMSerializer;
import vmlinux.reflect.source.JDOMSource;

public class RSSGenerator
{
	public static class RSSObject implements XObject
	{
		public RSSChannel channel;
	}
	public static class RSSChannel implements XObject
	{
		public String title;
		public String description;
		public String link;
		public String language="zh-cn";
		public String generator="vmlinux.util.RSSGenerator";
		public String lastBuildDate=new java.util.Date().toGMTString();
		
		public RSSItem[] item;
	}
	public static class RSSItem implements XObject
	{
		public String title;
		public String link;
		public String author;
		public String pubDate=new java.util.Date().toGMTString();
		public String guid;
		public String description;
	}
	public static RSSObject loadRSS(String file)
	{
		RSSObject o=(RSSObject)JDOMSource.buildObject(file, RSSObject.class);
		return o;
	}

	protected RSSChannel ch;
	protected ArrayList<RSSItem> items;
	
	public RSSGenerator(String title,String link,String desc)
	{
		ch=new RSSChannel();
		ch.title=title;
		ch.link=link;
		ch.description=desc;
		items=new ArrayList<RSSItem>();
	}
	
	public void setTitle(String title)
	{
		ch.title=title;
	}
	public void setLink(String link)
	{
		ch.link=link;
	}
	public void setDesc(String desc)
	{
		ch.description=desc;
	}
	public void setLang(String lang)
	{
		ch.language=lang;
	}
	public void addItem(RSSItem item)
	{
		items.add(item);
	}
	public RSSItem getItem(int n)
	{
		return items.get(n);
	}
	public void addItem(String title,String author,String link,String desc,java.util.Date pubdate)
	{
		RSSItem i=new RSSItem();
		i.title=title;
		i.author=author;
		i.link=link;
		i.description=desc;
		i.guid=link;
		i.pubDate=pubdate.toGMTString();
		items.add(i);
	}
	public void addItem(String title,String link,String desc)
	{
		addItem(title,"auto-author",link,desc,new java.util.Date());
	}
	
	public String toString()
	{
		ch.item=(RSSItem[])items.toArray(new RSSItem[0]);
		StringBuffer sb=new StringBuffer("<rss version=\"2.0\">");
		sb.append(JDOMSerializer.serializeObjectToString("channel",ch));
		sb.append("</rss>");
		return sb.toString();
	}
}

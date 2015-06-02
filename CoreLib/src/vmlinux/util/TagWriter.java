package vmlinux.util;

import java.io.IOException;
import java.io.Writer;

public class TagWriter
{
	protected Writer writer;
	protected boolean finished;
	protected String lastTag;
	
	public boolean isFinished()
	{
		return finished;
	}
	public TagWriter(Writer writer)
	{
		this.writer=writer;
		finished=true;
	}
	protected void beginTag(String tag)
	{
		lastTag=tag;
		finished=false;
	}
	protected void finishTag()
	{
		lastTag=null;
		finished=true;
	}
	public void tagStart(String tag) throws IOException
	{
		writer.write("<");
		writer.write(tag);
		beginTag(tag);
	}
	public void tagAttribute(String name,String value) throws IOException
	{
		writer.write(" ");
		writer.write(name);
		writer.write("=\"");
		writer.write(value);
		writer.write("\"");
	}
	public void tagRight() throws IOException
	{
		writer.write(">");
	}
	public void tagRightEnd() throws IOException
	{
		writer.write("/>");
		finishTag();
	}
	public void tagEnd(String tag) throws IOException
	{
		writer.write("</");
		writer.write(tag);
		writer.write(">");
		finishTag();
	}
	public void tagEnd() throws IOException
	{
		if(lastTag==null)
			throw new IllegalStateException("tag stack is empty");
		tagEnd(lastTag);
	}
	public void tagText(String text) throws IOException
	{
		writer.write(text);
	}
}

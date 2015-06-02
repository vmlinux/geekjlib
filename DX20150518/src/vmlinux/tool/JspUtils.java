package vmlinux.tool;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import com.jspsmart.upload.SmartFiles;
import com.jspsmart.upload.SmartUpload;

public class JspUtils
{
	private SmartUpload su;
	private PageContext pc;
	private String encoding;
	
	public JspUtils(PageContext pageContext,String encoding) throws Exception
	{
		pc=pageContext;
		pc.getRequest().setCharacterEncoding(encoding);
		this.encoding=encoding;
	}
	
	public void initUpload() throws Exception
	{
		if(su==null)
		{
			su=new SmartUpload(pc.getServletConfig()
					,(HttpServletRequest)pc.getRequest(),(HttpServletResponse)pc.getResponse(),encoding);
		}
		//su.initialize(pc);
		//su.upload();
	}
	
	public String getParameter(String name)
	{
		String v=su==null?pc.getRequest().getParameter(name)
				:su.getRequest().getParameter(name);
		return v==null?"":v;
	}
	
	public boolean hasParameter(String name)
	{
		String v=getParameter(name);
		return !(v==null||v.length()==0);
	}
	
	public String getParameter(String name,String valIfEmpty)
	{
		String v=getParameter(name);
		return (v==null||v.length()==0)?valIfEmpty:v;
	}
	
	public String getFormatedParameter(String name,String formatIfNotEmpty)
	{
		String v=getParameter(name);
		return (v==null||v.length()==0)?"":vmlinux.util.StringUtil.format(formatIfNotEmpty,v);
	}
	
	public String[] getParameterValues(String name)
	{
		return su==null?pc.getRequest().getParameterValues(name)
				:su.getRequest().getParameterValues(name);
	}
	
	public SmartFiles getFiles()
	{
		return su.getFiles();
	}
	
	public int getFileCount()
	{
		return getFiles().getCount();
	}
	
	public long getSize() throws Exception
	{
		return getFiles().getSize();
	}
	
	public int getFileIndex(String name)
	{
		SmartFiles f=getFiles();
		int c=f.getCount();
		for(int i=0;i<c;++i)
		{
			if(name.equalsIgnoreCase(f.getFile(i).getFieldName()))
				return i;
		}
		return -1;
	}
	
	public long getFileSize(int i)
	{
		return getFiles().getFile(i).getSize();
	}
	
	public String getFileField(int i)
	{
		return getFiles().getFile(i).getFieldName();
	}
	
	public String getFileName(int i)
	{
		return getFiles().getFile(i).getFileName();
	}
	
	public String getFilePath(int i)
	{
		return getFiles().getFile(i).getFilePathName();
	}
	
	public String getFileExt(int i)
	{
		return getFiles().getFile(i).getFileExt();
	}
	
	public void saveFile(int i,String filename) throws Exception
	{
		getFiles().getFile(i).saveAs(filename, SmartUpload.SAVE_VIRTUAL);
	}
	
	public void saveFileAs(int i,String filename) throws Exception
	{
		getFiles().getFile(i).saveAs(filename, SmartUpload.SAVE_PHYSICAL);
	}
	
	public void save(String filename) throws Exception
	{
		su.save(filename,SmartUpload.SAVE_VIRTUAL);
	}
	
	public void saveAs(String filename) throws Exception
	{
		su.save(filename,SmartUpload.SAVE_PHYSICAL);
	}
}

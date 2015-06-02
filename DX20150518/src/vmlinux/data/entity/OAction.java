package vmlinux.data.entity;

import java.io.FileOutputStream;
import java.io.PrintStream;

import vmlinux.data.entity.*;
import vmlinux.data.reporter.*;
import vmlinux.reflect.XObject;
import vmlinux.util.StringUtil;

public class OAction implements XObject
{
	public String _commit;
	public String _preaction;	//delete or truncate
	public java.util.Date _schedule;	//time to start
	public OSource sourcefrom;
	public OSource sourceto;
	public OSource sourcelog;
	public OData[] data;
	public String _name;
	public String _logfile;
	
	private int ncommit;
	private IStatusReporter reporter;
	public ExecParam param;
	private FileOutputStream logfilestream;
	private PrintStream logstream;
	
	public void prepare() throws Exception
	{
		if(_logfile!=null && !"none".equalsIgnoreCase(_logfile))
		{
			logfilestream=new FileOutputStream(this.formatQuery(_logfile));
			logstream=new PrintStream(logfilestream);
		}
		if(param==null)
		{
			param=new ExecParam();
		}
		if("auto".equalsIgnoreCase(_commit))
		{
			ncommit=1;
		}
		else
		{
			try
			{
				ncommit=Integer.valueOf(_commit);
			}
			catch(Exception ex)
			{
			}
			if(ncommit<10)
			{
				ncommit=100;
			}
		}
		if(sourcefrom!=null && sourcefrom._name==null)
		{
			sourcefrom._name="sourcefrom";
		}
		if(sourceto!=null && sourceto._name==null)
		{
			sourceto._name="sourceto";
		}
		if(sourcelog!=null && sourcelog._name==null)
		{
			sourcelog._name="sourcelog";
		}
		for(int i=0;i<data.length;++i)
		{
			OData dt=data[i];
			if(dt.from!=null && dt.from.source==null)
			{
				dt.from.source=sourcefrom;
			}
			if(dt.to!=null && dt.to.source==null)
			{
				dt.to.source=sourceto;
			}
			if(dt.difffrom!=null && dt.difffrom.source==null)
			{
				dt.difffrom.source=sourceto;
			}
			if(dt.diffto!=null && dt.diffto.source==null)
			{
				dt.diffto.source=sourceto;
			}
			if(dt.command!=null && dt.command.source==null)
			{
				dt.command.source=sourceto;
			}
			if(dt.matchfrom!=null && dt.matchfrom.source==null)
			{
				dt.matchfrom.source=sourcefrom;
			}
			if(dt.check!=null && dt.check.source==null)
			{
				dt.check.source=sourcefrom;
			}
			dt.prepare(this);
		}
	}
	
	public void finish() throws Exception
	{
		if(logstream!=null)
		{
			logstream.close();
		}
		if(logfilestream!=null)
		{
			logfilestream.close();
		}
	}
	
	public void setReporter(IStatusReporter rep)
	{
		this.reporter=rep;
	}
	
	public void reportStatus(String msg)
	{
		String s=StringUtil.format(msg,param);
		if(this.reporter!=null)
		{
			reporter.setRunningMessage(s);
		}
		System.out.println(s);
		if(logstream!=null)
		{
			logstream.println(s);
		}
	}

	public void reportBreak(String msg)
	{
		String s=StringUtil.format(msg,param);
		if(this.reporter!=null)
		{
			reporter.setBreakMessage(s);
		}
		System.out.println(s);
		if(logstream!=null)
		{
			logstream.println(s);
		}
	}

	public void reportError(String msg,Exception ex)
	{
		String s=StringUtil.format(msg,param);
		if(this.reporter!=null)
		{
			reporter.setRunningMessage(s+":"+ex.toString());
		}
		System.err.println(s);
		if(ex!=null)
		{
			ex.printStackTrace();
		}
		if(logstream!=null)
		{
			logstream.println(s+":"+ex.toString());
		}
	}
	public void reportCriticalError(String msg,Exception ex)
	{
		String s=StringUtil.format(msg,param);
		if(this.reporter!=null)
		{
			reporter.setErrorMessage(s+":"+ex.toString());
		}
		System.err.println(s);
		if(ex!=null)
		{
			ex.printStackTrace();
			ex.printStackTrace(logstream);
		}
		if(logstream!=null)
		{
			logstream.println(s+":"+ex.toString());
		}
	}
	
	public String formatQuery(String query)
	{
		if(param!=null)
		{
			return StringUtil.format(query, param);
		}
		return query;
	}
}


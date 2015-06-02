package vmlinux.util;

import java.io.PrintStream;
import java.util.Date;

import org.apache.commons.logging.Log;

public class LogUtil implements Log
{
	//log level presents
	public static final int MAX=0xffff;
	public static final int MIN=0x0020;
	public static final int NONE=0x0000;
	public static final int FAVOR=0x0022;
	
	//log levels
	public static final int DEBUG=0x0001;
	public static final int INFO=0x0002;
	public static final int WARN=0x0004;
	public static final int TRACE=0x0008;
	public static final int ERROR=0x0010;
	public static final int FATAL=0x0020;
	
	public static Log getLog(Class clazz)
	{
		return getLog(clazz,MAX);
	}
	public static Log getLog(Class clazz,int level)
	{
		return getLog(clazz.getName(),level);
	}
	public static Log getLog(String name)
	{
		return getLog(name,MAX);
	}
	public static Log getLog(String name,int level)
	{
		return new LogUtil(System.out,name,level);
	}
	
	protected PrintStream writer;
	protected String name;
	
	protected boolean debugEnable=true;
	protected boolean infoEnable=true;
	protected boolean warnEnable=true;
	protected boolean traceEnable=true;
	protected boolean errorEnable=true;
	protected boolean fatalEnable=true;
	
	public LogUtil(PrintStream writer,String name,int level)
	{
		this.writer=writer;
		this.name=name;
		debugEnable=(DEBUG&level)==DEBUG;
		infoEnable=(INFO&level)==INFO;
		warnEnable=(WARN&level)==WARN;
		traceEnable=(TRACE&level)==TRACE;
		errorEnable=(ERROR&level)==ERROR;
		fatalEnable=(FATAL&level)==FATAL;
	}
	protected void write(Object msg)
	{
		writer.print(new Date());
		writer.print(" - ");
		writer.println(msg);
	}
	protected void write(String status,Object msg)
	{
		write("["+status+"] "+msg);
	}
	public void debug(Object obj)
	{
		if(debugEnable)
			write("debug",obj);
	}

	public void debug(Object obj, Throwable throwable)
	{
		if(debugEnable)
		{
			write("debug",obj);
			throwable.printStackTrace(writer);
		}
	}

	public void error(Object obj)
	{
		if(errorEnable)
			write("error",obj);
	}

	public void error(Object obj, Throwable throwable)
	{
		if(errorEnable)
		{
			write("error",obj);
			throwable.printStackTrace(writer);
		}
	}

	public void fatal(Object obj)
	{
		if(fatalEnable)
			write("fatal",obj);
	}

	public void fatal(Object obj, Throwable throwable)
	{
		if(fatalEnable)
		{
			write("fatal",obj);
			throwable.printStackTrace(writer);
		}
	}

	public void info(Object obj)
	{
		if(infoEnable)
			write("info",obj);
	}

	public void info(Object obj, Throwable throwable)
	{
		if(infoEnable)
		{
			write("info",obj);
			throwable.printStackTrace(writer);
		}
	}

	public boolean isDebugEnabled()
	{
		return debugEnable;
	}

	public boolean isErrorEnabled()
	{
		return errorEnable;
	}

	public boolean isFatalEnabled()
	{
		return fatalEnable;
	}

	public boolean isInfoEnabled()
	{
		return infoEnable;
	}

	public boolean isTraceEnabled()
	{
		return traceEnable;
	}

	public boolean isWarnEnabled()
	{
		return warnEnable;
	}

	public void trace(Object obj)
	{
		if(traceEnable)
			write("trace",obj);
	}

	public void trace(Object obj, Throwable throwable)
	{
		if(traceEnable)
		{
			write("trace",obj);
			throwable.printStackTrace(writer);
		}
	}

	public void warn(Object obj)
	{
		if(warnEnable)
			write("warn",obj);
	}

	public void warn(Object obj, Throwable throwable)
	{
		if(warnEnable)
		{
			write("warn",obj);
			throwable.printStackTrace(writer);
		}
	}

}

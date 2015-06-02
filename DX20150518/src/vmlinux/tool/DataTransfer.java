package vmlinux.tool;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.sql.*;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;
import vmlinux.data.entity.*;
import vmlinux.data.processor.CSVProcessor;
import vmlinux.data.processor.CommandProcessor;
import vmlinux.data.processor.ExcelProcessor;
import vmlinux.data.processor.FileProcessor;
import vmlinux.data.processor.JsonProcessor;
import vmlinux.data.processor.MailProcessor;
import vmlinux.data.processor.ParamProcessor;
import vmlinux.data.processor.TableProcessor;
import vmlinux.data.reporter.DbStatusReporter;
import vmlinux.data.reporter.IStatusReporter;
import vmlinux.reflect.source.*;
import vmlinux.util.*;

public class DataTransfer
{
	public static Properties prop=new Properties();
	
	public static String getProperty(String name)
	{
		return prop==null?null:prop.getProperty(name);
	}
	
/*	
	static
	{
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}
		catch(Exception ex)
		{
			//ex.printStackTrace();
			System.err.println("Oracle driver not ready");
		}
		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		}
		catch(Exception ex)
		{
			//ex.printStackTrace();
			System.err.println("DB2 driver not ready");
		}
	}
*/	
	public static void main(String[] args) throws Exception
	{
		/*
		if(args.length==1)
		{
			OAction conf=(OAction)JDOMSource.buildObject(args[0], OAction.class);
			process(conf);
		}
		else
		{
			System.out.println("DataTransfer [config.xml]");
		}
		*/
		ArgumentParser parser = ArgumentParsers.newArgumentParser("DataTransfer");
		for(int i=1;i<=7;++i)
		{
			parser.addArgument("-p"+i)
				.help("Set value of p"+i);
		}
		parser.addArgument("-ss")
			.help("starting step, number(>=1) or step name (param step will not skip)");
		parser.addArgument("-one")
			.help("run one step only, param step not included");
		parser.addArgument("-prop")
			.help("load a properties set file (default: datatransfer.properties)")
			.setDefault("datatransfer.properties");
		parser.addArgument("-myname")
			.help("task name");
		parser.addArgument("-logfile")
			.help("log file");
		parser.addArgument("file").nargs("+").required(true)
        	.help("xml config File to use");
		Namespace ns = null;
		try
		{
			ns = parser.parseArgs(args);
		}
		catch (ArgumentParserException e)
		{
			parser.handleError(e);
			return;
		}
		String xml=(String)ns.getList("file").get(0);
		OAction conf=(OAction)JDOMSource.buildObject(xml, OAction.class);
		ExecParam p=(ExecParam)MapSource.buildObject(ns.getAttrs(), ExecParam.class);
		String propfile=ns.getString("prop");
		if(conf.param==null)
		{
			conf.param=p;
		}
		else
		{
			if(p.p1!=null)
			{
				conf.param.p1=p.p1;
			}
			if(p.p2!=null)
			{
				conf.param.p2=p.p2;
			}
			if(p.p3!=null)
			{
				conf.param.p3=p.p3;
			}
			if(p.p4!=null)
			{
				conf.param.p4=p.p4;
			}
			if(p.p5!=null)
			{
				conf.param.p5=p.p5;
			}
			if(p.p6!=null)
			{
				conf.param.p6=p.p6;
			}
			if(p.p7!=null)
			{
				conf.param.p7=p.p7;
			}
			if(p.ss!=null)
			{
				conf.param.ss=p.ss;
			}
			if(p.one!=null)
			{
				conf.param.one=p.ss;
			}
			if(p.myname!=null)
			{
				conf.param.myname=p.myname;
				conf._name=p.myname;
			}
			if(p.logfile!=null)
			{
				conf._logfile=p.logfile;
			}
		}
		if(p.ss!=null && p.ss.length()<=4 && !p.ss.matches("^\\d+$"))
		{
			System.err.println("开始节点名称长度<5,无法匹配");
		}
		File f=new File(propfile);
		if(f.exists())
		{
			prop.load(new FileInputStream(f));
			if(conf._logfile==null)
			{
				conf._logfile=prop.getProperty("$logfile");
			}
		}
		else
		{
			System.err.println("properties not found:"+f.getAbsolutePath());
		}
		process(conf,xml);
	}
	
	public static boolean checkXml(String xml) throws Exception
	{
		OAction conf=(OAction)JDOMSource.buildObject(xml, OAction.class);
		return (conf==null || conf.data==null)?false:true;
	}
	
	public static String fileParameter(OData conf,String f)
	{
		if(f!=null && f.startsWith("$"))
		{
			int p=f.indexOf("/");
			if(p>=0)
			{
				String pname=f.substring(0, p);
				f=prop.getProperty(pname)+f.substring(p);
			}
			else
			{
				f=prop.getProperty(f);
			}
		}
		return f=conf.getQueryFrom(f);
	}
	
	public static void process(OAction conf,String xml) throws Exception
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date dtnow=new java.util.Date();
		if(conf._schedule!=null && conf._schedule.after(dtnow))
		{
			System.out.println("Job will run at "+sdf.format(conf._schedule));
			long ms=conf._schedule.getTime()-dtnow.getTime();
			System.out.println("Sleep for "+(ms/1000)+" seconds now...");
			Thread.sleep(ms);
		}
		conf.prepare();
		if(conf._name==null)
		{
			conf._name=xml;
		}
		IStatusReporter rpter=null;
		if(conf.sourcelog!=null)
		{
			conf.sourcelog.prepare();
			rpter=new DbStatusReporter(conf.sourcelog.getDataSource(), "data_t", conf.param.myname, dtnow);
			if(!rpter.setStarting())
			{
				System.err.println("Job is managed but not in queue, stop now");
				return;
			}
			conf.setReporter(rpter);
		}
		conf.param.myname=conf._name;
		if(conf.param.p1!=null && conf.param.p1.startsWith("$"))
		{
			conf.param.p1=prop.getProperty(conf.param.p1);
		}
		if(conf.param.p2!=null && conf.param.p2.startsWith("$"))
		{
			conf.param.p2=prop.getProperty(conf.param.p2);
		}
		if(conf.param.p3!=null && conf.param.p3.startsWith("$"))
		{
			conf.param.p3=prop.getProperty(conf.param.p3);
		}
		if(conf.param.p4!=null && conf.param.p4.startsWith("$"))
		{
			conf.param.p4=prop.getProperty(conf.param.p4);
		}
		if(conf.param.p5!=null && conf.param.p5.startsWith("$"))
		{
			conf.param.p5=prop.getProperty(conf.param.p5);
		}
		if(conf.param.p6!=null && conf.param.p6.startsWith("$"))
		{
			conf.param.p6=prop.getProperty(conf.param.p6);
		}
		if(conf.param.p7!=null && conf.param.p7.startsWith("$"))
		{
			conf.param.p7=prop.getProperty(conf.param.p7);
		}
		try
		{
			for(int i=0;i<conf.data.length;++i)
			{
				String ss=conf.param.ss;
				String startstep=null;
				int startn=0;
				boolean startflag=false;
				if(ss!=null)
				{
					if(ss.matches("^\\d+$"))
					{
						startn=Integer.parseInt(ss);
					}
					else
					{
						startstep=ss;
					}
				}
				if(startn==0 && startstep==null)
				{
					startflag=true;
				}
				OData dt=conf.data[i];
				conf.param.stepn=i+1;
				if(dt.info!=null)
				{
					conf.reportStatus("Step "+(i+1)+": "+dt.info);
					//System.out.println("Step "+(i+1)+": "+dt.info);
					conf.param.step=dt.info;
				}
				else
				{
					conf.param.step=null;
				}
				if(dt.from!=null)
				{
					if(dt.to!=null && dt.to.param!=null)
					{
						System.out.println((i+1)+": "+dt.getQueryFrom()+" => param: "+dt.to.param._name);
					}
					else
					{
						dt.to.table.prepare(dt);
						System.out.println((i+1)+": "+dt.getQueryFrom()+" => "+dt.to.table.getTableName());
					}
				}
				else if(dt.command!=null)
				{
					if(dt.command.string!=null)
					{
						System.out.println((i+1)+": "+dt.getQueryFrom(dt.command.string));
					}
					else if(dt.command.generate!=null)
					{
						System.out.println((i+1)+": "+dt.getQueryFrom(dt.command.generate));
					}
				}
				if(!startflag)
				{
					if(startn>0)
					{
						if(conf.param.stepn>=startn)
						{
							startflag=true;
						}
					}
					else if(startstep!=null && startstep.equals(conf.param.step))
					{
						startflag=true;
					}
					else if(conf.param.step!=null && startstep!=null && startstep.length()>4
							&& conf.param.step.startsWith(startstep))
					{
						startflag=true;
					}
					if(startflag)
					{
						conf.param.ss=null;
					}
				}
				if(dt.getParentParam()!=null)
				{
					if(dt.getParentParam().conda!=null && dt.getParentParam().conda.equals(dt.getParentParam().condb))
					{
						conf.reportBreak("  ( match conda=condb, break )");
						break;
					}
				}
				if(startflag || (dt.from!=null && dt.to!=null && dt.to.param!=null)
						|| "true".equalsIgnoreCase(dt._noskip))
				{
					System.out.println("  Step "+(i+1)+"开始处理: "+sdf.format(new java.util.Date()));
					if("true".equalsIgnoreCase(dt._skipexception))
					{
						try
						{
							process(dt);
						}
						catch(Exception exe)
						{
							conf.reportError(conf.param.stepn+". "+conf.param.step+" 跳过错误: ", exe);
						}
					}
					else
					{
						process(dt);
					}
					if("true".equalsIgnoreCase(conf.param.one))
					{
						if(dt.from!=null && dt.to!=null && dt.to.param!=null)
						{
							//load param is not in count
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					conf.reportStatus("  ( skip )");
				}
			}
		}
		catch(Exception ex)
		{
			conf.reportCriticalError(conf.param.stepn+". "+conf.param.step+" 执行错误: "+ex.getMessage(), ex);
		}
		if(rpter!=null)
		{
			rpter.finish();
		}
		conf.finish();
	}
	
	
	private static void processParam(OData conf) throws Exception
	{
		ParamProcessor p=new ParamProcessor();
		p.process(conf);
	}
	
	//write csv
	private static void processFile(OData conf) throws Exception
	{
		FileProcessor p=new FileProcessor();
		p.process(conf);
	}

	public static void process(OData conf) throws Exception
	{
		if(conf.check!=null)
		{
			if(conf._check==null)
				conf._check="true";
			DataSource dscheck=conf.check.source.getDataSource();
			DbExecuteEx excheck=new DbExecuteEx(dscheck);
			String p=excheck.executeValue(conf.getQueryFrom(conf.check.query));
			conf.getParentParam().check=p;
			if(!conf._check.equals(p))
			{
				conf.reportStatus("验证失败，不执行环节");
				return;
			}
		}
		if(conf.command!=null)
		{
			processCommand(conf);
		}
		else if(conf.filefrom!=null && conf.filefrom._file!=null)
		{
			conf.filefrom.prepare();
			String f=conf.filefrom._file.toLowerCase();
			if(f.endsWith(".csv")|| f.endsWith(".txt"))
			{
				processCSV(conf);
			}
			else if(f.endsWith(".xls")|| f.endsWith(".xlsx"))
			{
				processExcel(conf);
			}
			else if(f.endsWith(".json"))
			{
				processJSON(conf);
			}
			else
			{
				Exception ex=new RuntimeException("unknown file type");
				conf.reportError("未知导入文件类型:"+f, ex);
				throw ex;
			}
		}
		else if(conf.mailfrom!=null)
		{
			conf.mailfrom.prepare();
			processMail(conf);
		}
		else if(conf.to.param!=null)
		{
			processParam(conf);
		}
		else if(conf.to.table!=null && "file".equalsIgnoreCase(conf.to.table._type))
		{
			processFile(conf);
		}
		else
		{
			processTable(conf);
		}
	}

	//read excel
	private static void processExcel(OData conf) throws Exception
	{
		ExcelProcessor p=new ExcelProcessor();
		p.process(conf);
	}

	//read csv
	private static void processCSV(OData conf) throws Exception
	{
		CSVProcessor p=new CSVProcessor();
		p.process(conf);
	}

	//read json
	private static void processJSON(OData conf) throws Exception
	{
		JsonProcessor p=new JsonProcessor();
		p.process(conf);
	}

	private static void processCommand(OData conf) throws Exception
	{
		CommandProcessor p=new CommandProcessor();
		p.process(conf);
	}
	public static void setIfNotNull(Hashtable<String,Object> tab,String key,Object val)
	{
		if(val!=null)
		{
			tab.put(key.toLowerCase(), val);
		}
	}
	
	public static void setIfNotNull(Hashtable<String,Object> tab,String key,Object val,int len)
	{
		if(val!=null)
		{
			if(String.class.equals(val.getClass()))
			{
				String s=(String)val;
				tab.put(key.toLowerCase(), s.length()>len?s.substring(0, len-5)+" ...":s);
			}
			else
			{
				tab.put(key.toLowerCase(), val);
			}
		}
	}
	
	//read mail
	private static void processMail(OData conf) throws Exception
	{
		MailProcessor p=new MailProcessor();
		p.process(conf);
	}
	private static void processTable(OData conf) throws Exception
	{
		TableProcessor p=new TableProcessor();
		p.process(conf);
	}
}

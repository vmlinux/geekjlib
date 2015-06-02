package vmlinux.data.reporter;

import java.util.Date;

import javax.sql.DataSource;

import vmlinux.data.reporter.IStatusReporter;
import vmlinux.util.DbExecuteEx;

/*task manager*/
public class DbStatusReporter implements IStatusReporter
{
	private DataSource ds;
	private String tnameprefix;
	private String name;
	private java.util.Date tmflg;
	private DbExecuteEx exec;
	private int logidx;
	
	public DbStatusReporter(DataSource ds,String tnameprefix,String name,java.util.Date tmflg)
	{
		this.ds=ds;
		this.name=name;
		this.tmflg=tmflg;
		this.tnameprefix=tnameprefix;
		this.logidx=0;
		exec=new DbExecuteEx(ds);
	}
	public boolean setStarting()
	{
		logidx++;
		try
		{
			java.util.Date logtm=new Date();
			exec.format("select tstat from "+tnameprefix+"_task where tname={0}", name);
			String ret=exec.executeValue();
			System.err.println(name+":setStarting get : "+ret);
			if(!"queue".equalsIgnoreCase(ret))
			{
				return false;
			}
			exec.format("insert into "+tnameprefix+"_log (tname,createtm,logtm,logmsg,tstat,logrow) values ({0},{1},{2},{3},{4},{5})"
					, name,tmflg,logtm,"loading","running",logidx);
			int n=exec.executeUpdate();
			if(n==1)
			{
				System.out.println(" log to database: "+name);
				exec.format("update "+tnameprefix+"_task set run_count=run_count+1,tstat={0},tstartm={1},tstatm={1} where tname={2}", "running",logtm,name);
				n=exec.executeUpdate();
			}
			return true;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	public void setRunningMessage(String str)
	{
		logidx++;
		try
		{
			java.util.Date logtm=new Date();
			exec.format("update "+tnameprefix+"_log set logtm={2},logmsg={3},tstat={4},logrow={5} where tname={0} and createtm={1}"
					, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"running",logidx);
			int n=exec.executeUpdate();
			if(n==0)
			{
				exec.format("insert into "+tnameprefix+"_log (tname,createtm,logtm,logmsg,tstat,logrow) values ({0},{1},{2},{3},{4},{5})"
						, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"running",logidx);
				n=exec.executeUpdate();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	/*error and break*/
	public void setErrorMessage(String str)
	{
		logidx++;
		try
		{
			java.util.Date logtm=new Date();
			exec.format("update "+tnameprefix+"_log set logtm={2},logmsg={3},tstat={4},logrow={5} where tname={0} and createtm={1}"
					, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"error",logidx);
			int n=exec.executeUpdate();
			if(n==0)
			{
				exec.format("insert into "+tnameprefix+"_log (tname,createtm,logtm,logmsg,tstat,logrow) values ({0},{1},{2},{3},{4},{5})"
						, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"error",logidx);
				n=exec.executeUpdate();
			}
			exec.format("update "+tnameprefix+"_task set tstat={0},tstatm={1} where tname={2}", "error",logtm,name);
			n=exec.executeUpdate();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void setBreakMessage(String str)
	{
		logidx++;
		try
		{
			java.util.Date logtm=new Date();
			exec.format("update "+tnameprefix+"_log set logtm={2},logmsg={3},tstat={4},logrow={5} where tname={0} and createtm={1}"
					, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"break",logidx);
			int n=exec.executeUpdate();
			if(n==0)
			{
				exec.format("insert into "+tnameprefix+"_log (tname,createtm,logtm,logmsg,tstat,logrow) values ({0},{1},{2},{3},{4},{5})"
						, name,tmflg,logtm,str==null?"unknown error,please view log.":str,"break",logidx);
				n=exec.executeUpdate();
			}
			exec.format("update "+tnameprefix+"_task set tstat={0},tstatm={1} where tname={2}", "break",logtm,name);
			n=exec.executeUpdate();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void finish()
	{
		logidx++;
		System.out.println(name+":setFinish");
		if(exec!=null)
		{
			try
			{
				java.util.Date logtm=new Date();
				exec.format("select tstat from "+tnameprefix+"_task where tname={0}", name);
				String stat=exec.executeValue();
				if(!"break".equalsIgnoreCase(stat) && !"error".equalsIgnoreCase(stat))
				{
					exec.format("update "+tnameprefix+"_log set tstat={4} where tname={0} and createtm={1}"
							, name,tmflg,logtm,"done","finish");
					int n=exec.executeUpdate();
					if(n==0)
					{
						exec.format("insert into "+tnameprefix+"_log (tname,createtm,logtm,logmsg,tstat,logrow) values ({0},{1},{2},{3},{4},{5})"
								, name,tmflg,logtm,"done","finish",logidx);
						n=exec.executeUpdate();
					}
					exec.format("update "+tnameprefix+"_task set tstat={0},tstatm={1} where tname={2} and tstat not in ('error','break')", "finish",logtm,name);
					n=exec.executeUpdate();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				exec.close();
			}
		}
	}
}


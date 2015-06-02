package vmlinux.tool.runner;

import org.jcrontab.Crontab;

import vmlinux.tool.JsonDAO;
import vmlinux.tool.object.OTask;

public class TimerRunner
{

	public static void main(String[] args) throws Exception
	{
		System.out.println("this is TimerRunner reporting...");
		JsonDAO.getInstance().executeUpdate("update data_t_task set control_state='manual',tstat='queue' "
				+" where control_state='dirty' and tstat='timer'");
		OTask[] t=(OTask[])JsonDAO.getInstance().executeArray(
				"select * from data_t_task "
				+" where control_state='manual' and tstat='queue' and tloadtm<{0}", OTask.class
				,new java.util.Date());
		if(t!=null && t.length>0)
		{
			for(int i=0;i<t.length;++i)
			{
				OTask tsk=t[i];
				String taskdefpath=JsonDAO.getInstance().getProperty("$taskroot")+tsk.tname;
				String arg=vmlinux.util.StringUtil.format("{0}{1}{2}{3}{4}{5}{6}{7} -myname {8} {9}/main.xml"
						,getFormatedParameter(tsk.p1," -p1 {0}")
						,getFormatedParameter(tsk.p2," -p2 {0}")
						,getFormatedParameter(tsk.p3," -p3 {0}")
						,getFormatedParameter(tsk.p4," -p4 {0}")
						,getFormatedParameter(tsk.p5," -p5 {0}")
						,getFormatedParameter(tsk.p6," -p6 {0}")
						,getFormatedParameter(tsk.p7," -p7 {0}")
						,getFormatedParameter(tsk.p_ss," -ss {0}")
						,tsk.tname,taskdefpath);
				System.out.println("TimerRunner:"+arg);
				String[] arglist=arg.trim().split("\\s+");
				Crontab.getInstance().newTask("vmlinux.tool.DataTransfer", "main", arglist);
			}
		}
		System.out.println("TimerRunner finished");
	}

	public static String getFormatedParameter(String v,String formatIfNotEmpty)
	{
		return (v==null||v.length()==0)?"":vmlinux.util.StringUtil.format(formatIfNotEmpty,v);
	}

}

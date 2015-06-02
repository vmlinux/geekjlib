package vmlinux.tool.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import vmlinux.tool.JsonDAO;
import vmlinux.tool.object.OTask;

public class ScheduleRunner
{

	public static void main(String[] args) throws Exception
	{
		System.out.println("this is ScheduleRunner reporting...");
		Properties prop=new Properties();
		File f=new File("jcrontab.properties");
		String targetfile=null;
		if(f.exists())
		{
			try
			{
				prop.load(new FileInputStream(f));
				targetfile=prop.getProperty("org.jcrontab.data.file");
				if(targetfile==null)
				{
					System.err.println("jcrontab schedule file not configured:"+f.getAbsolutePath());
					return;
				}
			}
			catch(java.io.IOException ex)
			{
				System.err.println("jcrontab properties read error:"+f.getAbsolutePath());
			}
		}
		else
		{
			System.err.println("jcrontab properties not found:"+f.getAbsolutePath());
		}
		JsonDAO.getInstance().executeUpdate("update data_t_task set maintain_flag=1 "
				+" where control_state='dirty' and tstat='todeal'");
		OTask[] t=(OTask[])JsonDAO.getInstance().executeArray(
				"select a.*,b.crontabm,b.crontabh,b.crontabdom,b.crontabmon,b.crontabdow,b.tclass,b.tdef "
				+" from data_t_task a,data_t_crontab b "
				+" where (a.maintain_flag=1 or control_state='schedule') and b.tname=a.tname", OTask.class);
		int i=0;
		for(i=0;i<t.length;++i)
		{
			if("dirty".equalsIgnoreCase(t[i].control_state))
			{
				break;
			}
		}
		if(i<t.length || JsonDAO.getInstance().setSyncFlag(0)>0)
		{
			FileOutputStream fos=null;
			try
			{
				fos=new FileOutputStream(targetfile);
				fos.write("* * * * * vmlinux.tool.runner.ScheduleRunner\r\n".getBytes());
				fos.write("* * * * * vmlinux.tool.runner.TimerRunner\r\n".getBytes());
				if(t!=null && t.length>0)
				{
					for(i=0;i<t.length;++i)
					{
						fos.write(vmlinux.util.StringUtil.format("{crontabm} {crontabh} {crontabdom} {crontabmon} {crontabdow} {tclass} {tdef}\r\n", t[i]).getBytes());
					}
				}
				JsonDAO.getInstance().executeUpdate("update data_t_task set control_state='schedule',tstat='queue',maintain_flag=null "
						+" where maintain_flag=1");
			}
			finally
			{
				fos.close();
			}
		}
		else
		{
			System.out.println("ScheduleRunner has noting to publish");
		}
		JsonDAO.getInstance().executeUpdate("update data_t_task set tstat='queue' "
				+" where tstat in ('finish','break') and control_state='schedule'");
		System.out.println("ScheduleRunner finished");
	}

}

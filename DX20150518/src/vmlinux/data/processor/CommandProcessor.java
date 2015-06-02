package vmlinux.data.processor;

import java.util.ArrayList;

import javax.sql.DataSource;

import vmlinux.data.entity.OData;
import vmlinux.util.DbExecuteEx;

public class CommandProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		if(conf.command!=null)
		{
			if(conf.command.string!=null)
			{
				if(conf.command._os==null || "DB".equalsIgnoreCase(conf.command._os))
				{
					DataSource ds=conf.command.source.getDataSource();
					DbExecuteEx exec=new DbExecuteEx(ds);
					try
					{
						int ret=exec.executeUpdate(conf.getQueryFrom(conf.command.string));
						exec.commit();
						conf.reportStatus("  命令执行完成："+ret);
					}
					finally
					{
						exec.close();
					}
				}
				else if("OS".equalsIgnoreCase(conf.command._os))
				{
					Process p=Runtime.getRuntime().exec(conf.getQueryFrom(conf.command.string));
					p.waitFor();
					conf.reportStatus("  命令执行完成："+p.exitValue());
				}
				else
				{
					String os=System.getProperty("os.name").toUpperCase();
					throw new RuntimeException("unknown target command os");
				}
			}
			else if(conf.command.generate!=null)
			{
				DataSource ds=conf.command.source.getDataSource();
				DbExecuteEx exec=new DbExecuteEx(ds);
				try
				{
					ArrayList cmd=exec.executeList(conf.getQueryFrom(conf.command.generate),String.class);
					if(conf.command._os==null || "DB".equalsIgnoreCase(conf.command._os))
					{
						try
						{
							for(int i=0;i<cmd.size();++i)
							{
								String sql=conf.getQueryFrom((String)cmd.get(i));
								int ret=exec.executeUpdate(sql);
								exec.commit();
								conf.reportStatus("  "+sql+"执行完成："+ret);
							}
						}
						finally
						{
							exec.close();
						}
					}
					else if("OS".equalsIgnoreCase(conf.command._os))
					{
						for(int i=0;i<cmd.size();++i)
						{
							String cstr=conf.getQueryFrom((String)cmd.get(i));
							Process p=Runtime.getRuntime().exec(cstr);
							p.waitFor();
							conf.reportStatus("  "+cstr+"执行完成："+p.exitValue());
						}
					}
					else
					{
						String os=System.getProperty("os.name").toUpperCase();
						throw new RuntimeException("unknown target command os");
					}
				}
				finally
				{
					exec.close();
				}
			}
		}
	}

}

package vmlinux.data.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import vmlinux.data.entity.OData;
import vmlinux.tool.DataTransfer;
import vmlinux.util.CSVWriter;
import vmlinux.util.DbExecuteEx;

public class FileProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		DataSource dsfrom=conf.from.source.getDataSource();
		DbExecuteEx exfrom=new DbExecuteEx(dsfrom);
		DataSource dsmatch=null;
		DbExecuteEx exmatch=null;
		CSVWriter cw=null;
		try
		{
			String file=DataTransfer.fileParameter(conf,conf.to.table.getTableName()+".csv");/*conf.to.table.getTableName();
			if(file!=null && file.startsWith("$"))
			{
				int p=file.indexOf("/");
				if(p>=0)
				{
					String pname=file.substring(0, p);
					file=prop.getProperty(pname)+file.substring(p);
				}
				else
				{
					file=prop.getProperty(file);
				}
			}
			file=conf.getQueryFrom(file+".csv");*/
			if("none".equalsIgnoreCase(conf.getPreAction()))
			{
				cw=new CSVWriter(file,true);
			}
			else
			{
				cw=new CSVWriter(file);
			}
			conf.reportStatus("  输出到文件："+file);
			ResultSet rs=exfrom.executeQuery(conf.getQueryFrom());
			int cols=0;
			if(conf.matchfrom!=null)
			{
				dsmatch=conf.matchfrom.source.getDataSource();
				exmatch=new DbExecuteEx(dsmatch);
				ResultSet mrs=exmatch.executeQuery(conf.getQueryFrom(conf.matchfrom.query));
				ResultSetMetaData mrsdata=mrs.getMetaData();
				cols=mrsdata.getColumnCount();
				if(!"none".equalsIgnoreCase(conf.getPreAction()) || cw.isZeroFile())
				{
					for(int i=1;i<=cols;++i)
					{
						cw.writeString(mrsdata.getColumnName(i));
					}
					cw.writeNext();
				}
				mrs.close();
			}
			else
			{
				ResultSetMetaData rsdata=rs.getMetaData();
				cols=rsdata.getColumnCount();
				if(!"none".equalsIgnoreCase(conf.getPreAction()) || cw.isZeroFile())
				{
					for(int i=1;i<=cols;++i)
					{
						cw.writeString(rsdata.getColumnName(i));
					}
					cw.writeNext();
				}
			}
			int n=0;
			int nc=conf.getNCommit();
			while(rs.next())
			{
				if(conf.matchfrom!=null)
				{
					conf.getParentParam().matchkey=rs.getString(conf.matchfrom._keyfield.toLowerCase());
					String q=conf.getQueryFrom(conf.matchfrom.query);
					ResultSet mrs=exmatch.executeQuery(q);
					while(mrs.next())
					{
						for(int i=1;i<=cols;++i)
						{
							String val=mrs.getString(i);
							cw.writeString(val);
						}
						cw.writeNext();
					}
					mrs.close();
				}
				else
				{
					for(int i=1;i<=cols;++i)
					{
						String val=rs.getString(i);
						cw.writeString(val);
					}
					cw.writeNext();
				}
				++n;
				if(n%nc==0)
				{
					//System.out.println("已处理 "+n+" 行");
					conf.reportStatus("已处理 "+n+" 行");
				}
			}
			if(n%nc!=0)
			{
				conf.reportStatus("已处理 "+n+" 行");
			}
		}
		finally
		{
			exfrom.close();
			if(cw!=null)
			{
				cw.close();
			}
		}
	}

}

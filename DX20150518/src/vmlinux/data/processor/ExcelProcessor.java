package vmlinux.data.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Hashtable;

import javax.sql.DataSource;

import vmlinux.data.entity.ExecParam;
import vmlinux.data.entity.FieldMap;
import vmlinux.data.entity.OData;
import vmlinux.tool.DataTransfer;
import vmlinux.util.DbExecuteEx;
import vmlinux.util.ExcelReader;

public class ExcelProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		//init excel
		ExcelReader xls=conf.filefrom._sheet!=null?new ExcelReader(DataTransfer.fileParameter(conf,conf.filefrom._file),conf.getQueryFrom(conf.filefrom._sheet))
			:new ExcelReader(DataTransfer.fileParameter(conf,conf.filefrom._file));
		if(xls.next())
		{
			for(int i=0;i<conf.filefrom.col.length;++i)
			{
				FieldMap f=conf.filefrom.col[i];
				xls.changeHeader(f._, f._name.toLowerCase());
			}
		}
		else
		{
			return;
		}
        
        //init data processing part
		DataSource dsto=conf.to.source.getDataSource();
		DbExecuteEx exto=null;
		Connection conn=null;
		DbExecuteEx exdfrom=null;
		DbExecuteEx exdto=null;
		DataSource dsmatch=null;
		DbExecuteEx exmatch=null;
		Connection dconn=null;
		if(conf.autoCommit())
		{
			exto=new DbExecuteEx(dsto);
		}
		else
		{
			conn=dsto.getConnection();
			conn.setAutoCommit(false);
			exto=new DbExecuteEx(conn);
		}
		try
		{
			Hashtable<String, Class> dcolmap=null;
			if(conf.matchfrom!=null)
			{
				dsmatch=conf.matchfrom.source.getDataSource();
				exmatch=new DbExecuteEx(dsmatch);
			}
			if(conf.diffto!=null && conf.difffrom!=null)
			{
				DataSource dsdfrom=conf.difffrom.source.getDataSource();
				DataSource dsdto=conf.diffto.source.getDataSource();
				exdfrom=new DbExecuteEx(dsdfrom);
				if(conf.autoCommit())
				{
					exdto=new DbExecuteEx(dsdto);
				}
				else
				{
					dconn=dsto.getConnection();
					dconn.setAutoCommit(false);
					exdto=new DbExecuteEx(dconn);
				}
				if(conf.diffto._preaction==null)
				{
					conf.diffto._preaction=conf.getPreAction();
				}
				if("truncate".equalsIgnoreCase(conf.diffto._preaction))
				{
					conf.diffto.table.truncate(exdto);
				}
				if("deleteall".equalsIgnoreCase(conf.diffto._preaction))
				{
					conf.diffto.table.deleteAll(exdto);
				}
				dcolmap=new Hashtable<String, Class>();
				ResultSet drsto=exdto.executeQuery("select * from "+conf.diffto.table.getTableName());
				ResultSetMetaData drstometa=drsto.getMetaData();
				for(int i=0;i<drstometa.getColumnCount();++i)
				{
					dcolmap.put(drstometa.getColumnName(i+1).toLowerCase()
							, Class.forName(drstometa.getColumnClassName(i+1)));
				}
				drsto.close();
			}
			if("truncate".equalsIgnoreCase(conf.getPreAction()))
			{
				conf.to.table.truncate(exto);
			}
			if("deleteall".equalsIgnoreCase(conf.getPreAction()))
			{
				conf.to.table.deleteAll(exto);
			}
			ResultSet rsto=exto.executeQuery("select * from "+conf.to.table.getTableName());
			ResultSetMetaData rstometa=rsto.getMetaData();
			Hashtable<String, Class> colmap=new Hashtable<String, Class>();
			for(int i=0;i<rstometa.getColumnCount();++i)
			{
				colmap.put(rstometa.getColumnName(i+1).toLowerCase()
						, Class.forName(rstometa.getColumnClassName(i+1)));
			}
			rsto.close();

			//start csv
	        int n=0;
	        int nc=conf.getNCommit();
	        do
	        {
	        	Hashtable<String,Object> row=null;
				try
				{
					ExecParam param=conf.getParentParam();
		        	row=new Hashtable<String, Object>();
		        	for(int i=0;i<xls.columns();++i)
		        	{
		        		DataTransfer.setIfNotNull(row,xls.getHeader(i),xls.getData(i));
		        	}

					if(n==0 && dcolmap!=null)
					{
						conf.difffrom.prepare(row);
					}
					if(conf.matchfrom!=null)
					{
						param.matchkey=(String)row.get(conf.matchfrom._keyfield.toLowerCase());
						ResultSet mrs=exmatch.executeQuery(conf.getQueryFrom(conf.matchfrom.query));
						while(mrs.next())
						{
							conf.to.table.insertRow(mrs, exto, colmap);
						}
						mrs.close();
					}
					else
					{
			        	conf.to.table.insertRow(row, exto, colmap);
			        	if(dcolmap!=null)	//do diff
			        	{
							param.diffkey=(String)row.get(conf.difffrom._keyfield.toLowerCase());
							ResultSet drs=exdfrom.executeQuery(conf.getQueryFrom(conf.difffrom.query));
							if(drs.next())
							{
								if(conf.difffrom.isDiff(row,drs))
								{
									conf.diffto.table.insertRow(row, exdto, dcolmap);
								}
							}
							else if("true".equalsIgnoreCase(conf.difffrom._addnew))
							{
								conf.diffto.table.insertRow(row, exdto, dcolmap);
							}
							drs.close();
			        	}
					}
				}
				catch(Exception ex)
				{
					String msg="Data Sample: "+conf.to.table.getDataSample(row);
					System.err.println(msg);
					ex.printStackTrace();
					if(!"true".equalsIgnoreCase(conf._skipexception))
					{
						throw new RuntimeException(msg+"\r\n"+ex.toString());
					}
					else
					{
						System.err.println("[auto skip exception]");
					}
				}
				++n;
				if(conf.autoCommit())
				{
					if(n%nc==0)
					{
						//System.out.println("已处理 "+n+" 行");
						conf.reportStatus("已处理 "+n+" 行");
					}
				}
				else if(conf.doCommit(n))
				{
					conn.commit();
					//System.out.println("已处理 "+n+" 行");
					conf.reportStatus("已处理 "+n+" 行");
					if(dconn!=null)
					{
						dconn.commit();
					}
				}
			}while(xls.next());
			if(conf.autoCommit())
			{
				if(n%nc!=0)
				{
					//System.out.println("已处理 "+n+" 行");
					conf.reportStatus("已处理 "+n+" 行");
				}
			}
			else if(!conf.doCommit(n))
			{
				conn.commit();
				//System.out.println("已处理 "+n+" 行");
				conf.reportStatus("已处理 "+n+" 行");
				if(dconn!=null)
				{
					dconn.commit();
				}
			}
		}
		finally
		{
			exto.close();
			if(exdfrom!=null)
			{
				exdfrom.close();
			}
			if(exdto!=null)
			{
				exdto.close();
			}
			if(exmatch!=null)
			{
				exmatch.close();
			}
			xls.close();
		}
	}

}

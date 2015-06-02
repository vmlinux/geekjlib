package vmlinux.data.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Hashtable;

import javax.sql.DataSource;

import vmlinux.data.entity.ExecParam;
import vmlinux.data.entity.OData;
import vmlinux.util.DbExecuteEx;

public class TableProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		//conf.prepare();
		DataSource dsfrom=conf.from.source.getDataSource();
		DbExecuteEx exfrom=new DbExecuteEx(dsfrom);
		DataSource dsmatch=null;
		DbExecuteEx exmatch=null;
		DataSource dsto=conf.to.source.getDataSource();
		DbExecuteEx exto=null;
		Connection conn=null;
		DbExecuteEx exdfrom=null;
		DbExecuteEx exdto=null;
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
				ResultSet drsto=exdto.executeQuery("select * from "+conf.diffto.table.getTableName()+" where 1=0");
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
			ResultSet rsto=exto.executeQuery("select * from "+conf.to.table.getTableName()+" where 1=0");
			ResultSetMetaData rstometa=rsto.getMetaData();
			Hashtable<String, Class> colmap=new Hashtable<String, Class>();
			for(int i=0;i<rstometa.getColumnCount();++i)
			{
				colmap.put(rstometa.getColumnName(i+1).toLowerCase()
						, Class.forName(rstometa.getColumnClassName(i+1)));
			}
			rsto.close();
			ResultSet rs=exfrom.executeQuery(conf.getQueryFrom());
			ResultSetMetaData rsmeta=rs.getMetaData();
			if(dcolmap!=null)
			{
				conf.difffrom.prepare(rs);
			}
			int n=0;
			int nc=conf.getNCommit();
			ExecParam param=conf.getParentParam();
			while(rs.next())
			{
				try
				{
					if(conf.matchfrom!=null)
					{
						param.matchkey=rs.getString(conf.matchfrom._keyfield.toLowerCase());
						ResultSet mrs=exmatch.executeQuery(conf.getQueryFrom(conf.matchfrom.query));
						while(mrs.next())
						{
							conf.to.table.insertRow(mrs, exto, colmap);
						}
						mrs.close();
					}
					else
					{
						if(conf.to.table._method!=null)
						{
							String method=conf.to.table._method.toLowerCase();
							if(method.startsWith("split("))
							{
								String p=method.substring(6);
								String[] pp=p.split("[=)]", 3);
								String fname=pp[0];
								Hashtable<String, Object> rowdata=new Hashtable<String, Object>();
								for(int i=0;i<rsmeta.getColumnCount();++i)
								{
									rowdata.put(rsmeta.getColumnName(i+1).toLowerCase(),rs.getObject(i+1));
								}
								String[] ff=rs.getString(fname).split(pp[1]);
								for(int i=0;i<ff.length;++i)
								{
									rowdata.put(fname, ff[i]);
									conf.to.table.insertRow(rowdata, exto, colmap);
								}
							}
							else if(method.startsWith("splitrow("))
							{
								String p=method.substring(9);
								String[] pp=p.split("[=)]", 3);
								String fname=pp[0];
								Hashtable<String, Object> rowdata=new Hashtable<String, Object>();
								for(int i=0;i<rsmeta.getColumnCount();++i)
								{
									rowdata.put(rsmeta.getColumnName(i+1).toLowerCase(),rs.getObject(i+1));
								}
								String[] line=rs.getString(fname).split("((\\r)*\\n)+");
								for(int j=0;j<line.length;++j)
								{
									Hashtable<String, Object> rowtmp=(Hashtable)rowdata.clone();
									String[] ff=line[j].split(pp[1]);
									rowtmp.put(fname, line[j]);
									for(int i=0;i<ff.length;++i)
									{
										rowtmp.put(fname+(i+1), ff[i]);
									}
									conf.to.table.insertRow(rowtmp, exto, colmap);
								}
							}
						}
						else
						{
							conf.to.table.insertRow(rs, exto, colmap);
						}
						if(dcolmap!=null)	//do diff
						{
							param.diffkey=rs.getString(conf.difffrom._keyfield.toLowerCase());
							ResultSet drs=exdfrom.executeQuery(conf.getQueryFrom(conf.difffrom.query));
							if(drs.next())
							{
								if(conf.difffrom.isDiff(rs,drs))
								{
									conf.diffto.table.insertRow(rs, exdto, dcolmap);
								}
							}
							else if("true".equalsIgnoreCase(conf.difffrom._addnew))
							{
								conf.diffto.table.insertRow(rs, exdto, dcolmap);
							}
							drs.close();
						}
					}
				}
				catch(Exception ex)
				{
					String msg="Data Sample: "+conf.to.table.getDataSample(rs);
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
			}
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
			exfrom.close();
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
		}
	}

}

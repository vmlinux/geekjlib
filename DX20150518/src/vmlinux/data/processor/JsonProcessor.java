package vmlinux.data.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vmlinux.data.entity.ExecParam;
import vmlinux.data.entity.FieldMap;
import vmlinux.data.entity.OData;
import vmlinux.tool.DataTransfer;
import vmlinux.util.DbExecuteEx;

public class JsonProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		//init json
		JSONObject jo=new JSONObject(vmlinux.util.StringUtil.readFile(
				DataTransfer.fileParameter(conf,conf.filefrom._file)));
		JSONArray jrows=jo.getJSONArray(conf.filefrom._array);
		String[] flds=null;
		String[] newflds=null;
		if(jrows.length()>0)
		{
			jo=jrows.getJSONObject(0);
			Hashtable<String, String> xd=new Hashtable<String, String>();
			for(int i=0;i<jrows.length() && i<100; ++i)
			{
				jo=jrows.getJSONObject(i);
				Iterator<String> it=jo.keys();
				while(it.hasNext())
				{
					xd.put(it.next(), "");
				}
			}
			flds=new String[xd.size()];
			Enumeration<String> enu=xd.keys();
			int n=0;
			while(enu.hasMoreElements())
			{
				flds[n++]=enu.nextElement();
			}
			newflds=flds.clone();
			if("true".equalsIgnoreCase(conf.filefrom._autosqlinit))
			{
				Hashtable<String, FieldMap> tab=new Hashtable<String, FieldMap>();
				for(int i=0;i<flds.length;++i)
				{
					FieldMap f=new FieldMap();
					f._=flds[i];
					f._name=f._;
					f._type="string";
					f._sqltype=conf.filefrom._defaultsqltype;
					tab.put(f._, f);
				}
				if(conf.filefrom.col!=null)
				{
					for(int i=0;i<conf.filefrom.col.length;++i)
					{
						FieldMap f=conf.filefrom.col[i];
						newflds[i]=f._name.toLowerCase();
						tab.put(f._name, f);
					}
				}
				conf.filefrom.col=tab.values().toArray(new FieldMap[0]);
				Arrays.sort(conf.filefrom.col,new Comparator<FieldMap>(){
					@Override
					public int compare(FieldMap o1, FieldMap o2)
					{
						return o1._name.compareTo(o2._name);
					}
				});
			}
			else
			{
				for(int i=0;i<conf.filefrom.col.length;++i)
				{
					FieldMap f=conf.filefrom.col[i];
					newflds[i]=f._name.toLowerCase();
				}
			}
		}
		else
		{
			System.out.println(" no data, exit.");
			return;
		}
		
		if(conf.to!=null && conf.to.table!=null)
		{
			conf.to.table.prepare(conf);
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
		if(!"none".equalsIgnoreCase(conf._preaction)
				&& "true".equalsIgnoreCase(conf.filefrom._autosqlinit)
				&& conf.to!=null && conf.to.table!=null && conf.to.table._name!=null)
		{
			try
			{
				exto.executeUpdate("drop table "+conf.to.table._name);
			}
			catch(Exception ex)
			{
				System.err.println("[auto init failed:drop "+conf.to.table._name+"]");
				//ex.printStackTrace();
			}
			try
			{
				exto.executeUpdate(conf.filefrom.generateSQLDML(conf.to.table._name));
			}
			catch(Exception ex)
			{
				System.err.println("[auto init failed:create "+conf.to.table._name+"]");
				//ex.printStackTrace();
			}
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

			//start json
			int n=0;
			int nc=conf.getNCommit();
			ExecParam param=conf.getParentParam();
	        for(;n<jrows.length();++n)
	        {
	        	jo=jrows.getJSONObject(n);
	        	Hashtable<String,Object> row=null;
				try
				{
					/*
			        if(n==18)
			        {
			        	System.out.println("debug line");
			        }
			        */
		        	row=new Hashtable<String, Object>();
		        	for(int i=0;i<flds.length;++i)
		        	{
		        		String v=null;
		        		try
		        		{
		        			v=jo.get(flds[i]).toString();
		        		}
		        		catch(JSONException ex)
		        		{
		        			
		        		}
		        		if("true".equalsIgnoreCase(conf.filefrom._trimdata) && v!=null)
		        		{
		        			v=v.trim();
		        		}
		        		if("true".equalsIgnoreCase(conf.filefrom._ignorenull)
		        				&& "null".equalsIgnoreCase(v))
		        		{
		        			v=null;
		        		}
		        		DataTransfer.setIfNotNull(row,newflds[i],v);
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
			};
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
		}
	}

}

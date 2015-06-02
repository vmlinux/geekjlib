package vmlinux.data.processor;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import vmlinux.data.entity.ColumnDefine;
import vmlinux.data.entity.ExecParam;
import vmlinux.data.entity.OData;
import vmlinux.reflect.XDynamic;
import vmlinux.util.DbExecuteEx;

public class ParamProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		if(conf.to.param!=null)
		{
			DataSource dsfrom=conf.from.source.getDataSource();
			DbExecuteEx exfrom=new DbExecuteEx(dsfrom);
			String p=null;
			if(conf.to.param._method==null || "value".equalsIgnoreCase(conf.to.param._method))
			{
				p=exfrom.executeValue(conf.getQueryFrom());
			}
			if("create_table".equalsIgnoreCase(conf.to.param._method))
			{
				p=processParam_create_table(exfrom,conf.getQueryFrom());
			}
			if("map_param".equalsIgnoreCase(conf.to.param._method))
			{
				p=processParam_map_param(conf,exfrom,conf.getQueryFrom());
			}
			if(conf.to.param._name!=null)
			{
				ExecParam param=conf.getParentParam();
				if(conf.to.param._name.startsWith("v."))
				{
					if(param.v==null)
					{
						param.v=new XDynamic();
					}
					param.v.setProperty(conf.to.param._name.substring(2), p);
					conf.reportStatus("  参数："+conf.to.param._name+" => "+p);
				}
				else
				{
					Field f=p.getClass().getField(conf.to.param._name);
					if(f!=null)
					{
						f.set(param, p);
						conf.reportStatus("  参数："+f.getName()+" => "+p);
					}
				}
			}
			return;
		}
	}

	private String processParam_create_table(DbExecuteEx exfrom,String query) throws Exception
	{
		ColumnDefine[] cols=(ColumnDefine[])exfrom.executeArray(query, ColumnDefine.class);
		StringBuffer sb=new StringBuffer("create table "+cols[0].table_name+"(");
		for(int i=0;i<cols.length;++i)
		{
			if(i>0)
			{
				sb.append(",");
			}
			sb.append(cols[i].column_name+" ");
			sb.append(cols[i].data_type);
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String processParam_map_param(OData conf,DbExecuteEx exfrom,String query) throws Exception
	{
		ResultSet res=exfrom.executeRow(query);
		ResultSetMetaData meta=res.getMetaData();
		if(res.next())
		{
			int l=meta.getColumnCount();
			ExecParam p=conf.getParentParam();
			for(int i=0;i<l;++i)
			{
				String name=meta.getColumnName(i).toLowerCase();
				String v=res.getString(i);
				if(name.startsWith("v."))
				{
					if(p.v==null)
					{
						p.v=new XDynamic();
					}
					p.v.setProperty(conf.to.param._name.substring(2), v);
					conf.reportStatus("  参数："+conf.to.param._name+" => "+v);
				}
				else
				{
					Field f=p.getClass().getField(name);
					if(f!=null)
					{
						f.set(p, v);
						conf.reportStatus("  参数："+f.getName()+" => "+v);
					}
				}
			}
		}
		res.close();
		return "ok";
	}
}

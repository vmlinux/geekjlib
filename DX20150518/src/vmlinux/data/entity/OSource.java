package vmlinux.data.entity;

import java.util.Hashtable;

import javax.sql.DataSource;

import vmlinux.reflect.XObject;
import vmlinux.tool.DataTransfer;
import vmlinux.util.DbExecuteEx;

public class OSource implements XObject
{
	public static Hashtable<String,DataSource> sourceshare=new Hashtable<String, DataSource>();
	
	public String _driver;	//prop
	public String _url;	//prop
	public int _maxconn;
	public String _alias;
	public String _name;
	
	public DataSource getDataSource() throws Exception
	{
		DataSource ds=null;
		if(_alias!=null && _alias.length()>0)
		{
			ds=sourceshare.get(_alias);
		}
		if(ds==null && _url!=null)
		{
			ds=sourceshare.get(_url);
		}
		if(ds==null && _url!=null)
		{
			if(_driver!=null && _driver.length()>0)
			{
				Class.forName(_driver);
			}
			if(_maxconn>0)
			{
				ds=DbExecuteEx.setupDataSource(_url,_maxconn);
			}
			else
			{
				ds=DbExecuteEx.setupDataSource(_url);
			}
			sourceshare.put(_url, ds);
		}
		if(ds!=null && _name!=null && _name.length()>0)
		{
			sourceshare.put(_name, ds);
		}
		if(ds==null)
		{
			//throw new RuntimeException(vmlinux.util.StringUtil.format("datasource is null(alias={_alias},url={_url},driver={_driver})", this));
		}
		return ds;
	}
	
	public void prepare() throws Exception
	{
		if(_driver!=null && _driver.startsWith("$"))
		{
			_driver=DataTransfer.getProperty(_driver);
		}
		if(_url!=null && _url.startsWith("$"))
		{
			_url=DataTransfer.getProperty(_url);
		}
		getDataSource();
	}
}


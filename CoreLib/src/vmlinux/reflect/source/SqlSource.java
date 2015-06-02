package vmlinux.reflect.source;

import java.util.*;
import java.util.Date;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;

import vmlinux.reflect.*;
import vmlinux.util.StringUtil;

@SuppressWarnings("unchecked")
//NOTE: this class is not yet tested
public class SqlSource extends XObjectSourceBase
{
	public static Object batchBuild(ResultSet src, String xclass)
	{
		try
		{
			return batchBuild(src, Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectSqlSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object batchBuild(ResultSet src, Class xclass)
	{
		try
		{
			int len=src.getFetchSize();
			int row=src.getRow();
			Object result=Array.newInstance(xclass, len-row);
			int i=0;
			while(src.next())
			{
				Array.set(result, i, buildObject(src,xclass));
				++i;
			}
			return result;
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectSqlSource::batchBuild:"+ex);
		}
		return null;
	}
	public static Object buildObject(ResultSet src,String xclass)
	{
		try
		{
			return buildObject(src,Class.forName(xclass));
		}
		catch(Exception ex)
		{
			System.err.println("[err]XObjectSqlSource::buildObject:"+ex);
		}
		return null;
	}
	public static Object buildObject(ResultSet src,Class xclass)
	{
		XObjectBuilder builder=new XObjectBuilder();
		SqlSource source=new SqlSource(src);
		try
		{
			return builder.build(source, xclass);
		}
		catch (Exception ex)
		{
			System.err.println("[err]XObjectSqlSource::buildObject:"+ex);
		}
		return null;
	}
	
	protected ResultSet source;
	protected String prefix;
	protected HashMap<String,Integer> colnames;
	protected ResultSetMetaData sourcemeta;
	
	public SqlSource(ResultSet result)
	{
		this.source=result;
		this.prefix="";
		loadColnames();
	}
	protected SqlSource(String prefix,ResultSet result)
	{
		this.prefix=prefix;
		this.source=result;
		loadColnames();
	}
	
	protected void loadColnames()
	{
		try
		{
			sourcemeta=this.source.getMetaData();
			colnames=new HashMap<String,Integer>();
			int n=sourcemeta.getColumnCount();
			for(int i=1;i<=n;++i)
			{
				colnames.put(sourcemeta.getColumnName(i).toUpperCase(),new Integer(i));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	protected String getColumnType(String name)
	{
		name=name.toUpperCase();
		if(colnames.containsKey(name))
		{
			try
			{
				int n=(Integer)colnames.get(name);
				String tn=sourcemeta.getColumnTypeName(n);
				return tn;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	protected Object getData(String name,Class clazz)
	{
		name=name.toUpperCase();
		if(colnames.containsKey(name))
		{
			try
			{
				String lname=StringUtil.trim(name, '_');
				Object data=null;
				if(Date.class.equals(clazz))
				{
					data=source.getTimestamp(prefix+lname);
				}
				else if(String.class.equals(clazz))
				{
					if("CLOB".equalsIgnoreCase(getColumnType(prefix+lname)))
					{
						data=source.getClob(prefix+lname);
						if(data!=null)
						{
							java.sql.Clob clob=(java.sql.Clob)data;
							data=clob.getSubString((int)1, (int)clob.length());
						}
					}
					else
					{
						data=source.getString(prefix+lname);
					}
				}
				else
				{
					data=source.getObject(prefix+lname);
				}
				return data;
			}
			catch(SQLException ex)
			{
				throw new RuntimeException(ex);
			}
		}
		return null;
	}
	
	public Object get(String name)
	{
		return getData(name,null);
	}

	public Object getArray(String name, Class aclass)
	{
		name=name.toUpperCase();
		try
		{
			String lname=StringUtil.trim(name, '_');
			if(colnames.containsKey(lname))
			{
				Object data=source.getArray(lname).getArray();
				return getArrayX(data, aclass);
			}
			return null;
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public byte[] getByteArray(String name)
	{
		name=name.toUpperCase();
		try
		{
			String lname=StringUtil.trim(name, '_');
			if(colnames.containsKey(lname))
			{
				Object data=source.getArray(lname).getArray();
				return (byte[])getArrayX(data, byte.class);
			}
			return null;
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	            
	public boolean getBoolean(String name)
	{
		Object data=getData(name,boolean.class);
		return getBooleanX(data);
	}

	public Date getDate(String name)
	{
		Object data=getData(name,Date.class);
		return getDateX(data);
	}

	public double getDouble(String name)
	{
		Object data=getData(name,double.class);
		return getDoubleX(data);
	}

	public float getFloat(String name)
	{
		Object data=getData(name,float.class);
		return getFloatX(data);
	}

	public int getInt32(String name)
	{
		Object data=getData(name,int.class);
		return getInt32X(data);
	}

	public long getLong(String name)
	{
		Object data=getData(name,long.class);
		return getLongX(data);
	}
	
	public byte getByte(String name)
	{
		Object data=getData(name,byte.class);
		return getByteX(data);
	}

	public Object getObject(String name, Class xclass)
			throws IllegalAccessException, InstantiationException
	{
		name=name.toUpperCase();
		try
		{
			ResultSetMetaData metadata=sourcemeta;
			int cc=metadata.getColumnCount();
			String lname=StringUtil.trim(name, '_');
			String oprefix=prefix+lname+".";
			//dealing object with single _ field
			boolean istobj=false;
			//Class tclass=null;
			Field tfield=null;
			String tname=prefix+lname;
			try
			{
				tfield=xclass.getField("_");
				istobj=tfield!=null;
				/*
				if(istobj)
					tclass=tfield.getType();
				*/
			}
			catch(Exception ex)
			{
				//ignore error
			}
			for(int i=1;i<=cc;++i)
			{
				String colname=metadata.getColumnName(i);
				if(colname.startsWith(oprefix))
				{
					SqlSource src=new SqlSource(oprefix,source);
					return getObjectInner(src, xclass);
				}
				else if(istobj && colname.equals(tname))//_ field
				{
					Object obj=XObjectBuilder.createObject(xclass, this);
					tfield.set(obj, getX(get(name),tfield.getType()));
					return obj;
				}
			}
			return null;
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IllegalArgumentException ex)
		{
			System.err.println("[err]SqlSource::getObject:"+name);
			throw new RuntimeException(ex);
		}
	}

	public String getString(String name)
	{
		Object data=getData(name,String.class);
		return getStringX(data);
	}

	public XDynamic getDynamic()
	{
		return null;
	}
}

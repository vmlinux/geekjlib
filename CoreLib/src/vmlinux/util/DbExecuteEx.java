package vmlinux.util;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.apache.commons.dbcp.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;

import vmlinux.reflect.source.*;

@SuppressWarnings("unchecked")
public class DbExecuteEx extends DbExecute
{
	public static class DatabasePool extends PoolingDataSource
	{
		public DatabasePool(ObjectPool pool)
		{
			super(pool);
		}
		
		public void putConnection(Connection conn)
		{
			try
			{
				_pool.returnObject(conn);
			}
			catch(Exception ex)
			{
				
			}
		}
	}
	
	public static DataSource setupDataSource(String connectURI) 
	{
        ObjectPool connectionPool = new GenericObjectPool(
        		null,8,GenericObjectPool.WHEN_EXHAUSTED_GROW,100);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);
        new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        DataSource dataSource = new DbExecuteEx.DatabasePool(connectionPool);

        return dataSource;
    }
	
	public static DataSource setupDataSource(String connectURI,int maxConn)
	{
		int maxAct=maxConn>8?8:maxConn;
        ObjectPool connectionPool = new GenericObjectPool(
        		null,maxAct,GenericObjectPool.WHEN_EXHAUSTED_GROW,maxConn);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);
        new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        DataSource dataSource = new DbExecuteEx.DatabasePool(connectionPool);

        return dataSource;
	}
	
	public static String getOracleConnectionString(String server,String sid,String user,String pwd)
	{
		StringBuffer sb=new StringBuffer("jdbc:oracle:thin:");
		sb.append(user);
		sb.append("/");
		sb.append(pwd);
		sb.append("@");
		sb.append(server);
		sb.append(":1521:");
		sb.append(sid);
		return sb.toString();
	}
	
	public static String getAccessConnectionString(String file)
	{
		if(file.endsWith(".accdb"))
		{
			return getAccess2007ConnectionString(file);
		}
		else
		{
			return getAccess2003ConnectionString(file);
		}
	}
	
	public static String getExcelConnectionString(String file)
	{
		if(file.endsWith(".xlsx") || file.endsWith(".xlsm") || file.endsWith(".xlsb"))
		{
			return getExcel2007ConnectionString(file);
		}
		else
		{
			return getExcel2003ConnectionString(file);
		}
	}
	
	public static String getAccess2007ConnectionString(String file)
	{
		StringBuffer sb=new StringBuffer("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=");
		sb.append(file);
		sb.append(";READONLY=FALSE");
		return sb.toString();
	}

	public static String getAccessTextConnectionString(String file)
	{
		StringBuffer sb=new StringBuffer("jdbc:odbc:Driver={Microsoft Access Text Driver (*.txt, *.csv)};DBQ=");
		sb.append(file);
		sb.append(";FIRSTROWHASNAMES=1;READONLY=FALSE");
		return sb.toString();
	}

	public static String getExcel2007ConnectionString(String file)
	{
		StringBuffer sb=new StringBuffer("jdbc:odbc:Driver={Microsoft Excel Driver (*.xls, *.xlsx, *.xlsm, *.xlsb)};DBQ=");
		sb.append(file);
		sb.append(";FIRSTROWHASNAMES=1;READONLY=FALSE");
		return sb.toString();
	}
	
	public static String getAccess2003ConnectionString(String file)
	{
		StringBuffer sb=new StringBuffer("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=");
		sb.append(file);
		sb.append(";READONLY=FALSE");
		return sb.toString();
	}
	
	public static String getExcel2003ConnectionString(String file)
	{
		StringBuffer sb=new StringBuffer("jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ=");
		sb.append(file);
		sb.append(";FIRSTROWHASNAMES=1;READONLY=FALSE");
		return sb.toString();
	}
	
	public static Connection getTransactionConnection(DataSource ds)
	{
		try
		{
			Connection conn=ds.getConnection();
			conn.setAutoCommit(false);
			return conn;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void rollbackTransactionConnection(Connection conn)
	{
		if(conn!=null)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public static void commitTransactionConnection(Connection conn)
	{
		if(conn!=null)
		{
			try
			{
				conn.commit();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	protected DatabasePool mypool;
	
	public DbExecuteEx(DataSource ds)
	{
		super(ds);
		if(DatabasePool.class.equals(ds.getClass()))
		{
			mypool=(DatabasePool)ds;
		}
	}
	
	public DbExecuteEx(Connection conn)
	{
		super(conn);
		mypool=null;
	}

	protected void closeConn(Connection conn)
	{
		if(mypool!=null)
		{
			mypool.putConnection(conn);
			this.conn=null;
		}
		else
		{
			super.closeConn(conn);
		}
	}
	
	public int executeArray(Object[] arr,Class clazz) throws SQLException
	{
		ResultSet xrs=executeQuery();
		int i=0;
		while(i<arr.length && xrs.next())
		{
			if(String.class.equals(clazz))
			{
				arr[i]=xrs.getString(1);
			}
			else if(java.util.Date.class.equals(clazz))
			{
				arr[i]=xrs.getTimestamp(1);
			}
			else
			{
				arr[i]=SqlSource.buildObject(xrs, clazz);
			}
			i++;
		}
		return i;
	}

	public int executeArray(String sql,Object[] arr,Class clazz) throws SQLException
	{
		ResultSet xrs=executeQuery(sql);
		int i=0;
		while(i<arr.length && xrs.next())
		{
			if(String.class.equals(clazz))
			{
				arr[i]=xrs.getString(1);
			}
			else if(java.util.Date.class.equals(clazz))
			{
				arr[i]=xrs.getTimestamp(1);
			}
			else
			{
				arr[i]=SqlSource.buildObject(xrs, clazz);
			}
			i++;
		}
		return i;
	}

	public ArrayList executeList(Class clazz) throws SQLException
	{
		ArrayList x=new ArrayList();
		ResultSet xrs=executeQuery();
		int i=0;
		while(xrs.next())
		{
			if(String.class.equals(clazz))
			{
				x.add(xrs.getString(1));
			}
			else if(java.util.Date.class.equals(clazz))
			{
				x.add(xrs.getTimestamp(1));
			}
			else
			{
				x.add(SqlSource.buildObject(xrs, clazz));
			}
			i++;
		}
		return x.size()>0?x:null;
	}
	
	public ArrayList executeList(String sql,Class clazz) throws SQLException
	{
		ArrayList x=new ArrayList();
		ResultSet xrs=executeQuery(sql);
		int i=0;
		while(xrs.next())
		{
			if(String.class.equals(clazz))
			{
				x.add(xrs.getString(1));
			}
			else if(java.util.Date.class.equals(clazz))
			{
				x.add(xrs.getTimestamp(1));
			}
			else
			{
				x.add(SqlSource.buildObject(xrs, clazz));
			}
			i++;
		}
		return x.size()>0?x:null;
	}
	
	public Object executeArray(Class clazz) throws SQLException
	{
		ArrayList x=new ArrayList();
		ResultSet xrs=executeQuery();
		while(xrs.next())
		{
			if(String.class.equals(clazz))
			{
				x.add(xrs.getString(1));
			}
			else if(java.util.Date.class.equals(clazz))
			{
				x.add(xrs.getTimestamp(1));
			}
			else
			{
				x.add(SqlSource.buildObject(xrs, clazz));
			}
		}
		if(x.size()>0)
		{
			Object arr=java.lang.reflect.Array.newInstance(clazz, x.size());
			for(int i=0;i<x.size();++i)
			{
				java.lang.reflect.Array.set(arr, i, x.get(i));
			}
			return arr;
		}
		return null;
	}
	
	public Object executeArray(String sql,Class clazz) throws SQLException
	{
		ArrayList x=new ArrayList();
		ResultSet xrs=executeQuery(sql);
		while(xrs.next())
		{
			if(String.class.equals(clazz))
			{
				x.add(xrs.getString(1));
			}
			else if(java.util.Date.class.equals(clazz))
			{
				x.add(xrs.getTimestamp(1));
			}
			else
			{
				x.add(SqlSource.buildObject(xrs, clazz));
			}
		}
		if(x.size()>0)
		{
			Object arr=java.lang.reflect.Array.newInstance(clazz, x.size());
			for(int i=0;i<x.size();++i)
			{
				java.lang.reflect.Array.set(arr, i, x.get(i));
			}
			return arr;
		}
		return null;
	}
	
	public PreparedStatement prepareUpdate(String sql,Object o) throws SQLException
	{
		Class clazz=o.getClass();
		StringBuffer sb=new StringBuffer();
		Field[] f=clazz.getFields();
		for(int i=0,c=0;i<f.length;++i)
		{
			String name=f[i].getName();
			if(name.endsWith("_"))
				continue;
			if(c>0)
				sb.append(",");
			sb.append(name);
			++c;
		}
		return prepareUpdate(sql,o,sb.toString().split(","));
	}

	public PreparedStatement prepareInsert(String sql,Object o) throws SQLException
	{
		Class clazz=o.getClass();
		StringBuffer sb=new StringBuffer();
		Field[] f=clazz.getFields();
		for(int i=0,c=0;i<f.length;++i)
		{
			String name=f[i].getName();
			if(name.endsWith("_"))
				continue;
			if(c>0)
				sb.append(",");
			sb.append(name);
			++c;
		}
		return prepareInsert(sql,o,sb.toString().split(","));
	}
	
	public PreparedStatement prepareUpdate(String sql,Object o,String[] fields) throws SQLException
	{
		//sql like update db_t_abc ?? where id=?
		Class clazz=o.getClass();
		StringBuffer sb=new StringBuffer("set");
		for(int i=0;i<fields.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(" "+fields[i]+"=?");
		}
		sql=sql.replaceFirst("\\?\\?", sb.toString());
		PreparedStatement stmt=prepare(sql);
		try
		{
			for(int i=0;i<fields.length;++i)
			{
				Field f=clazz.getField(fields[i]);
				f.setAccessible(true);
				Class fclazz=f.getType();
				if(String.class.equals(fclazz))
				{
					String v=(String)f.get(o);
					stmt.setString(i+1, v);
				}
				else if(Double.class.equals(fclazz) || double.class.equals(fclazz))
				{
					double v=f.getDouble(o);
					stmt.setDouble(i+1, v);
				}
				else if(Integer.class.equals(fclazz) || int.class.equals(fclazz))
				{
					int v=f.getInt(o);
					stmt.setInt(i+1, v);
				}
				else if(java.util.Date.class.equals(fclazz))
				{
					java.util.Date v=(java.util.Date)f.get(o);
					if(v!=null)
						stmt.setTimestamp(i+1, new Timestamp(v.getTime()));
					else
						stmt.setTimestamp(i+1, null);
				}
				else if(Float.class.equals(fclazz) || float.class.equals(fclazz))
				{
					float v=f.getFloat(o);
					stmt.setFloat(i+1, v);
				}
				else
				{
					System.err.println("Unknown field type "+f.getName()+":"+fclazz);
				}
			}
			return stmt;
		}
		catch(NoSuchFieldException ex)
		{
			
		}
		catch(SecurityException ex)
		{
			
		}
		catch(IllegalAccessException ex)
		{
			
		}
		return null;
	}

	public PreparedStatement prepareInsert(String sql,Object o,String[] fields) throws SQLException
	{
		//sql like insert into db_t_abc (??) values (??)
		Class clazz=o.getClass();
		StringBuffer sb1=new StringBuffer();
		StringBuffer sb2=new StringBuffer();
		for(int i=0;i<fields.length;++i)
		{
			if(i>0)
			{
				sb1.append(",");
				sb2.append(",");
			}
			sb1.append(" "+fields[i]);
			sb2.append(" ?");
		}
		sql=sql.replaceFirst("\\?\\?", sb1.toString());
		sql=sql.replaceFirst("\\?\\?", sb2.toString());
		PreparedStatement stmt=prepare(sql);
		try
		{
			for(int i=0;i<fields.length;++i)
			{
				Field f=clazz.getField(fields[i]);
				f.setAccessible(true);
				Class fclazz=f.getType();
				if(String.class.equals(fclazz))
				{
					String v=(String)f.get(o);
					stmt.setString(i+1, v);
				}
				else if(Double.class.equals(fclazz) || double.class.equals(fclazz))
				{
					double v=f.getDouble(o);
					stmt.setDouble(i+1, v);
				}
				else if(Integer.class.equals(fclazz) || int.class.equals(fclazz))
				{
					int v=f.getInt(o);
					stmt.setInt(i+1, v);
				}
				else if(java.util.Date.class.equals(fclazz))
				{
					java.util.Date v=(java.util.Date)f.get(o);
					if(v!=null)
						stmt.setTimestamp(i+1, new Timestamp(v.getTime()));
					else
						stmt.setTimestamp(i+1, null);
				}
				else if(Float.class.equals(fclazz) || float.class.equals(fclazz))
				{
					float v=f.getFloat(o);
					stmt.setFloat(i+1, v);
				}
				else
				{
					System.err.println("Unknown field type "+f.getName()+":"+fclazz);
				}
			}
			return stmt;
		}
		catch(NoSuchFieldException ex)
		{
			
		}
		catch(SecurityException ex)
		{
			
		}
		catch(IllegalAccessException ex)
		{
			
		}
		return null;
	}
}

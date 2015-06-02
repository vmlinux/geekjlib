package vmlinux.tool;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import vmlinux.reflect.XObject;
import vmlinux.reflect.serializer.JSONSerializer;
import vmlinux.util.DbExecuteEx;

public class JsonDAO
{
	public static class ResultInfo implements XObject
	{
		public String json;
		public String msg;
		public String clazz;
	}
	public static class SqlInfo implements XObject
	{
		public String sqlname;
		public String sqlstr;
		public String headmap;
		public String colheaders;
		public String columns;
		public String tabconf;
		public String colnames;
	}
	public static class ParamInfo implements XObject
	{
		public String planstr;
		public String p1;
		public String p2;
		public String p3;
		public String p4;
		public String p5;
		public String p6;
		public String p7;
	}
	public static abstract class CachedObject
	{
		public abstract Object getObject() throws Exception;
		public Object getCachedObject(HttpSession sess,String name)
		{
			Object o=sess.getAttribute(name);
			if(o==null)
			{
				try
				{
					o=getObject();
					sess.setAttribute(name, o);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			return o;
		}
	}
	
	private static JsonDAO inst;
	private static Hashtable<String, JsonDAO> pool;
	private static Properties cfg;
	private static Properties prop;
	
	public static JsonDAO getInstance()
	{
		if(inst==null)
		{
			inst=new JsonDAO();
			//TaskManager.getInstance().check();
			prop=new Properties();
			File f=new File("jsondao.properties");
			if(f.exists())
			{
				try
				{
					prop.load(new FileInputStream(f));
				}
				catch(java.io.IOException ex)
				{
					System.err.println("properties read error:"+f.getAbsolutePath());
				}
			}
			else
			{
				System.err.println("properties not found:"+f.getAbsolutePath());
			}
		}
		return inst;
	}
	
	public static JsonDAO getInstance(String url)
	{
		JsonDAO j=null;
		if(pool==null)
		{
			pool=new Hashtable<String, JsonDAO>();
		}
		if(!pool.containsKey(url))
		{
			j=new JsonDAO(url);
			pool.put(url, j);
		}
		else
		{
			j=pool.get(url);
		}
		return j;
	}
	
	public static JsonDAO getConfiguredInstance(String name)
	{
		String url=getConfiguredString(name);
		if(url!=null)
		{
			return getInstance(url);
		}
		return null;
	}
	
	public static String getConfiguredString(String name)
	{
		if(cfg==null)
		{
			cfg=new Properties();
			URL url=JsonDAO.class.getResource("/jsondao.property");
			try
			{
				cfg.load(url.openStream());
			}
			catch(Exception ex)
			{
				
			}
		}
		return cfg.getProperty(name);
	}
	
	public static void writePageLog(HttpServletRequest request,String user,String name) throws SQLException
	{
		String ip=request.getHeader("X-Real-IP");
		if(ip==null || ip.length()==0)
		{
			ip=request.getRemoteAddr();
		}
		getInstance().executeUpdate(getConfiguredString("jdpc.pagelogsql")
			,user,ip,name);
	}
	
	private String connstr;
	private DataSource ds;
	private int sync_flag;

	public JsonDAO()
	{
		connstr=null;
	}
	public JsonDAO(String conn)
	{
		connstr=conn;
	}
	public synchronized int setSyncFlag(int v)
	{
		int oldv=this.sync_flag;
		this.sync_flag=v;
		return oldv;
	}
	public String getProperty(String name)
	{
		return prop.getProperty(name);
	}
	private Connection getConnection() throws SQLException
	{
		Connection conn=null;
		if(ds==null)
		{
			System.out.println("getConnection:1");
			if(connstr==null)
			{
				connstr=prop.getProperty("$main_url");
				String drvstr=prop.getProperty("$main_driver");
				System.out.println("getConnection:2:"+connstr);
				try
				{
					Class.forName(drvstr);
				}
				catch(Exception ex)
				{
					
				}
			}
			ds=DbExecuteEx.setupDataSource(connstr);
		}
		if(ds!=null)
		{
			conn=ds.getConnection();
		}
		if(conn!=null && !conn.getAutoCommit())
		{
			conn.setAutoCommit(true);
		}
		return conn;
	}

	public String executeValue(String sql) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			return exec.executeValue(sql);
		}
		finally
		{
			exec.close();
		}
	}
	
	public String executeValue(String sql,Object ... params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			return exec.executeValue();
		}
		finally
		{
			exec.close();
		}
	}

	public String[] executeValues(String sql) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			ResultSet rs=exec.executeRow(sql);
			ResultSetMetaData meta=rs.getMetaData();
			String[] ret=new String[meta.getColumnCount()];
			if(rs.next())
			{
				for(int i=0;i<ret.length;++i)
				{
					ret[i]=rs.getString(i+1);
				}
			}
			return ret;
		}
		finally
		{
			exec.close();
		}
	}
	
	public String[] executeValues(String sql,Object ... params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			ResultSet rs=exec.executeRow();
			ResultSetMetaData meta=rs.getMetaData();
			String[] ret=new String[meta.getColumnCount()];
			if(rs.next())
			{
				for(int i=0;i<ret.length;++i)
				{
					ret[i]=rs.getString(i+1);
				}
			}
			return ret;
		}
		finally
		{
			exec.close();
		}
	}

	public String executeConcat(String sql,String p) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			ResultSet rs=exec.executeQuery(sql);
			StringBuilder sb=new StringBuilder();
			int count=0;
			while(rs.next())
			{
				sb.append(rs.getString(1));
				if(count>0)
					sb.append(p);
				count++;
			}
			rs.close();
			return sb.toString();
		}
		finally
		{
			exec.close();
		}
	}

	public String executeConcat(String sql,String p,Object ... params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			ResultSet rs=exec.executeQuery();
			StringBuilder sb=new StringBuilder();
			int count=0;
			while(rs.next())
			{
				sb.append(rs.getString(1));
				if(count>0)
					sb.append(p);
				count++;
			}
			rs.close();
			return sb.toString();
		}
		finally
		{
			exec.close();
		}
	}

	public String executeList(String sql) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		String liststr="{\"json\":[";
		try
		{
			ResultSet rs=exec.executeQuery(sql);
			while(rs.next())
			{
				String objstr="{";
				ResultSetMetaData md=rs.getMetaData();
				int n=md.getColumnCount();
				for(int i=1;i<=n;++i)
				{
					String fld=md.getColumnName(i);
					String val=rs.getString(i);
					if(i>1)
					{
						objstr+=",";
					}
					if(val==null || "null".equalsIgnoreCase(val))
					{
						val="";
					}
					objstr+="\""+fld+"\":\""+ val.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"")
						.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") +"\"";
				}
				objstr+="}";
				liststr+=objstr+",";
			}
			rs.close();
			liststr+="{}";
		}
		finally
		{
			exec.close();
		}
		liststr+="],\"msg\":\"ok\"}";
		return liststr;
	}

	public String executeTextList(String sql,Object ...params ) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		StringBuffer liststr=new StringBuffer();
		try
		{
			exec.format(sql, params);
			ResultSet rs=exec.executeQuery();
			ResultSetMetaData md=rs.getMetaData();
			int n=md.getColumnCount();
			for(int i=1;i<=n;++i)
			{
				String fld=md.getColumnName(i);
				liststr.append(fld);
				if(i<n)
					liststr.append("\t");
			}
			liststr.append("\r\n");
			while(rs.next())
			{
				for(int i=1;i<=n;++i)
				{
					//String fld=md.getColumnName(i);
					String val=rs.getString(i);
					if(i>1)
					{
						liststr.append("\t");
					}
					if(val==null || "null".equalsIgnoreCase(val))
					{
						val="";
					}
					liststr.append(val.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
				}
				liststr.append("\r\n");
			}
			rs.close();
		}
		finally
		{
			exec.close();
		}
		return liststr.toString();
	}

	public String executeList(String sql,Object ...params ) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		String liststr="{\"json\":[";
		try
		{
			exec.format(sql, params);
			ResultSet rs=exec.executeQuery();
			while(rs.next())
			{
				String objstr="{";
				ResultSetMetaData md=rs.getMetaData();
				int n=md.getColumnCount();
				for(int i=1;i<=n;++i)
				{
					String fld=md.getColumnName(i);
					String val=rs.getString(i);
					if(i>1)
					{
						objstr+=",";
					}
					if(val==null || "null".equalsIgnoreCase(val))
					{
						val="";
					}
					objstr+="\""+fld+"\":\""+ val.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"")
						.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") +"\"";
				}
				objstr+="}";
				liststr+=objstr+",";
			}
			rs.close();
			liststr+="{}";
		}
		finally
		{
			exec.close();
		}
		liststr+="],\"msg\":\"ok\"}";
		return liststr;
	}

	public String executeOne(String sql) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		String onestr="{";
		try
		{
			ResultSet rs=exec.executeQuery(sql);
			if(rs.next())
			{
				onestr+="\"json\":{";
				ResultSetMetaData md=rs.getMetaData();
				int n=md.getColumnCount();
				for(int i=1;i<=n;++i)
				{
					String fld=md.getColumnName(i);
					String val=rs.getString(i);
					if(i>1)
					{
						onestr+=",";
					}
					if(val==null || "null".equalsIgnoreCase(val))
					{
						val="";
					}
					onestr+="\""+fld+"\":\""+ val.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"")
						.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") +"\"";
				}
				onestr+="},";
			}
			rs.close();
			onestr+="\"msg\":\"ok\"";
		}
		finally
		{
			exec.close();
		}
		onestr+="}";
		return onestr;
	}
	
	public String executeOne(String sql,Object ... params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		String onestr="{";
		try
		{
			exec.format(sql, params);
			ResultSet rs=exec.executeQuery();
			if(rs.next())
			{
				onestr+="\"json\":{";
				ResultSetMetaData md=rs.getMetaData();
				int n=md.getColumnCount();
				for(int i=1;i<=n;++i)
				{
					String fld=md.getColumnName(i);
					String val=rs.getString(i);
					if(i>1)
					{
						onestr+=",";
					}
					if(val==null || "null".equalsIgnoreCase(val))
					{
						val="";
					}
					onestr+="\""+fld+"\":\""+ val.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"")
						.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") +"\"";
				}
				onestr+="},";
			}
			rs.close();
			onestr+="\"msg\":\"ok\"";
		}
		finally
		{
			exec.close();
		}
		onestr+="}";
		return onestr;
	}
	
	public Object executeObject(String sql,Class clazz) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			Object ret=exec.executeObject(sql,clazz);
			return ret;
		}
		finally
		{
			exec.close();
		}
	}
	public Object executeObject(String sql,Class clazz,Object ...params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			Object ret=exec.executeObject(clazz);
			return ret;
		}
		finally
		{
			exec.close();
		}
	}
	
	public Object executeArray(String sql,Class clazz) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			Object ret=exec.executeArray(sql, clazz);
			return ret;
		}
		finally
		{
			exec.close();
		}
	}
	public Object executeArray(String sql,Class clazz,Object ...params) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			Object ret=exec.executeArray(clazz);
			return ret;
		}
		finally
		{
			exec.close();
		}
	}

	public ResultInfo executeObject(String sql,String clazz) throws SQLException
	{
		ResultInfo r=new ResultInfo();
		r.clazz=clazz;
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			Object ret=exec.executeObject(sql,Class.forName(clazz));
			r.json=JSONSerializer.serializeObject(ret);
			r.msg="ok";
		}
		catch(ClassNotFoundException ex)
		{
			r.msg=ex.getMessage();
		}
		finally
		{
			exec.close();
		}
		return r;
	}
	
	public ResultInfo executeObject(String sql,String clazz,Object ... params) throws SQLException
	{
		ResultInfo r=new ResultInfo();
		r.clazz=clazz;
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			Object ret=exec.executeObject(Class.forName(clazz));
			r.json=JSONSerializer.serializeObject(ret);
			r.msg="ok";
		}
		catch(ClassNotFoundException ex)
		{
			r.msg=ex.getMessage();
		}
		finally
		{
			exec.close();
		}
		return r;
	}
	
	public int executeUpdate(String sql) throws SQLException
	{
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			return exec.executeUpdate(sql);
		}
		finally
		{
			exec.close();
		}
	}
	
	public int executeUpdate(String sql,Object ... params) throws SQLException
	{
		//SqlAdapter adapter=AppContext.getInstance().getSqlAdapter();
		Connection conn=getConnection();
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			exec.format(sql, params);
			return exec.executeUpdate();
		}
		finally
		{
			exec.close();
		}
	}
	
	public int executeBatch(String sql,XObject[] arr) throws SQLException
	{
		Connection conn=getConnection();
		boolean ac=conn.getAutoCommit();
		conn.setAutoCommit(false);
		DbExecuteEx exec=new DbExecuteEx(conn);
		try
		{
			for(int i=0;i<arr.length;++i)
			{
				exec.format(sql, arr[i]);
				exec.executeUpdate();
			}
			conn.commit();
			return arr.length;
		}
		catch(SQLException ex)
		{
			conn.rollback();
			throw ex;
		}
		finally
		{
			exec.close();
			conn.setAutoCommit(ac);
		}
	}
	
	public int executeBatch(String sql,XObject[] arr,Object ... params) throws SQLException
	{
		Connection conn=getConnection();
		boolean ac=conn.getAutoCommit();
		conn.setAutoCommit(false);
		DbExecuteEx exec=new DbExecuteEx(conn);
		Object[] p=new Object[params.length+1];
		for(int i=0;i<params.length;++i)
		{
			p[i+1]=params[i];
		}
		try
		{
			for(int i=0;i<arr.length;++i)
			{
				p[0]=arr[i];
				exec.format(sql, p);
				exec.executeUpdate();
			}
			conn.commit();
			return arr.length;
		}
		catch(SQLException ex)
		{
			conn.rollback();
			throw ex;
		}
		finally
		{
			conn.setAutoCommit(ac);
			exec.close();
		}
	}
	
}

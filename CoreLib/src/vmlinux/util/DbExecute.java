package vmlinux.util;

import java.lang.reflect.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.sql.*;

import vmlinux.codec.*;
import vmlinux.reflect.source.*;

@SuppressWarnings("unchecked")
public class DbExecute
{
	protected DataSource ds;
	protected Connection conn;

	protected PreparedStatement pstmt;
	protected CallableStatement cstmt;
	protected Statement stmt;

	protected ResultSet rs;

	protected int ret;

	protected ResultSet rsv;

	protected String rets;

	protected Object reto;

	public DbExecute(DataSource ds)
	{
		this.ds=ds;
		try
		{
			conn = ds.getConnection();
		}
		catch (SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public DbExecute(Connection conn)
	{
		this.ds=null;
		this.conn = conn;
	}
	
	public DbExecute(String connstr)
	{
		try
		{
			conn = DriverManager.getConnection(connstr);
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public int getResult()
	{
		return ret;
	}

	public String getResultString()
	{
		return rets;
	}

	public ResultSet getResultSet()
	{
		return rs;
	}

	public Object getResultObject()
	{
		return reto;
	}

	public Connection getConnection()
	{
		return conn;
	}

	public boolean isReady()
	{
		return conn != null;
	}

	Object format_param(String name,Object[] params) throws SQLException
	{
		String[] p=name.split(":",2);
		String[] n=p[0].split("\\.",2);
		String o_param=null;
		int o_index=0;
		String o_field=null;
		/*
		if(p.length>1)
		{
			o_param=p[1];
		}
		if(n.length>1)
		{
			o_index=Integer.valueOf(n[0]);
			o_field=n[1];
		}
		else
		{
			if(n[0].matches("^\\d+$"))
			{
				o_index=Integer.valueOf(n[0]);
			}
			else
			{
				o_field=n[0];
			}
		}
		Object o=params[o_index];
		*/
		Object o=null;
		if(p.length>1)
		{
			o_param=p[1];
		}
		if(n.length>1)
		{
			if(!n[0].matches("^\\d+$"))
			{
				o_field=n[0];
				o=params[0];
				o_param=n[1];
			}
			else
			{
				o_field=n[1];
				o_index=Integer.valueOf(n[0]);
				o=params[o_index];
			}
		}
		else
		{
			if(n[0].matches("^\\d+$"))
			{
				o_index=Integer.valueOf(n[0]);
			}
			else
			{
				o_field=n[0];
			}
			o=params[o_index];
		}
		////////////////////
		if(o==null)
		{
			return null;
		}
		Class o_class=o.getClass();
		if(o_field!=null)
		{
			try
			{
				Field f_field=o_class.getField(o_field);
				o=f_field.get(o);
			}
			catch(Exception ex)
			{
				//ex.printStackTrace();
				throw new SQLException("Error format field "+o_field);
			}
		}
		if(o==null)
		{
			return null;
		}
		o_class=o.getClass();
		if(String.class.equals(o_class) && o_param!=null)
		{
			if(o_param.startsWith("date,"))
			{
				SimpleDateFormat sdf=new SimpleDateFormat(o_param.substring(5));
				try
				{
					o=sdf.parse((String)o);
				}
				catch(Exception ex)
				{
					throw new SQLException("Date format error "+o_param);
				}
			}
			else if(o_param.equalsIgnoreCase("double"))
			{
				o=Double.parseDouble((String)o);
			}
			else if(o_param.equalsIgnoreCase("int"))
			{
				o=Integer.parseInt((String)o);
			}
			else
			{
				if("md5".equalsIgnoreCase(o_param))
				{
					o=StringUtil.md5Digest((String)o);
				}
				else if("sha1".equalsIgnoreCase(o_param))
				{
					o=StringUtil.sha1Digest((String)o);
				}
				else if("base64".equalsIgnoreCase(o_param))
				{
					o=Base64.encode(((String)o).getBytes());
				}
				else if("debase64".equalsIgnoreCase(o_param))
				{
					o=new String(Base64.decode((String)o));
				}
				else if(o_param.matches("len\\d+"))
				{
					int l=Integer.valueOf(o_param.substring(3));
					String s=(String)o;
					if(s.length()>l)
					{
						o=s.substring(0,l);
					}
				}
				else if(o_param!=null && o_param.matches("s\\d+(,\\d+)?"))
				{
					String s=(String)o;
					String[] l=o_param.split(",",2);
					int spos=Integer.valueOf(l[0].substring(1));
					int len=s.length();
					if(spos>=len)
					{
						return "";
					}
					else if(l.length>1)
					{
						int epos=Integer.valueOf(l[1]);
						if(epos>=len)
						{
							return s.substring(spos);
						}
						else
						{
							return s.substring(spos, epos);
						}
					}
					else
					{
						return s.substring(spos);
					}
				}
				else if(o_param!=null && o_param.matches("(d[\\+\\-\\*\\/]\\d+)?(,\\d+(\\.\\d+)?[df])?"))
				{
					String s=(String)o;
					String[] l=o_param.split(",",2);
					String cal=l[0];
					String fmt=l.length>1?l[1]:"d";
					long nn=Long.valueOf(s);
					if(cal.length()>=3)
					{
						long d=Long.valueOf(cal.substring(2));
						char c=cal.charAt(1);
						if(c=='+')
						{
							nn+=d;
						}
						else if(c=='-')
						{
							nn-=d;
						}
						else if(c=='*')
						{
							nn*=d;
						}
						else if(c=='/')
						{
							nn/=d;
						}
						s=""+nn;
					}
					if(fmt.endsWith("d"))
					{
						String f=fmt.substring(0, fmt.length()-1);
						l=f.split("\\.",2);
						int len=Integer.valueOf(l[0]);
						for(int i=s.length();i<len;++i)
						{
							s="0"+s;
						}
					}
					return s;
				}
				else
				{
					throw new SQLException("Unknown format param "+o_param);
				}
			}
			
		}
		else if(o_param!=null)
		{
			throw new SQLException("Format on non-string with param "+o_param);
		}
		return o;
	}
	
	public CallableStatement formatCall(String sql,Object... params) throws SQLException
	{
		StringBuffer sb=new StringBuffer();	//{0} {1.attr} {1.attr:H}
		char c;
		int s=0;	//0:out {}; 1:in {}
		int l=sql.length();
		ArrayList p=new ArrayList();
		StringBuffer n=new StringBuffer();
		String name;
		for(int i=0;i<l;++i)
		{
			c=sql.charAt(i);
			if(s==0)
			{
				if(c=='{')
				{
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						sb.append('{');
						++i;
					}
					else
					{
						s=1;
					}
				}
				else if(c=='}')
				{
					sb.append('}');
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						++i;
					}
				}
				else
				{
					sb.append(c);
				}
			}
			else if(s==1)
			{
				if(c=='}')
				{
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						n.append('}');
						++i;
					}
					else
					{
						name=n.toString();
						//System.out.println("name="+name);
						p.add(format_param(name,params));
						sb.append(" ? ");
						n.delete(0, n.length());
						s=0;
					}
				}
				else if(c=='{')
				{
					n.append('{');
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						++i;
					}
				}
				else
				{
					n.append(c);
				}
			}
		}
		prepareCall(sb.toString());
		for(int i=1;i<=p.size();++i)
		{
			Object x=p.get(i-1);
			Class t=x.getClass();
			if(int.class.equals(t) || Integer.class.equals(t))
			{
				cstmt.setInt(i, (Integer)x);
			}
			else if(String.class.equals(t))
			{
				cstmt.setString(i, (String)x);
			}
			else if(double.class.equals(t) || Double.class.equals(t))
			{
				cstmt.setDouble(i, (Double)x);
			}
			else if(java.util.Date.class.equals(t))
			{
				cstmt.setTimestamp(i, new Timestamp(((java.util.Date)x).getTime()));
			}
			else if(Timestamp.class.equals(t))
			{
				cstmt.setTimestamp(i, (Timestamp)x);
			}
			else if(float.class.equals(t) || Float.class.equals(t))
			{
				cstmt.setFloat(i, (Float)x);
			}
			else if(byte.class.equals(t) || Byte.class.equals(t))
			{
				cstmt.setByte(i, (Byte)x);
			}
			else
			{
				throw new SQLException("Can't apply format on "+t.getName());
			}
		}
		return cstmt;
	}
	
	public PreparedStatement formatDirect(String sql,Object... params) throws SQLException
	{
		StringBuffer sb=new StringBuffer();	//{0} {1.attr} {1.attr:H}
		char c;
		int s=0;	//0:out {}; 1:in {}
		int l=sql.length();
		String pstr;
		StringBuffer n=new StringBuffer();
		String name;
		for(int i=0;i<l;++i)
		{
			c=sql.charAt(i);
			if(s==0)
			{
				if(c=='{')
				{
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						sb.append('{');
						++i;
					}
					else
					{
						s=1;
					}
				}
				else if(c=='}')
				{
					sb.append('}');
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						++i;
					}
				}
				else
				{
					sb.append(c);
				}
			}
			else if(s==1)
			{
				if(c=='}')
				{
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						n.append('}');
						++i;
					}
					else
					{
						name=n.toString();
						//System.out.println("name="+name);
						{
							Object x=format_param(name,params);
							Class t=x.getClass();
							if(int.class.equals(t) || Integer.class.equals(t))
							{
								//pstmt.setInt(i, (Integer)x);
								pstr=Integer.toString((Integer)x);
							}
							else if(String.class.equals(t))
							{
								//pstmt.setString(i, (String)x);
								pstr="'"+((String)x).replaceAll("'", "''")+"'";
							}
							else if(double.class.equals(t) || Double.class.equals(t))
							{
								//pstmt.setDouble(i, (Double)x);
								pstr=Double.toString((Double)x);
							}
							else if(java.util.Date.class.equals(t))
							{
								//pstmt.setTimestamp(i, new Timestamp(((java.util.Date)x).getTime()));
								//this is for oracle
								pstr="to_date('"+new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format((java.util.Date)x)+"','YYYY/MM/DD HH24:MI:SS')";
							}
							else if(Timestamp.class.equals(t))
							{
								pstr="to_date('"+new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format((Timestamp)x)+"','YYYY/MM/DD HH24:MI:SS')";
							}
							else if(float.class.equals(t) || Float.class.equals(t))
							{
								//pstmt.setFloat(i, (Float)x);
								pstr=Float.toString((Float)x);
							}
							else if(byte.class.equals(t) || Byte.class.equals(t))
							{
								//pstmt.setByte(i, (Byte)x);
								pstr=Byte.toString((Byte)x);
							}
							else
							{
								throw new SQLException("Can't apply format on "+t.getName());
							}
						}
						sb.append(" "+pstr+" ");
						n.delete(0, n.length());
						s=0;
					}
				}
				else if(c=='{')
				{
					n.append('{');
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						++i;
					}
				}
				else
				{
					n.append(c);
				}
			}
		}
		prepare(sb.toString());
		return pstmt;
	}

	public PreparedStatement format(String sql,Object... params) throws SQLException
	{
		StringBuffer sb=new StringBuffer();	//{0} {1.attr} {1.attr:H}
		char c;
		int s=0;	//0:out {}; 1:in {}
		int l=sql.length();
		ArrayList p=new ArrayList();
		StringBuffer n=new StringBuffer();
		String name;
		for(int i=0;i<l;++i)
		{
			c=sql.charAt(i);
			if(s==0)
			{
				if(c=='{')
				{
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						sb.append('{');
						++i;
					}
					else
					{
						s=1;
					}
				}
				else if(c=='}')
				{
					sb.append('}');
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						++i;
					}
				}
				else
				{
					sb.append(c);
				}
			}
			else if(s==1)
			{
				if(c=='}')
				{
					if(i<l-1 && sql.charAt(i+1)=='}')
					{
						//skip
						n.append('}');
						++i;
					}
					else
					{
						name=n.toString();
						//System.out.println("name="+name);
						p.add(format_param(name,params));
						sb.append(" ? ");
						n.delete(0, n.length());
						s=0;
					}
				}
				else if(c=='{')
				{
					n.append('{');
					if(i<l-1 && sql.charAt(i+1)=='{')
					{
						//skip
						++i;
					}
				}
				else
				{
					n.append(c);
				}
			}
		}
		prepare(sb.toString());
		for(int i=1;i<=p.size();++i)
		{
			Object x=p.get(i-1);
			if(x==null)
			{
				//assume the field type is string
				pstmt.setString(i, null);
				continue;
			}
			Class t=x.getClass();
			if(int.class.equals(t) || Integer.class.equals(t))
			{
				pstmt.setInt(i, (Integer)x);
			}
			else if(String.class.equals(t))
			{
				pstmt.setString(i, (String)x);
			}
			else if(double.class.equals(t) || Double.class.equals(t))
			{
				pstmt.setDouble(i, (Double)x);
			}
			else if(java.util.Date.class.equals(t))
			{
				pstmt.setTimestamp(i, new Timestamp(((java.util.Date)x).getTime()));
			}
			else if(Timestamp.class.equals(t))
			{
				pstmt.setTimestamp(i, (Timestamp)x);
			}
			else if(long.class.equals(t) || Long.class.equals(t))
			{
				pstmt.setLong(i, (Long)x);
			}
			else if(float.class.equals(t) || Float.class.equals(t))
			{
				pstmt.setFloat(i, (Float)x);
			}
			else if(byte.class.equals(t) || Byte.class.equals(t))
			{
				pstmt.setByte(i, (Byte)x);
			}
			else
			{
				throw new SQLException("Can't apply format on "+t.getName());
			}
		}
		return pstmt;
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
		closeStmt(cstmt);
		if(conn==null && ds!=null)
		{
			conn=ds.getConnection();
		}
		cstmt = conn.prepareCall(sql);
		return cstmt;
	}
	
	// execute prepared statement
	public PreparedStatement prepare(String sql) throws SQLException
	{
		closeStmt(pstmt);
		if(conn==null && ds!=null)
		{
			conn=ds.getConnection();
		}
		pstmt = conn.prepareStatement(sql);
		return pstmt;
	}

	public boolean execute() throws SQLException
	{
		return pstmt.execute();
	}

	public int executeUpdate() throws SQLException
	{
		ret = pstmt.executeUpdate();
		return ret;
	}

	public String executeValue() throws SQLException
	{
		closeRS(rsv);
		rsv = pstmt.executeQuery();
		if (rsv.next())
		{
			rets = rsv.getString(1);
		}
		else
		{
			rets = null;
		}
		rsv.close();
		return rets;
	}
	
	public int executeInt() throws SQLException
	{
		String r=executeValue();
		try
		{
			return Integer.valueOf(r);
		}
		catch(Exception ex)
		{
			
		}
		return -1;
	}
	
	public ResultSet executeRow() throws SQLException
	{
		closeRS(rsv);
		rsv = pstmt.executeQuery();
		return rsv;
	}

	public ResultSet executeQuery() throws SQLException
	{
		closeRS(rs);
		rs = pstmt.executeQuery();
		return rs;
	}

	public Object executeObject(Class clazz) throws SQLException
	{
		closeRS(rsv);
		rsv = pstmt.executeQuery();
		if (rsv.next())
		{
			reto = SqlSource.buildObject(rsv, clazz);
		}
		else
		{
			reto = null;
		}
		rsv.close();
		return reto;
	}

	// execute statement
	public Statement create() throws SQLException
	{
		closeStmt(stmt);
		if(conn==null && ds!=null)
		{
			conn=ds.getConnection();
		}
		stmt = conn.createStatement();
		return stmt;
	}

	public boolean execute(String sql) throws SQLException
	{
		//if(stmt==null)
			create();
		return stmt.execute(sql);
	}

	public int executeUpdate(String sql) throws SQLException
	{
		//if(stmt==null)
			create();
		ret = stmt.executeUpdate(sql);
		return ret;
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		closeRS(rs);
		//if(stmt==null)
			create();
		rs = stmt.executeQuery(sql);
		return rs;
	}

	public String executeValue(String sql) throws SQLException
	{
		closeRS(rsv);
		//if(stmt==null)
			create();
		rsv = stmt.executeQuery(sql);
		if (rsv.next())
		{
			rets = rsv.getString(1);
		}
		else
		{
			rets = null;
		}
		rsv.close();
		return rets;
	}
	
	public int executeInt(String sql) throws SQLException
	{
		String r=executeValue(sql);
		try
		{
			return Integer.valueOf(r);
		}
		catch(Exception ex)
		{
			
		}
		return -1;
	}

	public ResultSet executeRow(String sql) throws SQLException
	{
		closeRS(rsv);
		//if(stmt==null)
			create();
		rsv = stmt.executeQuery(sql);
		return rsv;
	}
	
	public Object executeObject(String sql, Class clazz) throws SQLException
	{
		closeRS(rsv);
		//if(stmt==null)
			create();
		rsv = stmt.executeQuery(sql);
		if (rsv.next())
		{
			reto = SqlSource.buildObject(rsv, clazz);
		}
		else
		{
			reto = null;
		}
		rsv.close();
		return reto;
	}

	protected void closeStmt(Statement stmt)
	{
		if (stmt != null)
		{
			try
			{
				stmt.close();
				stmt=null;
			}
			catch (Exception ex)
			{

			}
		}
	}

	protected void closeRS(ResultSet rs)
	{
		if (rs != null)
		{
			try
			{
				rs.close();
				rs=null;
			}
			catch (Exception ex)
			{

			}
		}
	}
	
	protected void closeConn(Connection conn)
	{
		if(conn!=null)
		{
			try
			{
				conn.close();
				conn=null;
			}
			catch(Exception ex)
			{
				
			}
		}
	}

	public void close()
	{
		closeRS(rsv);
		closeRS(rs);
		closeStmt(stmt);
		closeStmt(pstmt);
		closeStmt(cstmt);
		closeConn(conn);
	}

	public void commit()
	{
		try
		{
			if(!conn.getAutoCommit())
				conn.commit();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public void rollback()
	{
		try
		{
			conn.rollback();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

}

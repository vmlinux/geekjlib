package vmlinux.data.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import vmlinux.data.entity.OData;
import vmlinux.data.entity.OMap;
import vmlinux.reflect.XObject;
import vmlinux.reflect.XObjectSourceBase;
import vmlinux.tool.DataTransfer;
import vmlinux.util.DbExecuteEx;

public class OTable implements XObject
{
	public String _name;
	public String _updateby;
	public String _type;
	public String _method;
	public OMap[] map;
	
	public String str;
	
	public boolean truncate(DbExecuteEx exec) throws Exception
	{
		int ret=exec.executeUpdate("truncate table "+getTableName());
		return ret>0;
	}
	
	public boolean deleteAll(DbExecuteEx exec) throws Exception
	{
		int ret=exec.executeUpdate("delete from "+getTableName());
		exec.commit();
		return ret>0;
	}
	
	public String getTableName()
	{
		return _name;
	}
	
	public void prepare(OData conf)
	{
		_name=conf.getQueryFrom(_name);
	}
	
	public String getDataSample(ResultSet rs)
	{
		StringBuffer sb=new StringBuffer();
		try
		{
			for(int i=0;i<map.length;++i)
			{
				String s=rs.getString(map[i]._from);
				if(i>0)
				{
					sb.append(", ");
				}
				sb.append("\""+s+"\"");
			}
		}
		catch(Exception ex)
		{
			
		}
		return sb.toString();
	}

	public String getDataSample(Hashtable<String,Object> rs)
	{
		if(rs==null)
			return null;
		StringBuffer sb=new StringBuffer();
		try
		{
			for(int i=0;i<map.length;++i)
			{
				Object v=rs.get(map[i]._from);
				String s=String.class.equals(v.getClass())?(String)v:v.toString();
				if(i>0)
				{
					sb.append(", ");
				}
				sb.append("\""+s+"\"");
			}
		}
		catch(Exception ex)
		{
			
		}
		return sb.toString();
	}

	public boolean insertRow(Hashtable<String,Object> rs,DbExecuteEx exec,Hashtable<String,Class> colmap) throws Exception
	{
		PreparedStatement stmt=exec.prepare(getInsertString(rs,colmap));
		for(int i=0;i<map.length;++i)
		{
			Class clazz=colmap.get(map[i]._to);
			Object val=rs.get(map[i]._from);
			if(val==null)
			{
				stmt.setNull(i+1, map[i].type);
				continue;
			}
			Class vclazz=val.getClass();
			if(Timestamp.class.equals(clazz))
			{
				if(Timestamp.class.equals(vclazz))
				{
					stmt.setTimestamp(i+1, (Timestamp)val);
				}
				else if(java.util.Date.class.equals(vclazz))
				{
					stmt.setTimestamp(i+1, new Timestamp(((java.util.Date)val).getTime()));
				}
				else if(String.class.equals(vclazz))
				{
					String v=((String)val).trim().replaceAll("/", "-");
					if(v.length()==0)
					{
						stmt.setNull(i+1, Types.TIMESTAMP);
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+\\s+\\d+:\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format1.parse(v).getTime()));
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+\\s+\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format1.parse(v+":0").getTime()));
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format2.parse(v).getTime()));
					}
					else if(v.matches("\\d+:\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format3.parse(v).getTime()));
					}
					else
					{
						throw new RuntimeException("unknown date format: "+v);
					}
				}
				else
				{
					String v=val.toString().trim().replaceAll("/", "-");
					if(v.length()==0)
					{
						stmt.setNull(i+1, Types.TIMESTAMP);
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+\\s+\\d+:\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format1.parse(v).getTime()));
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+\\s+\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format1.parse(v+":0").getTime()));
					}
					else if(v.matches("\\d+\\-\\d+\\-\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format2.parse(v).getTime()));
					}
					else if(v.matches("\\d+:\\d+:\\d+"))
					{
						stmt.setTimestamp(i+1, new Timestamp(XObjectSourceBase.format3.parse(v).getTime()));
					}
					else
					{
						throw new RuntimeException("unknown date format: "+v);
					}
				}
			}
			else if(Time.class.equals(clazz))
			{
				if(Time.class.equals(vclazz))
				{
					stmt.setTime(i+1, (Time)val);
				}
				else if(java.util.Date.class.equals(vclazz))
				{
					stmt.setTime(i+1, new Time(((java.util.Date)val).getTime()));
				}
				else if(String.class.equals(vclazz))
				{
					stmt.setTime(i+1, new Time(XObjectSourceBase.format1.parse((String)val).getTime()));
				}
				else
				{
					stmt.setTime(i+1, new Time(XObjectSourceBase.format1.parse(val.toString()).getTime()));
				}
			}
			else if(java.sql.Date.class.equals(clazz))
			{
				if(java.sql.Date.class.equals(vclazz))
				{
					stmt.setDate(i+1, (java.sql.Date)val);
				}
				else if(java.util.Date.class.equals(vclazz))
				{
					stmt.setDate(i+1, new java.sql.Date(((java.util.Date)val).getTime()));
				}
				else if(String.class.equals(vclazz))
				{
					stmt.setDate(i+1, new java.sql.Date(XObjectSourceBase.format1.parse((String)val).getTime()));
				}
				else
				{
					stmt.setDate(i+1, new java.sql.Date(XObjectSourceBase.format1.parse(val.toString()).getTime()));
				}
			}
			else if(Integer.class.equals(clazz))
			{
				stmt.setInt(i+1, Integer.parseInt(val.toString()));
			}
			else if(String.class.equals(clazz))
			{
				if(String.class.equals(vclazz))
				{
					stmt.setString(i+1, ((String)val).trim());
				}
				else
				{
					stmt.setString(i+1, val.toString());
				}
			}
			else
			{
				Object o=val;
				if(o!=null)
				{
					stmt.setObject(i+1, o);
				}
				else
				{
					stmt.setNull(i+1, map[i].type);
				}
			}
		}
		int ret=stmt.executeUpdate();
		return ret>0;
	}

	public boolean insertRow(ResultSet rs,DbExecuteEx exec,Hashtable<String,Class> colmap) throws Exception
	{
		PreparedStatement stmt=exec.prepare(getInsertString(rs,colmap));
		for(int i=0;i<map.length;++i)
		{
			Class clazz=colmap.get(map[i]._to);
			if(Timestamp.class.equals(clazz))
			{
				Timestamp ts=null;
				try
				{
					ts=rs.getTimestamp(map[i]._from);
				}
				catch(Exception ex)
				{
					String val=rs.getString(map[i]._from);
					if(val!=null)
					{
						if(val.length()==8)	//nbmart idap
						{
							ts=new Timestamp(new SimpleDateFormat("yyyyMMdd").parse(val).getTime());
						}
					}
				}
				stmt.setTimestamp(i+1, ts);
			}
			else if(Time.class.equals(clazz))
			{
				stmt.setTime(i+1, rs.getTime(map[i]._from));
			}
			else if(java.sql.Date.class.equals(clazz))
			{
				stmt.setDate(i+1, rs.getDate(map[i]._from));
			}
			else if(Integer.class.equals(clazz))
			{
				stmt.setInt(i+1, rs.getInt(map[i]._from));
			}
			else if(String.class.equals(clazz))
			{
				String s=rs.getString(map[i]._from);
				if(s!=null)
				{
					stmt.setString(i+1, s.trim());
				}
				else
				{
					stmt.setNull(i+1, map[i].type);
				}
			}
			else
			{
				Object o=rs.getObject(map[i]._from);
				if(o!=null)
				{
					Class oclazz=o.getClass();
					if(oracle.sql.TIMESTAMP.class.equals(oclazz))
					{
						o=rs.getTimestamp(map[i]._from);
					}
					else if(oracle.sql.DATE.class.equals(oclazz))
					{
						o=rs.getDate(map[i]._from);
					}
					stmt.setObject(i+1, o);
				}
				else
				{
					stmt.setNull(i+1, map[i].type);
				}
			}
		}
		int ret=stmt.executeUpdate();
		return ret>0;
	}

	public String getInsertString(Hashtable<String,Object> rs,Hashtable<String,Class> colmap) throws Exception
	{
		//if(str==null)
		{
			StringBuffer sb=new StringBuffer("insert into "+getTableName()+" (");
			StringBuffer sb2=new StringBuffer(" values (");
			//if(map==null || map.length==0)
			{
				//map=new OMap[rs.size()];
				ArrayList<OMap> arr=new ArrayList<OMap>();
				Iterator<Map.Entry<String,Object>> i=rs.entrySet().iterator();
				int c=0;
				while(i.hasNext())
				{
					Map.Entry<String, Object> p=i.next();
					String cn=p.getKey().toLowerCase();
					if(!colmap.containsKey(cn))
					{
						continue;
					}
					Object v=p.getValue();
					Class vclazz=v.getClass();
					OMap m=new OMap(cn,cn);
					if(c>0)
					{
						sb.append(", ");
						sb2.append(", ");
					}
					sb.append(m._to);
					if(String.class.equals(vclazz))
					{
						m.type=Types.VARCHAR;
					}
					else if(java.util.Date.class.equals(vclazz))
					{
						m.type=Types.DATE;
					}
					else if(Integer.class.equals(vclazz))
					{
						m.type=Types.INTEGER;
					}
					else if(Long.class.equals(vclazz))
					{
						m.type=Types.BIGINT;
					}
					else if(Double.class.equals(vclazz))
					{
						m.type=Types.DOUBLE;
					}
					else
					{
						m.type=Types.NULL;
					}
					sb2.append("?");
					arr.add(m);
					++c;
				}
				map=arr.toArray(new OMap[0]);
			}
			/*
			else
			{
				for(int i=0;i<map.length;++i)
				{
					if(i>0)
					{
						sb.append(", ");
						sb2.append(", ");
					}
					sb.append(map[i]._to);
					sb2.append("?");
				}
			}
			*/
			sb.append(")");
			sb.append(sb2);
			sb.append(")");
			str=sb.toString();
		}
		return str;
	}

	public String getInsertString(ResultSet rs,Hashtable<String,Class> colmap) throws Exception
	{
		//if(str==null)
		{
			StringBuffer sb=new StringBuffer("insert into "+getTableName()+" (");
			StringBuffer sb2=new StringBuffer(" values (");
			//if(map==null || map.length==0)
			{
				ResultSetMetaData mt=rs.getMetaData();
				//map=new OMap[mt.getColumnCount()];
				ArrayList<OMap> arr=new ArrayList<OMap>();
				int n=0;
				for(int i=0;i<mt.getColumnCount();++i)
				{
					String cn=mt.getColumnName(i+1).toLowerCase();
					if(!colmap.containsKey(cn))
					{
						continue;
					}
					OMap m=new OMap(cn,cn);
					if(n>0)
					{
						sb.append(", ");
						sb2.append(", ");
					}
					sb.append(m._to);
					m.type=mt.getColumnType(i+1);
					sb2.append("?");
					arr.add(m);
					n++;
				}
				map=arr.toArray(new OMap[0]);
			}
			/*
			else
			{
				for(int i=0;i<map.length;++i)
				{
					if(i>0)
					{
						sb.append(", ");
						sb2.append(", ");
					}
					sb.append(map[i]._to);
					sb2.append("?");
				}
			}
			*/
			sb.append(")");
			sb.append(sb2);
			sb.append(")");
			str=sb.toString();
		}
		return str;
	}
}

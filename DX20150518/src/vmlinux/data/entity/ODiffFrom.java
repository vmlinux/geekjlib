package vmlinux.data.entity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;

import vmlinux.data.entity.OSource;
import vmlinux.reflect.XObject;

public class ODiffFrom implements XObject
{
	public OSource source;
	public String query;
	public String _keyfield;
	public String _autotrim;
	public String excludelist;
	public String includelist;
	public String _debug;
	public String _addnew;
	
	private boolean btrim;
	private Hashtable<String, Integer> fldlist;
	
	public void prepare(ResultSet rs) throws SQLException
	{
		btrim="true".equalsIgnoreCase(_autotrim);
		fldlist=new Hashtable<String, Integer>();
		ResultSetMetaData rsmeta=rs.getMetaData();
		if(includelist!=null && includelist.trim().length()>0)
		{
			includelist=includelist.toLowerCase().trim();
			for(int i=1;i<=rsmeta.getColumnCount();++i)
			{
				String f=rsmeta.getColumnName(i).toLowerCase();
				if(includelist.indexOf("/"+f+"/")>=0)
				{
					fldlist.put(f, i);
				}
			}
		}
		else if(excludelist!=null && excludelist.trim().length()>0)
		{
			excludelist=excludelist.toLowerCase().trim();
			for(int i=1;i<=rsmeta.getColumnCount();++i)
			{
				String f=rsmeta.getColumnName(i).toLowerCase();
				if(excludelist.indexOf("/"+f+"/")>=0)
				{
					continue;
				}
				else
				{
					fldlist.put(f, i);
				}
			}
		}
	}
	
	public void prepare(Hashtable<String,Object> rs) throws SQLException
	{
		btrim="true".equalsIgnoreCase(_autotrim);
		fldlist=new Hashtable<String, Integer>();
		if(includelist!=null && includelist.trim().length()>0)
		{
			includelist=includelist.toLowerCase().trim();
			Iterator<String> i=rs.keySet().iterator();
			while(i.hasNext())
			{
				String f=i.next().toLowerCase();
				if(includelist.indexOf("/"+f+"/")>=0)
				{
					fldlist.put(f, new Integer(1));
				}
			}
		}
		else if(excludelist!=null && excludelist.trim().length()>0)
		{
			excludelist=excludelist.toLowerCase().trim();
			Iterator<String> i=rs.keySet().iterator();
			while(i.hasNext())
			{
				String f=i.next().toLowerCase();
				if(excludelist.indexOf("/"+f+"/")>=0)
				{
					continue;
				}
				else
				{
					fldlist.put(f, new Integer(1));
				}
			}
		}
	}

	public boolean isDiff(ResultSet rs1,ResultSet rs2) throws SQLException
	{
		Iterator<Integer> it=fldlist.values().iterator();
		while(it.hasNext())
		{
			Integer i=it.next();
			String v1=rs1.getString(i);
			String v2=rs2.getString(i);
			v1=(v1==null)?"":v1;
			v2=(v2==null)?"":v2;
			if(!v1.equals(v2))
			{
				if("true".equalsIgnoreCase(_debug))
				{
					System.out.println("diff:"+i+"("+v1+"/"+v2+")");
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isDiff(Hashtable<String,Object> rs1,ResultSet rs2) throws SQLException
	{
		Iterator<String> it=fldlist.keySet().iterator();
		while(it.hasNext())
		{
			String k=it.next();
			Object v=rs1.get(k);
			String v1=String.class.equals(v.getClass())?(String)v:v.toString();
			String v2=rs2.getString(k);
			v1=(v1==null)?"":v1;
			v2=(v2==null)?"":v2;
			if(!v1.equals(v2))
			{
				if("true".equalsIgnoreCase(_debug))
				{
					System.out.println("diff:"+k+"("+v1+"/"+v2+")");
				}
				return true;
			}
		}
		return false;
	}
}

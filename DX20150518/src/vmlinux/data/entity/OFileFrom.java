package vmlinux.data.entity;

import java.util.Map;

import vmlinux.data.entity.FieldMap;
import vmlinux.reflect.XObject;

public class OFileFrom implements XObject
{
	public String _file;
	public String _sheet;	//excel sheet name
	public String _format;	//csv file format
	public String _ignorenull;
	public String _trimdata;
	public String _array;	//name of json array
	public FieldMap[] col;
	public String _autosqlinit;
	public String _defaultsqltype="varchar2(500)";
	
	public void prepare()
	{
		/*
		if(_file!=null && _file.startsWith("$"))
		{
			_file=prop.getProperty(_file);
		}
		*/
	}
	
	public String generateSQLDML(String tab)
	{
		StringBuffer sb=new StringBuffer("create table "+tab+"(");
		for(int i=0;i<col.length;++i)
		{
			if(i>0)
			{
				sb.append(",");
			}
			String colname=col[i]._name;
			//colname=colname.replaceAll("[\\(（][^\\)）]*[\\)）]", "");
			//colname=colname.replaceAll("[\\.\\+\\-\\*\\|\\(\\)\\?/,=;'\"]", "");
			sb.append("\""+colname+"\"");
			sb.append(" ");
			sb.append(col[i]._sqltype==null?_defaultsqltype:col[i]._sqltype);
		}
		sb.append(")");
		//System.out.println(sb.toString());
		return sb.toString();
	}
	
	public String transformData(Map m,String n)
	{
		return (String)m.get(n);
	}
}

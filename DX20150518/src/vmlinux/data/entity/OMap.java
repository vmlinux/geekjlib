package vmlinux.data.entity;

import vmlinux.reflect.XObject;

public class OMap implements XObject
{
	public String _from;
	public String _to;
	public int type;
	
	public OMap()
	{
		
	}
	
	public OMap(String f,String t)
	{
		this._from=f;
		this._to=t;
	}
}

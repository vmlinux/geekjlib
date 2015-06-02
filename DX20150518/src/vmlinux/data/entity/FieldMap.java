package vmlinux.data.entity;

import vmlinux.reflect.XObject;

public class FieldMap implements XObject
{
	public String _name;
	public String _type="string";	//string,date,regex,number,integer
	public String _format;
	public String _transform;
	public String _sqltype;
	public String _;
}

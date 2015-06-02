package vmlinux.data.entity;

import vmlinux.reflect.XObject;

public class OMailFrom implements XObject
{
	public String _smtp_host;
	public String _smtp_auth;
	public String _pop3_host;
	public String _ssl;
	public String mail_user;
	public String mail_pass;
	public int mail_days;
	public String attach_path;
	public String excludelist;
	public String matchsubject;
	
	public void prepare()
	{

	}
}

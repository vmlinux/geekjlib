package vmlinux.codec;

import java.util.UUID;

public class GUID
{
	public static String randomGUID()
	{
		java.util.UUID u=UUID.randomUUID();
		return u.toString().replaceAll("-", "").toUpperCase();
	}
	
	public static String randomUUID()
	{
		java.util.UUID u=UUID.randomUUID();
		return u.toString();
	}
}

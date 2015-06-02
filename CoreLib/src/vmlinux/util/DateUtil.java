package vmlinux.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtil
{
	public static final SimpleDateFormat f1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Date parseDate(String str)
	{
		try
		{
			Date dt=f1.parse(str);
			return dt;
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}

package vmlinux.codec;

import vmlinux.util.StringUtil;

public class Parameter
{
	public static String encode(String str)
	{
		return StringUtil.replacePlain(StringUtil.replacePlain(str, "%", "%37"),"&","%38");
	}
	public static String decode(String str)
	{
		return StringUtil.replacePlain(StringUtil.replacePlain(str, "%38", "&"),"%37","%");
	}
}

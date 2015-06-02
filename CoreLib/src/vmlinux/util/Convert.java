package vmlinux.util;

import java.util.List;

public class Convert
{

	public static int[] toIntArray(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		int len=list.size();
		int[] result=new int[len];
		for(int i=0;i<len;++i)
		{
			result[i]=toSafeInt((String)list.get(i));
		}
		return result;
	}

	public static long[] toLongArray(String data)
	{
		List list=StringUtil.splitPlain(data, ";");
		int len=list.size();
		long[] result=new long[len];
		for(int i=0;i<len;++i)
		{
			result[i]=toSafeLong((String)list.get(i));
		}
		return result;
	}
	public static int toSafeInt(String s)
	{
		if(s!=null)
		{
			try
			{
				return Integer.parseInt(s.trim());
			}
			catch(Exception ex)
			{
				
			}
		}
		return 0;
	}
	public static long toSafeLong(String s)
	{
		if(s!=null)
		{
			try
			{
				return Long.parseLong(s.trim());
			}
			catch(Exception ex)
			{
				
			}
		}
		return 0L;
	}
	
	public static final char[] chars16=
		new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	public static String toByteHexString(byte[] bytes)
	{
		StringBuffer sb=new StringBuffer(bytes.length<<1);
		byte b;
		for(int i=0;i<bytes.length;++i)
		{
			b=bytes[i];
			sb.append(chars16[b>>4 & 0xf]);
			sb.append(chars16[b & 0xf]);
		}
		return sb.toString();
	}
	
	public static String toLiteralString(long num)
	{
		return toLiteralString(""+num);
	}
	public static final char[] literalDigit=new char[]{'零','壹','贰','叁','肆','伍','陆','柒','捌','玖'};
	public static final String[] literalStep=new String[]{"","拾","佰","仟"};
	public static final String[] literalGrade=new String[]{"","萬","亿","萬亿","萬萬亿","萬萬萬亿","萬萬萬萬亿"};
	public static final String literalSeperate=" ";
	public static final String literalDot="点";
	public static String toLiteralString(String num)
	{
		StringBuffer sb=new StringBuffer();
		String dot="";
		int p=num.indexOf(".");
		if(p>=0)
		{
			dot=num.substring(p+1);
			num=num.substring(0,p);
		}
		if("0".equals(num))
			return sb.append(literalDigit[0]).toString();
		int len=literalStep.length;
		int nl=num.length();
		boolean afterZero=false;
		int gzCount=0;
		for(int i=0;i<nl;++i)
		{
			int n=num.charAt(i)-'0';
			int g=(nl-i-1)/len;
			int s=(nl-i-1)%len;
			if(n==0)
			{
				gzCount++;
				if(s==0)
				{
					if(afterZero)
					{
						afterZero=false;
						sb.deleteCharAt(sb.length()-1);
					}
					if(gzCount<len)
					{
						gzCount=0;
						sb.append(literalGrade[g]);
						sb.append(literalSeperate);
					}
				}
				else if(!afterZero)
				{
					afterZero=true;
					sb.append(literalDigit[0]);
				}
			}
			else
			{
				if(afterZero)
					afterZero=false;
				sb.append(literalDigit[n]+literalStep[s]);
				if(s==0)
				{
					gzCount=0;
					sb.append(literalGrade[g]);
					sb.append(literalSeperate);
				}
			}
		}
		if(dot.length()>0)
		{
			sb.append(literalDot);
			for(int i=0;i<dot.length();++i)
			{
				int n=dot.charAt(i)-'0';
				sb.append(literalDigit[n]);
			}
		}
		return sb.toString();
	}
}

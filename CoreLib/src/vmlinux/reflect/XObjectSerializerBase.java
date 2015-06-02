package vmlinux.reflect;

import java.text.*;
import java.util.Date;

import vmlinux.codec.Base64;
import vmlinux.util.StringUtil;

@SuppressWarnings({"deprecation","unchecked"})
public abstract class XObjectSerializerBase implements XObjectSerializer
{
	static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	protected String formatInt32Inner(int data)
	{
		return Integer.toString(data);
	}
	protected String formatInt32Inner(Integer data)
	{
		return data.toString();
	}
	
	protected String formatLongInner(long data)
	{
		return Long.toString(data);
	}
	protected String formatLongInner(Long data)
	{
		return data.toString();
	}
	
	protected String formatStringInner(String data)
	{
		return data;
	}
	
	protected String formatDateInner(Date data)
	{
		if(data!=null)
		{
			return sdf.format(data);
		}
		else
		{
			return null;
		}
	}
	
	protected String formatDoubleInner(double data)
	{
		return Double.toString(data);
	}
	protected String formatDoubleInner(Double data)
	{
		return data.toString();
	}
	
	protected String formatFloatInner(float data)
	{
		return Float.toString(data);
	}
	protected String formatFloatInner(Float data)
	{
		return data.toString();
	}
	
	protected String formatBooleanInner(boolean data)
	{
		return Boolean.toString(data);
	}
	protected String formatBooleanInner(Boolean data)
	{
		return data.toString();
	}
	
	protected String formatByteArrayInner(byte[] data)
	{
		return Base64.encode(data);
	}
	
	protected String formatByteInner(byte data)
	{
		return Byte.toString(data);
	}
	protected String formatByteInner(Byte data)
	{
		return data.toString();
	}
	
	protected String formatInt32ArrayInner(int[] data)
	{
		StringBuffer sb=new StringBuffer(data.length<<1);
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(formatInt32Inner(data[i]));
		}
		return sb.toString();
	}
	
	protected String formatBooleanArrayInner(boolean[] data)
	{
		StringBuffer sb=new StringBuffer(data.length<<1);
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(formatBooleanInner(data[i]));
		}
		return sb.toString();
	}
	
	protected String formatLongArrayInner(long[] data)
	{
		StringBuffer sb=new StringBuffer(data.length<<1);
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(formatLongInner(data[i]));
		}
		return sb.toString();
	}
	
	protected String formatFloatArrayInner(float[] data)
	{
		StringBuffer sb=new StringBuffer(data.length<<1);
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(formatFloatInner(data[i]));
		}
		return sb.toString();
	}
	
	protected String formatDoubleArrayInner(double[] data)
	{
		StringBuffer sb=new StringBuffer(data.length<<1);
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
				sb.append(",");
			sb.append(formatDoubleInner(data[i]));
		}
		return sb.toString();
	}
	
	protected String formatStringArrayInner(String[] data)
	{
		return StringUtil.concatQuote(data, ",");
	}
	protected String formatDateArrayInner(Date[] data)
	{
		//TODO: implement date array output
		return null;
	}
}

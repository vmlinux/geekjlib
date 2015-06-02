package vmlinux.codec;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import vmlinux.util.Convert;

public class MD5
{
	public static String digest(InputStream s)
	{
		try
		{
			byte[] buff=new byte[s.available()];
			s.read(buff);
			return digest(buff);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String digest(byte[] bytes)
	{
		try
		{
			MessageDigest md=MessageDigest.getInstance("MD5");
			byte[] md5=md.digest(bytes);
			return Convert.toByteHexString(md5);
		}
		catch(NoSuchAlgorithmException ex)
		{
			System.err.println("no md5???");
		}
		return null;
	}

	public static String digestBase64(InputStream s)
	{
		try
		{
			byte[] buff=new byte[s.available()];
			s.read(buff);
			return digest(buff);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public static String digestBase64(byte[] bytes)
	{
		try
		{
			MessageDigest md=MessageDigest.getInstance("MD5");
			byte[] md5=md.digest(bytes);
			return Base64.encode(md5);
		}
		catch(NoSuchAlgorithmException ex)
		{
			System.err.println("no md5???");
		}
		return null;
	}
}

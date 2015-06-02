package vmlinux.codec;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import vmlinux.util.Convert;

public class SHA1
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
			MessageDigest md=MessageDigest.getInstance("SHA1");
			byte[] md5=md.digest(bytes);
			return Convert.toByteHexString(md5);
		}
		catch(NoSuchAlgorithmException ex)
		{
			System.err.println("no sha1???");
		}
		return null;
	}

	public static String digestBase64(InputStream s)
	{
		try
		{
			byte[] buff=new byte[s.available()];
			s.read(buff);
			return digestBase64(buff);
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
			MessageDigest md=MessageDigest.getInstance("SHA1");
			byte[] md5=md.digest(bytes);
			return Base64.encode(md5);
		}
		catch(NoSuchAlgorithmException ex)
		{
			System.err.println("no sha1???");
		}
		return null;
	}
}

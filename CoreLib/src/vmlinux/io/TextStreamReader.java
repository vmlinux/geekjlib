package vmlinux.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class TextStreamReader extends BufferedReader
{
	public TextStreamReader(String file) throws FileNotFoundException
	{
		this(new FileInputStream(file));
	}
	public TextStreamReader(String file,String encoding) throws FileNotFoundException, UnsupportedEncodingException
	{
		this(new FileInputStream(file),encoding);
	}
	public TextStreamReader(InputStream stream,String encoding) throws UnsupportedEncodingException
	{
		super(new InputStreamReader(stream,encoding));
	}
	public TextStreamReader(InputStream stream)
	{
		super(new InputStreamReader(stream));
	}
}

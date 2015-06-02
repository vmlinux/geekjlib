package vmlinux.reflect;

import java.util.*;

public interface XObjectSource
{
	public void setBuilder(XObjectBuilder xob);
	public void setContainer(Object obj);
	public Object containerObject();
	public String getString(String name);
	public boolean getBoolean(String name);
	public int getInt32(String name);
	public long getLong(String name);
	public float getFloat(String name);
	public double getDouble(String name);
	public Date getDate(String name);
	public byte getByte(String name);
	public byte[] getByteArray(String name);
	public Object getObject(String name,Class xclass) throws IllegalAccessException, InstantiationException;
	public Object get(String name);
	public Object getArray(String name,Class aclass);
	
	public XDynamic getDynamic();
}

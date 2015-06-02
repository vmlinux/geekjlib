package vmlinux.reflect;

public interface XObjectSerializer
{
	public Object serialize(String name,Object o) throws IllegalAccessException;
}

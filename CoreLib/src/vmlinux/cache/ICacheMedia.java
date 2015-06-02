package vmlinux.cache;

public interface ICacheMedia
{
	void setSubLevelMedia(ICacheMedia media);
	void setMediaSize(int size);//object or bytes?
	void put(String name,Object o);
	Object get(String name);
}

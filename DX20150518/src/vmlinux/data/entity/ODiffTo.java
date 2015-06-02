package vmlinux.data.entity;

import vmlinux.data.entity.OSource;
import vmlinux.data.entity.OTable;
import vmlinux.reflect.XObject;

public class ODiffTo implements XObject
{
	public OSource source;
	public OTable table;
	public String _preaction;
}

package vmlinux.util;

public class ExcelWriter
{
	private ExcelSheet sheet;
	
	public ExcelWriter(String file)
	{
		ExcelSheet sh=new ExcelSheet(file, "gb2312");
		this.sheet=sh;
	}
	
	public ExcelWriter(ExcelSheet sheet)
	{
		this.sheet=sheet;
	}
}

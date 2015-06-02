package vmlinux.data.reporter;

public interface IStatusReporter
{
	public boolean setStarting();
	public void setRunningMessage(String str);
	public void setErrorMessage(String str);
	public void setBreakMessage(String str);
	public void finish();
}

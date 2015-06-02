package vmlinux.data.entity;

import vmlinux.data.entity.*;
import vmlinux.reflect.XObject;

public class OData implements XObject
{
	public String _commit;
	public String _preaction;
	public String _noskip;
	public String _skipexception;
	public String _check;
	public String info;
	public OFrom from;
	public OFrom check;
	public OTo to;
	public ODiffFrom difffrom;
	public ODiffTo diffto;
	public OCMD command;
	public OMatchFrom matchfrom;
	public OMailFrom mailfrom;
	public OFileFrom filefrom;
	
	private int ncommit;
	private OAction parent;
	
	public void prepare(OAction p) throws Exception
	{
		parent=p;
		if(_commit==null)
		{
			_commit=p._commit;
		}
		if("auto".equalsIgnoreCase(_commit))
		{
			ncommit=1;
		}
		else
		{
			try
			{
				ncommit=Integer.valueOf(_commit);
			}
			catch(Exception ex)
			{
			}
			if(ncommit<10)
			{
				ncommit=100;
			}
		}
		
		if(_preaction==null)
		{
			_preaction=p._preaction;
		}
		if(from!=null && from.source!=null)
		{
			from.source.prepare();
		}
		if(to!=null && to.source!=null)
		{
			to.source.prepare();
		}
		if(difffrom!=null && difffrom.source!=null)
		{
			difffrom.source.prepare();
		}
		if(diffto!=null && diffto.source!=null)
		{
			diffto.source.prepare();
		}
		if(command!=null && command.source!=null)
		{
			command.source.prepare();
		}
		if(matchfrom!=null && matchfrom.source!=null)
		{
			matchfrom.source.prepare();
		}
	}
	
	public boolean doCommit(int n)
	{
		if(ncommit==1)
		{
			return true;
		}
		if(n>0 && n%ncommit==0)
		{
			return true;
		}
		return false;
	}
	
	public boolean autoCommit()
	{
		return ncommit==1;
	}
	
	public int getNCommit()
	{
		return ncommit;
	}
	
	public String getPreAction()
	{
		if("deleteall".equalsIgnoreCase(_preaction))
		{
			return "deleteall";
		}
		if("truncate".equalsIgnoreCase(_preaction))
		{
			return "truncate";
		}
		return "none";
	}
	
	public ExecParam getParentParam()
	{
		return parent.param;
	}
	public void reportStatus(String msg)
	{
		if(parent!=null)
		{
			if(this.info!=null)
			{
				parent.reportStatus(info+": "+msg);
			}
			else
			{
				parent.reportStatus(msg);
			}
		}
	}
	public void reportError(String msg,Exception ex)
	{
		if(parent!=null)
		{
			if(this.info!=null)
			{
				parent.reportError(info+": "+msg,ex);
			}
			else
			{
				parent.reportError(msg,ex);
			}
		}
		else if(ex!=null)
		{
			ex.printStackTrace();
		}
	}
	
	public String getQueryFrom()
	{
		if(parent!=null)
		{
			return parent.formatQuery(from.query);
		}
		return from.query;
	}
	
	public String getQueryFrom(String q)
	{
		if(parent!=null)
		{
			return parent.formatQuery(q);
		}
		return q;
	}
}

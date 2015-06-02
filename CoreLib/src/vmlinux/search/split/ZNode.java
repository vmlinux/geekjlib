package vmlinux.search.split;

import java.io.*;
import java.util.*;
import java.util.Map.*;

public class ZNode
{
	public String key;
	public Hashtable<String,ZNode> list;
	private int maxkey;
	private boolean rootnode;
	private boolean headnode;
	private ZNode parent;	//for serialize purpose
	private String corrkey;		//纠错
	private int lvl;
	
	public ZNode(String key)
	{
		this(key,false);
	}
	
	public ZNode(String key,boolean headnode)
	{
		this.key=key;
		this.maxkey=0;
		this.headnode=headnode;
		this.rootnode=false;
		//System.out.println("create ZNode "+key);
	}

	public void setHeadNode(boolean headnode)
	{
		this.headnode=headnode;
	}
	
	public void setRootNode(boolean rootnode)
	{
		this.rootnode=rootnode;
	}
	
	public void setCorrectKey(String key)
	{
		this.corrkey=key;
	}
	
	public void setLevel(int lvl)
	{
		this.lvl=lvl;
	}
	
	public ZNode getNode(String key)
	{
		if(list==null)
		{
			list=new Hashtable<String, ZNode>();
		}
		ZNode n=list.get(key);
		if(n==null)
		{
			n=new ZNode(key);
			n.parent=this;
			if(key.length()>maxkey)
			{
				maxkey=key.length();
			}
			list.put(key, n);
		}
		return n;
	}

	public ZNode getHeadNode(ZNode node,String key)
	{
		if(list==null)
		{
			list=new Hashtable<String, ZNode>();
		}
		ZNode n=list.get(key);
		if(n==null)
		{
			/*
			n=new ZNode(key);
			*/
			n=node;
			if(key.length()>maxkey)
			{
				maxkey=key.length();
			}
			if(n.parent!=null && n.parent.getLevel()<getLevel())
			{
				n.parent=this;
			}
			list.put(key, n);
		}
		n.headnode=true;
		return n;
	}
	
	public void addNode(ZNode node,String key)
	{
		if(list==null)
		{
			list=new Hashtable<String, ZNode>();
		}
		list.put(key, node);
	}

	public void getWords(ZDict root,ArrayList<ZPiece> arr,String prefix,String s,int lvl)
	{
		if(list!=null)
		{
			int len=s.length();

			int maxlen=len>maxkey?maxkey:len;
			for(int i=maxlen;i>=1;--i)
			{
				String key=s.substring(0,i);
				ZNode n=list.get(key);
				if(n!=null)
				{
					if(n.corrkey==null)
					{
						int nlv=n.getLevel();
						if(nlv>=lvl)
						{
							arr.add(new ZPiece(prefix+key,nlv));
						}
						n.getWords(root,arr,prefix+key,s.substring(key.length()),lvl);
					}
					else
					{
						if(n.corrkey.indexOf(key)==0)
						{
							//避免无限循环
							int nlv=n.getLevel();
							if(nlv>=lvl)
							{
								arr.add(new ZPiece(prefix+n.corrkey,nlv));
							}
							root.getWords(arr, s.substring(key.length()),lvl);
						}
						else
						{
							root.getWords(arr,n.corrkey+s.substring(key.length()),lvl);
						}
					}
				}
			}
		}
	}

	public int getWordsML(ZDict root,ArrayList<ZPiece> arr,String prefix,String s)
	{
		if(list!=null)
		{
			int len=s.length();

			int maxlen=len>maxkey?maxkey:len;
			for(int i=maxlen;i>=1;--i)
			{
				String key=s.substring(0,i);
				ZNode n=list.get(key);
				if(n!=null)
				{
					if(n.corrkey==null)
					{
						return n.getWordsML(root,arr,prefix+key,s.substring(key.length()));
					}
					else
					{
						if(n.corrkey.indexOf(key)==0)
						{
							//避免无限循环
							//arr.add(prefix+n.corrkey);
							int l=n.getWordsML(root,arr,prefix+n.corrkey,s.substring(key.length()));
							return l+key.length()-n.corrkey.length();
						}
						else
						{
							int l=root.getWordsML(arr, n.corrkey+s.substring(key.length()));
							return l+key.length()-n.corrkey.length();
						}
					}
				}
			}
		}
		
		arr.add(new ZPiece(prefix,getLevel()));
		return prefix.length();
	}

	public int getLevel()
	{
		if(parent==null)
		{
			return lvl;
		}
		else if(headnode && parent!=null && parent.rootnode)
		{
			return lvl;
		}
		else
		{
			return parent.getLevel()+1;
		}
	}
	
	public void printList(String prefix)
	{
		if(corrkey!=null)
		{
			System.out.println(prefix+corrkey+"("+key+")"+"("+getLevel()+")");
		}
		else
		{
			System.out.println(prefix+key+"("+getLevel()+")");
		}
		if(list!=null)
		{
			Iterator<Entry<String,ZNode>> i=list.entrySet().iterator();
			while(i.hasNext())
			{
				Entry<String,ZNode> e=i.next();
				String k=corrkey==null?key:corrkey;
				e.getValue().printList(prefix+k+"-");
			}
		}
	}
	
	public void serializeList(int level,String prefix,FileWriter fw) throws IOException
	{
		if(level<getLevel())
		{
			return;
		}

		if(list!=null)
		{
			Iterator<Entry<String,ZNode>> i=list.entrySet().iterator();
			while(i.hasNext())
			{
				Entry<String,ZNode> e=i.next();
				ZNode node=e.getValue();
				String sep=node.headnode?"*":"/";
				String k=corrkey==null?key:corrkey;
				/*
				if(!rootnode && node.headnode)
				{
					fw.write((prefix+k+sep+node.key));//.substring(2));
					fw.write("\r\n");
					node.serializeList(1, "", fw);
				}
				else*/
				{
					node.serializeList(level+1,prefix+k+sep,fw);
				}
			}
		}
		else
		{
			String k=corrkey==null?key:corrkey;
			fw.write((prefix+k).substring(2));
			fw.write("\r\n");
		}
	}
}

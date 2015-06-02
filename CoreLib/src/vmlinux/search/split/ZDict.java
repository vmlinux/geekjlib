package vmlinux.search.split;

import java.io.*;
import java.util.*;
import java.util.Map.*;

@SuppressWarnings("unchecked")
public class ZDict
{	
	private Hashtable<String, ZNode> items=new Hashtable<String, ZNode>();
	private Hashtable<String, String> params=new Hashtable<String, String>();
	
	public ZDict()
	{
		
	}
	
	public ZDict(String file) throws IOException
	{
		loadFile(file);
	}
	
	public void loadFile(String file) throws IOException
	{
		params.clear();
		BufferedReader rd=new BufferedReader(new FileReader(file));
		String line=null;
		while((line=rd.readLine())!=null)
		{
			addWord(line);
		}
		rd.close();
	}
	
	public void loadFolder(String dir) throws IOException
	{
		File f=new File(dir);
		File[] fs=f.listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name)
			{
				return name.endsWith(".dic");
			}
			
		});
		for(int i=0;i<fs.length;++i)
		{
			loadFile(fs[i].getAbsolutePath());
		}
	}
	
	public void addAll(String[] ss)
	{
		for(int i=0;i<ss.length;++i)
		{
			addWord(ss[i]);
		}
	}
	
	public void addParameter(String p)
	{
		String[] ps=p.split("=");
		if(ps.length==2)
		{
			addParameter(ps[0],ps[1]);
		}
	}
	
	public void addParameter(String n,String v)
	{
		params.put(n.trim(), v.trim());
	}
	
	public String getParam(String n)
	{
		return params.get(n);
	}
	
	public int getIntParam(String n)
	{
		String s=params.get(n);
		if(s==null)
			return 0;
		else
			return Integer.valueOf(s);
	}
	
	public void addWord(String w)
	{
		if(w==null || w.length()==0)
			return;
		if(w.startsWith("@"))
		{
			addParameter(w.substring(1));
			return;
		}
		ZNode item=getItem(w);
		StringBuffer sb=new StringBuffer(item.key);
		ZNode node=item;
		boolean headnode=true;
		for(int i=1;i<w.length();++i)
		{
			char c=w.charAt(i);
			if(c=='/')
			{
				if(headnode)
				{
					node=getHeadNode(node,sb.toString());
				}
				else
				{
					node=getNode(node,sb.toString());
				}
				sb.delete(0, sb.length());
				headnode=false;
			}
			else if(c=='*')
			{
				addWord(w.substring(i+1));
				if(headnode)
				{
					node=getHeadNode(node,sb.toString());
				}
				else
				{
					node=getNode(node,sb.toString());
				}
				sb.delete(0, sb.length());
				headnode=true;
			}
			else if(c=='=')
			{
				//纠错
				getHeadNode(node,w.substring(i+1));
				getHeadNode(node,sb.toString()).setCorrectKey(w.substring(i+1));
				break;
			}
			else if(c==' ' || c=='\t')
			{
				break;
			}
			else
			{
				sb.append(c);
			}
		}
		if(sb.length()>0)
		{
			if(headnode)
			{
				node=getHeadNode(node,sb.toString());
			}
			else
			{
				node=getNode(node,sb.toString());
			}
		}
	}
	
	public ZNode getItem(String s)
	{
		String key=s.substring(0,1);
		ZNode n=items.get(key);
		if(n==null)
		{
			n=new ZNode(key,true);
			n.setRootNode(true);
			items.put(key, n);
		}
		return n;
	}
	
	public boolean hasWord(String s)
	{
		ArrayList<ZPiece> tmp=new ArrayList<ZPiece>();
		int l=getWordsML(tmp, s);
		return l>0;
	}
	
	public ZNode getNode(ZNode node,String key)
	{
		return node.getNode(key);
	}
	
	public ZNode getHeadNode(ZNode node,String key)
	{
		ZNode item=getItem(key);
		ZNode n=item.getNode(key);
		n.setHeadNode(true);
		n.setLevel(getIntParam("level"));
		return node.getHeadNode(n,key);
	}
	
	public void getWords(ArrayList<ZPiece> arr,String s,int level)
	{
		String itemkey=s.substring(0,1);
		ZNode item=items.get(itemkey);
		if(item==null)
		{
			return;
		}
		item.getWords(this,arr,"",s,level);
	}

	public int getWordsML(ArrayList<ZPiece> arr,String s)
	{
		String itemkey=s.substring(0,1);
		ZNode item=items.get(itemkey);
		if(item==null)
		{
			return 0;
		}
		return item.getWordsML(this,arr,"",s);
	}

	public void printList()
	{
		Iterator<Entry<String,ZNode>> i=items.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String,ZNode> e=i.next();
			e.getValue().printList("");
		}
	}
	
	public void serializeList(String file) throws IOException
	{
		FileWriter fw=new FileWriter(file);
		Iterator<Entry<String,ZNode>> i=items.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String,ZNode> e=i.next();
			e.getValue().serializeList(1,"",fw);
		}
		fw.close();
	}
}

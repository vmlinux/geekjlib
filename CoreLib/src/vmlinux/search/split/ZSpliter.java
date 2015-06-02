package vmlinux.search.split;

import java.util.*;

public class ZSpliter
{
	private ZDict dict;
	private ZDict lcdict;
	
	public ZSpliter(ZDict dict)
	{
		this.dict=dict;
		lcdict=new ZDict();
		lcdict.addAll(new String[]{
				"年","月","日","小时","分钟","秒","个*星期","季度"
				,"分","元","美元","日元","人民币","欧元","美分","卢布"
				,"个*月","人/次","项","号","点","天","家","国"
				,"次","部分","度","张","条","座","月份","下","笔","种"
				,"亿","万","万亿","亿*美元"
		});
	}
	
	public String listKeywords(String s)
	{
		return listKeywords(s,2);
	}
	
	public String listKeywords(String s,int l)
	{
		ArrayList<ZPiece> str=split(s,l);
		StringBuffer sb=new StringBuffer();
		int c=0;
		for(int i=0;i<str.size();++i)
		{
			ZPiece z=str.get(i);
			if(c>0)
			{
				sb.append(", ");
			}
			sb.append(z.str);
			++c;
		}
		return sb.toString();
	}
	
	public ArrayList<ZPiece> split(String s)
	{
		return split(s,0);
	}
	
	private void addPiece(Hashtable<String,ZPiece> ht,ArrayList<String> arr,int v)
	{
		for(int i=0;i<arr.size();++i)
		{
			ZPiece z=new ZPiece(arr.get(i),v);
			ht.put(z.str, z);
		}
	}
	
	private void addPiece(Hashtable<String,ZPiece> ht,ArrayList<ZPiece> arr)
	{
		for(int i=0;i<arr.size();++i)
		{
			ZPiece z=arr.get(i);
			ht.put(z.str, z);
		}
	}

	public ArrayList<ZPiece> split(String s,int lvl)
	{
		ArrayList<ZPiece> list=new ArrayList<ZPiece>();
		Hashtable<String,ZPiece> keys=new Hashtable<String,ZPiece>();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();++i)
		{
			String ss=s.substring(i);
			ArrayList<ZPiece> tmp=new ArrayList<ZPiece>();
			dict.getWords(tmp, ss, lvl);

			if(tmp.size()==0)
			{
				//sb.append(ss.substring(0, 1));
			}
			else
			{
				if(sb.length()>0)
				{
					addPiece(keys,splitAscii(sb.toString()),0);
					sb.delete(0, sb.length());
				}
				addPiece(keys,tmp);
			}
		}
		if(sb.length()>0)
		{
			addPiece(keys,splitAscii(sb.toString()),0);
		}
		list.addAll(keys.values());
		return list;
	}
	
	public String readWords(String s)
	{
		ArrayList<ZPiece> str=splitML(s);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<str.size();++i)
		{
			ZPiece z=str.get(i);
			sb.append(z.str);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public ArrayList<String> regexTranslate(ArrayList<String> list,Hashtable<String,String> regtrans)
	{
		ArrayList<String> tab=new ArrayList<String>();
		for(int i=0;i<list.size();++i)
		{
			Iterator<Map.Entry<String, String>> e=regtrans.entrySet().iterator();
			String s=list.get(i);
			while(e.hasNext())
			{
				Map.Entry<String, String> p=e.next();
				if(s.matches(p.getKey()))
				{
					tab.add(p.getValue());
				}
			}
		}
		return tab;
	}
	
	public ArrayList<String> filterKeys(ArrayList<String> words)
	{
		ArrayList<String> list=new ArrayList<String>();
		for(int i=0;i<words.size();++i)
		{
			String w=words.get(i);
			if(dict.hasWord(w))
			{
				list.add(w);
			}
		}
		return list;
	}
	
	public ArrayList<ZPiece> matchKeys(String s,Hashtable<String, String> regtrans)
	{
		ArrayList<ZPiece> list=new ArrayList<ZPiece>();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();)
		{
			String ss=s.substring(i);
			ArrayList<ZPiece> tmp=new ArrayList<ZPiece>();
			int l=dict.getWordsML(tmp, ss);

			if(l==0)
			{
				sb.append(ss.substring(0,1));
				i++;
			}
			else
			{
				if(sb.length()>0)
				{
					//copyPiece(list,splitAscii(sb.toString()),0);
					copyPiece(list,filterKeys(regexTranslate(splitAscii(sb.toString()), regtrans)),0);
					sb.delete(0, sb.length());
				}
				list.addAll(tmp);
				i+=l;
			}

		}
		if(sb.length()>0)
		{
			//copyPiece(list,splitAscii(sb.toString()),0);
			copyPiece(list,filterKeys(regexTranslate(splitAscii(sb.toString()), regtrans)),0);
			sb.delete(0, sb.length());
		}
		return list;
	}
	
	private void copyPiece(ArrayList<ZPiece> arr,ArrayList<String> arrstr,int v)
	{
		for(int i=0;i<arrstr.size();++i)
		{
			ZPiece z=new ZPiece(arrstr.get(i),v);
			arr.add(z);
		}
	}
	
	public ArrayList<ZPiece> splitML(String s)
	{
		ArrayList<ZPiece> list=new ArrayList<ZPiece>();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();)
		{
			String ss=s.substring(i);
			ArrayList<ZPiece> tmp=new ArrayList<ZPiece>();
			int l=dict.getWordsML(tmp, ss);

			if(l==0)
			{
				sb.append(ss.substring(0,1));
				i++;
			}
			else
			{
				if(sb.length()>0)
				{
					copyPiece(list,splitAscii(sb.toString()),0);
					sb.delete(0, sb.length());
				}
				list.addAll(tmp);
				i+=l;
			}

		}
		if(sb.length()>0)
		{
			copyPiece(list,splitAscii(sb.toString()),0);
			sb.delete(0, sb.length());
		}
		return list;
	}
	
	public ArrayList<String> splitAscii(String s)
	{
		ArrayList<String> list=new ArrayList<String>();
		StringBuffer sb=new StringBuffer();
		boolean digit=false;
		for(int i=0;i<s.length();++i)
		{
			char c=s.charAt(i);
			if(c>255)
			{
				if(c=='一' || c=='二'
					|| c=='三' || c=='四'
					|| c=='五' || c=='六'
					|| c=='七' || c=='八'
					|| c=='九' || c=='十'
					|| c=='零' || c=='百'
					|| c=='千' || c=='万'
					|| c=='亿' || c=='两' || c=='半')
				{
					sb.append(c);
					digit=true;
				}
				else if(c=='为' || c=='是' || c=='和'
					|| c=='，' || c=='。' || c=='：' || c=='；')
				{
					if(sb.length()>0)
					{
						String ss=sb.toString();
						list.add(ss);
						sb.delete(0, sb.length());
					}
					list.add(""+c);
				}
				else
				{
					if(sb.length()>0)
					{
						String ss=sb.toString();
						if(ss.matches("[\\d\\.]+") || digit)
						{
							ArrayList<ZPiece> tmp=new ArrayList<ZPiece>();
							int l=lcdict.getWordsML(tmp, s.substring(i));
							if(l==0)
							{
								list.add(ss);
								list.add(""+c);
							}
							else
							{
								list.add(ss+tmp.get(0).str);
								i+=l-1;
							}
						}
						else
						{
							list.add(ss);
							list.add(""+c);
						}
						sb.delete(0, sb.length());
					}
					else
					{
						list.add(""+c);
					}
					digit=false;
				}
			}
			else if((c>='0' && c<='9')
					||(c>='a' && c<='z')
					||(c>='A' && c<='Z'))
			{
				sb.append(c);
				digit=false;
			}
			else if(c==' ' && c=='\t')
			{
				if(sb.length()>0)
				{
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				digit=false;
			}
			else if(c=='.' || c=='%')
			{
				String ss=sb.toString();
				if(ss.matches("[\\d\\.]+"))
				{
					sb.append(c);
				}
				else
				{
					if(ss.length()>0)
					{
						list.add(ss);
						sb.delete(0, sb.length());
					}
					list.add(""+c);
					digit=false;
				}
			}
			else
			{
				if(sb.length()>0)
				{
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(""+c);
				digit=false;
			}
		}
		if(sb.length()>0)
		{
			list.add(sb.toString());
		}
		return list;
	}
}

package vmlinux.util;

import java.util.Map;

@SuppressWarnings("unchecked")
public class JSParser
{
	protected Map map;
	
	public JSParser()
	{
		
	}
	
	protected void addValue(String name,Object val)
	{
		if(map!=null)
		{
			String nm=name.trim();
			if(nm.startsWith("var "))
			{
				nm=nm.substring(4).trim();
			}
			map.put(nm, decodeUnicode(val.toString()));
		}
	}

	public void js2map(String js)
	{
		StringBuffer id=new StringBuffer();
		StringBuffer sb=new StringBuffer();
		int t=0;
		int l=js.length();
		for(int i=0;i<l;++i)
		{
			char c=js.charAt(i);
			char c1=(i<l-1)?js.charAt(i+1):'\0';
			if(c=='/' && c1=='/')
			{
				int p=js.indexOf("\n",i+2);
				if(p>0)
				{
					i=p;
				}
				else
				{
					i=l;
				}
			}
			else if(c=='/' && c1=='*')
			{
				int p=js.indexOf("*/",i+2);
				if(p>0)
				{
					i=p+1;
				}
				else
				{
					i=l;
				}
			}
			else if(c=='"' || c=='\'')
			{
				int p=matchQuote(js,i+1,c);
				if(t==1 && id.length()>0)
				{
					addValue(id.toString(),js.substring(i+1,p+1));
					id=new StringBuffer();
					sb=new StringBuffer();
					t=0;
				}
				i=p+1;
			}
			else if(c=='{')
			{
				int p=matchBracket(js, i+1, '{', '}');
				if(t==1 && id.length()>0)
				{
					addValue(id.toString(),js.substring(i,p+1));
					id=new StringBuffer();
					sb=new StringBuffer();
					t=0;
				}
				i=p+1;
			}
			else if(c=='[')
			{
				int p=matchBracket(js, i+1, '[', ']');
				if(t==1 && id.length()>0)
				{
					addValue(id.toString(),js.substring(i,p+1));
					id=new StringBuffer();
					sb=new StringBuffer();
					t=0;
				}
				i=p+1;
			}
			else if(c=='(')
			{
				int p=matchBracket(js, i+1, '(', ')');
				if(t==1 && id.length()>0)
				{
					addValue(id.toString(),js.substring(i,p+1));
					id=new StringBuffer();
					sb=new StringBuffer();
					t=0;
				}
				i=p+1;
			}
			else if(c==';')
			{
				t=0;	//init status
				if(id.length()>0)
				{
					addValue(id.toString(),sb.toString());
					id=new StringBuffer();
					sb=new StringBuffer();
					t=0;
				}
				id=new StringBuffer();
				sb=new StringBuffer();
			}
			else if(c=='=' && t==0)
			{
				t=1;
			}
			else if(t==0)
			{
				id.append(c);
			}
			else if(t==1)
			{
				sb.append(c);
			}
		}
	}

	public static String decodeUnicode(String s)
	{
		StringBuffer sb=new StringBuffer();
		int l=s.length();
		for(int i=0;i<l-1;++i)
		{
			char c=s.charAt(i);
			char cc=s.charAt(i+1);
			if(c=='\\' && cc=='u')
			{
				String ss=s.substring(i+2, i+6);
				int v=0;
				for(int j=0;j<4;++j)
				{
					c=ss.charAt(j);
					if(c>='0' && c<='9')
					{
						v=(v<<4)+(c-'0');
					}
					else if(c>='a' && c<='f')
					{
						v=(v<<4)+(c-'a')+10;
					}
					else if(c>='A' && c<='F')
					{
						v=(v<<4)+(c-'A')+10;
					}
					else
					{
						throw new RuntimeException("Invalid unicode char : \\u"+ss);
					}
				}
				sb.append((char)v);
				i=i+5;
			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static int matchQuote(String s,int p,char ch)
	{
		int l=s.length();
		for(int i=p;i<l;++i)
		{
			char c=s.charAt(i);
			if(c==ch && s.charAt(i-1)!='\\')
			{
				return i;
			}
		}
		return l;
	}
	
	public static int matchBracket(String s,int p,char ch,char cc)
	{
		int l=s.length();
		int n=1;
		for(int i=p;i<l;++i)
		{
			char c=s.charAt(i);
			if(c==ch)
			{
				n++;
			}
			else if(c==cc)
			{
				n--;
				if(n==0)
				{
					return i;
				}
			}
			else if(c=='/')
			{
				if(i<l-1)
				{
					if(s.charAt(i+1)=='/')
					{
						int q=s.indexOf("\n",i+2);
						if(q>0)
						{
							i=q;
						}
						else
						{
							i=l;
						}
					}
					else if(s.charAt(i+1)=='*')
					{
						int q=s.indexOf("*/",i+2);
						if(q>0)
						{
							i=q+1;
						}
						else
						{
							i=l;
						}
					}
				}
			}
		}
		return l;
	}

	public Map parse(String js,Map map)
	{
		this.map=map;
		js2map(js);
		return map;
	}
}

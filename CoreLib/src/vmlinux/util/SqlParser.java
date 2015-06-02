package vmlinux.util;

import java.util.*;

import vmlinux.reflect.*;

public class SqlParser
{
	public static class StringTokens implements XObject
	{
		public String[] tokens;
		public String str;
		
		public StringTokens()
		{
			
		}
		
		public StringTokens(String str)
		{
			this.str=str;
			tokens=getTokens(str);
		}

		public int findToken(String s)
		{
			return findToken(tokens,s);
		}
		
		public static int findToken(String[] ss,String s)
		{
			for(int i=0;i<ss.length;++i	)
			{
				if(s.equalsIgnoreCase(ss[i]))
				{
					return i;
				}
			}
			return -1;
		}

		public static String[] getTokens(String s)
		{
			int l=s.length();
			Vector<String> v=new Vector<String>();
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<l;++i)
			{
				char c=s.charAt(i);
				if(c==' ')
				{
					if(sb.length()>0)
					{
						v.add(sb.toString());
						sb.delete(0, sb.length());
					}
				}
				else if(c=='"' || c=='\'' || c=='(')
				{
					if(sb.length()>0)
					{
						v.add(sb.toString());
						sb.delete(0, sb.length());
					}
					char cc=c;
					if(c=='(')
					{
						cc=')';
					}
					int p=s.indexOf(cc,i+1);
					if(p>0)
					{
						while(cc=='\'' && p<s.length()-1 && s.charAt(p+1)=='\'' && p>0)
						{
							p=s.indexOf(cc,p+2);
						}
						v.add(s.substring(i,p+1));
						i=p;
					}
					else
					{
						throw new RuntimeException("missing close of "+c+" at "+i);
					}
				}
				else if(c=='>' || c=='=' || c=='<' || c=='!')
				{
					if(sb.length()>0)
					{
						v.add(sb.toString());
						sb.delete(0, sb.length());
					}
					char cc=s.charAt(i+1);
					if(cc=='=')
					{
						i++;
						v.add(c+"=");
					}
					else
					{
						v.add(c+"");
					}
				}
				else if(c==',')
				{
					v.add(sb.toString());
					sb.delete(0, sb.length());
					v.add(",");
				}
				else
				{
					sb.append(c);
				}
			}
			if(sb.length()>0)
			{
				v.add(sb.toString());
			}
			String[] ss=new String[v.size()];
			return (String[])v.toArray(ss);
		}
	}
	
	public static class TestTokens extends StringTokens
	{
		public String vname;
		public String testop;
		public String value;
		
		public TestTokens()
		{
			
		}
		
		public TestTokens(String str)
		{
			super(str);
			vname=tokens[0];
			testop=tokens[1];
			value=tokens[2];
		}
		
		public String getStringValue()
		{
			if(value!=null)
			{
				if((value.startsWith("'") && value.endsWith("'"))
						|| (value.startsWith("\"") && value.startsWith("\"")))
				{
					String v=value.substring(1,value.length()-1);
					return v.replaceAll("''", "'");
				}
				else
				{
					return value;
				}
			}
			return null;
		}
	}
	
	public static class TestParams extends XDynamic
	{
		public String getValue(String name)
		{
			TestTokens tt=(TestTokens)get(name.toLowerCase());
			return tt.getStringValue();
		}
		
		public String getOperator(String name)
		{
			TestTokens tt=(TestTokens)get(name.toLowerCase());
			return tt.testop;
		}
	}
	
	public static abstract class SqlTokens extends StringTokens
	{
		public SqlTokens()
		{
			
		}
		
		public SqlTokens(String str)
		{
			super(str);
		}
		
		public String concatSeperated(String[] ss,String pre,String sep)
		{
			if(ss!=null && ss.length>0)
			{
				StringBuffer sb=new StringBuffer(pre);
				for(int i=0;i<ss.length;++i)
				{
					if(i>0)
					{
						sb.append(sep);
					}
					sb.append(ss[i]);
				}
				return sb.toString();
			}
			return "";
		}
		
		public String[] copySeperated(int p,int l)
		{
			Vector<String> v=new Vector<String>();
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<l;++i)
			{
				String s=tokens[p+i];
				if(",".equals(s))
				{
					if(sb.length()>0)
					{
						v.add(sb.toString());
						sb.delete(0, sb.length());
					}
				}
				else if("and".equalsIgnoreCase(s) || "or".equalsIgnoreCase(s))
				{
					if(sb.length()>0)
					{
						v.add(sb.toString());
						sb.delete(0, sb.length());
					}
					v.add(s);
				}
				else
				{
					if(sb.length()>0)
					{
						sb.append(" ");
					}
					sb.append(s);
				}
			}
			if(sb.length()>0)
			{
				v.add(sb.toString());
			}
			String[] ss=new String[v.size()];
			return (String[])v.toArray(ss);
		}
		
		public abstract String[] getWhere();
		
		public TestParams getWhereParams()
		{
			TestParams wp=new TestParams();
			String[] ss=getWhere();
			if(ss!=null)
			{
				for(int i=0;i<ss.length;++i)
				{
					String s=ss[i];
					if("and".equalsIgnoreCase(s) || "or".equalsIgnoreCase(s))
					{
						continue;
					}
					TestTokens t=new TestTokens(s);
					wp.put(t.vname.toLowerCase(), t);
				}
			}
			return wp;
		}
	}
	
	public static class DeleteTokens extends SqlTokens
	{
		public int pwhere;
		
		public DeleteTokens()
		{
			
		}
		
		public DeleteTokens(String sql)
		{
			super(sql);
			if(!"delete".equalsIgnoreCase(tokens[0]))
			{
				throw new RuntimeException("not a delete sql");
			}
			pwhere=findToken("where");
		}
		
		public String[] getTable()
		{
			int pend=pwhere>0?pwhere:tokens.length;
			int len=pend-1;
			return copySeperated(1,len);
		}

		public String[] getWhere()
		{
			if(pwhere>0)
			{
				int pend=tokens.length;
				int len=pend-pwhere-1;
				return copySeperated(pwhere+1,len);
			}
			return null;
		}
		
		public String getFormated()
		{
			StringBuffer sb=new StringBuffer();
			sb.append(concatSeperated(getTable(), "delete ", ","));
			sb.append(concatSeperated(getWhere(), " where ", " "));
			return sb.toString();
		}
	}
	
	public static class UpdateTokens extends SqlTokens
	{
		public int pset;
		public int pwhere;
		
		public UpdateTokens()
		{
			
		}
		
		public UpdateTokens(String sql)
		{
			super(sql);
			if(!"update".equalsIgnoreCase(tokens[0]))
			{
				throw new RuntimeException("not an update sql");
			}
			pset=findToken("set");
			if(pset<0)
			{
				throw new RuntimeException("invalid update query, need set");
			}
			pwhere=findToken("where");
		}
		
		public String[] getTable()
		{
			int pend=pset>0?pset:tokens.length;
			int len=pend-1;
			return copySeperated(1,len);
		}

		public String[] getField()
		{
			int pend=pwhere>0?pwhere:tokens.length;
			int len=pend-pset-1;
			return copySeperated(pset+1,len);
		}
		
		public String[] getWhere()
		{
			if(pwhere>0)
			{
				int pend=tokens.length;
				int len=pend-pwhere-1;
				return copySeperated(pwhere+1,len);
			}
			return null;
		}
		
		public String getFormated()
		{
			StringBuffer sb=new StringBuffer();
			sb.append(concatSeperated(getTable(), "update ", ","));
			sb.append(concatSeperated(getField(), " set ", ","));
			sb.append(concatSeperated(getWhere(), " where ", " "));
			return sb.toString();
		}
		
		public TestParams getSetParams()
		{
			TestParams sp=new TestParams();
			String[] ss=getField();
			if(ss!=null)
			{
				for(int i=0;i<ss.length;++i)
				{
					String s=ss[i];
					if(",".equalsIgnoreCase(s))
					{
						continue;
					}
					TestTokens t=new TestTokens(s);
					sp.put(t.vname.toLowerCase(), t);
				}
			}
			return sp;
		}
	}
	
	public static class SelectTokens extends SqlTokens
	{
		public int pfrom;
		public int pwhere;
		public int pgroup;
		public int porder;
		
		public SelectTokens()
		{
			
		}
		
		public SelectTokens(String sql)
		{
			super(sql);
			if(!"select".equalsIgnoreCase(tokens[0]))
			{
				throw new RuntimeException("not a select sql");
			}
			pfrom=findToken("from");
			if(pfrom<0)
			{
				throw new RuntimeException("invalid select query, need from");
			}
			pwhere=findToken("where");
			pgroup=findToken("group");
			if(pgroup>0)	//group by
			{
				pgroup++;
			}
			porder=findToken("order");
			if(porder>0)	//order by
			{
				porder++;
			}
		}
		
		public String[] getField()
		{
			int pend=pfrom>0?pfrom:tokens.length;
			int len=pend-1;
			return copySeperated(1,len);
		}
		
		public String[] getTable()
		{
			int pend=pwhere>0?pwhere:tokens.length;
			int len=pend-pfrom-1;
			return copySeperated(pfrom+1,len);
		}
		
		public String[] getWhere()
		{
			if(pwhere>0)
			{
				int pend=pgroup>0?pgroup-1:(porder>0?porder-1:tokens.length);
				int len=pend-pwhere-1;
				return copySeperated(pwhere+1,len);
			}
			return null;
		}
		
		public String[] getGroupBy()
		{
			if(pgroup>0)
			{
				int pend=porder>0?porder-1:tokens.length;
				int len=pend-pgroup-1;
				return copySeperated(pgroup+1,len);
			}
			return null;
		}

		public String[] getOrderBy()
		{
			if(porder>0)
			{
				int pend=tokens.length;
				int len=pend-porder-1;
				return copySeperated(porder+1,len);
			}
			return null;
		}
		
		public String getFormated()
		{
			StringBuffer sb=new StringBuffer();
			sb.append(concatSeperated(getField(), "select ", ","));
			sb.append(concatSeperated(getTable(), " from ", ","));
			sb.append(concatSeperated(getWhere(), " where ", " "));
			sb.append(concatSeperated(getGroupBy(), " group by ", ","));
			sb.append(concatSeperated(getOrderBy(), " order by ", ","));
			return sb.toString();
		}
	}
	
}

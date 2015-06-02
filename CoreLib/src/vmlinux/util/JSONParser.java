package vmlinux.util;

import java.util.Map;

@SuppressWarnings("unchecked")
public class JSONParser
{
	public static final int STATUS_NORMAL=0;
	public static final int STATUS_TOKEN=1;
	public static final int STATUS_VALUE=2;
	public static final int STATUS_OBJECT=3;
	public static final int STATUS_ARRAY=4;
	public static boolean isJSON(String str)
	{
		return str.startsWith("{")||str.startsWith("[");
	}
	public static boolean isJSONObject(String str)
	{
		return str.startsWith("{");
	}
	public static boolean isJSONArray(String str)
	{
		return str.startsWith("[");
	}
	
	public static class PathStack
	{
		protected String[] path;
		protected int pos;
		
		public PathStack(int length)
		{
			path=new String[length];
			pos=0;
		}
		public void push(String p)
		{
			if(pos==path.length)
				throw new StackOverflowError("PathStack::push:overflow");
			else
				path[pos++]=p;
		}
		public String pop()
		{
			return pos>0?path[--pos]:null;
		}
		//peek top element
		public String peek()
		{
			return path[pos-1];
		}
		//is empty
		public boolean empty()
		{
			return pos==0;
		}
		public int count()
		{
			return pos;
		}
		public void clear()
		{
			pos=0;
		}
		//get path
		public String path()
		{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<pos;++i)
			{
				if(i>0)
					sb.append(".");
				sb.append(path[i]);
			}
			return sb.toString();
		}
		//get path as prefix
		public String prefix()
		{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<pos;++i)
			{
				sb.append(path[i]);
				sb.append(".");
			}
			return sb.toString();
		}
	}
	protected String json;	//string to parse
	protected int pos;	//current position
	protected PathStack path;
	protected Map map;
	protected int len;
	
	public JSONParser()
	{
		this.path=new PathStack(8);
	}
	public JSONParser(int depth)
	{
		this.path=new PathStack(depth);
	}
	
	public Map parse(String json,Map map)
	{
		this.json=json;
		this.map=map;
		this.pos=0;
		this.path.clear();
		this.len=json.length();
		//start parse
		char c;
		for(;pos<len;++pos)
		{
			c=json.charAt(pos);
			if(c=='{')
			{
				parseObject();
			}
			else if(c=='[')
			{
				parseArray();
			}
			else
			{
				addObject("",json);
				pos=len;
			}
			//skip
		}
		
		return map;
	}
	protected String tokenTrim(StringBuffer token)
	{
		String t=token.toString().trim();
		if(t.indexOf("\"")>=0)
		{
			t=StringUtil.replacePlain(t, "\"", "");
		}
		if(t.indexOf("'")>=0)
		{
			t=StringUtil.replacePlain(t, "'", "");
		}
		return t;
	}
	protected String valueDecode(StringBuffer value)
	{
		String v=value.toString().trim();
		if(v.startsWith("\"")||v.startsWith("\'"))
			return StringUtil.decodeEntity(v.substring(1, v.length()-1));
		else
			return StringUtil.decodeEntity(v);
	}
	protected void addValue(StringBuffer token,StringBuffer value)
	{
		String key=path.prefix()+tokenTrim(token);
		map.put(key, valueDecode(value));
		token.delete(0, token.length());
		value.delete(0, value.length());
	}
	protected void addValue(String token,StringBuffer value)
	{
		String key=path.prefix()+token;
		map.put(key, valueDecode(value));
		value.delete(0, value.length());
	}
	protected void addObject(StringBuffer token,Object value)
	{
		String key=path.prefix()+tokenTrim(token);
		map.put(key, value);
		token.delete(0, token.length());
	}
	protected void addObject(String token,Object value)
	{
		String key=path.prefix()+token;
		map.put(key, value);
	}
	protected void parseObject()
	{
		char c;
		StringBuffer token=new StringBuffer();
		StringBuffer value=new StringBuffer();
		int status=STATUS_TOKEN;
		int vstatus=STATUS_NORMAL;
		for(pos++;pos<len;++pos)
		{
			c=json.charAt(pos);
			if(c=='}')
			{
				//finish object
				if(vstatus!=STATUS_ARRAY)
					addValue(token,value);
				path.pop();
				break;
			}
			else if(c=='{')
			{
				path.push(tokenTrim(token));
				parseObject();
				vstatus=STATUS_OBJECT;
			}
			else if(c=='[')
			{
				path.push(tokenTrim(token));
				parseArray();
				vstatus=STATUS_ARRAY;
			}
			else if(c==',')
			{
				//commit value
				if(vstatus!=STATUS_ARRAY)
					addValue(token,value);
				else
					token.delete(0, token.length());
				status=STATUS_TOKEN;
			}
			else if(c==':')
			{
				if(status==STATUS_TOKEN)
				{
					//commit token
					status=STATUS_VALUE;
					vstatus=STATUS_NORMAL;
				}
				else
				{
					value.append(c);
				}
			}
			else
			{
				if(status==STATUS_TOKEN)
				{
					token.append(c);
				}
				else if(status==STATUS_VALUE)
				{
					if(c=='\'' || c=='\"')
					{
						int p=json.indexOf(c,pos+1);
						value.append(json.substring(pos,p+1));
						pos=p;
					}
					else
					{
						value.append(c);
					}
				}
			}
		}
	}
	protected void parseArray()
	{
		char c;
		int index=0;
		int vstatus=STATUS_NORMAL;
		StringBuffer value=new StringBuffer();
		for(pos++;pos<len;++pos)
		{
			c=json.charAt(pos);
			if(c==']')
			{
				//finish array
				if(vstatus!=STATUS_ARRAY)
					addValue(Integer.toString(index), value);
				path.pop();
				break;
			}
			else if(c=='{')
			{
				path.push(Integer.toString(index));
				parseObject();
				vstatus=STATUS_OBJECT;
			}
			else if(c=='[')
			{
				path.push(Integer.toString(index));
				parseArray();
				vstatus=STATUS_ARRAY;
			}
			else if(c==',')
			{
				if(vstatus!=STATUS_ARRAY)
					addValue(Integer.toString(index++), value);
				else
					index++;
			}
			else
			{
				value.append(c);
			}
		}
	}
}

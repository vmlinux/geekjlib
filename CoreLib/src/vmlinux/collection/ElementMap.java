package vmlinux.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Element;

public class ElementMap implements Map
{
	Element elem;
	
	public ElementMap(Element e)
	{
		this.elem=e;
	}
	public void clear()
	{
		elem.removeContent();
	}

	public boolean containsKey(Object key)
	{
		String v=key instanceof String?(String)key:key.toString();
		return elem.getChild(v)==null
			&& elem.getAttribute(v)==null;
	}

	public boolean containsValue(Object value)
	{
		String v=value instanceof String?(String)value:value.toString();
		List l=elem.getAttributes();
		Iterator i=l.iterator();
		Attribute a;
		while(i.hasNext())
		{
			a=(Attribute)i.next();
			if(v.equals(a.getValue()))
				return true;
		}
		Element e;
		l=elem.getChildren();
		i=l.iterator();
		while(i.hasNext())
		{
			e=(Element)i.next();
			if(v.equals(e.getText()))
				return true;
		}
		return false;
	}

	public Set entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object get(Object key)
	{
		String v=key instanceof String?(String)key:key.toString();
		String val=elem.getAttributeValue(v);
		if(val==null)
			return elem.getChildText(v);
		return val;
	}

	public boolean isEmpty()
	{
		return elem.getAttributes().size()==0 
			&& elem.getChildren().size()==0;
	}

	public Set keySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object put(Object key, Object value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void putAll(Map t)
	{
		// TODO Auto-generated method stub

	}

	public Object remove(Object key)
	{
		String v=key instanceof String?(String)key:key.toString();
		Object o=elem.getAttribute(v);
		if(o!=null)
		{
			elem.removeAttribute((Attribute)o);
			return o;
		}
		o=elem.getChild(v);
		if(o!=null)
		{
			elem.removeChild(v);
		}
		return null;
	}

	public int size()
	{
		return elem.getAttributes().size()
			+ elem.getChildren().size();
	}

	public Collection values()
	{
		// TODO Auto-generated method stub
		return null;
	}

}

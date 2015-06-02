package vmlinux.config;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class XMLConfig
{
	public static Element getElementByAttribute(List el,String an,String av)
	{
		if(an!=null && av!=null && el!=null)
		{
			Iterator i=el.iterator();
			Element e;
			while(i.hasNext())
			{
				e=(Element)i.next();
				if(av.equals(e.getAttributeValue(an)))
					return e;
			}
		}
		return null;
	}
}

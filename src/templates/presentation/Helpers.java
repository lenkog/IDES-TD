package templates.presentation;

import ides.api.model.fsa.FSAEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import templates.diagram.Connector;
import templates.model.TemplateLink;

public class Helpers
{
	public static Set<String> matchEvents(Connector c)
	{
		Set<String> leftEvents=new HashSet<String>();
		Set<String> rightEvents=new HashSet<String>();
		if(c.getLeftEntity().getComponent().hasModel())
		{
			for(Iterator<FSAEvent> i=c.getLeftEntity().getComponent().getModel().getEventIterator();i.hasNext();)
			{
				leftEvents.add(i.next().getSymbol());
			}
		}
		else
		{
			for(TemplateLink link:c.getLinks())
			{
				if(link.getLeftComponent()==c.getLeftEntity().getComponent())
				{
					leftEvents.add(link.getLeftEventName());
				}
				else
				{
					leftEvents.add(link.getRightEventName());
				}
			}
		}
		if(c.getRightEntity().getComponent().hasModel())
		{
			for(Iterator<FSAEvent> i=c.getRightEntity().getComponent().getModel().getEventIterator();i.hasNext();)
			{
				rightEvents.add(i.next().getSymbol());
			}
		}
		else
		{
			for(TemplateLink link:c.getLinks())
			{
				if(link.getRightComponent()==c.getLeftEntity().getComponent())
				{
					rightEvents.add(link.getLeftEventName());
				}
				else
				{
					rightEvents.add(link.getRightEventName());
				}
			}
		}
		return matchEvents(leftEvents,rightEvents);
	}
	
	public static Set<String> matchEvents(Set<String> leftEvents,Set<String> rightEvents)
	{
		Set<String> ret=new HashSet<String>(leftEvents);
		ret.retainAll(rightEvents);
		return ret;		
	}
}

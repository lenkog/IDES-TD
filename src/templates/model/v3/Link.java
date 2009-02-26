package templates.model.v3;

import ides.api.core.Hub;
import ides.api.plugin.model.DESEvent;

import java.util.Hashtable;

import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

public class Link implements TemplateLink
{
	protected Hashtable<String, Object> annotations = new Hashtable<String, Object>();

	public Object getAnnotation(String key)
	{
		return annotations.get(key);
	}

	public boolean hasAnnotation(String key)
	{
		return annotations.containsKey(key);
	}

	public void removeAnnotation(String key)
	{
		annotations.remove(key);
	}

	public void setAnnotation(String key, Object annotation)
	{
		if (annotation != null)
		{
			annotations.put(key, annotation);
		}
	}

	protected long id;

	protected TemplateComponent left;

	protected TemplateComponent right;

	protected String leftEvent = "";

	protected String rightEvent = "";

	public Link(long id, TemplateComponent left, TemplateComponent right)
	{
		if (right == null || left == null)
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinkInit"));
		}
		this.id = id;
		this.left = left;
		this.right = right;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public TemplateComponent getChannel()
	{
		if (left.getType() == TemplateComponent.TYPE_CHANNEL)
		{
			return left;
		}
		else if (right.getType() == TemplateComponent.TYPE_CHANNEL)
		{
			return right;
		}
		return null;
	}

	public DESEvent getRightEvent()
	{
		if (!right.hasModel())
		{
			return null;
		}
		for (DESEvent event : right.getModel().getEventSet())
		{
			if (event.getSymbol().equals(rightEvent))
			{
				return event;
			}
		}
		return null;
	}

	public String getRightEventName()
	{
		return rightEvent;
	}

	public TemplateComponent getModule()
	{
		if (left.getType() == TemplateComponent.TYPE_MODULE)
		{
			return left;
		}
		else if (right.getType() == TemplateComponent.TYPE_MODULE)
		{
			return right;
		}
		return null;
	}

	public DESEvent getLeftEvent()
	{
		if (!left.hasModel())
		{
			return null;
		}
		for (DESEvent event : left.getModel().getEventSet())
		{
			if (event.getSymbol().equals(leftEvent))
			{
				return event;
			}
		}
		return null;
	}

	public String getLeftEventName()
	{
		return leftEvent;
	}

	public boolean existsRightEvent()
	{
		return getRightEvent() != null;
	}

	public boolean existsLeftEvent()
	{
		return getLeftEvent() != null;
	}

	public void setRightEventName(String name)
	{
		if (name == null)
		{
			name = "";
		}
		rightEvent = name;
	}

	public void setLeftEventName(String name)
	{
		if (name == null)
		{
			name = "";
		}
		leftEvent = name;
	}

	public TemplateComponent[] getComponents()
	{
		return new TemplateComponent[] { left, right };
	}

	public TemplateComponent getLeftComponent()
	{
		return left;
	}

	public TemplateComponent getRightComponent()
	{
		return right;
	}

}

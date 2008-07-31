package templates.model.v3;

import ides.api.model.fsa.FSAModel;

import java.util.Hashtable;

import templates.model.TemplateComponent;

public class Component implements TemplateComponent
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

	protected int type=TemplateComponent.TYPE_INDETERMINATE;
	
	protected FSAModel fsa = null;

	public Component(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public FSAModel getModel()
	{
		return fsa;
	}

	public boolean hasModel()
	{
		return fsa != null;
	}

	public void setModel(FSAModel fsa)
	{
		this.fsa = fsa;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		if(type==TYPE_MODULE||type==TYPE_CHANNEL)
		{
			this.type=type;
		}
		else
		{
			this.type=TYPE_INDETERMINATE;
		}
	}
}
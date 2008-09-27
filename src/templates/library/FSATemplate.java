package templates.library;

import java.awt.Color;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;

import javax.swing.Icon;

import templates.utils.EntityIcon;

public class FSATemplate implements Template
{
	protected String tag;
	protected String description;
	protected FSAModel model;
	protected TemplateIcon icon;
	
	public FSATemplate(TemplateDescriptor td,FSAModel model)
	{
		tag=td.tag;
		description=td.description;
		this.model=model;
		icon=new TemplateIcon(tag,td.color,Hub.getMainWindow().getGraphics().create());
	}
	
	public EntityIcon getIcon()
	{
		return icon;
	}

	public String getName()
	{
		return tag;
	}

	public FSAModel instantiate()
	{
		return model.clone();
	}

	public String getDescription()
	{
		return description;
	}

}

package templates.library;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESModel;

import javax.swing.Icon;

import templates.utils.EntityIcon;

public interface Template
{
	public static final String TEMPLATE_DESC="templates.library.TemplateDescriptor";
	
	public String getName();

	public EntityIcon getIcon();
	
	public String getDescription();
	
	public FSAModel getModel();

	public FSAModel instantiate();
}

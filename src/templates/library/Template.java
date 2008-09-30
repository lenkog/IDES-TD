package templates.library;

import ides.api.model.fsa.FSAModel;

import javax.swing.Icon;

import templates.utils.EntityIcon;

public interface Template
{
	public static final String TEMPLATE_DESC="templates.library.TemplateDescriptor";
	
	public String getName();

	public TemplateIcon getIcon();
	
	public String getDescription();

	public FSAModel instantiate();
}

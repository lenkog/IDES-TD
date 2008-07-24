package template.plugin;

import ides.api.core.Hub;
import ides.api.plugin.Plugin;
import ides.api.plugin.PluginInitException;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.presentation.ToolsetManager;

import java.util.ResourceBundle;

import template.model.TemplateModel;
import template.model.v3.TemplateDesign;
import template.presentation.TemplateToolset;

public class TemplatePlugin implements Plugin
{

	public String getCredits()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLicense()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void initialize() throws PluginInitException
	{
		// Resources
		Hub.addResouceBundle(ResourceBundle.getBundle("templates"));

		// Models
		ModelManager.instance().registerModel(TemplateDesign.myDescriptor);

		// Toolsets
		ToolsetManager
				.instance().registerToolset(TemplateModel.class,
						new TemplateToolset());
	}

	public void unload()
	{
		// TODO Auto-generated method stub

	}

}

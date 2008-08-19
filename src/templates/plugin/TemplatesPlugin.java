package templates.plugin;

import ides.api.core.Hub;
import ides.api.plugin.Plugin;
import ides.api.plugin.PluginInitException;
import ides.api.plugin.io.IOPluginManager;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.presentation.ToolsetManager;

import java.util.ResourceBundle;

import templates.io.TemplateFileIO;
import templates.model.TemplateModel;
import templates.model.v3.TemplateDesign;
import templates.presentation.TemplateToolset;

public class TemplatesPlugin implements Plugin
{

	public String getCredits()
	{
		return Hub.string("TD_DEVELOPERS");
	}

	public String getDescription()
	{
		return Hub.string("TD_DESC");
	}

	public String getLicense()
	{
		return Hub.string("TD_LICENSE");
	}

	public String getName()
	{
		return Hub.string("TD_SHORT");
	}

	public String getVersion()
	{
		return Hub.string("TD_VER");
	}

	public void initialize() throws PluginInitException
	{
		// Resources
		Hub.addResouceBundle(ResourceBundle.getBundle("templates"));

		// Models
		ModelManager.instance().registerModel(TemplateDesign.myDescriptor);

		// Toolsets
		ToolsetManager.instance().registerToolset(TemplateModel.class,
				new TemplateToolset());

		// IO
		TemplateFileIO ioPlugin = new TemplateFileIO();
		IOPluginManager.instance().registerDataSaver(ioPlugin,
				TemplateModel.class);
		IOPluginManager.instance().registerDataLoader(ioPlugin,
				ioPlugin.getIOTypeDescriptor());
		IOPluginManager.instance().registerMetaSaver(ioPlugin,TemplateModel.class);
		for(String tag:ioPlugin.getMetaTags())
		{
			IOPluginManager.instance().registerMetaLoader(ioPlugin,ioPlugin.getIOTypeDescriptor(),tag);
		}
	}

	public void unload()
	{
	}

}

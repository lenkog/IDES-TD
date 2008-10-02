package templates.plugin;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.Plugin;
import ides.api.plugin.PluginInitException;
import ides.api.plugin.io.IOPluginManager;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.OperationManager;
import ides.api.plugin.presentation.ToolsetManager;

import java.util.ResourceBundle;

import templates.io.JPEGExporter;
import templates.io.PNGExporter;
import templates.io.TemplateFileIO;
import templates.library.TemplateMetaIO;
import templates.model.TemplateModel;
import templates.model.v3.TemplateDesign;
import templates.operations.CentralizedSupSolution;
import templates.operations.ChannelSup;
import templates.operations.ModularSupSolution;
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
		IOPluginManager.instance().registerMetaSaver(ioPlugin,
				TemplateModel.class);
		for (String tag : ioPlugin.getMetaTags())
		{
			IOPluginManager.instance().registerMetaLoader(ioPlugin,
					ioPlugin.getIOTypeDescriptor(),
					tag);
		}
		//registers meta loader/saver used by Template Library
		//this will not impact loading/saving of FSA models not related to the library
		TemplateMetaIO ioLibrary=new TemplateMetaIO();
		for (String tag : ioLibrary.getMetaTags())
		{
			IOPluginManager.instance().registerMetaLoader(ioLibrary,
					"FSA",tag);
		}
		IOPluginManager.instance().registerMetaSaver(ioLibrary,FSAModel.class);
		//export
		IOPluginManager.instance().registerExport(new JPEGExporter(),TemplateModel.class);
		IOPluginManager.instance().registerExport(new PNGExporter(),TemplateModel.class);

		// Operations
		OperationManager.instance().register(new ChannelSup());
		OperationManager.instance().register(new ModularSupSolution());
		OperationManager.instance().register(new CentralizedSupSolution());
	}

	public void unload()
	{
	}

}

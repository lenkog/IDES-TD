package templates.library;

import ides.api.core.Annotable;
import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.utilities.GeneralUtils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TemplateLibrary
{
	Map<String,Template> templates=new HashMap<String,Template>();
	Map<Template,File> files=new HashMap<Template,File>();
	File dir;
	
	public TemplateLibrary(File dir)
	{
		this.dir=dir;
		String errors="";
		for(File file:dir.listFiles())
		{
			FSAModel model=null;
			try
			{
				model=loadTemplateModel(file);
			}catch(FileLoadException e)
			{
				model = (FSAModel)e.getPartialModel();
				errors+=e.getMessage();
			}
			if(model==null)
			{
				continue;
			}
			TemplateDescriptor td=(TemplateDescriptor)model.getAnnotation(Template.TEMPLATE_DESC);
			model.setName(td.tag);
			model.modelSaved();
			Template template=new FSATemplate(td,model);
			templates.put(template.getName(),template);
			files.put(template,file);
		}
		if(!"".equals(errors))
		{
			Hub.getNoticeManager().postErrorTemporary(Hub.string("TD_problemLoadingTemplate"),GeneralUtils.truncateMessage(errors));
		}
	}
	
	public Collection<Template> getTemplates()
	{
		return new HashSet<Template>(templates.values());
	}
	
	public Template getTemplate(String name)
	{
		return templates.get(name);
	}
	
	public void addTemplate(TemplateDescriptor td, FSAModel model) throws IOException
	{
		model.setAnnotation(Template.TEMPLATE_DESC,td);
		StringBuffer fileName=new StringBuffer("");
		for (Character ch : td.tag.toCharArray())
		{
			if (Character.isLetterOrDigit(ch))
			{
				fileName.append(ch);
			}
		}
		File file=new File(dir.getAbsolutePath()+File.separator+fileName.toString()+"."+Hub.getIOSubsystem().MODEL_FILE_EXT);
		if(fileName.length()==0||file.exists())
		{
			int idx=0;
			do
			{
				file=new File(dir.getAbsolutePath()+File.separator+fileName.toString()+idx+"."+Hub.getIOSubsystem().MODEL_FILE_EXT);
				idx++;
			}
			while(file.exists());
		}
		Hub.getIOSubsystem().save(model,file);
		model.setAnnotation(Annotable.FILE,file);
		model.setName(td.tag);
		model.modelSaved();
		Template template=new FSATemplate(td,model);		
		templates.put(template.getName(),template);
		files.put(template,file);
		fireCollectionChanged();
	}
	
	public void removeTemplate(String name) throws IOException
	{
		Template template=getTemplate(name);
		if(template!=null)
		{
			File file=files.get(template);
			if(file!=null)
			{
				file.delete();
				files.remove(template);
			}
			templates.remove(name);
			fireCollectionChanged();
		}
	}
	
	public void reloadTemplate(String name) throws IOException
	{
		Template oldTemplate=getTemplate(name);
		if(oldTemplate==null)
		{
			return;
		}
		File file=files.get(oldTemplate);
		templates.remove(oldTemplate.getName());
		files.remove(oldTemplate);
		fireCollectionChanged();
		String errors="";
		FSAModel model=null;
		try
		{
			model=loadTemplateModel(file);
		}catch(FileLoadException e)
		{
			if(e.getPartialModel()==null)
			{
				throw new IOException(e.getMessage());
			}
			else
			{
				model=(FSAModel)e.getPartialModel();
				errors=e.getMessage();
			}
		}
		Template newTemplate=new FSATemplate((TemplateDescriptor)model.getAnnotation(Template.TEMPLATE_DESC),model);
		templates.put(newTemplate.getName(),newTemplate);
		files.put(newTemplate,file);
		fireCollectionChanged();
		if(!"".equals(errors))
		{
			throw new IOException(errors);
		}
	}
	
	protected FSAModel loadTemplateModel(File file) throws FileLoadException
	{
		String errors="";
		DESModel model=null;
		try
		{
			model=Hub.getIOSubsystem().load(file);
		}catch(IOException e)
		{
			if (e instanceof FileLoadException)
			{
				model = ((FileLoadException)e).getPartialModel();
			}
			errors+=Hub.string("TD_cantLoadTemplate")+" "+file.getAbsolutePath()+" ["+e.getMessage()+"]\n";
		}
		if(model==null)
		{
		}
		else if(!model.hasAnnotation(Template.TEMPLATE_DESC))
		{
			errors+=Hub.string("TD_cantLoadTemplate")+" "+file.getAbsolutePath()+" ["+Hub.string("TD_missingTemplateInfo")+"]\n";
			model=null;
		}
		else if(!(model instanceof FSAModel))
		{
			errors+=Hub.string("TD_cantLoadTemplate")+" "+file.getAbsolutePath()+" ["+Hub.string("TD_nonFSATemplate")+"]\n";
			model=null;
		}
		if(model==null||!"".equals(errors))
		{
			throw new FileLoadException(errors,model);
		}
		return (FSAModel)model;
	}
	
	protected void fireCollectionChanged()
	{
		for(TemplateLibraryListener listener:getTemplateLibraryListeners())
		{
			listener.templateCollectionChanged(this);
		}
	}
	
	private ArrayList<TemplateLibraryListener> listeners = new ArrayList<TemplateLibraryListener>();

	/**
	 * Attaches the given subscriber to this publisher. The given subscriber
	 * will receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void addListener(TemplateLibraryListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Removes the given subscriber to this publisher. The given subscriber will
	 * no longer receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void removeListener(TemplateLibraryListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Returns all current subscribers to this publisher.
	 * 
	 * @return all current subscribers to this publisher
	 */
	public TemplateLibraryListener[] getTemplateLibraryListeners()
	{
		return listeners.toArray(new TemplateLibraryListener[] {});
	}

}

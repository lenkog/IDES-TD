package templates.io;

import ides.api.core.Hub;
import ides.api.plugin.io.FileIOPlugin;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.io.FileSaveException;
import ides.api.plugin.io.IOSubsytem;
import ides.api.plugin.model.DESModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import templates.model.TemplateComponent;
import templates.model.TemplateModel;

public class TemplateFileIO implements FileIOPlugin
{
	protected static final String TYPE = "TemplateDesign";

	protected static final String META = "layout";

	protected static final String VERSION = "3";

	protected static final String FILE = "templateComponentFile";

	protected static final String LAST_SAVE_FILE = "templateLastSaveFile";

	public String getIOTypeDescriptor()
	{
		return TYPE;
	}

	public Set<String> getMetaTags()
	{
		Set<String> tags = new HashSet<String>();
		tags.add(META);
		return tags;
	}

	protected String componentFile(File parentFile, TemplateComponent component)
	{
		StringBuffer name = new StringBuffer();
		if (component.hasModel())
		{
			name.append("_");
			for (Character ch : component
					.getModel().getName().substring(3).toCharArray())
			{
				if (Character.isLetterOrDigit(ch))
				{
					name.append(ch);
				}
			}
		}
		String parentName = parentFile.getName();
		if (parentName.endsWith("." + IOSubsytem.MODEL_FILE_EXT))
		{
			parentName = parentName.substring(0, parentName.length()
					- IOSubsytem.MODEL_FILE_EXT.length() - 1);
		}
		return parentName + "_" + component.getId() + name + "."
				+ IOSubsytem.MODEL_FILE_EXT;
	}

	public void saveData(PrintStream stream, DESModel model, String file)
			throws FileSaveException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new FileSaveException(Hub.string("TD_ioWrongModelType"));
		}
		TemplateModel td = (TemplateModel)model;
		File outFile = new File(file);
		boolean savingToNewFile = !td.hasAnnotation(LAST_SAVE_FILE)
				|| !outFile.equals(td.getAnnotation(LAST_SAVE_FILE));
		Map<TemplateComponent, File> fileMap = new HashMap<TemplateComponent, File>();
		Set<File> overwritten = new TreeSet<File>();
		for (TemplateComponent component : td.getComponents())
		{
			if (component.hasModel())
			{
				fileMap.put(component, new File(outFile
						.getParentFile().getAbsolutePath()
						+ File.separator + componentFile(outFile, component)));
				if (!component.getModel().hasAnnotation(FILE)
						|| !fileMap.get(component).equals(component
								.getModel().getAnnotation(FILE)))
				{
					if (fileMap.get(component).exists())
					{
						overwritten.add(fileMap.get(component));
					}
				}
			}
		}
		if (!overwritten.isEmpty())
		{
			String message = Hub.string("TD_ioWarnOverwrite1") + "\n";
			for (File overfile : overwritten)
			{
				message += overfile.getAbsolutePath() + "\n";
			}
			message += Hub.string("TD_ioWarnOverwrite2") + "\n"
					+ Hub.string("TD_ioWarnOverwrite3");
			int choice = JOptionPane.showConfirmDialog(null,
					message,
					Hub.string("TD_ioWarnOverwriteTitle"),
					JOptionPane.YES_NO_OPTION);
			if (choice != JOptionPane.YES_OPTION)
			{
				throw new FileSaveException(Hub.string("TD_ioDontOverwrite"));
			}
		}
		Set<File> filesToErase = new HashSet<File>();
		try
		{
			for (TemplateComponent component : fileMap.keySet())
			{
				Hub.getIOSubsystem().save(component.getModel(),
						fileMap.get(component));
				if (!savingToNewFile
						&& component.getModel().hasAnnotation(FILE)
						&& !fileMap.get(component).equals(component
								.getModel().getAnnotation(FILE)))
				{
					filesToErase.add((File)component
							.getModel().getAnnotation(FILE));
				}
				component
						.getModel().setAnnotation(FILE, fileMap.get(component));
			}
		}
		catch (IOException e)
		{
			throw new FileSaveException(e);
		}
		td.setAnnotation(LAST_SAVE_FILE, outFile);
		for (File f : filesToErase)
		{
			f.delete();
		}
		// try
		// {
		//			
		// }catch(IOException e)
		// {
		// throw new FileSaveException(e);
		// }
	}

	public void saveMeta(PrintStream arg0, DESModel arg1, String arg2)
			throws FileSaveException
	{
		// TODO Auto-generated method stub

	}

	public String getSaveDataVersion()
	{
		return VERSION;
	}

	public String getSaveMetaVersion(String arg0)
	{
		if (META.equals(arg0))
		{
			return VERSION;
		}
		return "";
	}

	public DESModel loadData(String arg0, InputStream arg1, String file)
			throws FileLoadException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void loadMeta(String arg0, InputStream arg1, DESModel arg2,
			String arg3) throws FileLoadException
	{
		// TODO Auto-generated method stub

	}

}

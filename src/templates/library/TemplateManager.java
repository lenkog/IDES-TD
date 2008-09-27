package templates.library;

import java.io.File;

public class TemplateManager
{
	private static TemplateManager me = null;
	
	protected TemplateLibrary localLib;
	protected TemplateLibrary sharedLib;

	private TemplateManager()
	{
		File local=new File("templates");
		if(!local.exists())
		{
			local.mkdir();
		}
		localLib=new TemplateLibrary(local);
	}

	public static TemplateManager instance()
	{
		if (me == null)
		{
			me = new TemplateManager();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	public TemplateLibrary getMainLibrary()
	{
		return localLib;
	}
}

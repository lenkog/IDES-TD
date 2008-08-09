package templates.library;

public class TemplateManager
{

	private static TemplateManager me = null;

	private TemplateManager()
	{
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
		return new TemplateLibrary();
	}
}

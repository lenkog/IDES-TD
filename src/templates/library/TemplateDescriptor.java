package templates.library;

import java.awt.Color;

public class TemplateDescriptor
{
	public String tag="TAG";
	public Color color=Color.WHITE;
	public String description="";
	
	public static String shortDescription(String d)
	{
		if(d==null)
		{
			return null;
		}
		String[] lines=d.split("\n");
		if(lines.length==0)
		{
			return "";
		}
		return lines[0].length()>50?lines[0].substring(0,47)+"...":lines[0];
	}
}

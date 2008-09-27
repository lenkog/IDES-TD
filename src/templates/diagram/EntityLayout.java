package templates.diagram;

import java.awt.Color;
import java.awt.Point;

public class EntityLayout
{
	public Point location;

	public String label;

	/**
	 * True if entity is a template instance, and was modified.
	 */
	public boolean flag;

	public Color color;

	public String tag;

	public EntityLayout()
	{
		location = new Point();
		label = "";
		flag = false;
		color = null;
		tag = "";
	}
}

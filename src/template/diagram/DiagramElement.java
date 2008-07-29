package template.diagram;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public abstract class DiagramElement
{
	protected static FontMetrics globalFontMetrics = null;

	public static void setGlobalFontMetrics(FontMetrics fm)
	{
		globalFontMetrics = fm;
	}

	public static FontMetrics getGlobalFontMetrics()
	{
		return globalFontMetrics;
	}

	protected Point location;

	public Point getLocation()
	{
		return location;
	}

	public void setLocation(Point location)
	{
		this.location = location;
	}

	public void translate(Point delta)
	{
		location.x += delta.x;
		location.y += delta.y;
	}

	public abstract Rectangle getBounds();

	public abstract void draw(Graphics2D g2d);

}

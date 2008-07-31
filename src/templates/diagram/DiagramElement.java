package templates.diagram;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public abstract class DiagramElement
{
	protected static FontMetrics globalFontMetrics = null;
	
	protected static Font globalFont = null;

	public static void setGlobalFontMetrics(FontMetrics fm)
	{
		globalFontMetrics = fm;
	}

	public static FontMetrics getGlobalFontMetrics()
	{
		return globalFontMetrics;
	}

	public static void setGlobalFont(Font f)
	{
		globalFont = f;
	}

	public static Font getGlobalFont()
	{
		return globalFont;
	}

	protected DiagramElementLayout layout;

	public Point getLocation()
	{
		return layout.location;
	}

	public void setLocation(Point location)
	{
		layout.location = location;
	}

	public void translate(Point delta)
	{
		layout.location.x += delta.x;
		layout.location.y += delta.y;
	}

	public abstract Rectangle getBounds();

	public abstract void draw(Graphics2D g2d);
	
	public abstract boolean contains(Point p);

}

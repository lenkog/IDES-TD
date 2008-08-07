package templates.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

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

	protected static final Color COLOR_NORM = Color.BLACK;

	// protected static final Color COLOR_HILITE=Color.BLUE;
	protected static final Color COLOR_SELECT = Color.RED;

	// protected static final Color COLOR_HILITESELECT=Color.MAGENTA;

	protected static final Stroke LINE_STROKE = new BasicStroke(1);
	protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);
	protected static final Stroke MARKER_STROKE = new BasicStroke(
			1,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10f,
			new float[] { 1, 2 },
			0f);

	protected DiagramElementLayout layout;

	protected boolean highlight = false;

	protected boolean selected = false;

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

	public void setHighlight(boolean b)
	{
		highlight = b;
	}

	public void setSelected(boolean b)
	{
		selected = b;
	}

	public abstract Rectangle getBounds();

	public abstract void draw(Graphics2D g2d);

	public abstract boolean contains(Point p);

	public abstract boolean intersects(Rectangle r);
}

package templates.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

public abstract class DiagramElement
{
	protected static Graphics globalFontRenderer = null;

	protected static Font globalFont = null;

	public static void setGlobalFontRenderer(Graphics g)
	{
		globalFontRenderer = g;
	}

	public static Graphics getGlobalFontRenderer()
	{
		return globalFontRenderer;
	}

	public static FontMetrics getGlobalFontMetrics()
	{
		if (globalFontRenderer == null || globalFont == null)
		{
			return null;
		}
		return globalFontRenderer.getFontMetrics(globalFont);
	}

	public static void setGlobalFont(Font f)
	{
		globalFont = f;
	}

	public static Font getGlobalFont()
	{
		return globalFont;
	}

	public static final Color COLOR_NORM = Color.BLACK;

	public static final Color COLOR_INCONSIST = new Color(255,120,0);

	public static final Color COLOR_SELECT = new Color(70,100,140);

	public static final Color COLOR_SELECT_INCONSIST = Color.ORANGE;

	protected static final Stroke LINE_STROKE = new BasicStroke(1);

	protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);

	protected static final Stroke MARKER_STROKE = new BasicStroke(
			1,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10f,
			new float[] { 1, 2 },
			0f);

	protected boolean highlight = false;

	protected boolean selected = false;

	protected boolean inconsistent = false;

	public abstract void translate(Point delta);

	public void setHighlight(boolean b)
	{
		highlight = b;
	}

	public void setSelected(boolean b)
	{
		selected = b;
	}

	public void setInconsistent(boolean b)
	{
		inconsistent = b;
	}

	public abstract Rectangle getBounds();

	public abstract void draw(Graphics2D g2d);

	public abstract void draw(Graphics2D g2d, boolean showInconsistency);

	public abstract boolean contains(Point p);

	public abstract boolean intersects(Rectangle r);
}

package templates.diagram;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

public class SimpleIcon implements Icon
{
	protected static final int BOX_DISTANCE = 20;

	public boolean selected = false;

	public int getIconHeight()
	{
		return BOX_DISTANCE;
	}

	public int getIconWidth()
	{
		return BOX_DISTANCE;
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D)g;
		if (selected)
		{
			g2d.setColor(DiagramElement.COLOR_SELECT);
		}
		else
		{
			g2d.setColor(DiagramElement.COLOR_NORM);
		}
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(DiagramElement.FAT_LINE_STROKE);
		g2d.drawRect(x, y, BOX_DISTANCE, BOX_DISTANCE);
		g2d.setStroke(oldStroke);
	}

	public SimpleIcon clone()
	{
		return new SimpleIcon();
	}
}

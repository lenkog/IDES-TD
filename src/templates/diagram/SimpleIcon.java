package templates.diagram;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

public class SimpleIcon implements Icon
{
	protected static final int BOX_DISTANCE = 20;

	protected boolean isModule;

	public SimpleIcon(boolean isModule)
	{
		this.isModule = isModule;
	}

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
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(DiagramElement.FAT_LINE_STROKE);
		Color oldColor=g2d.getColor();
		g2d.setColor(Color.WHITE);
		if (isModule)
		{
			g2d.fillRect(x,y,BOX_DISTANCE,BOX_DISTANCE);
			g2d.setColor(oldColor);
			g2d.drawRect(x, y, BOX_DISTANCE, BOX_DISTANCE);
		}
		else
		{
			g2d.fillOval(x,y,BOX_DISTANCE,BOX_DISTANCE);
			g2d.setColor(oldColor);
			g2d.drawOval(x, y, BOX_DISTANCE, BOX_DISTANCE);
		}
		g2d.setStroke(oldStroke);
	}

	public SimpleIcon clone()
	{
		return new SimpleIcon(isModule);
	}
}

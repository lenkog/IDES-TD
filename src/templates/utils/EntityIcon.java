package templates.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public interface EntityIcon extends Icon
{
	public int BOX_DISTANCE = 20;
	public EntityIcon clone();
	public void setIsModule(boolean b);
	public void paintIcon(Component arg0, Graphics g, int x, int y, Color color);
	public void setColor(Color color);
	public Color getColor();
	public void setFlagged(boolean b);
	public boolean isFlagged();
	public String getTag();
}

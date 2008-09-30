package templates.library;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

import templates.diagram.DiagramElement;
import templates.utils.EntityIcon;

public class TemplateIcon implements EntityIcon
{
	protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);
	protected static final int SPACING=2;
	
	protected Color bgColor;
	protected String tag;
	protected int w;
	protected int h;
	protected int deltaY;
	protected int deltaX;
	protected boolean isModule=true;
	
	public TemplateIcon(String tag,Color color,Graphics context)
	{
		this.tag=tag;
		this.bgColor=color;
		w=Math.max(context.getFontMetrics().stringWidth(tag)+2*SPACING,BOX_DISTANCE);
		h=Math.max(context.getFontMetrics().getHeight()+2*SPACING,BOX_DISTANCE);
		deltaY=context.getFontMetrics().getAscent();
		deltaX=(w-context.getFontMetrics().stringWidth(tag))/2;
	}
	
	protected TemplateIcon(String tag,Color color,int width,int height,int deltaX,int deltaY,boolean isModule)
	{
		this.tag=tag;
		this.bgColor=color;
		w=width;
		h=height;
		this.deltaY=deltaY;
		this.deltaX=deltaX;
		this.isModule=isModule;
	}

	public int getIconHeight()
	{
		return h;
	}

	public int getIconWidth()
	{
		return w;
	}

	public void paintIcon(Component arg0, Graphics g, int x, int y)
	{
		paintIcon(arg0,g,x,y,Color.BLACK);
	}
	
	public void paintIcon(Component arg0, Graphics g, int x, int y, Color color)
	{
		Graphics2D g2d = (Graphics2D)g;
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(FAT_LINE_STROKE);
		Color oldColor=g2d.getColor();
		g2d.setColor(bgColor);
		if (isModule)
		{
			g2d.fillRect(x,y,w,h);
			g2d.setColor(color);
			g2d.drawRect(x, y, w, h);
		}
		else
		{
			g2d.fillOval(x,y,w,h);
			g2d.setColor(color);
			g2d.drawOval(x, y,w, h);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(tag,x+SPACING+deltaX,y+SPACING+deltaY);
		g2d.setColor(oldColor);
		g2d.setStroke(oldStroke);
	}
	
	public TemplateIcon clone()
	{
		return new TemplateIcon(tag,bgColor,w,h,deltaX,deltaY,isModule);
	}

	public void setIsModule(boolean b)
	{
		isModule=b;
	}
	
	public Color getBackground()
	{
		return bgColor;
	}
}

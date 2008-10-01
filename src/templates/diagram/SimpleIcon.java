package templates.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import templates.utils.EntityIcon;

public class SimpleIcon implements EntityIcon
{
	protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);
	protected static final int SPACING=2;
	
	protected Color bgColor=Color.WHITE;
	protected String tag="";
	protected String flaggedTag="";
	protected int w=EntityIcon.BOX_DISTANCE;
	protected int flaggedW=EntityIcon.BOX_DISTANCE;
	protected int h=EntityIcon.BOX_DISTANCE;
	protected int flaggedH=EntityIcon.BOX_DISTANCE;
	protected int deltaY=0;
	protected int flaggedDeltaY=0;
	protected int deltaX=0;
	protected int flaggedDeltaX=0;
	protected boolean isModule=true;
	protected boolean flag=false;
	
	public SimpleIcon(String tag,Color color,Graphics context)
	{
		this.tag=tag;
		flaggedTag="".equals(tag)?"":tag+"^";
		this.bgColor=color;
		w=Math.max(context.getFontMetrics().stringWidth(tag)+2*SPACING,BOX_DISTANCE);
		h=Math.max(context.getFontMetrics().getHeight()+2*SPACING,BOX_DISTANCE);
		deltaY=context.getFontMetrics().getAscent();
		deltaX=(w-context.getFontMetrics().stringWidth(tag))/2;
		flaggedW=Math.max(context.getFontMetrics().stringWidth(flaggedTag)+2*SPACING,BOX_DISTANCE);
		flaggedH=h;
		flaggedDeltaY=deltaY;
		flaggedDeltaX=(w-context.getFontMetrics().stringWidth(flaggedTag))/2;
	}
	
	public SimpleIcon()
	{
	}

	public int getIconHeight()
	{
		return flag?flaggedH:h;
	}

	public int getIconWidth()
	{
		return flag?flaggedW:w;
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
			g2d.fillRect(x,y,flag?flaggedW:w,flag?flaggedH:h);
			g2d.setColor(color);
			g2d.drawRect(x, y, flag?flaggedW:w, flag?flaggedH:h);
		}
		else
		{
			g2d.fillOval(x,y,flag?flaggedW:w,flag?flaggedH:h);
			g2d.setColor(color);
			g2d.drawOval(x, y,flag?flaggedW:w, flag?flaggedH:h);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(flag?flaggedTag:tag,x+SPACING+(flag?flaggedDeltaX:deltaX),y+SPACING+(flag?flaggedDeltaY:deltaY));
		g2d.setColor(oldColor);
		g2d.setStroke(oldStroke);
	}
	
	public SimpleIcon clone()
	{
		SimpleIcon icon=new SimpleIcon();
		icon.isModule=isModule;
		icon.bgColor=bgColor;
		icon.tag=tag;
		icon.w=w;
		icon.h=h;
		icon.deltaY=deltaY;
		icon.deltaX=deltaX;
		icon.flag=flag;
		icon.flaggedTag=flaggedTag;
		icon.flaggedW=flaggedW;
		icon.flaggedH=flaggedH;
		icon.flaggedDeltaY=flaggedDeltaY;
		icon.flaggedDeltaX=flaggedDeltaX;
		return icon;
	}

	public void setIsModule(boolean b)
	{
		isModule=b;
	}
	
	public Color getColor()
	{
		return bgColor;
	}

	public boolean isFlagged()
	{
		return flag;
	}

	public void setColor(Color color)
	{
		bgColor=color;
	}

	public void setFlagged(boolean b)
	{
		flag=b;
	}

	public String getTag()
	{
		return tag;
	}
}

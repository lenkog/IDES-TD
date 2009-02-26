package templates.diagram;

import ides.api.core.Annotable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.Vector;

import templates.model.TemplateComponent;
import templates.utils.EntityIcon;

public class Entity extends DiagramElement
{
	/**
	 * Denotes if the FSA model for an entity has been modified after being
	 * assigned to the entity.
	 */
	public static final String FLAG_MARK = "templates.diagram.Entity.flag";

	protected class LabelBox extends Rectangle
	{
		private static final long serialVersionUID = -3359948297694123889L;

		protected Vector<String> lines = new Vector<String>();

		public LabelBox(Vector<String> lines)
		{
			this.lines.addAll(lines);
			int maxWidth = 0;
			for (String line : lines)
			{
				maxWidth = Math.max(maxWidth, getGlobalFontMetrics()
						.stringWidth(line));
			}
			width = maxWidth;
			if (lines.size() == 1 && lines.firstElement().trim().length() == 0)
			{
				height = 0;
			}
			else
			{
				height = lines.size() * getGlobalFontMetrics().getHeight();
			}
		}

		public void draw(Graphics2D g2d)
		{
			for (int i = 0; i < lines.size(); ++i)
			{
				int deltaX = (width - getGlobalFontMetrics().stringWidth(lines
						.elementAt(i))) / 2;
				int deltaY = getGlobalFontMetrics().getHeight() * (i + 1)
						- getGlobalFontMetrics().getDescent();
				g2d.drawString(lines.elementAt(i), x + deltaX + 1, y + deltaY);
			}
		}
	}

	protected static final int LABEL_SPACING = 5;

	public static final int PORT_RADIUS = 5;

	private static final int HALO_RADIUS = 8;

	public final static int ON_NADA = 0;

	public final static int ON_ICON = 1;

	public final static int ON_LABEL = 2;

	public final static int ON_PORT = 4;

	public final static int ON_SUP = 8;

	protected TemplateComponent component;

	protected EntityLayout layout;

	private Rectangle bounds;

	private EntityIcon icon = new SimpleIcon();

	private LabelBox labelBox;

	private Ellipse2D[] ports = new Ellipse2D[4];

	private Ellipse2D supHalo;

	private static final String HALO_LABEL = "S";

	private int haloDX;

	private int haloDY;

	public Entity(TemplateComponent component) throws MissingLayoutException
	{
		if (!component.hasAnnotation(Annotable.LAYOUT)
				|| !(component.getAnnotation(Annotable.LAYOUT) instanceof EntityLayout))
		{
			throw new MissingLayoutException();
		}
		this.component = component;
		layout = (EntityLayout)component.getAnnotation(Annotable.LAYOUT);
		update();
	}

	public Entity(TemplateComponent component, EntityLayout layout)
	{
		this.component = component;
		this.layout = layout;
		component.setAnnotation(Annotable.LAYOUT, layout);
		update();
	}

	protected void computeBounds()
	{
		bounds = new Rectangle(
				layout.location.x - icon.getIconWidth() / 2,
				layout.location.y - icon.getIconHeight() / 2,
				icon.getIconWidth(),
				icon.getIconHeight())
				.union(labelBox)
				.union(ports[0].getBounds())
				.union(ports[1].getBounds())
				.union(ports[2].getBounds())
				.union(component.getType() == TemplateComponent.TYPE_CHANNEL ? ports[3]
						.getBounds().union(supHalo.getBounds())
						: ports[3].getBounds());
	}

	public TemplateComponent getComponent()
	{
		return component;
	}

	public void draw(Graphics2D g2d)
	{
		draw(g2d, false);
	}

	@Override
	public void draw(Graphics2D g2d, boolean showInconsistency)
	{
		if (selected)
		{
			if (showInconsistency && inconsistent)
			{
				g2d.setColor(COLOR_SELECT_INCONSIST);
			}
			else
			{
				g2d.setColor(COLOR_SELECT);
			}
		}
		else
		{
			if (showInconsistency && inconsistent)
			{
				g2d.setColor(COLOR_INCONSIST);
			}
			else
			{
				g2d.setColor(COLOR_NORM);
			}
		}
		if (highlight)
		{
			Color temp = g2d.getColor();
			g2d.setColor(Color.WHITE);
			g2d.fillRect(labelBox.x - 1,
					labelBox.y - 1,
					labelBox.width + 2,
					labelBox.height + 2);
			g2d.setColor(temp);
		}
		drawCore(g2d);
		if (highlight)
		{
			if (component.getType() == TemplateComponent.TYPE_CHANNEL)
			{
				g2d.draw(supHalo);
				g2d.drawString(HALO_LABEL,
						(int)supHalo.getCenterX() + haloDX,
						(int)supHalo.getCenterY() + haloDY);
			}
			g2d.setStroke(MARKER_STROKE);
			g2d.drawRect(labelBox.x - 1,
					labelBox.y - 1,
					labelBox.width + 2,
					labelBox.height + 2);
			g2d.drawOval((int)ports[0].getMinX(),
					(int)ports[0].getMinY(),
					(int)ports[0].getWidth(),
					(int)ports[0].getHeight());
			g2d.drawOval((int)ports[1].getMinX(),
					(int)ports[1].getMinY(),
					(int)ports[1].getWidth(),
					(int)ports[1].getHeight());
			g2d.drawOval((int)ports[2].getMinX(),
					(int)ports[2].getMinY(),
					(int)ports[2].getWidth(),
					(int)ports[2].getHeight());
			g2d.drawOval((int)ports[3].getMinX(),
					(int)ports[3].getMinY(),
					(int)ports[3].getWidth(),
					(int)ports[3].getHeight());
		}
	}

	public void drawPlain(Graphics2D g2d)
	{
		g2d.setColor(COLOR_NORM);
		drawCore(g2d);
	}

	private void drawCore(Graphics2D g2d)
	{
		icon.paintIcon(null,
				g2d,
				layout.location.x - icon.getIconWidth() / 2,
				layout.location.y - icon.getIconHeight() / 2,
				g2d.getColor());
		labelBox.draw(g2d);
	}

	public Point getLocation()
	{
		return layout.location;
	}

	public void setLocation(Point location)
	{
		layout.location = location;
		update();
	}

	public void translate(Point delta)
	{
		layout.location.x += delta.x;
		layout.location.y += delta.y;
		update();
	}

	public void update()
	{
		icon = new SimpleIcon(layout.tag, layout.color, DiagramElement
				.getGlobalFontRenderer());
		icon.setIsModule(component.getType() != TemplateComponent.TYPE_CHANNEL);
		icon.setFlagged(component.hasModel()
				&& component.getModel().hasAnnotation(FLAG_MARK));
		int maxWidth = 3 * icon.getIconWidth();
		String[] words = layout.label.split(" ");
		Vector<String> lines = new Vector<String>();
		int wordsConsumed = 0;
		while (wordsConsumed < words.length)
		{
			String line = "";
			int i;
			for (i = wordsConsumed; i < words.length; ++i)
			{
				line += words[i];
				if (getGlobalFontMetrics().stringWidth(line) > maxWidth)
				{
					break;
				}
				line += " ";
			}
			if (i == words.length) // all left-over words fit
			{
				line = line.substring(0, line.length() - 1);
				lines.add(line);
				wordsConsumed = words.length;
			}
			else
			// some left-over words didn't fit
			{
				if (i == wordsConsumed) // the first left-over word was too long
				{
					lines.add(line);
					wordsConsumed++;
				}
				else
				// at least one left-over word fit
				{
					line = line.substring(0, line.lastIndexOf(" "));
					lines.add(line);
					wordsConsumed = i;
				}
			}
		}
		labelBox = new LabelBox(lines);
		int deltaX = -(labelBox.width / 2);
		int deltaY = icon.getIconHeight() / 2 + LABEL_SPACING;
		labelBox.x = layout.location.x + deltaX;
		labelBox.y = layout.location.y + deltaY;
		ports[0] = new Ellipse2D.Float(
				layout.location.x - icon.getIconWidth() / 2 - 2 * PORT_RADIUS
						- 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[1] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				layout.location.y - icon.getIconHeight() / 2 - 2 * PORT_RADIUS
						- 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[2] = new Ellipse2D.Float(
				layout.location.x + icon.getIconWidth() / 2 + 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[3] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				(int)labelBox.getMaxY() + 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		if (component.getType() == TemplateComponent.TYPE_CHANNEL)
		{
			supHalo = new Ellipse2D.Float(
					layout.location.x + icon.getIconWidth() / 4,
					layout.location.y - icon.getIconHeight() / 4 - 2
							* HALO_RADIUS,
					2 * HALO_RADIUS,
					2 * HALO_RADIUS);
			haloDX = -getGlobalFontMetrics().stringWidth(HALO_LABEL) / 2 + 1;
			haloDY = getGlobalFontMetrics().getAscent() / 2;
		}
		computeBounds();
	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	@Override
	public boolean contains(Point p)
	{
		return labelBox.contains(p)
				|| ports[0].contains(p)
				|| ports[1].contains(p)
				|| ports[2].contains(p)
				|| ports[3].contains(p)
				|| (p.x >= layout.location.x - icon.getIconWidth() / 2
						&& p.x <= layout.location.x + icon.getIconWidth() / 2
						&& p.y >= layout.location.y - icon.getIconHeight() / 2 && p.y <= layout.location.y
						+ icon.getIconHeight() / 2)
				|| (component.getType() == TemplateComponent.TYPE_CHANNEL && supHalo
						.contains(p));
	}

	public boolean intersects(Rectangle r)
	{
		return labelBox.intersects(r)
				|| ports[0].intersects(r)
				|| ports[1].intersects(r)
				|| ports[2].intersects(r)
				|| ports[3].intersects(r)
				|| new Rectangle(
						layout.location.x - icon.getIconWidth() / 2,
						layout.location.y - icon.getIconHeight() / 2,
						icon.getIconWidth(),
						icon.getIconHeight()).intersects(r)
				|| (component.getType() == TemplateComponent.TYPE_CHANNEL && supHalo
						.intersects(r));
	}

	public int whereisPoint(Point p)
	{
		if (labelBox.contains(p))
		{
			return ON_LABEL;
		}
		if (ports[0].contains(p) || ports[1].contains(p)
				|| ports[2].contains(p) || ports[3].contains(p))
		{
			return ON_PORT;
		}
		if (component.getType() == TemplateComponent.TYPE_CHANNEL
				&& supHalo.contains(p))
		{
			return ON_SUP;
		}
		if (bounds.contains(p))
		{
			return ON_ICON;
		}
		return ON_NADA;
	}

	public String getLabel()
	{
		return layout.label;
	}

	public void setLabel(String label)
	{
		if (label == null)
		{
			label = "";
		}
		if (layout.label.equals(label))
		{
			return;
		}
		layout.label = label;
		update();
	}

	public Point[] getPorts()
	{
		return new Point[] {
				new Point((int)ports[0].getCenterX(), (int)ports[0]
						.getCenterY()),
				new Point((int)ports[1].getCenterX(), (int)ports[1]
						.getCenterY()),
				new Point((int)ports[2].getCenterX(), (int)ports[2]
						.getCenterY()),
				new Point((int)ports[3].getCenterX(), (int)ports[3]
						.getCenterY()) };
	}

	public EntityIcon getIcon()
	{
		return icon;
	}

	public void setIcon(EntityIcon icon)
	{
		layout.color = icon.getColor();
		layout.tag = icon.getTag();
		update();
	}
}

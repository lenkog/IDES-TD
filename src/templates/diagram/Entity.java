package templates.diagram;

import ides.api.core.Annotable;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.Vector;

import templates.model.TemplateComponent;

public class Entity extends DiagramElement
{
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
				maxWidth = Math.max(maxWidth, globalFontMetrics
						.stringWidth(line));
			}
			width = maxWidth;
			if (lines.size() == 1 && lines.firstElement().trim().length() == 0)
			{
				height = 0;
			}
			else
			{
				height = lines.size() * globalFontMetrics.getHeight();
			}
		}

		public void draw(Graphics2D g2d)
		{
			for (int i = 0; i < lines.size(); ++i)
			{
				int deltaX = (width - globalFontMetrics.stringWidth(lines
						.elementAt(i))) / 2;
				int deltaY = globalFontMetrics.getHeight() * (i + 1)
						- globalFontMetrics.getDescent();
				g2d.drawString(lines.elementAt(i), x + deltaX + 1, y + deltaY);
			}
		}
	}

	protected static final int LABEL_SPACING = 5;

	protected static final int BOX_DISTANCE = 10;

	private static final int PORT_RADIUS = 5;

	protected static final Stroke LINE_STROKE = new BasicStroke(2);

	protected static final Stroke MARKER_STROKE = new BasicStroke(
			1,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10f,
			new float[] { 1, 2 },
			0f);

	public final static int ON_NADA = 0;

	public final static int ON_ICON = 1;

	public final static int ON_LABEL = 2;

	public final static int ON_PORT = 4;

	protected TemplateComponent component;

	private Rectangle bounds;

	private LabelBox labelBox;

	private Ellipse2D[] ports = new Ellipse2D[4];

	public Entity(TemplateComponent component) throws MissingLayoutException
	{
		if (!component.hasAnnotation(Annotable.LAYOUT)
				|| !(component.getAnnotation(Annotable.LAYOUT) instanceof DiagramElementLayout))
		{
			throw new MissingLayoutException();
		}
		this.component = component;
		layout = (DiagramElementLayout)component
				.getAnnotation(Annotable.LAYOUT);
		update();
	}

	public Entity(TemplateComponent component, DiagramElementLayout layout)
	{
		this.component = component;
		this.layout = layout;
		component.setAnnotation(Annotable.LAYOUT, layout);
		update();
	}

	protected void computeBounds()
	{
		bounds = new Rectangle(
				layout.location.x - BOX_DISTANCE,
				layout.location.y - BOX_DISTANCE,
				2 * BOX_DISTANCE,
				2 * BOX_DISTANCE)
				.union(labelBox).union(ports[0].getBounds()).union(ports[1]
						.getBounds()).union(ports[2].getBounds())
				.union(ports[3].getBounds());
	}

	public TemplateComponent getComponent()
	{
		return component;
	}

	@Override
	public void draw(Graphics2D g2d)
	{
		if (selected)
		{
			g2d.setColor(COLOR_SELECT);
		}
		else
		{
			g2d.setColor(COLOR_NORM);
		}
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(LINE_STROKE);
		g2d.drawRect(layout.location.x - BOX_DISTANCE, layout.location.y
				- BOX_DISTANCE, BOX_DISTANCE * 2, BOX_DISTANCE * 2);
		g2d.setStroke(oldStroke);
		labelBox.draw(g2d);
		if (highlight)
		{
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

	public void translate(Point delta)
	{
		super.translate(delta);
		update();
	}

	public void update()
	{
		int maxWidth = 6 * BOX_DISTANCE;
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
				if (globalFontMetrics.stringWidth(line) > maxWidth)
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
		int deltaY = BOX_DISTANCE + LABEL_SPACING;
		labelBox.x = layout.location.x + deltaX;
		labelBox.y = layout.location.y + deltaY;
		ports[0] = new Ellipse2D.Float(
				layout.location.x - BOX_DISTANCE - 2 * PORT_RADIUS - 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[1] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				layout.location.y - BOX_DISTANCE - 2 * PORT_RADIUS - 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[2] = new Ellipse2D.Float(
				layout.location.x + BOX_DISTANCE + 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[3] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				(int)labelBox.getMaxY() + 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
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
				|| (p.x >= layout.location.x - BOX_DISTANCE
						&& p.x <= layout.location.x + BOX_DISTANCE
						&& p.y >= layout.location.y - BOX_DISTANCE && p.y <= layout.location.y
						+ BOX_DISTANCE);
	}

	public boolean intersects(Rectangle r)
	{
		return labelBox.intersects(r)
				|| ports[0].intersects(r)
				|| ports[1].intersects(r)
				|| ports[2].intersects(r)
				|| ports[3].intersects(r)
				|| new Rectangle(
						layout.location.x - BOX_DISTANCE,
						layout.location.y - BOX_DISTANCE,
						2 * BOX_DISTANCE,
						2 * BOX_DISTANCE).intersects(r);
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
}

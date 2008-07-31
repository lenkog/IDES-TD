package templates.diagram;

import ides.api.core.Annotable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.Box;

import templates.model.TemplateComponent;

public class Entity extends DiagramElement
{
	protected class LabelBox extends Rectangle
	{
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

		public void paint(Graphics2D g2d)
		{
			for (int i = 0; i < lines.size(); ++i)
			{
				int deltaX = (width - globalFontMetrics.stringWidth(lines
						.elementAt(i))) / 2;
				int deltaY = globalFontMetrics.getHeight() * (i + 1)
						- globalFontMetrics.getDescent();
				g2d.drawString(lines.elementAt(i), x + deltaX, y + deltaY);
			}
		}
	}

	protected static final int LABEL_SPACING = 5;

	protected static final int BOX_DISTANCE = 10;

	protected TemplateComponent component;

	private Rectangle bounds;

	private LabelBox labelBox;

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
				2 * BOX_DISTANCE).union(labelBox);
	}

	public TemplateComponent getComponent()
	{
		return component;
	}

	@Override
	public void draw(Graphics2D g2d)
	{
		g2d.setColor(Color.BLACK);
		g2d.drawRect(layout.location.x - BOX_DISTANCE, layout.location.y
				- BOX_DISTANCE, BOX_DISTANCE * 2, BOX_DISTANCE * 2);
		labelBox.paint(g2d);
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
				|| (p.x >= layout.location.x - BOX_DISTANCE
						&& p.x <= layout.location.x + BOX_DISTANCE
						&& p.y >= layout.location.y - BOX_DISTANCE && p.y <= layout.location.y
						+ BOX_DISTANCE);
	}

}

package template.diagram;

import ides.api.core.Annotable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import template.model.TemplateComponent;

public class Entity extends DiagramElement
{
	protected static final int INSET = 5;

	protected TemplateComponent component;

	private Rectangle bounds;

	public Entity(TemplateComponent component)
			throws MissingLayoutException
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

	public Entity(TemplateComponent component,
			DiagramElementLayout layout)
	{
		this.component = component;
		this.layout = layout;
		component.setAnnotation(Annotable.LAYOUT, layout);
		update();
	}

	protected void computeBounds()
	{
		int width = globalFontMetrics.stringWidth(layout.label) + 2 * INSET;
		int height = globalFontMetrics.getHeight() + 2 * INSET;
		bounds = new Rectangle(layout.location.x - width / 2, layout.location.y
				- height / 2, width, height);
	}

	public TemplateComponent getComponent()
	{
		return component;
	}

	@Override
	public void draw(Graphics2D g2d)
	{
		g2d.setColor(Color.BLACK);
		g2d.drawString(layout.label, (int)bounds.getMinX() + INSET, (int)bounds
				.getMinY()
				+ INSET);
		switch (component.getType())
		{
		case TemplateComponent.TYPE_CHANNEL:
			break;
		case TemplateComponent.TYPE_MODULE:
			break;
		default:
			g2d.drawLine((int)bounds.getMinX(),
					(int)bounds.getMinY(),
					(int)bounds.getMinX() + INSET,
					(int)bounds.getMinY());
			g2d.drawLine((int)bounds.getMinX(),
					(int)bounds.getMinY(),
					(int)bounds.getMinX(),
					(int)bounds.getMinY() + INSET);
			g2d.drawLine((int)bounds.getMinX(),
					(int)bounds.getMaxY(),
					(int)bounds.getMinX() + INSET,
					(int)bounds.getMaxY());
			g2d.drawLine((int)bounds.getMinX(),
					(int)bounds.getMaxY(),
					(int)bounds.getMinX(),
					(int)bounds.getMaxY() - INSET);
			g2d.drawLine((int)bounds.getMaxX(),
					(int)bounds.getMinY(),
					(int)bounds.getMaxX() - INSET,
					(int)bounds.getMinY());
			g2d.drawLine((int)bounds.getMaxX(),
					(int)bounds.getMinY(),
					(int)bounds.getMaxX(),
					(int)bounds.getMinY() + INSET);
			g2d.drawLine((int)bounds.getMaxX(),
					(int)bounds.getMaxY(),
					(int)bounds.getMaxX() - INSET,
					(int)bounds.getMaxY());
			g2d.drawLine((int)bounds.getMaxX(),
					(int)bounds.getMaxY(),
					(int)bounds.getMaxX(),
					(int)bounds.getMaxY() - INSET);
			break;
		}
	}
	
	public void translate(Point delta)
	{
		super.translate(delta);
		update();
	}
	
	public void update()
	{
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
		return bounds.contains(p);
	}

}

package templates.diagram;

import ides.api.core.Annotable;
import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;

import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

public class Connector extends DiagramElement
{
	private final static int SENSITIVITY = 4;

	protected Collection<TemplateLink> links = new HashSet<TemplateLink>();

	protected Entity left;

	protected Entity right;

	private Rectangle bounds;

	protected Line2D line;

	public Connector(Entity left, Entity right, Collection<TemplateLink> links)
			throws MissingLayoutException
	{
		layout = null;
		for (TemplateLink link : links)
		{
			if (link.hasAnnotation(Annotable.LAYOUT)
					&& link.getAnnotation(Annotable.LAYOUT) instanceof DiagramElementLayout)
			{
				layout = (DiagramElementLayout)link
						.getAnnotation(Annotable.LAYOUT);
				break;
			}
		}
		if (layout == null)
		{
			throw new MissingLayoutException();
		}
		for (TemplateLink link : links)
		{
			link.setAnnotation(Annotable.LAYOUT, layout);
		}
		this.left = left;
		this.right = right;
		this.links.addAll(links);
		update();
	}

	public Connector(Entity left, Entity right, Collection<TemplateLink> links,
			DiagramElementLayout layout)
	{
		this.layout = layout;
		for (TemplateLink link : links)
		{
			link.setAnnotation(Annotable.LAYOUT, layout);
		}
		this.left = left;
		this.right = right;
		this.links.addAll(links);
		update();
	}

	public Collection<TemplateLink> getLinks()
	{
		return new HashSet<TemplateLink>(links);
	}

	public Entity getLeftEntity()
	{
		return left;
	}

	public Entity getRightEntity()
	{
		return right;
	}

	public Entity[] getEntities()
	{
		return new Entity[] { left, right };
	}

	public void addLink(TemplateLink link)
	{
		if (!links.contains(link))
		{
			TemplateComponent[] endpoints = link.getComponents();
			if (!(left.getComponent() == endpoints[0] && right.getComponent() == endpoints[1])
					&& !(left.getComponent() == endpoints[1] && right
							.getComponent() == endpoints[0]))
			{
				throw new InconsistentModificationException(Hub
						.string("TD_inconsistencyConnecting"));
			}
			links.add(link);
		}
	}

	public void removeLink(TemplateLink link)
	{
		links.remove(link);
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
		g2d.drawLine((int)line.getX1(),
				(int)line.getY1(),
				(int)line.getX2(),
				(int)line.getY2());
	}

	public void translate(Point delta)
	{
		line = new Line2D.Float((float)line.getX1() + delta.x, (float)line
				.getY1()
				+ delta.y, (float)line.getX2() + delta.x, (float)line.getY2()
				+ delta.y);
	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	public void computeBounds()
	{
		bounds = line.getBounds();
	}

	public void update()
	{
		Point location1 = left.getLocation();
		Point location2 = right.getLocation();
		float slope = (location1.x - location2.x) == 0 ? 2 * Math
				.signum(location1.y - location2.y)
				: ((float)location1.y - location2.y)
						/ (location1.x - location2.x);
		int quad1 = 0;
		if (Math.abs(slope) > 1)
		{
			if (location1.y - location2.y > 0)
			{
				quad1 = 1;
			}
			else
			{
				quad1 = 3;
			}
		}
		else
		{
			if (location1.x - location2.x > 0)
			{
				quad1 = 0;
			}
			else
			{
				quad1 = 2;
			}
		}
		int quad2 = (quad1 + 2) % 4;
		line = new Line2D.Float(
				left.getPorts()[quad1].x,
				left.getPorts()[quad1].y,
				right.getPorts()[quad2].x,
				right.getPorts()[quad2].y);
		computeBounds();
	}

	@Override
	public boolean contains(Point p)
	{
		return line.ptSegDist(p) < SENSITIVITY;
	}

	public boolean intersects(Rectangle r)
	{
		return line.intersects(r)
				|| line.ptSegDist(r.getMinX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMinX(), r.getMaxY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMaxY()) < SENSITIVITY;
	}
}

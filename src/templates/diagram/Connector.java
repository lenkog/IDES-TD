package templates.diagram;

import ides.api.core.Annotable;
import ides.api.core.Hub;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

public class Connector extends DiagramElement
{
	protected class EventsBox extends Rectangle
	{
		private static final long serialVersionUID = 219703659050182848L;

		private static final int EVENT_LIST_LIMIT = 3;

		private static final String ELLIPSES = "...";

		protected class EventPair implements Comparable<EventPair>
		{
			public String event1;

			public String event2;

			public boolean isEllipses = false;

			public EventPair(String event1, String event2)
			{
				this.event1 = event1;
				this.event2 = event2;
			}

			public EventPair()
			{
				isEllipses = true;
			}

			public int compareTo(EventPair o)
			{
				return event1.compareTo(o.event1);
			}
		}

		protected boolean sideIsLeft = false;

		protected Vector<EventPair> events = new Vector<EventPair>();

		public EventsBox(boolean isSideLeft)
		{
			this.sideIsLeft = isSideLeft;
			for (TemplateLink link : getLinks())
			{
				TemplateComponent component=isSideLeft?getLeftEntity().getComponent():
					getRightEntity().getComponent();
				events.add(link.getLeftComponent()==component ? new EventPair(
						link.getLeftEventName(),
						link.getRightEventName()) : new EventPair(link
						.getRightEventName(), link.getLeftEventName()));
			}
			Collections.sort(events);
			if (events.size() > EVENT_LIST_LIMIT)
			{
				for (int i = 0; i < events.size() - EVENT_LIST_LIMIT; ++i)
				{
					events.removeElementAt(EVENT_LIST_LIMIT);
				}
				events.add(new EventPair());
			}
			if (events.isEmpty())
			{
				width = getGlobalFontMetrics().stringWidth(Hub
						.string("TD_noLinkEvents"));
				height = getGlobalFontMetrics().getHeight();
			}
			else
			{
				int maxWidth = 0;
				for (EventPair pair : events)
				{
					if (pair.isEllipses)
					{
						maxWidth = Math.max(maxWidth, getGlobalFontMetrics()
								.stringWidth(ELLIPSES));
					}
					else
					{
						int combinedWidth = globalFontRenderer
								.getFontMetrics(globalFont
										.deriveFont(Font.BOLD))
								.stringWidth(pair.event1)
								+ getGlobalFontMetrics().stringWidth(":"
										+ pair.event2);
						maxWidth = Math.max(maxWidth, combinedWidth);
					}
				}
				width = maxWidth;
				height = getGlobalFontMetrics().getHeight() * events.size();
			}
		}

		public void draw(Graphics2D g2d)
		{
			if (events.isEmpty())
			{
				g2d.drawString(Hub.string("TD_noLinkEvents"), x + 1, y
						+ getGlobalFontMetrics().getHeight()
						- getGlobalFontMetrics().getDescent());

			}
			else
			{
				for (int i = 0; i < events.size(); ++i)
				{
					int deltaY = getGlobalFontMetrics().getHeight() * (i + 1)
							- getGlobalFontMetrics().getDescent();
					if (events.elementAt(i).isEllipses)
					{
						g2d.drawString(ELLIPSES, x + 1, y + deltaY);
					}
					else
					{
						g2d.setFont(globalFont.deriveFont(Font.BOLD));
						g2d.drawString(events.elementAt(i).event1, x + 1, y
								+ deltaY);
						int deltaX = globalFontRenderer
								.getFontMetrics(globalFont
										.deriveFont(Font.BOLD))
								.stringWidth(events.elementAt(i).event1);
						g2d.setFont(globalFont);
						g2d.drawString(":" + events.elementAt(i).event2, x
								+ deltaX + 1, y + deltaY);
					}
				}
			}
		}
	}

	private final static int SENSITIVITY = 4;

	protected static final int LABEL_SPACING = 5;

	public final static int ON_NADA = 0;

	public final static int ON_LINE = 1;

	public final static int ON_LABEL = 2;

	protected Collection<TemplateLink> links = new HashSet<TemplateLink>();

	protected Entity left;

	protected Entity right;

	private Rectangle bounds;

	protected Line2D line;

	protected EventsBox leftEventBox;

	protected EventsBox rightEventBox;

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
			link.setAnnotation(Annotable.LAYOUT, layout);
			update();
		}
	}

	public void removeLink(TemplateLink link)
	{
		if (links.contains(link))
		{
			links.remove(link);
			update();
		}
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
		g2d.setStroke(LINE_STROKE);
		g2d.drawLine((int)line.getX1(),
				(int)line.getY1(),
				(int)line.getX2(),
				(int)line.getY2());
		leftEventBox.draw(g2d);
		rightEventBox.draw(g2d);
		if (highlight)
		{
			g2d.setStroke(MARKER_STROKE);
			g2d.drawRect(leftEventBox.x - 1,
					leftEventBox.y - 1,
					leftEventBox.width + 2,
					leftEventBox.height + 2);
			g2d.drawRect(rightEventBox.x - 1,
					rightEventBox.y - 1,
					rightEventBox.width + 2,
					rightEventBox.height + 2);
		}
	}

	public void translate(Point delta)
	{
		line = new Line2D.Float((float)line.getX1() + delta.x, (float)line
				.getY1()
				+ delta.y, (float)line.getX2() + delta.x, (float)line.getY2()
				+ delta.y);
		leftEventBox.translate(delta.x, delta.y);
		rightEventBox.translate(delta.x, delta.y);
	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	protected void computeBounds()
	{
		bounds = line.getBounds().union(leftEventBox).union(rightEventBox);
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
		leftEventBox = new EventsBox(true);
		rightEventBox = new EventsBox(false);
		if (quad1 == 0 || quad1 == 2)
		{
			boolean below;
			if (location1.y - location2.y > 0)
			{
				below = true;
			}
			else
			{
				below = false;
			}
			if (quad1 == 0)
			{
				leftEventBox.x = left.getPorts()[0].x - leftEventBox.width
						- LABEL_SPACING;
				leftEventBox.y = left.getPorts()[0].y
						- (below ? 0 : (LABEL_SPACING + leftEventBox.height));
				rightEventBox.x = right.getPorts()[2].x;
				rightEventBox.y = right.getPorts()[2].y
						- (below ? (LABEL_SPACING + rightEventBox.height) : 0);
			}
			else
			{
				leftEventBox.x = left.getPorts()[2].x;
				leftEventBox.y = left.getPorts()[2].y
						- (below ? 0 : (LABEL_SPACING + leftEventBox.height));
				rightEventBox.x = right.getPorts()[0].x - rightEventBox.width
						- LABEL_SPACING;
				rightEventBox.y = right.getPorts()[0].y
						- (below ? (LABEL_SPACING + rightEventBox.height) : 0);
			}
		}
		else
		{
			boolean onLeft;
			if (location1.x - location2.x > 0)
			{
				onLeft = false;
			}
			else
			{
				onLeft = true;
			}
			if (quad1 == 1)
			{
				leftEventBox.y = left.getPorts()[1].y - leftEventBox.height
						- LABEL_SPACING;
				leftEventBox.x = left.getPorts()[1].x
						- (onLeft ? (LABEL_SPACING + leftEventBox.width) : 0);
				rightEventBox.y = right.getPorts()[3].y;
				rightEventBox.x = right.getPorts()[3].x
						- (onLeft ? 0 : (LABEL_SPACING + rightEventBox.width));
			}
			else
			{
				leftEventBox.y = left.getPorts()[3].y;
				leftEventBox.x = left.getPorts()[3].x
						- (onLeft ? (LABEL_SPACING + leftEventBox.width) : 0);
				rightEventBox.y = right.getPorts()[1].y - rightEventBox.height
						- LABEL_SPACING;
				rightEventBox.x = right.getPorts()[1].x
						- (onLeft ? 0 : (LABEL_SPACING + rightEventBox.width));
			}
		}
		computeBounds();
	}

	@Override
	public boolean contains(Point p)
	{
		return line.ptSegDist(p) < SENSITIVITY || leftEventBox.contains(p)
				|| rightEventBox.contains(p);
	}

	public boolean intersects(Rectangle r)
	{
		return line.intersects(r) || leftEventBox.intersects(r)
				|| rightEventBox.intersects(r)
				|| line.ptSegDist(r.getMinX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMinX(), r.getMaxY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMaxY()) < SENSITIVITY;
	}

	public int whereisPoint(Point p)
	{
		if (leftEventBox.contains(p) || rightEventBox.contains(p))
		{
			return ON_LABEL;
		}
		else if (line.ptSegDist(p) < SENSITIVITY)
		{
			return ON_LINE;
		}
		return ON_NADA;
	}

}

package templates.diagram;

import ides.api.core.Hub;

import java.awt.Color;
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
	protected class EventBox extends Rectangle
	{
		private static final long serialVersionUID = 219703659050182848L;

		private static final int EVENT_LIST_LIMIT = 3;

		private static final String ELLIPSES = "...";

		private static final String EVENT_BINDER = "=";

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

		protected boolean showBothEvents = false;

		protected Vector<EventPair> events = new Vector<EventPair>();

		public EventBox()
		{
			showBothEvents = true;
			boolean isLeftLeft;
			if (left.getLocation().x == right.getLocation().x)
			{
				isLeftLeft = left.getLocation().y < right.getLocation().y;
			}
			else if (left.getLocation().x > right.getLocation().x)
			{
				isLeftLeft = false;
			}
			else
			{
				isLeftLeft = true;
			}

			for (TemplateLink link : getLinks())
			{
				TemplateComponent component = isLeftLeft ? getLeftEntity()
						.getComponent() : getRightEntity().getComponent();
				events
						.add(link.getLeftComponent() == component ? new EventPair(
								link.getLeftEventName(),
								link.getRightEventName())
								: new EventPair(link.getRightEventName(), link
										.getLeftEventName()));
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
						int combinedWidth = getGlobalFontMetrics()
								.stringWidth(pair.event1 + EVENT_BINDER
										+ pair.event2);
						maxWidth = Math.max(maxWidth, combinedWidth);
					}
				}
				width = maxWidth;
				height = getGlobalFontMetrics().getHeight() * events.size();
			}
		}

		public EventBox(boolean isSideLeft)
		{
			this.showBothEvents = false;
			for (TemplateLink link : getLinks())
			{
				events.add(new EventPair(isSideLeft ? link.getLeftEventName()
						: link.getRightEventName(), ""));
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
				width = 0;
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
						int combinedWidth = getGlobalFontMetrics()
								.stringWidth(pair.event1);
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
				if (showBothEvents)
				{
					g2d.drawString(Hub.string("TD_noLinkEvents"), x + 1, y
							+ getGlobalFontMetrics().getHeight()
							- getGlobalFontMetrics().getDescent());
				}
			}
			else
			{
				for (int i = 0; i < events.size(); ++i)
				{
					int deltaY = getGlobalFontMetrics().getHeight() * (i + 1)
							- getGlobalFontMetrics().getDescent();
					if (events.elementAt(i).isEllipses)
					{
						g2d.drawString(ELLIPSES, x, y + deltaY);
					}
					else
					{
						g2d.setFont(globalFont);
						g2d.drawString(events.elementAt(i).event1, x, y
								+ deltaY);
						if (showBothEvents)
						{
							int deltaX = getGlobalFontMetrics()
									.stringWidth(events.elementAt(i).event1);
							g2d.drawString(EVENT_BINDER
									+ events.elementAt(i).event2, x + deltaX, y
									+ deltaY);
						}
					}
				}
			}
		}
	}

	protected static Color BACKGROUND_COLOR = new Color(224, 224, 224, 200);

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

	protected EventBox leftEventBox;

	protected EventBox rightEventBox;

	protected EventBox centerEventBox;

	public Connector(Entity left, Entity right, Collection<TemplateLink> links)
	// throws MissingLayoutException
	{
		// layout = null;
		// for (TemplateLink link : links)
		// {
		// if (link.hasAnnotation(Annotable.LAYOUT)
		// && link.getAnnotation(Annotable.LAYOUT) instanceof
		// DiagramElementLayout)
		// {
		// layout = (DiagramElementLayout)link
		// .getAnnotation(Annotable.LAYOUT);
		// break;
		// }
		// }
		// if (layout == null)
		// {
		// throw new MissingLayoutException();
		// }
		// for (TemplateLink link : links)
		// {
		// link.setAnnotation(Annotable.LAYOUT, layout);
		// }
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
		g2d.setStroke(LINE_STROKE);
		g2d.drawLine((int)line.getX1(),
				(int)line.getY1(),
				(int)line.getX2(),
				(int)line.getY2());
		if (highlight)
		{
			Color temp = g2d.getColor();
			g2d.setColor(BACKGROUND_COLOR);
			if (leftEventBox.width > 0)
			{
				g2d.fillRect(leftEventBox.x - 2,
						leftEventBox.y - 1,
						leftEventBox.width + 4,

						leftEventBox.height + 2);
			}
			if (rightEventBox.width > 0)
			{
				g2d.fillRect(rightEventBox.x - 2,
						rightEventBox.y - 1,
						rightEventBox.width + 4,
						rightEventBox.height + 2);
			}
			g2d.setColor(temp);
			leftEventBox.draw(g2d);
			rightEventBox.draw(g2d);
			g2d.setColor(Color.WHITE);
			g2d.fillRect(centerEventBox.x - 1,
					centerEventBox.y - 1,
					centerEventBox.width + 2,
					centerEventBox.height + 2);
			g2d.setColor(temp);
			g2d.setStroke(MARKER_STROKE);
			g2d.drawRect(centerEventBox.x - 1,
					centerEventBox.y - 1,
					centerEventBox.width + 2,
					centerEventBox.height + 2);
			if (centerEventBox.y + centerEventBox.height + 1 > (int)(line
					.getY1() + (line.getY2() - line.getY1()) / 2))
			{
				if (centerEventBox.x > (int)(line.getX1() + (line.getX2() - line
						.getX1()) / 2))
				{
					g2d
							.drawLine(centerEventBox.x - 1,
									centerEventBox.y + centerEventBox.height
											/ 2,
									(int)(line.getX1() + (line.getX2() - line
											.getX1()) / 2),
									(int)(line.getY1() + (line.getY2() - line
											.getY1()) / 2));
				}
				else
				{
					g2d
							.drawLine(centerEventBox.x + centerEventBox.width
									+ 1,
									centerEventBox.y + centerEventBox.height
											/ 2,
									(int)(line.getX1() + (line.getX2() - line
											.getX1()) / 2),
									(int)(line.getY1() + (line.getY2() - line
											.getY1()) / 2));
				}
			}
			else
			{
				g2d
						.drawLine(centerEventBox.x + centerEventBox.width / 2,
								centerEventBox.y + centerEventBox.height + 1,
								(int)(line.getX1() + (line.getX2() - line
										.getX1()) / 2),
								(int)(line.getY1() + (line.getY2() - line
										.getY1()) / 2));
			}
		}
		centerEventBox.draw(g2d);
		if (highlight)
		{
			// g2d.setStroke(MARKER_STROKE);
			// g2d.drawRect(rightEventBox.x - 1,
			// rightEventBox.y - 1,
			// rightEventBox.width + 2,
			// rightEventBox.height + 2);
		}
	}

	public void translate(Point delta)
	{
		line = new Line2D.Float((float)line.getX1() + delta.x, (float)line
				.getY1()
				+ delta.y, (float)line.getX2() + delta.x, (float)line.getY2()
				+ delta.y);
		centerEventBox.translate(delta.x, delta.y);
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
		bounds = line
				.getBounds().union(centerEventBox).union(leftEventBox)
				.union(rightEventBox);
	}

	public void update()
	{
		Point location1 = left.getLocation();
		Point location2 = right.getLocation();
		float slope = (location1.x - location2.x) == 0 ? 2 * Math
				.signum(location1.y - location2.y)
				: ((float)location1.y - location2.y)
						/ (location1.x - location2.x);
		// quad1
		// 0=entity Right left of entity Left,
		// 1=entity Right above entity Left,
		// 2=entity Right right of entity Left,
		// 3=entity Right below entity Left
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
		centerEventBox = new EventBox();
		leftEventBox = new EventBox(true);
		rightEventBox = new EventBox(false);
		double midpointX = Math.min(line.getX1(), line.getX2())
				+ Math.abs(line.getX1() - line.getX2()) / 2;
		double midpointY = Math.min(line.getY1(), line.getY2())
				+ Math.abs(line.getY1() - line.getY2()) / 2;
		if (line.getY1() - line.getY2() == 0)
		{
			centerEventBox.x = (int)(midpointX - centerEventBox.width / 2);
			centerEventBox.y = (int)(midpointY - centerEventBox.height - LABEL_SPACING);
		}
		else if (line.getX1() - line.getX2() == 0)
		{
			centerEventBox.x = (int)(midpointX + LABEL_SPACING);
			centerEventBox.y = (int)(midpointY - centerEventBox.height / 2);
		}
		else
		{
			double lineS = (line.getY2() - line.getY1())
					/ (line.getX2() - line.getX1());
			double lineD = line.getY1() - lineS * line.getX1();
			double cornerX = midpointX - Math.signum(lineS)
					* (centerEventBox.getWidth() / 2 + LABEL_SPACING);
			double cornerY = midpointY + centerEventBox.getHeight() / 2
					+ LABEL_SPACING;
			double perpendicularS = -1 / lineS;
			double perpendicularD = cornerY - perpendicularS * cornerX;
			double intersectX = (perpendicularD - lineD)
					/ (lineS - perpendicularS);
			double intersectY = lineS * intersectX + lineD;
			centerEventBox.x = (int)(midpointX + intersectX - cornerX - centerEventBox.width / 2);
			centerEventBox.y = (int)(midpointY + intersectY - cornerY - centerEventBox.height / 2);
		}
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
				leftEventBox.x = left.getPorts()[0].x
						- (below ? leftEventBox.width + LABEL_SPACING
								: -LABEL_SPACING);
				leftEventBox.y = left.getPorts()[0].y + LABEL_SPACING;
				rightEventBox.x = right.getPorts()[2].x
						- (below ? rightEventBox.width + LABEL_SPACING
								: -LABEL_SPACING);
				rightEventBox.y = right.getPorts()[2].y + LABEL_SPACING;
			}
			else
			{
				leftEventBox.x = left.getPorts()[2].x
						- (below ? -LABEL_SPACING : leftEventBox.width
								+ LABEL_SPACING);
				leftEventBox.y = left.getPorts()[2].y + LABEL_SPACING;
				rightEventBox.x = right.getPorts()[0].x
						- (below ? -LABEL_SPACING : rightEventBox.width
								+ LABEL_SPACING);
				rightEventBox.y = right.getPorts()[0].y + LABEL_SPACING;
			}
		}
		else
		{
			boolean onLeft;
			if (location1.x == location2.x)
			{
				onLeft = quad1 != 1;
			}
			else if (location1.x - location2.x > 0)
			{
				onLeft = false;
			}
			else
			{
				onLeft = true;
			}
			if (quad1 == 1)
			{
				leftEventBox.y = left.getPorts()[1].y + LABEL_SPACING;
				leftEventBox.x = left.getPorts()[1].x
						- (onLeft ? -LABEL_SPACING : leftEventBox.width
								+ LABEL_SPACING);
				rightEventBox.y = right.getPorts()[3].y + LABEL_SPACING;
				rightEventBox.x = right.getPorts()[3].x
						- (onLeft ? -LABEL_SPACING : rightEventBox.width
								+ LABEL_SPACING);
			}
			else
			{
				leftEventBox.y = left.getPorts()[3].y + LABEL_SPACING;
				leftEventBox.x = left.getPorts()[3].x
						- (onLeft ? leftEventBox.width + LABEL_SPACING
								: -LABEL_SPACING);
				rightEventBox.y = right.getPorts()[1].y + LABEL_SPACING;
				rightEventBox.x = right.getPorts()[1].x
						- (onLeft ? rightEventBox.width + LABEL_SPACING
								: -LABEL_SPACING);
			}
		}
		computeBounds();
	}

	@Override
	public boolean contains(Point p)
	{
		return centerEventBox.contains(p)
				|| (line.getP1().distance(p) >= Entity.PORT_RADIUS
						&& line.getP2().distance(p) >= Entity.PORT_RADIUS && line
						.ptSegDist(p) < SENSITIVITY);
	}

	public boolean intersects(Rectangle r)
	{
		return line.intersects(r) || centerEventBox.intersects(r)
				|| line.ptSegDist(r.getMinX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMinX(), r.getMaxY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMinY()) < SENSITIVITY
				|| line.ptSegDist(r.getMaxX(), r.getMaxY()) < SENSITIVITY;
	}

	public int whereisPoint(Point p)
	{
		if (centerEventBox.contains(p))
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

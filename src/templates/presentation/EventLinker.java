package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAEvent;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.model.TemplateLink;

public class EventLinker extends JComponent implements MouseMotionListener,
		MouseListener
{
	private static final long serialVersionUID = -4822009760932585969L;

	protected static final int EVENT_SPACING = 2;

	protected static final int LINK_SPACING = 2;

	protected static final int MIN_LINK_WIDTH = 100;

	protected static final int NOTE_SPACE = 10;

	private static Image exclamation;

	protected boolean isLeftLeft;

	protected Connector connector;

	protected FontMetrics metrics;

	protected Vector<String> leftEvents = new Vector<String>();

	protected Set<String> leftMissingEvents = new HashSet<String>();

	protected Set<String> leftExtraEvents = new HashSet<String>();

	protected Vector<String> rightEvents = new Vector<String>();

	protected Set<String> rightMissingEvents = new HashSet<String>();

	protected Set<String> rightExtraEvents = new HashSet<String>();

	protected Map<String, String> softLinks = new HashMap<String, String>();

	public EventLinker(Connector connector)
	{
		this.connector = connector;
		metrics = DiagramElement.getGlobalFontMetrics();
		try
		{
			exclamation = ImageIO.read(Hub
					.getLocalResource(AssignEventsDialog.class,
							"images/icons/exclamation.gif"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		if (connector.getLeftEntity().getLocation().x > connector
				.getRightEntity().getLocation().x)
		{
			isLeftLeft = false;
		}
		else
		{
			isLeftLeft = true;
		}
		for (TemplateLink link : connector.getLinks())
		{
			softLinks.put(link.getLeftEventName(), link.getRightEventName());
		}
		addMouseMotionListener(this);
		addMouseListener(this);
		update();
	}

	public void update()
	{
		Set<String> leftSet = new HashSet<String>();
		Set<String> rightSet = new HashSet<String>();
		for (TemplateLink link : connector.getLinks())
		{
			leftSet.add(link.getLeftEventName());
			leftMissingEvents.add(link.getLeftEventName());
			rightSet.add(link.getRightEventName());
			rightMissingEvents.add(link.getRightEventName());
		}
		leftSet.addAll(leftExtraEvents);
		leftMissingEvents.addAll(leftExtraEvents);
		rightSet.addAll(rightExtraEvents);
		rightMissingEvents.addAll(rightExtraEvents);
		if (connector.getLeftEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getLeftEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				String name = i.next().getSymbol();
				leftSet.add(name);
				leftMissingEvents.remove(name);
			}
		}
		if (connector.getRightEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getRightEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				String name = i.next().getSymbol();
				rightSet.add(name);
				rightMissingEvents.remove(name);
			}
		}
		leftEvents.clear();
		rightEvents.clear();
		leftEvents.addAll(leftSet);
		rightEvents.addAll(rightSet);
		Collections.sort(leftEvents);
		Collections.sort(rightEvents);
		revalidate();
		repaint();
	}

	public void leftAddExtraEvent(String name)
	{
		leftExtraEvents.add(name);
		update();
	}

	public void rightAddExtraEvent(String name)
	{
		rightExtraEvents.add(name);
		update();
	}

	protected int getMaxEventWidth()
	{
		int maxWidth = 0;
		for (String name : leftEvents)
		{
			if (metrics.stringWidth(name) > maxWidth)
			{
				maxWidth = metrics.stringWidth(name);
			}
		}
		for (String name : rightEvents)
		{
			if (metrics.stringWidth(name) > maxWidth)
			{
				maxWidth = metrics.stringWidth(name);
			}
		}
		return maxWidth;
	}

	protected Rectangle getBoundsLeft(String event)
	{
		int idx = leftEvents.indexOf(event);
		if (idx >= 0)
		{
			int maxWidth = getMaxEventWidth();
			int linkWidth = Math.max(MIN_LINK_WIDTH, getSize().width - 2
					* maxWidth - 2 * NOTE_SPACE);
			int y = idx * (metrics.getHeight() + 2 * EVENT_SPACING);
			int x = isLeftLeft ? maxWidth
					- metrics.stringWidth(leftEvents.elementAt(idx))
					+ NOTE_SPACE : maxWidth + linkWidth + NOTE_SPACE;
			return new Rectangle(x, y, metrics.stringWidth(leftEvents
					.elementAt(idx)), metrics.getHeight());
		}
		return null;
	}

	protected Rectangle getBoundsRight(String event)
	{
		int idx = rightEvents.indexOf(event);
		if (idx >= 0)
		{
			int maxWidth = getMaxEventWidth();
			int linkWidth = Math.max(MIN_LINK_WIDTH, getSize().width - 2
					* maxWidth - 2 * NOTE_SPACE);
			int y = idx * (metrics.getHeight() + 2 * EVENT_SPACING);
			int x = isLeftLeft ? maxWidth + linkWidth + NOTE_SPACE : maxWidth
					- metrics.stringWidth(rightEvents.elementAt(idx))
					+ NOTE_SPACE;
			return new Rectangle(x, y, metrics.stringWidth(rightEvents
					.elementAt(idx)), metrics.getHeight());
		}
		return null;
	}

	protected String getLeftEventAt(Point p)
	{
		for (String event : leftEvents)
		{
			if (getBoundsLeft(event).contains(p))
			{
				return event;
			}
		}
		return null;
	}

	protected String getRightEventAt(Point p)
	{
		for (String event : rightEvents)
		{
			if (getBoundsRight(event).contains(p))
			{
				return event;
			}
		}
		return null;
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(2 * getMaxEventWidth() + MIN_LINK_WIDTH + 2
				* NOTE_SPACE, Math.max(leftEvents.size(), rightEvents.size())
				* (metrics.getHeight() + 2 * EVENT_SPACING));
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Point mouse = getMousePosition();
		g2d.setFont(DiagramElement.getGlobalFont());
		for (String event : leftEvents)
		{
			Rectangle r = getBoundsLeft(event);
			g2d.drawString(event, r.x, r.y + metrics.getAscent());
			if (leftMissingEvents.contains(event))
			{
				int x = isLeftLeft ? r.x - NOTE_SPACE : r.x
						+ metrics.stringWidth(event);
				g2d.drawImage(exclamation, x, r.y, null);
			}
		}
		for (String event : rightEvents)
		{
			Rectangle r = getBoundsRight(event);
			g2d.drawString(event, r.x, r.y + metrics.getAscent());
			if (rightMissingEvents.contains(event))
			{
				int x = isLeftLeft ? r.x + metrics.stringWidth(event) : r.x
						- NOTE_SPACE;
				g2d.drawImage(exclamation, x, r.y, null);
			}
		}
		for (String leftEvent : softLinks.keySet())
		{
			Rectangle l = getBoundsLeft(leftEvent);
			Rectangle r = getBoundsRight(softLinks.get(leftEvent));
			g2d.drawLine(isLeftLeft ? l.x + l.width + LINK_SPACING : l.x
					- LINK_SPACING, l.y + l.height / 2, isLeftLeft ? r.x
					- LINK_SPACING : r.x + r.width + LINK_SPACING, r.y
					+ r.height / 2);
		}
		if (mouse != null)
		{
			String event = getLeftEventAt(mouse);
			if (event != null)
			{
				Rectangle r = getBoundsLeft(event);
				g2d.drawRect(r.x - 2, r.y, r.width + 2, r.height);
			}
			else
			{
				event = getRightEventAt(mouse);
				if (event != null)
				{
					Rectangle r = getBoundsRight(event);
					g2d.drawRect(r.x - 2, r.y, r.width + 2, r.height);
				}
			}
			if (mouseDownOn != null)
			{
				Rectangle r = mouseDownOnLeft ? getBoundsLeft(mouseDownOn)
						: getBoundsRight(mouseDownOn);
				if (mouseDownOnLeft)
				{
					g2d.drawLine(isLeftLeft ? r.x + r.width + LINK_SPACING
							: r.x - LINK_SPACING,
							r.y + r.height / 2,
							mouse.x,
							mouse.y);
				}
				else
				{
					g2d.drawLine(isLeftLeft ? r.x - LINK_SPACING : r.x
							+ r.width + LINK_SPACING,
							r.y + r.height / 2,
							mouse.x,
							mouse.y);
				}
			}
		}
	}

	protected String mouseDownOn = null;

	protected boolean mouseDownOnLeft = true;

	protected boolean wasDragging = false;

	protected void linkEvents(String leftEvent, String rightEvent)
	{
		softLinks.put(leftEvent, rightEvent);
	}

	public void mouseDragged(MouseEvent arg0)
	{
		wasDragging = true;
		repaint();
	}

	public void mouseMoved(MouseEvent arg0)
	{
		repaint();
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if (arg0.getButton() == MouseEvent.BUTTON3)
		{

		}
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}

	public void mousePressed(MouseEvent arg0)
	{
		if (arg0.getButton() != MouseEvent.BUTTON1)
		{
			mouseDownOn = null;
			repaint();
			return;
		}
		String choice = null;
		boolean leftSide = true;
		String event = getLeftEventAt(arg0.getPoint());
		if (event != null)
		{
			choice = event;
			leftSide = true;
		}
		else
		{
			event = getRightEventAt(arg0.getPoint());
			if (event != null)
			{
				choice = event;
				leftSide = false;
			}
		}
		if (mouseDownOn == null)
		{
			mouseDownOn = choice;
			mouseDownOnLeft = leftSide;
		}
		else
		{
			if (choice != null && mouseDownOnLeft != leftSide)
			{
				linkEvents(leftSide ? choice : mouseDownOn,
						leftSide ? mouseDownOn : choice);
			}
			mouseDownOn = null;
		}
		repaint();
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (wasDragging && mouseDownOn != null)
		{
			String choice = null;
			boolean leftSide = true;
			String event = getLeftEventAt(arg0.getPoint());
			if (event != null)
			{
				choice = event;
				leftSide = true;
			}
			else
			{
				event = getRightEventAt(arg0.getPoint());
				if (event != null)
				{
					choice = event;
					leftSide = false;
				}
			}
			if (choice != null && mouseDownOnLeft != leftSide)
			{
				linkEvents(leftSide ? choice : mouseDownOn,
						leftSide ? mouseDownOn : choice);
			}
			mouseDownOn = null;
		}
		wasDragging = false;
		repaint();
	}
}

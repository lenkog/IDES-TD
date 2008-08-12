package templates.presentation;

import ides.api.model.fsa.FSAEvent;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.model.TemplateLink;

public class EventLinker extends JComponent
{
	private static final long serialVersionUID = -4822009760932585969L;

	protected static final int EVENT_SPACING = 2;

	protected static final int LINK_WIDTH = 100;

	protected boolean isLeftLeft;

	protected Connector connector;

	protected FontMetrics metrics;

	protected Vector<String> leftEvents = new Vector<String>();

	protected Vector<String> rightEvents = new Vector<String>();

	public EventLinker(Connector connector)
	{
		this.connector = connector;
		metrics = DiagramElement.getGlobalFontMetrics();
		if (connector.getLeftEntity().getLocation().x > connector
				.getRightEntity().getLocation().x)
		{
			isLeftLeft = false;
		}
		else
		{
			isLeftLeft = true;
		}
		rebuildEvents();
	}
	
	protected void rebuildEvents()
	{
		Set<String> leftSet = new HashSet<String>();
		Set<String> rightSet = new HashSet<String>();
		for (TemplateLink link : connector.getLinks())
		{
			leftSet.add(link.getLeftEventName());
			rightSet.add(link.getRightEventName());
		}
		if (connector.getLeftEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getLeftEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				leftSet.add(i.next().getSymbol());
			}
		}
		if (connector.getRightEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getRightEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				rightSet.add(i.next().getSymbol());
			}
		}
		leftEvents.clear();
		rightEvents.clear();
		leftEvents.addAll(leftSet);
		rightEvents.addAll(rightSet);
		Collections.sort(leftEvents);
		Collections.sort(rightEvents);		
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

	public Dimension getPreferredSize()
	{
		return new Dimension(2 * getMaxEventWidth() + LINK_WIDTH, Math
				.max(leftEvents.size(), rightEvents.size())
				* (metrics.getHeight() + 2 * EVENT_SPACING));
	}

	public void paint(Graphics g)
	{
		int maxWidth = getMaxEventWidth();
		g.setFont(DiagramElement.getGlobalFont());
		for (int i = 0; i < leftEvents.size(); ++i)
		{
			if (isLeftLeft)
			{
				g.drawString(leftEvents.elementAt(i), maxWidth
						- metrics.stringWidth(leftEvents.elementAt(i)), i
						* (metrics.getHeight() + 2 * EVENT_SPACING)
						+ metrics.getAscent());
			}
			else
			{
				g.drawString(leftEvents.elementAt(i), maxWidth + LINK_WIDTH, i
						* (metrics.getHeight() + 2 * EVENT_SPACING)
						+ metrics.getAscent());
			}
		}
		for (int i = 0; i < rightEvents.size(); ++i)
		{
			if (!isLeftLeft)
			{
				g.drawString(rightEvents.elementAt(i), maxWidth
						- metrics.stringWidth(rightEvents.elementAt(i)), i
						* (metrics.getHeight() + 2 * EVENT_SPACING)
						 + metrics.getAscent());
			}
			else
			{
				g.drawString(rightEvents.elementAt(i), maxWidth + LINK_WIDTH, i
						* (metrics.getHeight() + 2 * EVENT_SPACING)
						+ metrics.getAscent());
			}
		}
	}
}

/*
 * Copyright (c) 2009, Lenko Grigorov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAEvent;
import ides.api.model.fsa.FSAModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.undo.CompoundEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.actions.DiagramActions;
import templates.diagram.actions.DiagramUndoableEdits;
import templates.model.TemplateLink;

/**
 * The UI component which handles the user actions to establish links
 * between the events of two template design components.
 * Employed by {@link AssignEventsDialog}.
 * 
 * @author Lenko Grigorov
 */
public class EventLinker extends JComponent implements MouseMotionListener,
		MouseListener
{
	private static final long serialVersionUID = -4822009760932585969L;

	protected class EventLabel extends Rectangle implements
			Comparable<EventLabel>
	{
		private static final long serialVersionUID = 2717683995578252705L;

		public Entity mom;

		public String name;

		public boolean isInModel = false;

		public EventLabel(Entity mom, String name)
		{
			this.mom = mom;
			this.name = name;
			height = metrics.getHeight();
			width = metrics.stringWidth(name);
		}

		public void paint(Graphics2D g2d)
		{
			g2d.drawString(name, x, y + metrics.getAscent());
		}

		public int compareTo(EventLabel arg0)
		{
			if (mom != arg0.mom)
			{
				return new Integer(mom.hashCode()).compareTo(arg0.mom
						.hashCode());
			}
			return name.compareTo(arg0.name);
		}

		public boolean equals(Object o)
		{
			return this == o;
		}
	}

	protected class LabelLink extends Line2D.Float
	{
		private static final long serialVersionUID = 3684262819232368097L;

		EventLabel leftSide;

		EventLabel rightSide;

		public LabelLink(EventLabel leftSide, EventLabel rightSide)
		{
			this.leftSide = leftSide;
			this.rightSide = rightSide;
		}

		public boolean contains(Point p)
		{
			return ptSegDist(p.x, p.y) <= LINK_SENSITIVITY;
		}

		public void paint(Graphics2D g2d)
		{
			x1 = leftSide.x + leftSide.width + LINK_SPACING + 1;
			y1 = leftSide.y + leftSide.height / 2;
			x2 = rightSide.x - LINK_SPACING - 1;
			y2 = rightSide.y + rightSide.height / 2;
			g2d.draw(this);
		}

		public boolean equals(Object o)
		{
			return this == o;
		}
	}

	protected static final int EVENT_SPACING = 2;

	protected static final int LINK_SPACING = 4;

	protected static final int LINK_SENSITIVITY = 3;

	protected static final int MIN_LINK_WIDTH = 100;

	protected static final int MIN_HEIGHT = 250;

	protected static final int NOTE_SPACE = 10;

	protected static final Color SELECTED_COLOR = Color.RED;

	private static Image exclamation;

	protected boolean isLeftLeft;

	protected TemplateDiagram diagram;

	protected Connector connector;

	protected FontMetrics metrics;

	protected Set<EventLabel> labels = new HashSet<EventLabel>();

	protected Set<LabelLink> links = new HashSet<LabelLink>();

	public EventLinker(TemplateDiagram diagram, Connector connector)
	{
		this.diagram = diagram;
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
		String deleteAction = "deleteLink";
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, 0),
				deleteAction);
		getActionMap().put(deleteAction, deleteSelectedLink);

		if (connector.getLeftEntity().getLocation().x > connector
				.getRightEntity().getLocation().x)
		{
			isLeftLeft = false;
		}
		else
		{
			isLeftLeft = true;
		}
		Set<String> leftSet = new HashSet<String>();
		Set<String> rightSet = new HashSet<String>();
		Set<String> leftMissingEvents = new HashSet<String>();
		Set<String> rightMissingEvents = new HashSet<String>();
		for (Connector c : diagram.getAdjacentConnectors(connector
				.getLeftEntity()))
		{
			for (TemplateLink link : c.getLinks())
			{
				// System.out.println("con:" + c);
				// System.out.println("left ent:" + c.getLeftEntity() + "("
				// + c.getLeftEntity().getLabel() + ")");
				// System.out.println("right ent:" + c.getRightEntity() + "("
				// + c.getRightEntity().getLabel() + ")");
				// System.out.println("link:" + link);
				// System.out.println("boss com:"
				// + connector.getLeftEntity().getComponent());
				// System.out.println("link left com:" +
				// link.getLeftComponent());
				// System.out.println("con left com:"
				// + c.getLeftEntity().getComponent());
				// System.out
				// .println("link right com:" + link.getRightComponent());
				// System.out.println("con right com:"
				// + c.getRightEntity().getComponent());
				// System.out.println("link left e:" + link.getLeftEventName());
				// System.out.println("link right e:" +
				// link.getRightEventName());

				// System.out.println(connector.getLeftEntity().getLabel()+":"+
				// link.getLeftEventName()+":"+link.getLeftComponent()+","+c.
				// getLeftEntity().getComponent());
				if (link.getLeftComponent() == connector
						.getLeftEntity().getComponent())
				{
					leftSet.add(link.getLeftEventName());
					leftMissingEvents.add(link.getLeftEventName());
				}
				else
				{
					leftSet.add(link.getRightEventName());
					leftMissingEvents.add(link.getRightEventName());
				}
			}
		}
		for (Connector c : diagram.getAdjacentConnectors(connector
				.getRightEntity()))
		{
			for (TemplateLink link : c.getLinks())
			{
				if (link.getLeftComponent() == connector
						.getRightEntity().getComponent())
				{
					rightSet.add(link.getLeftEventName());
					rightMissingEvents.add(link.getLeftEventName());
				}
				else
				{
					rightSet.add(link.getRightEventName());
					rightMissingEvents.add(link.getRightEventName());
				}
			}
		}
		// for (String s : leftSet)
		// {
		// System.out.println("L" + s);
		// }
		// for (String s : rightSet)
		// {
		// System.out.println("R" + s);
		// }
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
		// for (String s : leftSet)
		// {
		// System.out.println("\'L" + s);
		// }
		// for (String s : rightSet)
		// {
		// System.out.println("\'R" + s);
		// }
		for (String event : leftSet)
		{
			EventLabel label = new EventLabel(connector.getLeftEntity(), event);
			label.isInModel = !leftMissingEvents.contains(event);
			labels.add(label);
		}
		for (String event : rightSet)
		{
			EventLabel label = new EventLabel(connector.getRightEntity(), event);
			label.isInModel = !rightMissingEvents.contains(event);
			labels.add(label);
		}
		for (TemplateLink link : connector.getLinks())
		{
			EventLabel left = getLabel(link.getLeftEventName(), connector
					.getLeftEntity());
			EventLabel right = getLabel(link.getRightEventName(), connector
					.getRightEntity());
			links.add(new LabelLink(
					isLeftLeft ? left : right,
					isLeftLeft ? right : left));
		}
		addMouseMotionListener(this);
		addMouseListener(this);
		update();
	}

	public void update()
	{
		Vector<EventLabel> leftSideLabels = new Vector<EventLabel>();
		Vector<EventLabel> rightSideLabels = new Vector<EventLabel>();
		for (EventLabel label : labels)
		{
			if (label.mom == (isLeftLeft ? connector.getLeftEntity()
					: connector.getRightEntity()))
			{
				leftSideLabels.add(label);
			}
			else
			{
				rightSideLabels.add(label);
			}
		}
		Collections.sort(leftSideLabels);
		Collections.sort(rightSideLabels);
		int maxWidth = getMaxLabelWidth();
		int linkWidth = Math.max(MIN_LINK_WIDTH, getSize().width - 2 * maxWidth
				- 2 * NOTE_SPACE);
		for (int i = 0; i < leftSideLabels.size(); ++i)
		{
			EventLabel label = leftSideLabels.elementAt(i);
			label.x = maxWidth - label.width + NOTE_SPACE;
			label.y = i * (metrics.getHeight() + 2 * EVENT_SPACING);
		}
		for (int i = 0; i < rightSideLabels.size(); ++i)
		{
			EventLabel label = rightSideLabels.elementAt(i);
			label.x = maxWidth + linkWidth + NOTE_SPACE;
			label.y = i * (metrics.getHeight() + 2 * EVENT_SPACING);
		}
		revalidate();
		repaint();
	}

	protected EventLabel getLabel(String name, Entity mom)
	{
		for (EventLabel label : labels)
		{
			if (label.name.equals(name) && label.mom == mom)
			{
				return label;
			}
		}
		return null;
	}

	public void addExtraLeftEvent(String name)
	{
		if (getLabel(name, connector.getLeftEntity()) == null)
		{
			EventLabel label = new EventLabel(connector.getLeftEntity(), name);
			label.isInModel = connector
					.getLeftEntity().getComponent().hasModel();
			labels.add(label);
			update();
		}
	}

	public void addExtraRightEvent(String name)
	{
		if (getLabel(name, connector.getRightEntity()) == null)
		{
			EventLabel label = new EventLabel(connector.getRightEntity(), name);
			label.isInModel = connector
					.getRightEntity().getComponent().hasModel();
			labels.add(label);
			update();
		}
	}

	protected int getMaxLabelWidth()
	{
		int maxWidth = 0;
		for (EventLabel label : labels)
		{
			if (label.width > maxWidth)
			{
				maxWidth = label.width;
			}
		}
		return maxWidth;
	}

	protected int getLabelMaxY()
	{
		int maxY = 0;
		for (EventLabel label : labels)
		{
			if (label.y + label.height > maxY)
			{
				maxY = label.y + label.height;
			}
		}
		return maxY;
	}

	protected EventLabel getLabelAt(Point p)
	{
		for (EventLabel label : labels)
		{
			if (label.contains(p))
			{
				return label;
			}
		}
		return null;
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(2 * getMaxLabelWidth() + MIN_LINK_WIDTH + 2
				* NOTE_SPACE, Math.max(MIN_HEIGHT, getLabelMaxY()
				+ EVENT_SPACING));
	}

	public Dimension getMinimumSize()
	{
		return new Dimension(2 * getMaxLabelWidth() + MIN_LINK_WIDTH + 2
				* NOTE_SPACE, MIN_HEIGHT);
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Point mouse = getMousePosition();
		g2d.setFont(DiagramElement.getGlobalFont());
		for (EventLabel label : labels)
		{
			label.paint(g2d);
			if (!label.isInModel)
			{
				int x = (isLeftLeft && label.mom == connector.getLeftEntity())
						|| (!isLeftLeft && label.mom == connector
								.getRightEntity()) ? label.x - NOTE_SPACE
						: label.x + label.width;
				g2d.drawImage(exclamation, x, label.y, null);
			}
			if (mouse != null && label.contains(mouse))
			{
				g2d.drawRect(label.x - 2,
						label.y,
						label.width + 2,
						label.height);
			}
		}
		for (LabelLink link : links)
		{
			if (link != selectedLink)
			{
				link.paint(g2d);
			}
		}
		if (selectedLink != null && links.contains(selectedLink))
		{
			Color temp = g2d.getColor();
			g2d.setColor(SELECTED_COLOR);
			selectedLink.paint(g2d);
			g2d.setColor(temp);
		}
		if (mouse != null && mouseDownOn != null)
		{
			int x = (isLeftLeft && mouseDownOn.mom == connector.getLeftEntity())
					|| (!isLeftLeft && mouseDownOn.mom == connector
							.getRightEntity()) ? mouseDownOn.x
					+ mouseDownOn.width + LINK_SPACING : mouseDownOn.x
					- LINK_SPACING;
			g2d.drawLine(x,
					mouseDownOn.y + mouseDownOn.height / 2,
					mouse.x,
					mouse.y);
		}
	}

	protected EventLabel mouseDownOn = null;

	protected boolean wasDragging = false;

	protected LabelLink selectedLink = null;

	protected void linkLabels(EventLabel first, EventLabel second)
	{
		EventLabel leftSide;
		EventLabel rightSide;
		if ((isLeftLeft && first.mom == connector.getLeftEntity())
				|| (!isLeftLeft && first.mom == connector.getRightEntity()))
		{
			leftSide = first;
			rightSide = second;
		}
		else
		{
			leftSide = second;
			rightSide = first;
		}
		unlinkLabel(leftSide);
		unlinkLabel(rightSide);
		links.add(new LabelLink(leftSide, rightSide));
		repaint();
	}

	public void unlinkLabel(EventLabel label)
	{
		LabelLink linkToRemove = null;
		for (LabelLink link : links)
		{
			if (link.leftSide == label || link.rightSide == label)
			{
				linkToRemove = link;
				break;
			}
		}
		if (linkToRemove != null)
		{
			links.remove(linkToRemove);
		}
		repaint();
	}

	public void unlinkAll()
	{
		links.clear();
		selectedLink = null;
		repaint();
	}

	protected EventLabel findMatchingRightLabel(String name)
	{
		for (EventLabel label : labels)
		{
			if (label.mom == connector.getRightEntity())
			{
				if (label.name.equals(name))
				{
					return label;
				}
			}
		}
		return null;
	}

	public void matchEvents()
	{
		Set<String> leftEvents = new HashSet<String>();
		Set<String> rightEvents = new HashSet<String>();
		for (EventLabel label : labels)
		{
			if (label.mom == connector.getLeftEntity())
			{
				leftEvents.add(label.name);
			}
			else
			{
				rightEvents.add(label.name);
			}
		}
		Set<String> matches = Helpers.matchEvents(leftEvents, rightEvents);
		unlinkAll();
		for (EventLabel label : labels)
		{
			if (label.mom == connector.getLeftEntity()
					&& matches.contains(label.name))
			{
				linkLabels(label, findMatchingRightLabel(label.name));
				matches.remove(label.name);
			}
		}
		repaint();
	}

	Action deleteSelectedLink = new AbstractAction(Hub.string("TD_delete"))
	{
		private static final long serialVersionUID = -1716730133755582880L;

		public void actionPerformed(ActionEvent arg0)
		{
			if (selectedLink != null)
			{
				unlinkLabel(selectedLink.leftSide);
				selectedLink = null;
				repaint();
			}
		}
	};

	protected class AddEventToModel extends AbstractAction
	{
		private static final long serialVersionUID = 4282137800712913380L;

		protected EventLabel label;

		public AddEventToModel(EventLabel label)
		{
			super(Hub.string("TD_addToModel"));
			this.label = label;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			label.isInModel = true;
			repaint();
		}
	};

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
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}

	public void mousePressed(MouseEvent arg0)
	{
		requestFocus();
		if (selectedLink != null && !selectedLink.contains(arg0.getPoint()))
		{
			selectedLink = null;
		}
		if (arg0.isPopupTrigger())
		{
			mousePopupTrigger(arg0);
		}
		if (arg0.getButton() != MouseEvent.BUTTON1)
		{
			mouseDownOn = null;
			repaint();
			return;
		}
		EventLabel choice = getLabelAt(arg0.getPoint());
		if (mouseDownOn == null)
		{
			mouseDownOn = choice;
		}
		else
		{
			if (choice != null && mouseDownOn.mom != choice.mom)
			{
				linkLabels(mouseDownOn, choice);
			}
			mouseDownOn = null;
		}
		repaint();
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (selectedLink != null && !selectedLink.contains(arg0.getPoint()))
		{
			selectedLink = null;
		}
		if (!wasDragging && selectedLink == null)
		{
			for (LabelLink link : links)
			{
				if (link.contains(arg0.getPoint()))
				{
					selectedLink = link;
					repaint();
					break;
				}
			}
		}
		if (wasDragging && mouseDownOn != null)
		{
			EventLabel choice = getLabelAt(arg0.getPoint());
			if (choice != null && mouseDownOn.mom != choice.mom)
			{
				linkLabels(mouseDownOn, choice);
			}
			mouseDownOn = null;
		}
		wasDragging = false;
		repaint();
		if (arg0.isPopupTrigger())
		{
			mousePopupTrigger(arg0);
		}
	}

	public void mousePopupTrigger(MouseEvent arg0)
	{
		if (selectedLink != null)
		{
			JPopupMenu popup = new JPopupMenu();
			popup.add(deleteSelectedLink);
			popup.show(arg0.getComponent(),
					arg0.getPoint().x,
					arg0.getPoint().y);
		}
		else
		{
			EventLabel label = getLabelAt(arg0.getPoint());
			if (label != null && !label.isInModel)
			{
				JPopupMenu popup = new JPopupMenu();
				AbstractAction action = new AddEventToModel(getLabelAt(arg0
						.getPoint()));
				action.setEnabled(label.mom.getComponent().hasModel());
				popup.add(action);
				popup.show(arg0.getComponent(), arg0.getPoint().x, arg0
						.getPoint().y);
			}
		}
	}

	public void commitChanges()
	{
		Set<String> leftEvents = new HashSet<String>();
		Set<String> rightEvents = new HashSet<String>();
		if (connector.getLeftEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getLeftEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				leftEvents.add(i.next().getSymbol());
			}
		}
		if (connector.getRightEntity().getComponent().hasModel())
		{
			for (Iterator<FSAEvent> i = connector
					.getRightEntity().getComponent().getModel()
					.getEventIterator(); i.hasNext();)
			{
				rightEvents.add(i.next().getSymbol());
			}
		}
		for (EventLabel label : labels)
		{
			if (label.mom == connector.getLeftEntity()
					&& connector.getLeftEntity().getComponent().hasModel())
			{
				if (!leftEvents.contains(label.name) && label.isInModel)
				{
					FSAModel model = connector
							.getLeftEntity().getComponent().getModel();
					model.add(model.assembleEvent(label.name));
				}
			}
			else if (label.mom == connector.getRightEntity()
					&& connector.getRightEntity().getComponent().hasModel())
			{
				if (!rightEvents.contains(label.name) && label.isInModel)
				{
					FSAModel model = connector
							.getRightEntity().getComponent().getModel();
					model.add(model.assembleEvent(label.name));
				}
			}
		}
		CompoundEdit edit = new CompoundEdit();
		List<DiagramActions.AddLinkAction> addActions = new LinkedList<DiagramActions.AddLinkAction>();
		for (LabelLink link : links)
		{
			String leftEvent = isLeftLeft ? link.leftSide.name
					: link.rightSide.name;
			String rightEvent = isLeftLeft ? link.rightSide.name
					: link.leftSide.name;
			boolean found = false;
			for (TemplateLink tlink : connector.getLinks())
			{
				if ((tlink.getLeftComponent() == connector
						.getLeftEntity().getComponent()
						&& tlink.getLeftEventName().equals(leftEvent) && tlink
						.getRightEventName().equals(rightEvent))
						|| (tlink.getRightComponent() == connector
								.getLeftEntity().getComponent()
								&& tlink.getLeftEventName().equals(rightEvent) && tlink
								.getRightEventName().equals(leftEvent)))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				addActions.add(new DiagramActions.AddLinkAction(
						edit,
						diagram,
						connector,
						leftEvent,
						rightEvent));
			}
		}
		Collection<TemplateLink> linksToRemove = new HashSet<TemplateLink>();
		for (TemplateLink link : connector.getLinks())
		{
			boolean found = false;
			for (LabelLink llink : links)
			{
				String leftEvent = isLeftLeft ? llink.leftSide.name
						: llink.rightSide.name;
				String rightEvent = isLeftLeft ? llink.rightSide.name
						: llink.leftSide.name;
				if ((link.getLeftComponent() == connector
						.getLeftEntity().getComponent()
						&& link.getLeftEventName().equals(leftEvent) && link
						.getRightEventName().equals(rightEvent))
						|| (link.getRightComponent() == connector
								.getLeftEntity().getComponent()
								&& link.getLeftEventName().equals(rightEvent) && link
								.getRightEventName().equals(leftEvent)))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				linksToRemove.add(link);
			}
		}
		if (!addActions.isEmpty() || !linksToRemove.isEmpty())
		{
			new DiagramActions.RemoveLinksAction(
					edit,
					diagram,
					connector,
					linksToRemove).execute();
			for (DiagramActions.AddLinkAction action : addActions)
			{
				action.execute();
			}
			edit.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(Hub
					.string("TD_undoSetLinkedEvents")));
			edit.end();
			Hub.getUndoManager().addEdit(edit);
		}
	}
}

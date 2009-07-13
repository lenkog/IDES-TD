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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.actions.DiagramActions;

/**
 * Processes the mouse actions of the user when manipulating a {@link TemplateEditableCanvas}. 
 * 
 * @author Lenko Grigorov
 */
public class MouseInterpreter implements MouseListener, MouseMotionListener
{
	protected TemplateEditableCanvas canvas;

	protected TemplateDiagram diagram;

	protected Point mouseDownAt = null;

	protected DiagramElement mouseDownOn = null;

	protected Point lastDragLocation = null;

	protected boolean draggedSelection = false;

	private int connectorClickCount = 0;

	protected Entity connectorOrigin = null;

	public MouseInterpreter(TemplateEditableCanvas canvas)
	{
		this.canvas = canvas;
		diagram = canvas.getDiagram();
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if (arg0.getButton() == MouseEvent.BUTTON1) // left button
		{
			if (arg0.getClickCount() == 1)
			{
				if (mouseDownOn != null)
				{
					Collection<DiagramElement> elements = new HashSet<DiagramElement>();
					elements.add(mouseDownOn);
					if (mouseDownOn instanceof Entity)
					{
						elements.addAll(diagram
								.getAdjacentConnectors((Entity)mouseDownOn));
					}
					diagram.setSelection(elements);
				}
			}
			else if (arg0.getClickCount() == 2)
			{
				if (mouseDownOn == null)
				{
					new DiagramActions.CreateEntityAction(diagram, arg0
							.getPoint()).execute();
				}
				else
				{
					if (mouseDownOn instanceof Entity)
					{
						int whichPart = ((Entity)mouseDownOn).whereisPoint(arg0
								.getPoint());
						if (whichPart == Entity.ON_LABEL)
						{
							EntityLabellingDialog.showAndLabel(canvas,
									(Entity)mouseDownOn);
						}
						else if (whichPart == Entity.ON_ICON)
						{
							if (((Entity)mouseDownOn).getComponent().getModel() == null)
							{
								AssignFSADialog.showAndAssign(canvas,
										(Entity)mouseDownOn);
							}
							else
							{
								new UIActions.OpenModelAction(
										canvas,
										(Entity)mouseDownOn)
										.actionPerformed(null);
							}
						}
						else if (whichPart == Entity.ON_SUP)
						{
							new UIActions.ShowSupAction(
									canvas,
									(Entity)mouseDownOn).actionPerformed(null);
						}
					}
					else if (mouseDownOn instanceof Connector)
					{
						AssignEventsDialog.showAndAssign(canvas,
								(Connector)mouseDownOn);
					}
				}
			}
		}
	}

	public void mousePopupTrigger(MouseEvent arg0)
	{
		if (arg0.getClickCount() == 1)
		{
			if (mouseDownOn == null)
			{
				Point p = canvas.localToComponent(arg0.getPoint());
				new DiagramPopup(canvas, arg0.getPoint())
						.show(canvas, p.x, p.y);
			}
			else
			{
				if (mouseDownOn instanceof Entity)
				{
					Point p = canvas.localToComponent(arg0.getPoint());
					new EntityPopup(canvas, (Entity)mouseDownOn).show(canvas,
							p.x,
							p.y);
				}
				else if (mouseDownOn instanceof Connector)
				{
					Point p = canvas.localToComponent(arg0.getPoint());
					new ConnectorPopup(canvas, (Connector)mouseDownOn)
							.show(canvas, p.x, p.y);
				}
			}
		}
	}

	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0)
	{
		mouseDownAt = arg0.getPoint();
		if (diagram.getConnectorAt(arg0.getPoint()) != null)
		{
			mouseDownOn = diagram.getConnectorAt(arg0.getPoint());
			if (!diagram.getSelection().contains(mouseDownOn))
			{
				diagram.setSelection(Arrays
						.asList(new DiagramElement[] { mouseDownOn }));
			}
		}
		else if (diagram.getEntityAt(arg0.getPoint()) != null)
		{
			mouseDownOn = diagram.getEntityAt(arg0.getPoint());
			if (!diagram.getSelection().contains(mouseDownOn))
			{
				Collection<DiagramElement> elements = new HashSet<DiagramElement>();
				elements.addAll(diagram
						.getAdjacentConnectors((Entity)mouseDownOn));
				elements.add(mouseDownOn);
				diagram.setSelection(elements);
			}
		}
		else
		// there's nothing under the mouse cursor
		{
			mouseDownOn = null;
			diagram.clearSelection();
		}
		if (canvas.isDrawingConnector()
				&& (arg0.getButton() != MouseEvent.BUTTON1
						|| mouseDownOn == null || !(mouseDownOn instanceof Entity)))
		{
			cancelConnector();
		}
		if (arg0.isPopupTrigger())
		{
			cancelConnector();
			mousePopupTrigger(arg0);
		}
		else if (!canvas.isDrawingConnector()
				&& arg0.getButton() == MouseEvent.BUTTON1
				&& mouseDownOn != null && mouseDownOn instanceof Entity)
		{
			int whichPart = ((Entity)mouseDownOn).whereisPoint(arg0.getPoint());
			if (whichPart == Entity.ON_PORT)
			{
				canvas.startConnector(arg0.getPoint());
				connectorOrigin = (Entity)mouseDownOn;
				connectorClickCount = 0;
				diagram.setSelection(Arrays
						.asList(new DiagramElement[] { mouseDownOn }));
			}

		}
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (canvas.isDrawingConnector()
				&& arg0.getButton() != MouseEvent.BUTTON1)
		{
			cancelConnector();
		}
		if (arg0.isPopupTrigger())
		{
			cancelConnector();
			mousePopupTrigger(arg0);
		}
		if (canvas.isDrawingConnector())
		{
			if (lastDragLocation == null && mouseDownAt.equals(arg0.getPoint()))
			{
				++connectorClickCount;
			}
			if (lastDragLocation != null || connectorClickCount > 1)
			// mouse was dragged or mouse was clicked two times
			{
				cancelConnector();
				Entity connectorEnd = diagram.getEntityAt(arg0.getPoint());
				if (connectorEnd != null
						&& connectorEnd != connectorOrigin
						&& diagram.getConnector(connectorOrigin, connectorEnd) == null)
				{
					new DiagramActions.CreateAndMatchConnectorAction(
							diagram,
							connectorOrigin,
							connectorEnd).execute();
				}
			}
		}
		if (draggedSelection)
		{
			draggedSelection = false;
			diagram.commitTranslation(diagram.getSelection(), new Point(
					lastDragLocation.x - mouseDownAt.x,
					lastDragLocation.y - mouseDownAt.y));
		}
		if (canvas.getSelectionBox() != null)
		{
			canvas.setSelectionBox(null);
			canvas.repaint();
		}
		lastDragLocation = null;
	}

	protected void cancelConnector()
	{
		if (canvas.isDrawingConnector())
		{
			canvas.finishConnector();
			canvas.repaint();
		}
	}

	public void mouseDragged(MouseEvent arg0)
	{
		if (lastDragLocation == null)
		{
			lastDragLocation = mouseDownAt;
		}
		if (mouseDownOn != null)
		{
			if (!canvas.isDrawingConnector())
			{
				Collection<DiagramElement> selection = diagram.getSelection();
				boolean hasEntities = false;
				for (DiagramElement element : selection)
				{
					if (element instanceof Entity)
					{
						hasEntities = true;
						break;
					}
				}
				if (!selection.isEmpty() && hasEntities)
				{
					draggedSelection = true;
					for (DiagramElement element : selection)
					{
						if (element instanceof Entity)
						{
							element.translate(new Point(arg0.getPoint().x
									- lastDragLocation.x, arg0.getPoint().y
									- lastDragLocation.y));
							for (Connector c : diagram
									.getAdjacentConnectors((Entity)element))
							{
								c.update();
							}
						}
					}
					canvas.repaint();
				}
			}
			else
			{
				mouseMoved(arg0);
			}
		}
		else
		{
			canvas.setSelectionBox(new Rectangle(Math.min(mouseDownAt.x, arg0
					.getX()), Math.min(mouseDownAt.y, arg0.getY()), Math
					.abs(mouseDownAt.x - arg0.getX()), Math.abs(mouseDownAt.y
					- arg0.getY())));
			canvas.repaint();
		}
		lastDragLocation = arg0.getPoint();
	}

	public void mouseMoved(MouseEvent arg0)
	{
		Connector c = diagram.getConnectorAt(arg0.getPoint());
		if (c != null)
		{
			if (c != canvas.getHighlightedElement())
			{
				canvas.highlight(c);
				canvas.repaint();
			}
		}
		else
		{
			Entity e = diagram.getEntityAt(arg0.getPoint());
			if (e != null)
			{
				if (e != canvas.getHighlightedElement())
				{
					canvas.highlight(e);
					canvas.repaint();
				}
			}
			else
			{
				if (null != canvas.getHighlightedElement())
				{
					canvas.removeHighlight();
					canvas.repaint();
				}
			}
		}
		if (canvas.isDrawingConnector())
		{
			canvas.repaint();
		}
	}

}

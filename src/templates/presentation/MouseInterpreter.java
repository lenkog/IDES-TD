package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;

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

public class MouseInterpreter implements MouseListener, MouseMotionListener
{
	protected TemplateEditableCanvas canvas;

	protected TemplateDiagram diagram;

	protected Point mouseDownAt = null;

	protected DiagramElement mouseDownOn = null;

	protected Point lastDragLocation = null;

	protected boolean draggedSelection = false;

	protected boolean creatingConnector = false;

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
				if (!creatingConnector)
				{
					if (mouseDownOn != null && mouseDownOn instanceof Entity)
					{
						int whichPart = ((Entity)mouseDownOn).whereisPoint(arg0
								.getPoint());
						if (whichPart == Entity.ON_PORT)
						{
							canvas.startConnector(arg0.getPoint());
							creatingConnector = true;
							connectorOrigin = (Entity)mouseDownOn;
						}
					}
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
								FSAModel fsa = ((Entity)mouseDownOn)
										.getComponent().getModel();
								if (Hub.getWorkspace().getModel(fsa.getName()) != fsa)
								{
									Hub.getWorkspace().addModel(fsa);
								}
								Hub
										.getWorkspace().setActiveModel(fsa
												.getName());
							}
						}
					}
					else if (mouseDownOn instanceof Connector)
					{
						AssignEventsDialog.showAndAssign(canvas, (Connector)mouseDownOn);
					}
				}
			}
		}
		else if (arg0.getButton() == MouseEvent.BUTTON3) // right button
		{
			if (arg0.getClickCount() == 1)
			{
				if (mouseDownOn == null)
				{

				}
				else
				{
					if (mouseDownOn instanceof Entity)
					{
						Point p = canvas.localToComponent(arg0.getPoint());
						new EntityPopup(canvas, (Entity)mouseDownOn)
								.show(canvas, p.x, p.y);
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
		if (creatingConnector)
		{
			canvas.finishConnector();
			if (diagram.getEntityAt(arg0.getPoint()) != null)
			{
				Entity connectorEnd = (Entity)diagram.getEntityAt(arg0
						.getPoint());
				if (connectorEnd != connectorOrigin
						&& diagram.getConnector(connectorOrigin, connectorEnd) == null)
				{
					new DiagramActions.CreateConnectorAction(
							diagram,
							connectorOrigin,
							connectorEnd).execute();
				}
			}
		}
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
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (creatingConnector)
		{
			creatingConnector = false;
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

	public void mouseDragged(MouseEvent arg0)
	{
		if (lastDragLocation == null)
		{
			lastDragLocation = mouseDownAt;
		}
		if (mouseDownOn != null)
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
		if (creatingConnector)
		{
			canvas.repaint();
		}
	}

}

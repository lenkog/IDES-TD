package templates.presentation;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Collection;

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
			}
			else if (arg0.getClickCount() == 2)
			{
				if (mouseDownOn == null)
				{
					new DiagramActions.CreateEntityAction(diagram, arg0
							.getPoint()).execute();
				}
				else if (mouseDownOn != null && mouseDownOn instanceof Entity)
				{
					int whichPart = ((Entity)mouseDownOn).whereisPoint(arg0
							.getPoint());
					if (whichPart == Entity.ON_LABEL)
					{
						EntityLabellingDialog.showAndLabel(canvas,
								(Entity)mouseDownOn);
					}
				}
			}
		}
		else if (arg0.getButton() == MouseEvent.BUTTON3) // right button
		{
			if (arg0.getClickCount() == 1)
			{
				if (mouseDownOn != null && mouseDownOn instanceof Entity)
				{
					Point p = canvas.localToComponent(arg0.getPoint());
					new EntityPopup(canvas, (Entity)mouseDownOn).show(canvas,
							p.x,
							p.y);
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
		if (arg0.getClickCount() == 1)
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
					diagram.setSelection(Arrays
							.asList(new DiagramElement[] { mouseDownOn }));
				}
			}
			else
			// there's nothing under the mouse cursor
			{
				mouseDownOn = null;
				diagram.clearSelection();
			}
		}
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (draggedSelection)
		{
			draggedSelection = false;
			diagram.commitTranslation(diagram.getSelection(), new Point(
					lastDragLocation.x - mouseDownAt.x,
					lastDragLocation.y - mouseDownAt.y));
		}
		if(canvas.getSelectionBox()!=null)
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
			if (!selection.isEmpty())
			{
				draggedSelection = true;
				for (DiagramElement element : selection)
				{
					element.translate(new Point(arg0.getPoint().x
							- lastDragLocation.x, arg0.getPoint().y
							- lastDragLocation.y));
				}
				canvas.repaint();
			}
		}
		else
		{
			canvas.setSelectionBox(new Rectangle(Math.min(mouseDownAt.x, arg0.getX()),
					Math.min(mouseDownAt.y, arg0.getY()),Math.abs(mouseDownAt.x-arg0.getX()),
					Math.abs(mouseDownAt.y-arg0.getY())));
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
	}

}

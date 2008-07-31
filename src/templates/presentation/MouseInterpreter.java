package templates.presentation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collection;

import templates.diagram.DiagramElement;
import templates.diagram.TemplateDiagram;
import templates.diagram.actions.DiagramActions;

public class MouseInterpreter implements MouseListener, MouseMotionListener
{
	TemplateEditableCanvas canvas;
	TemplateDiagram diagram;
	
	public MouseInterpreter(TemplateEditableCanvas canvas)
	{
		this.canvas=canvas;
		diagram=canvas.getDiagram();
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if(arg0.getClickCount()==1)
		{
			if (diagram.getConnectorAt(arg0.getPoint())!=null)
			{
			}
			else if(diagram.getEntityAt(arg0.getPoint())!=null)
			{
			}
			else //there's nothing under the mouse cursor
			{
				new DiagramActions.CreateEntityAction(diagram, arg0.getPoint())
						.execute();
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
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

}

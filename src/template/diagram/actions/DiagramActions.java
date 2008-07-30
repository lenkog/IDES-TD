package template.diagram.actions;

import ides.api.core.Hub;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import template.diagram.TemplateDiagram;
import template.diagram.Entity;

public class DiagramActions
{

	public static class CreateEntityAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4318087259767201282L;
		
		protected TemplateDiagram diagram;
		protected Point location;
		protected Entity[] buffer;
		
		public CreateEntityAction(TemplateDiagram diagram, Point location)
		{
			this(null,diagram,location,null);
		}
		
		public CreateEntityAction(TemplateDiagram diagram, Point location, Entity[] buffer)
		{
			this(null,diagram,location,buffer);
		}

		public CreateEntityAction(CompoundEdit parent, Point location, TemplateDiagram diagram)
		{
			this(parent,diagram,location,null);
		}
		
		public CreateEntityAction(CompoundEdit parent, TemplateDiagram diagram, Point location, Entity[] buffer)
		{
			this.parentEdit=parent;
			this.diagram=diagram;
			this.location=location;
			this.buffer=buffer;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.CreateEntityEdit edit = new DiagramUndoableEdits.CreateEntityEdit(
						diagram,
						location);
				edit.redo();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = edit.getEntity();
				}
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}
	
	public static class ShiftDiagramInViewAction extends AbstractAction
	{
		private static final long serialVersionUID = 2907001062138002843L;

		protected CompoundEdit parentEdit = null;

		protected TemplateDiagram diagram;

		public ShiftDiagramInViewAction(TemplateDiagram diagram)
		{
			this(null, diagram);
		}

		public ShiftDiagramInViewAction(CompoundEdit parentEdit, TemplateDiagram diagram)
		{
			this.parentEdit = parentEdit;
			this.diagram = diagram;
		}

		public void actionPerformed(ActionEvent event)
		{
			if (diagram != null)
			{
				Rectangle bounds = diagram.getBounds();
				if (bounds.x < 0 || bounds.y < 0)
				{
					UndoableEdit translation = new DiagramUndoableEdits.TranslateDiagramEdit(
							diagram,
							new Point(-bounds.x
									+ TemplateDiagram.DESIRED_DIAGRAM_INSET, -bounds.y
									+ TemplateDiagram.DESIRED_DIAGRAM_INSET));
					translation.redo();
					if (parentEdit != null)
					{
						parentEdit.addEdit(translation);
					}
					else
					{
						Hub.getUndoManager().addEdit(translation);
					}
				}
			}
		}

		public void execute()
		{
			actionPerformed(null);
		}
	}
	
}

package template.diagram.actions;

import ides.api.core.Hub;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import template.diagram.TemplateDiagram;
import template.diagram.VisualComponent;

public class DiagramActions
{

	public static class CreateComponentAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4318087259767201282L;
		
		protected TemplateDiagram diagram;
		protected Point location;
		protected VisualComponent[] buffer;
		
		public CreateComponentAction(TemplateDiagram diagram, Point location)
		{
			this(null,diagram,location,null);
		}
		
		public CreateComponentAction(TemplateDiagram diagram, Point location, VisualComponent[] buffer)
		{
			this(null,diagram,location,buffer);
		}

		public CreateComponentAction(CompoundEdit parent, Point location, TemplateDiagram diagram)
		{
			this(parent,diagram,location,null);
		}
		
		public CreateComponentAction(CompoundEdit parent, TemplateDiagram diagram, Point location, VisualComponent[] buffer)
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
				DiagramUndoableEdits.CreateComponentEdit edit = new DiagramUndoableEdits.CreateComponentEdit(
						diagram,
						location);
				edit.redo();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = edit.getComponent();
				}
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}
	
	public static class ShiftDiagramInViewAction extends AbstractAction
	{
		private static final long serialVersionUID = 2907001062138002843L;

		protected static final int DIAGRAM_INSET = 10;

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
									+ DIAGRAM_INSET, -bounds.y
									+ DIAGRAM_INSET));
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

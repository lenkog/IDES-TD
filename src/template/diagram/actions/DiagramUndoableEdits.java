package template.diagram.actions;

import ides.api.core.Hub;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import template.diagram.TemplateDiagram;
import template.diagram.VisualComponent;

public class DiagramUndoableEdits
{

	public static class UndoableDummyLabel extends AbstractUndoableEdit
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4900035740928121027L;

		String label = "";

		public UndoableDummyLabel(String label)
		{
			this.label = label;
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return label;
		}
	}

	public static class CreateComponentEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3487604066959821618L;

		protected TemplateDiagram diagram;

		protected Point location;

		protected VisualComponent component = null;

		public CreateComponentEdit(TemplateDiagram diagram, Point location)
		{
			this.diagram = diagram;
			this.location = location;
		}

		public VisualComponent getComponent()
		{
			return component;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (location == null)
			{
				throw new CannotRedoException();
			}
			if (component == null)
			{
				component = diagram.createComponent(location);
			}
			else
			{
				diagram.addComponent(component);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (component == null)
			{
				throw new CannotUndoException();
			}
			diagram.removeComponent(component);
		}

		@Override
		public boolean canUndo()
		{
			return (component != null);
		}

		@Override
		public boolean canRedo()
		{
			return (location != null);
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			if (usePluralDescription)
			{
				return Hub.string("TD_undoCreateComponents");
			}
			else
			{
				return Hub.string("TD_undoCreateComponent");
			}
		}
	}

	public static class TranslateDiagramEdit extends
			AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 1159217930658226725L;

		protected TemplateDiagram diagram;

		protected Point displacement;

		public TranslateDiagramEdit(TemplateDiagram diagram, Point delta)
		{
			this.diagram = diagram;
			this.displacement = delta;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			diagram.translate(displacement);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			diagram.translate(new Point(-displacement.x, -displacement.y));
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public boolean canRedo()
		{
			return true;
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return Hub.string("TD_undoTranslateDiagram");
		}
	}
}

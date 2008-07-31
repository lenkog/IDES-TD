package templates.diagram.actions;

import ides.api.core.Hub;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;

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

	public static class CreateEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3487604066959821618L;

		protected TemplateDiagram diagram;

		protected Point location;

		protected Entity entity = null;

		public CreateEntityEdit(TemplateDiagram diagram, Point location)
		{
			this.diagram = diagram;
			this.location = location;
		}

		public Entity getEntity()
		{
			return entity;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (location == null)
			{
				throw new CannotRedoException();
			}
			if (entity == null)
			{
				entity = diagram.createEntity(location);
			}
			else
			{
				diagram.add(entity);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			diagram.remove(entity);
		}

		@Override
		public boolean canUndo()
		{
			return (entity != null);
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
				return Hub.string("TD_undoCreateEntities");
			}
			else
			{
				return Hub.string("TD_undoCreateEntity");
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

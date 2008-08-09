package templates.diagram.actions;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;

import java.awt.Point;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.model.TemplateModel;

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

	public static class CreateConnectorEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 5291595117868352443L;

		protected TemplateDiagram diagram;

		protected Entity left;

		protected Entity right;

		protected Connector connector = null;

		public CreateConnectorEdit(TemplateDiagram diagram, Entity left,
				Entity right)
		{
			this.diagram = diagram;
			this.left = left;
			this.right = right;
		}

		public Connector getConnector()
		{
			return connector;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (left == null || right == null)
			{
				throw new CannotRedoException();
			}
			if (connector == null)
			{
				connector = diagram.createConnector(left, right);
			}
			else
			{
				diagram.add(connector);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (connector == null)
			{
				throw new CannotUndoException();
			}
			diagram.remove(connector);
		}

		@Override
		public boolean canUndo()
		{
			return (connector != null);
		}

		@Override
		public boolean canRedo()
		{
			return (left != null && right != null);
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
				return Hub.string("TD_undoCreateConnectors");
			}
			else
			{
				return Hub.string("TD_undoCreateConnector");
			}
		}
	}

	public static class RemoveEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 4636040678454819006L;

		protected TemplateDiagram diagram;

		protected Entity entity = null;

		public RemoveEntityEdit(TemplateDiagram diagram, Entity entity)
		{
			this.diagram = diagram;
			this.entity = entity;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (entity == null)
			{
				throw new CannotRedoException();
			}
			else
			{
				diagram.remove(entity);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			diagram.add(entity);
		}

		@Override
		public boolean canUndo()
		{
			return (entity != null);
		}

		@Override
		public boolean canRedo()
		{
			return (entity != null);
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
				return Hub.string("TD_undoRemoveEntities");
			}
			else
			{
				return Hub.string("TD_undoRemoveEntity");
			}
		}
	}

	public static class RemoveConnectorEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 800729106482826023L;

		protected TemplateDiagram diagram;

		protected Connector connector = null;

		public RemoveConnectorEdit(TemplateDiagram diagram, Connector connector)
		{
			this.diagram = diagram;
			this.connector = connector;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (connector == null)
			{
				throw new CannotRedoException();
			}
			else
			{
				diagram.remove(connector);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (connector == null)
			{
				throw new CannotUndoException();
			}
			diagram.add(connector);
		}

		@Override
		public boolean canUndo()
		{
			return (connector != null);
		}

		@Override
		public boolean canRedo()
		{
			return (connector != null);
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
				return Hub.string("TD_undoRemoveConnectors");
			}
			else
			{
				return Hub.string("TD_undoRemoveConnector");
			}
		}
	}

	public static class MovedSelectionEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 6345901897825712351L;

		protected TemplateDiagram diagram;

		protected Point delta;

		protected Collection<DiagramElement> selection = null;

		public MovedSelectionEdit(TemplateDiagram diagram,
				Collection<DiagramElement> selection, Point delta)
		{
			this.diagram = diagram;
			this.selection = selection;
			this.delta = delta;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			diagram.translate(selection, delta);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			diagram.translate(selection, new Point(-delta.x, -delta.y));
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
			return Hub.string("TD_undoMoveSelection");
		}
	}

	public static class LabelEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = -5392857255166419782L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected String oldLabel;

		protected String newLabel;

		public LabelEntityEdit(TemplateDiagram diagram, Entity entity,
				String label)
		{
			this.diagram = diagram;
			this.entity = entity;
			oldLabel = entity.getLabel();
			newLabel = label;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			diagram.labelEntity(entity, newLabel);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			diagram.labelEntity(entity, oldLabel);
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
			return Hub.string("TD_undoLabelEntity");
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

	public static class AssignFSAEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3692931034352868517L;

		protected TemplateDiagram diagram;

		protected Entity entity = null;

		protected FSAModel oldModel = null;

		protected FSAModel newModel = null;

		public AssignFSAEdit(TemplateDiagram diagram, Entity entity,
				FSAModel newModel)
		{
			this.diagram = diagram;
			this.entity = entity;
			this.oldModel = entity.getComponent().getModel();
			this.newModel = newModel;
		}

		public Entity getEntity()
		{
			return entity;
		}

		@Override
		public void redo() throws CannotRedoException
		{
			if (entity == null)
			{
				throw new CannotRedoException();
			}
			if (entity.getComponent().getModel() != null)
			{
				entity.getComponent().getModel().removeSubscriber(diagram);
			}
			diagram.getModel().assignFSA(entity.getComponent().getId(),
					newModel);
			if (newModel != null)
			{
				newModel.setName(TemplateModel.FSA_NAME_PREFIX
						+ entity.getLabel());
				newModel.addSubscriber(diagram);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			if (entity.getComponent().getModel() != null)
			{
				entity.getComponent().getModel().removeSubscriber(diagram);
			}
			diagram.getModel().assignFSA(entity.getComponent().getId(),
					oldModel);
			if (oldModel != null)
			{
				oldModel.setName(TemplateModel.FSA_NAME_PREFIX
						+ entity.getLabel());
				oldModel.addSubscriber(diagram);
			}
		}

		@Override
		public boolean canUndo()
		{
			return (entity != null);
		}

		@Override
		public boolean canRedo()
		{
			return (entity != null);
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return Hub.string("TD_undoAssignFSA");
		}
	}
}

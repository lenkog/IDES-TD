/*
 * Copyright (c) 2010, Lenko Grigorov
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

package templates.diagram.actions;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAMessage;
import ides.api.model.fsa.FSAModel;
import ides.api.model.supeventset.SupervisoryEvent;
import ides.api.plugin.model.DESEvent;

import java.awt.Point;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.utils.EntityIcon;

/**
 * Collection of all undoable edits of {@link TemplateDiagram}s.
 * 
 * @author Lenko Grigorov
 */
public class DiagramUndoableEdits
{

	/**
	 * Dummy edit which only serves to label a {@link CompoundEdit} with a
	 * specific user-readable description.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class UndoableDummyLabel extends AbstractUndoableEdit
	{
		private static final long serialVersionUID = 4900035740928121027L;

		/**
		 * The label to be shown to the user.
		 */
		String label = "";

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param label
		 *            the label to be shown to the user
		 */
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

	/**
	 * Edit which does and undoes the creation of an {@link Entity} in a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3487604066959821618L;

		/**
		 * The {@link TemplateDiagram} where the {@link Entity} should be
		 * created.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The location where the {@link Entity} should be placed.
		 */
		protected Point location;

		/**
		 * Holds the new {@link Entity} once it is created.
		 */
		protected Entity entity = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} where the {@link Entity}
		 *            should be created
		 * @param location
		 *            the location where the {@link Entity} should be placed
		 */
		public CreateEntityEdit(TemplateDiagram diagram, Point location)
		{
			this.diagram = diagram;
			this.location = location;
		}

		/**
		 * Retrieves the {@link Entity} created by the edit.
		 * 
		 * @return the {@link Entity} created by the edit
		 */
		public Entity getEntity()
		{
			return entity;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the creation of a {@link Connector} in a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateConnectorEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 5291595117868352443L;

		/**
		 * The {@link TemplateDiagram} where the {@link Connector} should be
		 * created.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The first {@link Entity} to be linked.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 */
		protected Entity left;

		/**
		 * The second {@link Entity} to be linked.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 */
		protected Entity right;

		/**
		 * Holds the new {@link Connector} once it is created.
		 */
		protected Connector connector = null;

		/**
		 * Constructs an edit with the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} where the {@link Connector}
		 *            should be created
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 */
		public CreateConnectorEdit(TemplateDiagram diagram, Entity left,
				Entity right)
		{
			this.diagram = diagram;
			this.left = left;
			this.right = right;
		}

		/**
		 * Retrieves the {@link Connector} created by the edit.
		 * 
		 * @return the {@link Connector} created by the edit
		 */
		public Connector getConnector()
		{
			return connector;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the addition of a {@link TemplateLink} between
	 * two events from the {@link TemplateComponent}s linked by a
	 * {@link Connector}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class AddLinkEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = -22893051179719542L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Connector} to
		 * which the {@link TemplateLink} should be added.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} to which the {@link TemplateLink} should be
		 * added.
		 */
		protected Connector connector;

		/**
		 * The event (from the "left" {@link TemplateComponent}) which has to be
		 * linked.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked components separately.
		 */
		protected String leftEvent;

		/**
		 * The event (from the "right" {@link TemplateComponent}) which has to
		 * be linked.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked components separately.
		 */
		protected String rightEvent;

		/**
		 * Holds the new {@link TemplateLink} once it is created.
		 */
		protected TemplateLink link = null;

		/**
		 * Constructs an edit with the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked components separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Connector} to which the {@link TemplateLink} should
		 *            be added
		 * @param connector
		 *            the {@link Connector} to which the {@link TemplateLink}
		 *            should be added
		 * @param leftEvent
		 *            the event (from the "left" {@link TemplateComponent})
		 *            which has to be linked
		 * @param rightEvent
		 *            the event (from the "right" {@link TemplateComponent})
		 *            which has to be linked
		 */
		public AddLinkEdit(TemplateDiagram diagram, Connector connector,
				String leftEvent, String rightEvent)
		{
			this.diagram = diagram;
			this.connector = connector;
			this.leftEvent = leftEvent;
			this.rightEvent = rightEvent;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (connector == null || leftEvent == null || rightEvent == null)
			{
				throw new CannotRedoException();
			}
			if (link == null)
			{
				link = diagram.createLink(connector, leftEvent, rightEvent);
			}
			else
			{
				diagram.addLink(connector, link);
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (connector == null || link == null)
			{
				throw new CannotUndoException();
			}
			diagram.removeLink(connector, link);
		}

		@Override
		public boolean canUndo()
		{
			return (connector != null && link != null);
		}

		@Override
		public boolean canRedo()
		{
			return (connector != null && leftEvent != null && rightEvent != null);
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return Hub.string("TD_undoSetLinkedEvents");
		}
	}

	/**
	 * Edit which does and undoes the removal of an {@link Entity} from a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class RemoveEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 4636040678454819006L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Entity} to be
		 * removed.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} to be removed.
		 */
		protected Entity entity = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Entity} to be removed
		 * @param entity
		 *            the {@link Entity} to be removed
		 */
		public RemoveEntityEdit(TemplateDiagram diagram, Entity entity)
		{
			this.diagram = diagram;
			this.entity = entity;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the removal of a {@link Connector} from a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class RemoveConnectorEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 800729106482826023L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Connector} to
		 * be removed.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} to be removed.
		 */
		protected Connector connector = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Connector} to be removed
		 * @param connector
		 *            the {@link Connector} to be removed
		 */
		public RemoveConnectorEdit(TemplateDiagram diagram, Connector connector)
		{
			this.diagram = diagram;
			this.connector = connector;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the removal of a {@link TemplateLink} from a
	 * {@link Connector}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class RemoveLinkEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3808507510444869728L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Connector} from
		 * which the {@link TemplateLink} should be removed.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} from which the {@link TemplateLink} should be
		 * removed.
		 */
		protected Connector connector = null;

		/**
		 * The {@link TemplateLink} to be removed.
		 */
		protected TemplateLink link = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Connector} from which the {@link TemplateLink}
		 *            should be removed
		 * @param connector
		 *            the {@link Connector} from which the {@link TemplateLink}
		 *            should be removed
		 * @param link
		 *            the {@link TemplateLink} to be removed
		 */
		public RemoveLinkEdit(TemplateDiagram diagram, Connector connector,
				TemplateLink link)
		{
			this.diagram = diagram;
			this.connector = connector;
			this.link = link;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (connector == null || link == null)
			{
				throw new CannotRedoException();
			}
			diagram.removeLink(connector, link);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (connector == null || link == null)
			{
				throw new CannotUndoException();
			}
			diagram.addLink(connector, link);
		}

		@Override
		public boolean canUndo()
		{
			return (connector != null && link != null);
		}

		@Override
		public boolean canRedo()
		{
			return (connector != null && link != null);
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return Hub.string("TD_undoSetLinkedEvents");
		}
	}

	/**
	 * Edit which does and undoes the relocation of {@link DiagramElement}s in a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class MovedSelectionEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 6345901897825712351L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link DiagramElement}
		 * s to be relocated.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The displacement of the relocation.
		 */
		protected Point delta;

		/**
		 * The {@link DiagramElement}s to be relocated.
		 */
		protected Collection<DiagramElement> selection = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link DiagramElement}s to be relocated
		 * @param selection
		 *            the {@link DiagramElement}s to be relocated
		 * @param delta
		 *            the displacement of the relocation
		 */
		public MovedSelectionEdit(TemplateDiagram diagram,
				Collection<DiagramElement> selection, Point delta)
		{
			this.diagram = diagram;
			this.selection = selection;
			this.delta = delta;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the setting of the label of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class LabelEntityEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = -5392857255166419782L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Entity} to be
		 * relabelled.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} to be relabelled.
		 */
		protected Entity entity;

		/**
		 * The old label of the {@link Entity}.
		 */
		protected String oldLabel;

		/**
		 * The new label of the {@link Entity}.
		 */
		protected String newLabel;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Entity} to be relabelled
		 * @param entity
		 *            the {@link Entity} to be relabelled
		 * @param label
		 *            the new label of the {@link Entity}
		 */
		public LabelEntityEdit(TemplateDiagram diagram, Entity entity,
				String label)
		{
			this.diagram = diagram;
			this.entity = entity;
			oldLabel = entity.getLabel();
			newLabel = label;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the translation of a {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class TranslateDiagramEdit extends
			AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 1159217930658226725L;

		/**
		 * The {@link TemplateDiagram} to be translated.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The displacement of the {@link TemplateDiagram}.
		 */
		protected Point displacement;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be translated
		 * @param delta
		 *            the displacement of the {@link TemplateDiagram}
		 */
		public TranslateDiagramEdit(TemplateDiagram diagram, Point delta)
		{
			this.diagram = diagram;
			this.displacement = delta;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
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

	/**
	 * Edit which does and undoes the assignment of an {@link FSAModel} to the
	 * {@link TemplateComponent} of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class AssignFSAEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 3692931034352868517L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Entity} to
		 * whose {@link TemplateComponent} the new {@link FSAModel} should be
		 * assigned.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} to whose {@link TemplateComponent} the new
		 * {@link FSAModel} should be assigned.
		 */
		protected Entity entity = null;

		/**
		 * The old {@link FSAModel} assigned to the {@link TemplateComponent} of
		 * the {@link Entity}.
		 */
		protected FSAModel oldModel = null;

		/**
		 * The new {@link FSAModel} assigned to the {@link TemplateComponent} of
		 * the {@link Entity}.
		 */
		protected FSAModel newModel = null;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Entity} to whose {@link TemplateComponent} the new
		 *            {@link FSAModel} should be assigned
		 * @param entity
		 *            the {@link Entity} to whose {@link TemplateComponent} the
		 *            new {@link FSAModel} should be assigned
		 * @param newModel
		 *            the new {@link FSAModel} to be assigned to the
		 *            {@link TemplateComponent} of the {@link Entity}
		 */
		public AssignFSAEdit(TemplateDiagram diagram, Entity entity,
				FSAModel newModel)
		{
			this.diagram = diagram;
			this.entity = entity;
			this.oldModel = entity.getComponent().getModel();
			this.newModel = newModel;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (entity == null)
			{
				throw new CannotRedoException();
			}
			if (entity.getComponent().hasModel()
					&& Hub.getWorkspace().getModel(entity
							.getComponent().getModel().getName()) == entity
							.getComponent().getModel())
			{
				Hub.getWorkspace().removeModel(entity
						.getComponent().getModel().getName());
			}
			diagram.getModel().assignFSA(entity.getComponent().getId(),
					newModel);
			if (newModel != null)
			{
				newModel.setName(TemplateModel.FSA_NAME_PREFIX
						+ entity.getLabel());
			}
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			if (entity.getComponent().hasModel()
					&& Hub.getWorkspace().getModel(entity
							.getComponent().getModel().getName()) == entity
							.getComponent().getModel())
			{
				Hub.getWorkspace().removeModel(entity
						.getComponent().getModel().getName());
			}
			diagram.getModel().assignFSA(entity.getComponent().getId(),
					oldModel);
			if (oldModel != null)
			{
				oldModel.setName(TemplateModel.FSA_NAME_PREFIX
						+ entity.getLabel());
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

	/**
	 * Edit which does and undoes setting the type (<i>module</i> or
	 * <i>channel</i>) of the {@link TemplateComponent} of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetTypeEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = -36438914037724751L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Entity}.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} which represents the {@link TemplateComponent}
		 * whose type has to be set.
		 */
		protected Entity entity = null;

		/**
		 * The old type of the {@link TemplateComponent} (
		 * {@link TemplateComponent#TYPE_MODULE} or
		 * {@link TemplateComponent#TYPE_CHANNEL}).
		 */
		protected int oldType;

		/**
		 * The new type of the {@link TemplateComponent} (
		 * {@link TemplateComponent#TYPE_MODULE} or
		 * {@link TemplateComponent#TYPE_CHANNEL}).
		 */
		protected int newType;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Entity}
		 * @param entity
		 *            the {@link Entity} which represents the
		 *            {@link TemplateComponent} whose type has to be set.
		 * @param newType
		 *            the type to be set ({@link TemplateComponent#TYPE_MODULE}
		 *            or {@link TemplateComponent#TYPE_CHANNEL})
		 */
		public SetTypeEdit(TemplateDiagram diagram, Entity entity, int newType)
		{
			this.diagram = diagram;
			this.entity = entity;
			this.oldType = entity.getComponent().getType();
			this.newType = newType;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (entity == null)
			{
				throw new CannotRedoException();
			}
			diagram.getModel().setComponentType(entity.getComponent().getId(),
					newType);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			diagram.getModel().setComponentType(entity.getComponent().getId(),
					oldType);
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
			return Hub.string("TD_undoSetType");
		}
	}

	/**
	 * Edit which does and undoes setting the icon of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetIconEdit extends AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 2630730309577424765L;

		/**
		 * The {@link TemplateDiagram} which contains the {@link Entity}.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} whose icon has to be set.
		 */
		protected Entity entity = null;

		/**
		 * The old icon of the {@link Entity}.
		 */
		protected EntityIcon oldIcon;

		/**
		 * The new icon of the {@link Entity}.
		 */
		protected EntityIcon newIcon;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} which contains the
		 *            {@link Entity}
		 * @param entity
		 *            the {@link Entity} whose icon has to be set
		 * @param newIcon
		 *            the new icon for the {@link Entity}
		 */
		public SetIconEdit(TemplateDiagram diagram, Entity entity,
				EntityIcon newIcon)
		{
			this.diagram = diagram;
			this.entity = entity;
			this.oldIcon = entity.getIcon();
			this.newIcon = newIcon;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (entity == null)
			{
				throw new CannotRedoException();
			}
			diagram.setEntityIcon(entity, newIcon);
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (entity == null)
			{
				throw new CannotUndoException();
			}
			diagram.setEntityIcon(entity, oldIcon);
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
				return Hub.string("TD_undoChangeIcons");
			}
			else
			{
				return Hub.string("TD_undoChangeIcon");
			}
		}
	}

	/**
	 * Edit which does and undoes setting the controllability of a
	 * {@link DESEvent} in the {@link FSAModel}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetControllabilityEdit extends
			AbstractDiagramUndoableEdit
	{
		private static final long serialVersionUID = 519962377917961801L;

		/**
		 * The {@link FSAModel} which contains the {@link DESEvent} whose
		 * controllability should be set.
		 */
		protected FSAModel model = null;

		/**
		 * The id of the {@link DESEvent} whose controllability has to be set.
		 */
		protected long eventID;

		/**
		 * The new controllability setting of the event.
		 */
		protected boolean newControllable;

		/**
		 * The old controllability setting of the event.
		 */
		protected boolean oldControllable;

		/**
		 * Constructs an edit with the given parameters.
		 * 
		 * @param model
		 *            the {@link FSAModel} which contains the {@link DESEvent}
		 *            whose controllability should be set
		 * @param eventID
		 *            the id of the {@link DESEvent} whose controllability has
		 *            to be set
		 * @param isControllable
		 *            the new controllability setting for the event (if
		 *            <code>true</code>, the event will become controllable; if
		 *            <code>false</code> the event will become uncontrollable)
		 */
		public SetControllabilityEdit(FSAModel model, long eventID,
				boolean isControllable)
		{
			this.model = model;
			this.eventID = eventID;
			SupervisoryEvent event = model.getEvent(eventID);
			if (event != null)
			{
				oldControllable = event.isControllable();
			}
			newControllable = isControllable;
		}

		/**
		 * Has to be called once after the instantiation of the edit, in order
		 * to enact it.
		 */
		@Override
		public void redo() throws CannotRedoException
		{
			if (model == null)
			{
				throw new CannotRedoException();
			}
			SupervisoryEvent event = model.getEvent(eventID);
			if (event == null)
			{
				throw new CannotRedoException();
			}
			event.setControllable(newControllable);
			model.fireFSAEventSetChanged(new FSAMessage(
					FSAMessage.MODIFY,
					FSAMessage.EVENT,
					event.getId(),
					model));
		}

		@Override
		public void undo() throws CannotUndoException
		{
			if (model == null)
			{
				throw new CannotUndoException();
			}
			SupervisoryEvent event = model.getEvent(eventID);
			if (event == null)
			{
				throw new CannotUndoException();
			}
			event.setControllable(oldControllable);
			model.fireFSAEventSetChanged(new FSAMessage(
					FSAMessage.MODIFY,
					FSAMessage.EVENT,
					event.getId(),
					model));
		}

		@Override
		public boolean canUndo()
		{
			return (model != null && model.getEvent(eventID) != null);
		}

		@Override
		public boolean canRedo()
		{
			return (model != null && model.getEvent(eventID) != null);
		}

		/**
		 * Returns the name that should be displayed besides the Undo/Redo menu
		 * items, so the user knows which action will be undone/redone.
		 */
		@Override
		public String getPresentationName()
		{
			return Hub.string("TD_undoSetControllability");
		}
	}

}

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

package templates.diagram.actions;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.SimpleIcon;
import templates.diagram.TemplateDiagram;
import templates.library.Template;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.presentation.Helpers;
import templates.utils.EntityIcon;

/**
 * Collection of all undoable actions working on {@link TemplateDiagram}s.
 * 
 * @author Lenko Grigorov
 */
public class DiagramActions
{

	/**
	 * Action which creates an {@link Entity} in a {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateEntityAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4318087259767201282L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The location for the new {@link Entity}.
		 */
		protected Point location;

		/**
		 * A buffer which will store the new {@link Entity} so it can be passed
		 * back.
		 */
		protected Entity[] buffer;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param location
		 *            the location for the new {@link Entity}
		 */
		public CreateEntityAction(TemplateDiagram diagram, Point location)
		{
			this(null, diagram, location, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param location
		 *            the location for the new {@link Entity}
		 * @param buffer
		 *            the buffer which will store the new {@link Entity} so the
		 *            caller can get access to it
		 */
		public CreateEntityAction(TemplateDiagram diagram, Point location,
				Entity[] buffer)
		{
			this(null, diagram, location, buffer);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param location
		 *            the location for the new {@link Entity}
		 */
		public CreateEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Point location)
		{
			this(parent, diagram, location, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param location
		 *            the location for the new {@link Entity}
		 * @param buffer
		 *            the buffer which will store the new {@link Entity} so the
		 *            caller can get access to it
		 */
		public CreateEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Point location, Entity[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.location = location;
			this.buffer = buffer;
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

	/**
	 * Action which creates a new instance of a {@link Template} in a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateTemplateInstanceAction extends
			AbstractDiagramAction
	{
		private static final long serialVersionUID = -8369451029215842373L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The template to be instantiated.
		 */
		protected Template template;

		/**
		 * The location where the template instance should appear.
		 */
		protected Point location;

		/**
		 * A buffer which will store the new instance so it can be passed back.
		 */
		protected Entity[] buffer;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param template
		 *            the template to be instantiated
		 * @param location
		 *            the location where the template instance should appear
		 */
		public CreateTemplateInstanceAction(TemplateDiagram diagram,
				Template template, Point location)
		{
			this(null, diagram, template, location, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param template
		 *            the template to be instantiated
		 * @param location
		 *            the location where the template instance should appear
		 * @param buffer
		 *            the buffer which will store the new {@link Template}
		 *            instance so the caller can get access to it
		 */
		public CreateTemplateInstanceAction(TemplateDiagram diagram,
				Template template, Point location, Entity[] buffer)
		{
			this(null, diagram, template, location, buffer);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param template
		 *            the template to be instantiated
		 * @param location
		 *            the location where the template instance should appear
		 */
		public CreateTemplateInstanceAction(CompoundEdit parent,
				TemplateDiagram diagram, Template template, Point location)
		{
			this(parent, diagram, template, location, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param template
		 *            the template to be instantiated
		 * @param location
		 *            the location where the template instance should appear
		 * @param buffer
		 *            the buffer which will store the new {@link Template}
		 *            instance so the caller can get access to it
		 */
		public CreateTemplateInstanceAction(CompoundEdit parent,
				TemplateDiagram diagram, Template template, Point location,
				Entity[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.template = template;
			this.location = location;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				Entity[] entityBuf = new Entity[1];
				new DiagramActions.CreateEntityAction(
						allEdits,
						diagram,
						location,
						entityBuf).execute();
				new DiagramActions.AssignFSAAction(
						allEdits,
						diagram,
						entityBuf[0],
						template.instantiate(),
						template.getIcon().clone()).execute();
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						Hub.string("TD_undoCreateEntity")));
				allEdits.end();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = entityBuf[0];
				}
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which creates a {@link Connector} in a {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateConnectorAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6870236352320831902L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
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
		 * A buffer which will store the new {@link Connector} so it can be
		 * passed back.
		 */
		protected Connector[] buffer;

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 */
		public CreateConnectorAction(TemplateDiagram diagram, Entity left,
				Entity right)
		{
			this(null, diagram, left, right, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 * @param buffer
		 *            the buffer which will store the new {@link Connector} so
		 *            the caller can get access to it
		 */
		public CreateConnectorAction(TemplateDiagram diagram, Entity left,
				Entity right, Connector[] buffer)
		{
			this(null, diagram, left, right, buffer);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 */
		public CreateConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right)
		{
			this(parent, diagram, left, right, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 * @param buffer
		 *            the buffer which will store the new {@link Connector} so
		 *            the caller can get access to it
		 */
		public CreateConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right,
				Connector[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.left = left;
			this.right = right;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.CreateConnectorEdit edit = new DiagramUndoableEdits.CreateConnectorEdit(
						diagram,
						left,
						right);
				edit.redo();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = edit.getConnector();
				}
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	/**
	 * Action which creates a {@link Connector} in a {@link TemplateDiagram} and
	 * links the matching events from the linked {@link TemplateComponent}s.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateAndMatchConnectorAction extends
			AbstractDiagramAction
	{
		private static final long serialVersionUID = 2328335456634040094L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
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
		 * A buffer which will store the new {@link Connector} so it can be
		 * passed back.
		 */
		protected Connector[] buffer;

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 */
		public CreateAndMatchConnectorAction(TemplateDiagram diagram,
				Entity left, Entity right)
		{
			this(null, diagram, left, right, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 * @param buffer
		 *            the buffer which will store the new {@link Connector} so
		 *            the caller can get access to it
		 */
		public CreateAndMatchConnectorAction(TemplateDiagram diagram,
				Entity left, Entity right, Connector[] buffer)
		{
			this(null, diagram, left, right, buffer);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 */
		public CreateAndMatchConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right)
		{
			this(parent, diagram, left, right, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked entities separately.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param left
		 *            the first {@link Entity} to be linked
		 * @param right
		 *            the second {@link Entity} to be linked
		 * @param buffer
		 *            the buffer which will store the new {@link Connector} so
		 *            the caller can get access to it
		 */
		public CreateAndMatchConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right,
				Connector[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.left = left;
			this.right = right;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				Connector[] myBuffer = new Connector[1];
				new CreateConnectorAction(
						allEdits,
						diagram,
						left,
						right,
						myBuffer).execute();
				String undoLabel = allEdits.getPresentationName();
				new MatchEventsAction(allEdits, diagram, myBuffer[0]).execute();
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						undoLabel));
				allEdits.end();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = myBuffer[0];
				}
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which creates a {@link TemplateLink} between two events from the
	 * {@link TemplateComponent}s linked by a {@link Connector} and adds the new
	 * {@link TemplateLink} to the {@link Connector}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class AddLinkAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -8377001331734586265L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} to which the new {@link TemplateLink} will be
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
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked components separately.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} to which the new
		 *            {@link TemplateLink} will be added
		 * @param leftEvent
		 *            the event (from the "left" {@link TemplateComponent})
		 *            which has to be linked
		 * @param rightEvent
		 *            the event (from the "right" {@link TemplateComponent})
		 *            which has to be linked
		 */
		public AddLinkAction(TemplateDiagram diagram, Connector connector,
				String leftEvent, String rightEvent)
		{
			this(null, diagram, connector, leftEvent, rightEvent);
		}

		/**
		 * Construct an action for the given parameters.
		 * <p>
		 * A connector is symmetric. "Left" and "right" are used only to enable
		 * addressing the two linked components separately.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} to which the new
		 *            {@link TemplateLink} will be added
		 * @param leftEvent
		 *            the event (from the "left" {@link TemplateComponent})
		 *            which has to be linked
		 * @param rightEvent
		 *            the event (from the "right" {@link TemplateComponent})
		 *            which has to be linked
		 */
		public AddLinkAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector, String leftEvent, String rightEvent)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
			this.leftEvent = leftEvent;
			this.rightEvent = rightEvent;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.AddLinkEdit edit = new DiagramUndoableEdits.AddLinkEdit(
						diagram,
						connector,
						leftEvent,
						rightEvent);
				edit.redo();
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	/**
	 * Action which removes {@link DiagramElement}s from a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class DeleteElementsAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4993580265901392619L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link DiagramElement}s which need to be removed.
		 */
		protected Collection<DiagramElement> elements;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param elements
		 *            the {@link DiagramElement}s which need to be removed
		 */
		public DeleteElementsAction(TemplateDiagram diagram,
				Collection<DiagramElement> elements)
		{
			this(null, diagram, elements);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param elements
		 *            the {@link DiagramElement}s which need to be removed
		 */
		public DeleteElementsAction(CompoundEdit parent,
				TemplateDiagram diagram, Collection<DiagramElement> elements)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.elements = elements;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				int connectors = 0;
				int entities = 0;
				for (DiagramElement element : elements)
				{
					if (element instanceof Connector)
					{
						DiagramUndoableEdits.RemoveConnectorEdit edit = new DiagramUndoableEdits.RemoveConnectorEdit(
								diagram,
								(Connector)element);
						edit.redo();
						allEdits.addEdit(edit);
						connectors++;
					}
				}
				for (DiagramElement element : elements)
				{
					if (element instanceof Entity)
					{
						DiagramUndoableEdits.RemoveEntityEdit edit = new DiagramUndoableEdits.RemoveEntityEdit(
								diagram,
								(Entity)element);
						edit.redo();
						allEdits.addEdit(edit);
						entities++;
					}
				}
				if (entities > 0 && connectors > 0)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveElements")));
				}
				else if (entities > 1)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveEntities")));
				}
				else if (connectors > 1)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveConnectors")));
				}
				allEdits.end();
				postEdit(allEdits);
			}
		}
	}

	/**
	 * Action which removes {@link TemplateLink}s from a {@link Connector}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class RemoveLinksAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1134740009176987043L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} from which to remove the {@link TemplateLink}s.
		 */
		protected Connector connector;

		/**
		 * The {@link TemplateLink}s to be removed.
		 */
		protected Collection<TemplateLink> links;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} from which to remove the
		 *            {@link TemplateLink}s
		 * @param links
		 *            the {@link TemplateLink}s to be removed
		 */
		public RemoveLinksAction(TemplateDiagram diagram, Connector connector,
				Collection<TemplateLink> links)
		{
			this(null, diagram, connector, links);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} from which to remove the
		 *            {@link TemplateLink}s
		 * @param links
		 *            the {@link TemplateLink}s to be removed
		 */
		public RemoveLinksAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector, Collection<TemplateLink> links)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
			this.links = links;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null && !links.isEmpty())
			{
				CompoundEdit allEdits = new CompoundEdit();
				for (TemplateLink link : links)
				{
					DiagramUndoableEdits.RemoveLinkEdit edit = new DiagramUndoableEdits.RemoveLinkEdit(
							diagram,
							connector,
							link);
					edit.redo();
					allEdits.addEdit(edit);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which finilizes the relocation of {@link DiagramElement}s in a
	 * {@link TemplateDiagram}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class MovedSelectionAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1222866680866778507L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The displacement applied to the {@link DiagramElement}s.
		 */
		protected Point delta;

		/**
		 * The {@link DiagramElement}s which were relocated.
		 */
		protected Collection<DiagramElement> selection;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param selection
		 *            the {@link DiagramElement}s which were relocated
		 * @param delta
		 *            the displacement applied to the {@link DiagramElement}s
		 */
		public MovedSelectionAction(TemplateDiagram diagram,
				Collection<DiagramElement> selection, Point delta)
		{
			this(null, diagram, selection, delta);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param selection
		 *            the {@link DiagramElement}s which were relocated
		 * @param delta
		 *            the displacement applied to the {@link DiagramElement}s
		 */
		public MovedSelectionAction(CompoundEdit parent,
				TemplateDiagram diagram, Collection<DiagramElement> selection,
				Point delta)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.selection = selection;
			this.delta = delta;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.MovedSelectionEdit edit = new DiagramUndoableEdits.MovedSelectionEdit(
						diagram,
						selection,
						delta);
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	/**
	 * Action which sets the label of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class LabelEntityAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6200645190959701337L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} to be relabelled.
		 */
		protected Entity entity;

		/**
		 * The new label for the {@link Entity}.
		 */
		protected String label;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} to be relabelled
		 * @param label
		 *            the new label for the {@link Entity}
		 */
		public LabelEntityAction(TemplateDiagram diagram, Entity entity,
				String label)
		{
			this(null, diagram, entity, label);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} to be relabelled
		 * @param label
		 *            the new label for the {@link Entity}
		 */
		public LabelEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, String label)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.label = label;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.LabelEntityEdit edit = new DiagramUndoableEdits.LabelEntityEdit(
						diagram,
						entity,
						label);
				edit.redo();
				postEditAdjustCanvas(diagram.getModel(), diagram, edit);
			}
		}
	}

	/**
	 * Action which replaces the {@link TemplateLink}s in a {@link Connector}
	 * with {@link TemplateLink}s only between matching events.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class MatchEventsAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 897761928605656221L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Connector} whose {@link TemplateLink}s should be replaced.
		 */
		protected Connector connector;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} whose {@link TemplateLink}s should
		 *            be replaced
		 */
		public MatchEventsAction(TemplateDiagram diagram, Connector connector)
		{
			this(null, diagram, connector);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param connector
		 *            the {@link Connector} whose {@link TemplateLink}s should
		 *            be replaced
		 */
		public MatchEventsAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				Set<String> matches = Helpers.matchEvents(connector);
				CompoundEdit allEdits = new CompoundEdit();
				new RemoveLinksAction(allEdits, diagram, connector, connector
						.getLinks()).execute();
				for (String name : matches)
				{
					DiagramUndoableEdits.AddLinkEdit edit = new DiagramUndoableEdits.AddLinkEdit(
							diagram,
							connector,
							name,
							name);
					edit.redo();
					allEdits.addEdit(edit);
				}
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						Hub.string("TD_comMatchEvents")));
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which translates a {@link TemplateDiagram} so that the diagram is
	 * in view (all elements have positive co-ordinates).
	 * <p>
	 * This action is not a sublass of {@link AbstractDiagramAction} since some
	 * methods of the abstract class depend on the functionality of this action.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class ShiftDiagramInViewAction extends AbstractAction
	{
		private static final long serialVersionUID = 2907001062138002843L;

		/**
		 * If this action is not independent, the {@link UndoableEdit} the
		 * action is part of. <code>null</code> if this action is independent.
		 */
		protected CompoundEdit parentEdit = null;

		/**
		 * The {@link TemplateDiagram} to be translated.
		 */
		protected TemplateDiagram diagram;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be translated
		 */
		public ShiftDiagramInViewAction(TemplateDiagram diagram)
		{
			this(null, diagram);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parentEdit
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be translated
		 */
		public ShiftDiagramInViewAction(CompoundEdit parentEdit,
				TemplateDiagram diagram)
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
							new Point(
									-bounds.x
											+ TemplateDiagram.DESIRED_DIAGRAM_INSET,
									-bounds.y
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

		/**
		 * Perform the action.
		 */
		public void execute()
		{
			actionPerformed(null);
		}
	}

	/**
	 * Action which assigns a blank {@link FSAModel} to the
	 * {@link TemplateComponent} of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class AssignNewFSAAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 9167035481992348194L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} whose {@link TemplateComponent} is to be assigned
		 * a blank {@link FSAModel}.
		 */
		protected Entity entity;

		/**
		 * A buffer which will store the new {@link FSAModel} so it can be
		 * passed back.
		 */
		protected FSAModel[] buffer;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned a blank {@link FSAModel}
		 */
		public AssignNewFSAAction(TemplateDiagram diagram, Entity entity)
		{
			this(null, diagram, entity, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned a blank {@link FSAModel}
		 * @param buffer
		 *            the buffer which will store the new {@link FSAModel} so
		 *            the caller can get access to it
		 */
		public AssignNewFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel[] buffer)
		{
			this(null, diagram, entity, buffer);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned a blank {@link FSAModel}
		 */
		public AssignNewFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity)
		{
			this(parent, diagram, entity, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned a blank {@link FSAModel}
		 * @param buffer
		 *            the buffer which will store the new {@link FSAModel} so
		 *            the caller can get access to it
		 */
		public AssignNewFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				FSAModel newModel = ModelManager
						.instance().createModel(FSAModel.class);
				if (newModel == null)
				{
					Hub.getNoticeManager().postErrorTemporary(Hub
							.string("TD_shortFSAnotSupported"),
							Hub.string("TD_FSAnotSupported"));
				}
				else
				{
					// add all linked events
					for (Connector c : diagram.getAdjacentConnectors(entity))
					{
						for (TemplateLink link : c.getLinks())
						{
							String event = c.getLeftEntity() == entity ? link
									.getLeftEventName() : link
									.getRightEventName();
							newModel.add(newModel.assembleEvent(event));
						}
					}
					CompoundEdit allEdits = new CompoundEdit();
					DiagramUndoableEdits.AssignFSAEdit edit = new DiagramUndoableEdits.AssignFSAEdit(
							diagram,
							entity,
							newModel);
					edit.redo();
					allEdits.addEdit(edit);
					DiagramUndoableEdits.SetIconEdit iconEdit = new DiagramUndoableEdits.SetIconEdit(
							diagram,
							entity,
							new SimpleIcon());
					iconEdit.redo();
					allEdits.addEdit(iconEdit);
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									edit.getPresentationName()));
					allEdits.end();
					if (buffer != null && buffer.length > 0)
					{
						buffer[0] = newModel;
					}
					postEditAdjustCanvas(diagram, allEdits);
				}
			}
		}
	}

	/**
	 * Action which assigns an {@link FSAModel} to the {@link TemplateComponent}
	 * of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class AssignFSAAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 7311186809287532347L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} whose {@link TemplateComponent} is to be assigned
		 * the {@link FSAModel}.
		 */
		protected Entity entity;

		/**
		 * The {@link FSAModel} to be assigned.
		 */
		protected FSAModel fsa;

		/**
		 * The new icon for the {@link Entity}.
		 */
		protected EntityIcon icon;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned the {@link FSAModel}
		 * @param fsa
		 *            the {@link FSAModel} to be assigned
		 */
		public AssignFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel fsa)
		{
			this(null, diagram, entity, fsa);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned the {@link FSAModel}
		 * @param fsa
		 *            the {@link FSAModel} to be assigned
		 * @param icon
		 *            the new icon for the {@link Entity}
		 */
		public AssignFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel fsa, EntityIcon icon)
		{
			this(null, diagram, entity, fsa, icon);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned the {@link FSAModel}
		 * @param fsa
		 *            the {@link FSAModel} to be assigned
		 */
		public AssignFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel fsa)
		{
			this(parent, diagram, entity, fsa, null);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} whose {@link TemplateComponent} is to
		 *            be assigned the {@link FSAModel}
		 * @param fsa
		 *            the {@link FSAModel} to be assigned
		 * @param icon
		 *            the new icon for the {@link Entity}
		 */
		public AssignFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel fsa, EntityIcon icon)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.fsa = fsa;
			if (icon == null)
			{
				icon = new SimpleIcon();
			}
			this.icon = icon;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				DiagramUndoableEdits.AssignFSAEdit edit = new DiagramUndoableEdits.AssignFSAEdit(
						diagram,
						entity,
						fsa);
				edit.redo();
				allEdits.addEdit(edit);
				DiagramUndoableEdits.SetIconEdit iconEdit = new DiagramUndoableEdits.SetIconEdit(
						diagram,
						entity,
						icon);
				iconEdit.redo();
				allEdits.addEdit(iconEdit);
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						edit.getPresentationName()));
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which sets the type (<i>module</i> or <i>channel</i>) of the
	 * {@link TemplateComponent} of an {@link Entity}.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetTypeAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1774285258523292831L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity} which represents the {@link TemplateComponent}
		 * whose type has to be set.
		 */
		protected Entity entity;

		/**
		 * The type to be set ({@link TemplateComponent#TYPE_MODULE} or
		 * {@link TemplateComponent#TYPE_CHANNEL}).
		 */
		protected int type;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} which represents the
		 *            {@link TemplateComponent} whose type has to be set
		 * @param type
		 *            the type to be set ({@link TemplateComponent#TYPE_MODULE}
		 *            or {@link TemplateComponent#TYPE_CHANNEL})
		 */
		public SetTypeAction(TemplateDiagram diagram, Entity entity, int type)
		{
			this(null, diagram, entity, type);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entity
		 *            the {@link Entity} which represents the
		 *            {@link TemplateComponent} whose type has to be set
		 * @param type
		 *            the type to be set ({@link TemplateComponent#TYPE_MODULE}
		 *            or {@link TemplateComponent#TYPE_CHANNEL})
		 */
		public SetTypeAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, int type)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.type = type;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.SetTypeEdit edit = new DiagramUndoableEdits.SetTypeEdit(
						diagram,
						entity,
						type);
				edit.redo();
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	/**
	 * Action which sets the background color of the icons of {@link Entity}s.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetIconColorAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 8626414278661880945L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity}s to whose icons the background color has to be
		 * set.
		 */
		protected Collection<Entity> entities;

		/**
		 * The color for the background of the icons.
		 */
		protected Color color;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entities
		 *            the {@link Entity}s to whose icons the background color
		 *            has to be set
		 * @param color
		 *            the color for the background of the icons
		 */
		public SetIconColorAction(TemplateDiagram diagram,
				Collection<Entity> entities, Color color)
		{
			this(null, diagram, entities, color);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entities
		 *            the {@link Entity}s to whose icons the background color
		 *            has to be set
		 * @param color
		 *            the color for the background of the icons
		 */
		public SetIconColorAction(CompoundEdit parent, TemplateDiagram diagram,
				Collection<Entity> entities, Color color)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entities = entities;
			this.color = color;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null && !entities.isEmpty())
			{
				CompoundEdit allEdits = new CompoundEdit();
				DiagramUndoableEdits.SetIconEdit edit = null;
				for (Entity entity : entities)
				{
					EntityIcon icon = entity.getIcon().clone();
					icon.setColor(color);
					edit = new DiagramUndoableEdits.SetIconEdit(
							diagram,
							entity,
							icon);
					edit.redo();
					allEdits.addEdit(edit);
				}
				if (entities.size() > 1)
				{
					edit.setLastOfMultiple(true);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	/**
	 * Action which resets the icons of {@link Entity}s to their default form.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class DefaultIconAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6507813585595950277L;

		/**
		 * The {@link TemplateDiagram} context to be used by the action.
		 */
		protected TemplateDiagram diagram;

		/**
		 * The {@link Entity}s whose icons have to be reset.
		 */
		protected Collection<Entity> entities;

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entities
		 *            the {@link Entity}s whose icons have to be reset
		 */
		public DefaultIconAction(TemplateDiagram diagram,
				Collection<Entity> entities)
		{
			this(null, diagram, entities);
		}

		/**
		 * Construct an action for the given parameters.
		 * 
		 * @param parent
		 *            the {@link UndoableEdit} to which this action should add
		 *            its own undo information
		 * @param diagram
		 *            the {@link TemplateDiagram} to be used by the action
		 * @param entities
		 *            the {@link Entity}s whose icons have to be reset
		 */
		public DefaultIconAction(CompoundEdit parent, TemplateDiagram diagram,
				Collection<Entity> entities)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entities = entities;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null && !entities.isEmpty())
			{
				CompoundEdit allEdits = new CompoundEdit();
				DiagramUndoableEdits.SetIconEdit edit = null;
				for (Entity entity : entities)
				{
					edit = new DiagramUndoableEdits.SetIconEdit(
							diagram,
							entity,
							new SimpleIcon());
					edit.redo();
					allEdits.addEdit(edit);
				}
				if (entities.size() > 1)
				{
					edit.setLastOfMultiple(true);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}
}

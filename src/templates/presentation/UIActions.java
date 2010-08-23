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

package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;
import ides.api.utilities.GeneralUtils;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.undo.CompoundEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.diagram.actions.DiagramUndoableEdits;
import templates.library.AddTemplateDialog;
import templates.library.TemplateManager;
import templates.model.TemplateComponent;
import templates.model.TemplateModel;
import templates.model.Validator;

/**
 * Collection of the actions available in the UI while working on a
 * {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class UIActions
{
	/**
	 * Action to create a new {@link Entity} in a template design.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class CreateEntityAction extends AbstractAction
	{
		private static final long serialVersionUID = 6528162027263301199L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which will contain the entity.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The location for the new entity.
		 */
		private Point location;

		/**
		 * Construct an action to add a new {@link Entity} at the given
		 * location.
		 * 
		 * @param canvas
		 *            the canvas which will contain the entity
		 * @param location
		 *            the location for the new entity
		 */
		public CreateEntityAction(TemplateEditableCanvas canvas, Point location)
		{
			super(Hub.string("TD_comCreateEntity"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintCreateEntity"));
			this.canvas = canvas;
			this.location = location;
		}

		/**
		 * Create a new {@link Entity} at the given location.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.CreateEntityAction(canvas.getDiagram(), location)
					.execute();
		}
	}

	/**
	 * Action to load and activate in IDES the underlying model of a template
	 * component.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class OpenModelAction extends AbstractAction
	{
		private static final long serialVersionUID = -1754004325055513445L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity for the template component whose
		 * underlying model has to be loaded and activated.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity for the template component whose underlying model has to
		 * be loaded and activated.
		 */
		private Entity entity;

		/**
		 * Construct an action to load and activate in IDES the underlying model
		 * of the specified template component.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity for the template
		 *            component whose underlying model has to be loaded and
		 *            activated
		 * @param entity
		 *            the entity for the template component whose underlying
		 *            model has to be loaded and activated
		 */
		public OpenModelAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comOpenModel"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintOpenModel"));
			this.canvas = canvas;
			this.entity = entity;
			setEnabled(entity.getComponent().hasModel());
		}

		/**
		 * Load and activate in IDES the underlying model of the specified
		 * template component. If the model is already loaded, only activate it.
		 * Do nothing if the template component does not have an underlying
		 * model.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			FSAModel fsa = entity.getComponent().getModel();
			if (fsa == null)
			{
				return;
			}
			if (Hub.getWorkspace().getModel(fsa.getName()) != fsa)
			{
				Hub.getWorkspace().addModel(fsa);
			}
			Hub.getWorkspace().setActiveModel(fsa.getName());
		}
	}

	/**
	 * Action to display the entity labelling dialog and let the user relabel an
	 * {@link Entity}.
	 * 
	 * @see EntityLabellingDialog
	 * @author Lenko Grigorov
	 */
	public static class LabelAction extends AbstractAction
	{
		private static final long serialVersionUID = 5645153856255010227L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity which will be relabelled.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity which will be relabelled.
		 */
		private Entity entity;

		/**
		 * Construct an action to display the entity labelling dialog and let
		 * the user relabel the given {@link Entity}.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity which will be
		 *            relabelled
		 * @param entity
		 *            the entity which will be relabelled
		 */
		public LabelAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comLabelEntity"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintLabelEntity"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Display the entity labelling dialog to let the user relabel the given
		 * {@link Entity}.
		 * 
		 * @see EntityLabellingDialog
		 */
		public void actionPerformed(ActionEvent evt)
		{
			EntityLabellingDialog.showAndLabel(canvas, entity);
		}

	}

	/**
	 * An action to delete a diagram element from a template diagram.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class DeleteAction extends AbstractAction
	{
		private static final long serialVersionUID = 5645153856255010227L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the diagram element to be deleted.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The diagram element to be deleted.
		 */
		private DiagramElement element;

		/**
		 * Construct an action to delete a given diagram element from a given
		 * template diagram.
		 * 
		 * @param canvas
		 *            the canvas which contains the diagram element to be
		 *            deleted
		 * @param element
		 *            the diagram element to be deleted
		 */
		public DeleteAction(TemplateEditableCanvas canvas,
				DiagramElement element)
		{
			super(Hub.string("TD_comDelete"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDelete"));
			this.canvas = canvas;
			this.element = element;
		}

		/**
		 * Deletes the given template element from the template diagram.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.DeleteElementsAction(canvas.getDiagram(), Arrays
					.asList(new DiagramElement[] { element })).execute();
		}

	}

	/**
	 * An action to display the dialog for specifying the linking of events
	 * between the template components connected by a connector.
	 * 
	 * @see EventLinksDialog
	 * @author Lenko Grigorov
	 */
	public static class EventLinksAction extends AbstractAction
	{
		private static final long serialVersionUID = 3909208160590384130L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the connector whose events links will be
		 * specified.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The connector whose events links will be specified.
		 */
		private Connector connector;

		/**
		 * Construct an action to display the dialog for specifying the linking
		 * of events between the template components connected by the given
		 * connector.
		 * 
		 * @param canvas
		 *            the canvas which contains the connector whose events links
		 *            will be specified
		 * @param connector
		 *            the connector whose events links will be specified
		 */
		public EventLinksAction(TemplateEditableCanvas canvas,
				Connector connector)
		{
			super(Hub.string("TD_comAssignEvents"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignEvents"));
			this.canvas = canvas;
			this.connector = connector;
		}

		/**
		 * Display the dialog for specifying the linking of events between the
		 * template components connected by the connector.
		 * 
		 * @see EventLinksDialog
		 */
		public void actionPerformed(ActionEvent evt)
		{
			EventLinksDialog.showAndAssign(canvas, connector);
		}
	}

	/**
	 * An action to display the dialog for assigning an FSA model to a template
	 * component.
	 * 
	 * @see AssignFSADialog
	 * @author Lenko Grigorov
	 */
	public static class AssignFSAAction extends AbstractAction
	{
		private static final long serialVersionUID = 5869946508125826812L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity to whose template component an
		 * FSA model will be assigned.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity to whose template component an FSA model will be assigned.
		 */
		private Entity entity;

		/**
		 * Construct an action to display the dialog for assigning an FSA model
		 * to the given template component.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity to whose template
		 *            component an FSA model will be assigned
		 * @param entity
		 *            the entity to whose template component an FSA model will
		 *            be assigned
		 */
		public AssignFSAAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comAssignFSA"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignFSA"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Display the dialog for assigning an FSA model to the template
		 * component.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			AssignFSADialog.showAndAssign(canvas, entity);
		}
	}

	/**
	 * An action to remove all links between the events of the template
	 * components connected by a connector.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class DeleteAllLinksAction extends AbstractAction
	{
		private static final long serialVersionUID = 909787927503152877L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the connector whose event links will be
		 * removed.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The connector whose event links will be removed.
		 */
		private Connector connector;

		/**
		 * Construct an action to remove all links between the events of the
		 * template components connected by the given connector.
		 * 
		 * @param canvas
		 *            the canvas which contains the connector whose event links
		 *            will be removed
		 * @param connector
		 *            the connector whose event links will be removed
		 */
		public DeleteAllLinksAction(TemplateEditableCanvas canvas,
				Connector connector)
		{
			super(Hub.string("TD_comDeleteAllLinks"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDeleteAllLinks"));
			this.canvas = canvas;
			this.connector = connector;
		}

		/**
		 * Remove all links between the events of the template components
		 * connected by the connector.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			CompoundEdit allEdits = new CompoundEdit();
			new DiagramActions.RemoveLinksAction(
					allEdits,
					canvas.getDiagram(),
					connector,
					connector.getLinks()).execute();
			allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(Hub
					.string("TD_comDeleteAllLinks")));
			allEdits.end();
			Hub.getUndoManager().addEdit(allEdits);
		}
	}

	/**
	 * An action to replace all links between the template components connected
	 * by a connector with links only between the events with matching names.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class MatchEventsAction extends AbstractAction
	{
		private static final long serialVersionUID = 621843884453574726L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the connector whose event links will be
		 * replaced.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The connector whose event links will be replaced.
		 */
		private Connector connector;

		/**
		 * Construct an action to replace all links between the template
		 * components connected by the given connector with links only between
		 * the events with matching names.
		 * 
		 * @param canvas
		 *            the canvas which contains the connector whose event links
		 *            will be replaced
		 * @param connector
		 *            the connector whose event links will be replaced
		 */
		public MatchEventsAction(TemplateEditableCanvas canvas,
				Connector connector)
		{
			super(Hub.string("TD_comMatchEvents"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintMatchEvents"));
			this.canvas = canvas;
			this.connector = connector;
		}

		/**
		 * Replace all links between the template components connected by the
		 * connector with links only between the events with matching names. If
		 * there are no matching events, this action will result in the removal
		 * of all event links between the template components.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.MatchEventsAction(canvas.getDiagram(), connector)
					.execute();
		}
	}

	/**
	 * An action to set the type of a template component to
	 * {@link TemplateComponent#TYPE_MODULE} (module).
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetModuleAction extends AbstractAction
	{
		private static final long serialVersionUID = -5184006756790356068L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the template component whose type will be
		 * set.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The {@link Entity} for the template component whose type will be set.
		 */
		private Entity entity;

		/**
		 * Construct an action to set the type of the given template component
		 * to {@link TemplateComponent#TYPE_MODULE} (module).
		 * 
		 * @param canvas
		 *            the canvas which contains the template component whose
		 *            type will be set
		 * @param entity
		 *            the {@link Entity} for the template component whose type
		 *            will be set
		 */
		public SetModuleAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetModule"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetModule"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Set the type of the template component to
		 * {@link TemplateComponent#TYPE_MODULE} (module).
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.SetTypeAction(
					canvas.getDiagram(),
					entity,
					TemplateComponent.TYPE_MODULE).execute();
		}
	}

	/**
	 * An action to set the type of a template component to
	 * {@link TemplateComponent#TYPE_CHANNEL} (channel).
	 * 
	 * @author Lenko Grigorov
	 */
	public static class SetChannelAction extends AbstractAction
	{
		private static final long serialVersionUID = 9154827758752183093L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the template component whose type will be
		 * set.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The {@link Entity} for the template component whose type will be set.
		 */
		private Entity entity;

		/**
		 * Construct an action to set the type of the given template component
		 * to {@link TemplateComponent#TYPE_CHANNEL} (channel).
		 * 
		 * @param canvas
		 *            the canvas which contains the template component whose
		 *            type will be set
		 * @param entity
		 *            the {@link Entity} for the template component whose type
		 *            will be set
		 */
		public SetChannelAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetChannel"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetChannel"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Set the type of the template component to
		 * {@link TemplateComponent#TYPE_CHANNEL} (channel).
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.SetTypeAction(
					canvas.getDiagram(),
					entity,
					TemplateComponent.TYPE_CHANNEL).execute();
		}
	}

	/**
	 * An action to display the color chooser dialog to let the user select the
	 * background color of the icon of an entity.
	 * 
	 * @see JColorChooser
	 * @author Lenko Grigorov
	 */
	public static class SetColorAction extends AbstractAction
	{
		private static final long serialVersionUID = 276229727963021742L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity whose background color will be
		 * changed.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity whose background color will be changed.
		 */
		private Entity entity;

		/**
		 * Construct an action to display the color chooser dialog to let the
		 * user select the background color of the icon of the given entity.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity whose background
		 *            color will be changed
		 * @param entity
		 *            the entity whose background color will be changed
		 */
		public SetColorAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetColor"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetColor"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Display the color chooser dialog to let the user select the
		 * background color of the icon of the entity.
		 * 
		 * @see JColorChooser
		 */
		public void actionPerformed(ActionEvent evt)
		{
			Color color = JColorChooser.showDialog(Hub.getMainWindow(), Hub
					.string("TD_colorBoxTitle"), entity.getIcon().getColor());
			if (color != null)
			{
				new DiagramActions.SetIconColorAction(
						canvas.getDiagram(),
						Arrays.asList(new Entity[] { entity }),
						color).execute();
			}
		}
	}

	/**
	 * An action to reset the icon of an entity to its default form.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class ResetIconAction extends AbstractAction
	{
		private static final long serialVersionUID = 3216719161898588398L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity whose icon will be reset.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity whose icon will be reset.
		 */
		private Entity entity;

		/**
		 * Construct an action to reset the icon of the given entity to its
		 * default form.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity whose icon will be
		 *            reset
		 * @param entity
		 *            the entity whose icon will be reset
		 */
		public ResetIconAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comDefaultIcon"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDefaultIcon"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Reset the icon of the entity to its default form.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.DefaultIconAction(canvas.getDiagram(), Arrays
					.asList(new Entity[] { entity })).execute();
		}
	}

	/**
	 * An action to create a new template based on an entity in a template
	 * design.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class MakeTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = 3531359340371698458L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The entity on which to base the new template.
		 */
		private Entity entity;

		/**
		 * Construct an action to create a new template based on the given
		 * entity.
		 * 
		 * @param entity
		 *            the entity on which to base the new template
		 */
		public MakeTemplateAction(Entity entity)
		{
			super(Hub.string("TD_comMakeTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintMakeTemplate"));
			this.entity = entity;
		}

		/**
		 * Create a new template based on the entity.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			if (entity.getComponent().hasModel())
			{
				AddTemplateDialog.addTemplate(TemplateManager
						.instance().getMainLibrary(), entity
						.getComponent().getModel());
			}
		}
	}

	/**
	 * An action to compute and display the supervisor for a channel in a
	 * template design.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class ShowSupAction extends AbstractAction
	{
		private static final long serialVersionUID = -4796270429679682331L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the channel for which a supervisor will be
		 * computed.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The channel for which a supervisor will be computed.
		 */
		private Entity channel;

		/**
		 * Construct an action to compute and display the supervisor for the
		 * given channel.
		 * 
		 * @param canvas
		 *            the canvas which contains the channel for which a
		 *            supervisor will be computed
		 * @param channel
		 *            the channel for which a supervisor will be computed
		 */
		public ShowSupAction(TemplateEditableCanvas canvas, Entity channel)
		{
			super(Hub.string("TD_comShowSup"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintShowSup"));
			this.canvas = canvas;
			this.channel = channel;
		}

		/**
		 * Compute and display the supervisor for the channel. If there are
		 * consistency issues in the template design involving the channel, warn
		 * the user and do nothing.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			if (!Validator.canComputeSup(canvas.getDiagram().getModel(),
					channel.getComponent().getId()))
			{
				Hub.displayAlert(Hub.string("TD_cantComputeSup"));
				return;
			}
			Operation channelsup = OperationManager
					.instance().getOperation("tdchannelsup");
			Object[] result = channelsup.perform(new Object[] {
					canvas.getModel(), channel.getComponent().getId() });
			FSAModel sys = (FSAModel)result[0];
			FSAModel spec = (FSAModel)result[1];
			FSAModel sup = (FSAModel)result[2];
			sys.setName("M_" + channel.getLabel());
			spec.setName("C_" + channel.getLabel());
			sup.setName("S_" + channel.getLabel());
			Hub.setUserTextAnnotation(sys, channelsup.getName() + "("
					+ channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[0]);
			Hub.setUserTextAnnotation(spec, channelsup.getName() + "("
					+ channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[1]);
			Hub.setUserTextAnnotation(sup, channelsup.getName() + "("
					+ channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[2]);
			Hub.getWorkspace().addModel(sys);
			Hub.getWorkspace().addModel(spec);
			Hub.getWorkspace().addModel(sup);
			Hub.getWorkspace().setActiveModel(sup.getName());
			if (!channelsup.getWarnings().isEmpty())
			{
				String warning = "";
				for (String w : channelsup.getWarnings())
				{
					warning += w + "\n";
				}
				Hub.displayAlert(GeneralUtils.truncateMessage(warning));
			}
		}
	}

	/**
	 * An action to start drawing a new connector from an entity in a template
	 * design.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class ConnectAction extends AbstractAction
	{
		private static final long serialVersionUID = -5487737966864595772L;

		// private static ImageIcon icon = new ImageIcon();

		/**
		 * The canvas which contains the entity where the new connector will
		 * originate.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The entity where the new connector will originate.
		 */
		private Entity entity;

		/**
		 * Construct an action to start drawing a new connector from the given
		 * entity.
		 * 
		 * @param canvas
		 *            the canvas which contains the entity where the new
		 *            connector will originate
		 * @param entity
		 *            the entity where the new connector will originate
		 */
		public ConnectAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comConnect"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintConnect"));
			this.canvas = canvas;
			this.entity = entity;
		}

		/**
		 * Start drawing a new connector from the entity.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			if (canvas.isDrawingConnector())
			{
				canvas.finishConnector();
				canvas.repaint();
			}
			Point p = canvas.localToComponent(entity.getPorts()[3]);
			MouseEvent me = new MouseEvent(
					canvas,
					p.hashCode(),
					System.currentTimeMillis(),
					InputEvent.BUTTON1_DOWN_MASK,
					(int)p.x,
					(int)p.y,
					0,
					false,
					MouseEvent.BUTTON1);
			canvas.mousePressed(me);
			me = new MouseEvent(
					canvas,
					me.hashCode(),
					System.currentTimeMillis(),
					InputEvent.BUTTON1_MASK,
					(int)p.x,
					(int)p.y,
					1,
					false,
					MouseEvent.BUTTON1);
			canvas.mouseReleased(me);
		}
	}

	/**
	 * @deprecated This class is not maintained. It might be used in future
	 *             releases of the Template Design plugin.
	 * @author Lenko Grigorov
	 */
	@Deprecated
	public static class SetControllabilityAction extends AbstractAction
	{

		private static final long serialVersionUID = 35724268661411961L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity channel;

		public SetControllabilityAction(TemplateEditableCanvas canvas,
				Entity channel)
		{
			super(Hub.string("TD_comSetControllability"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub
					.string("TD_comHintSetControllability"));
			this.canvas = canvas;
			this.channel = channel;
		}

		public void actionPerformed(ActionEvent evt)
		{
			if (!channel.getComponent().hasModel())
			{
				Hub.displayAlert(Hub.string("TD_noModelNoEvents"));
			}
			else
			{
				ControllabilityDialog.showAndModify(canvas, channel);
			}
		}
	}
}

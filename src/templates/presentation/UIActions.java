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

package templates.presentation;

import ides.api.core.Annotable;
import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;

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
 * Collection of the actions available in the UI while working on a {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class UIActions
{
	public static class CreateEntityAction extends AbstractAction
	{
		private static final long serialVersionUID = 6528162027263301199L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Point location;

		public CreateEntityAction(TemplateEditableCanvas canvas, Point location)
		{
			super(Hub.string("TD_comCreateEntity"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintCreateEntity"));
			this.canvas = canvas;
			this.location = location;
		}

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.CreateEntityAction(canvas.getDiagram(), location)
					.execute();
		}
	}

	public static class OpenModelAction extends AbstractAction
	{
		private static final long serialVersionUID = -1754004325055513445L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

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

	public static class LabelAction extends AbstractAction
	{
		private static final long serialVersionUID = 5645153856255010227L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public LabelAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comLabelEntity"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintLabelEntity"));
			this.canvas = canvas;
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent evt)
		{
			EntityLabellingDialog.showAndLabel(canvas, entity);
		}

	}

	public static class DeleteAction extends AbstractAction
	{
		private static final long serialVersionUID = 5645153856255010227L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private DiagramElement element;

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

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.DeleteElementsAction(canvas.getDiagram(), Arrays
					.asList(new DiagramElement[] { element })).execute();
		}

	}

	public static class AssignEventsAction extends AbstractAction
	{
		private static final long serialVersionUID = 3909208160590384130L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Connector connector;

		public AssignEventsAction(TemplateEditableCanvas canvas,
				Connector connector)
		{
			super(Hub.string("TD_comAssignEvents"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignEvents"));
			this.canvas = canvas;
			this.connector = connector;
		}

		public void actionPerformed(ActionEvent evt)
		{
			EventLinksDialog.showAndAssign(canvas, connector);
		}
	}

	public static class AssignFSAAction extends AbstractAction
	{
		private static final long serialVersionUID = 5869946508125826812L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public AssignFSAAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comAssignFSA"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignFSA"));
			this.canvas = canvas;
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent evt)
		{
			AssignFSADialog.showAndAssign(canvas, entity);
		}
	}

	public static class DeleteAllLinksAction extends AbstractAction
	{
		private static final long serialVersionUID = 909787927503152877L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Connector connector;

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

	public static class MatchEventsAction extends AbstractAction
	{
		private static final long serialVersionUID = 621843884453574726L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Connector connector;

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

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.MatchEventsAction(canvas.getDiagram(), connector)
					.execute();
		}
	}

	public static class SetModuleAction extends AbstractAction
	{
		private static final long serialVersionUID = -5184006756790356068L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public SetModuleAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetModule"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetModule"));
			this.canvas = canvas;
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.SetTypeAction(
					canvas.getDiagram(),
					entity,
					TemplateComponent.TYPE_MODULE).execute();
		}
	}

	public static class SetChannelAction extends AbstractAction
	{
		private static final long serialVersionUID = 9154827758752183093L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public SetChannelAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetChannel"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetChannel"));
			this.canvas = canvas;
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.SetTypeAction(
					canvas.getDiagram(),
					entity,
					TemplateComponent.TYPE_CHANNEL).execute();
		}
	}

	public static class SetColorAction extends AbstractAction
	{
		private static final long serialVersionUID = 276229727963021742L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public SetColorAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comSetColor"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintSetColor"));
			this.canvas = canvas;
			this.entity = entity;
		}

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

	public static class ResetIconAction extends AbstractAction
	{
		private static final long serialVersionUID = 3216719161898588398L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public ResetIconAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comDefaultIcon"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDefaultIcon"));
			this.canvas = canvas;
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.DefaultIconAction(canvas.getDiagram(), Arrays
					.asList(new Entity[] { entity })).execute();
		}
	}

	public static class MakeTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = 3531359340371698458L;

		// private static ImageIcon icon = new ImageIcon();

		private Entity entity;

		public MakeTemplateAction(Entity entity)
		{
			super(Hub.string("TD_comMakeTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintMakeTemplate"));
			this.entity = entity;
		}

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

	public static class ShowSupAction extends AbstractAction
	{
		private static final long serialVersionUID = -4796270429679682331L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity channel;

		public ShowSupAction(TemplateEditableCanvas canvas, Entity channel)
		{
			super(Hub.string("TD_comShowSup"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintShowSup"));
			this.canvas = canvas;
			this.channel = channel;
		}

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
			sys.setAnnotation(Annotable.TEXT_ANNOTATION, channelsup.getName()
					+ "(" + channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[0]);
			spec.setAnnotation(Annotable.TEXT_ANNOTATION, channelsup.getName()
					+ "(" + channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[1]);
			sup.setAnnotation(Annotable.TEXT_ANNOTATION, channelsup.getName()
					+ "(" + channel.getLabel() + "): "
					+ channelsup.getDescriptionOfOutputs()[2]);
			Hub.getWorkspace().addModel(sys);
			Hub.getWorkspace().addModel(spec);
			Hub.getWorkspace().addModel(sup);
			Hub.getWorkspace().setActiveModel(sup.getName());
		}
	}

	public static class ConnectAction extends AbstractAction
	{
		private static final long serialVersionUID = -5487737966864595772L;

		// private static ImageIcon icon = new ImageIcon();

		protected TemplateEditableCanvas canvas;

		private Entity entity;

		public ConnectAction(TemplateEditableCanvas canvas, Entity entity)
		{
			super(Hub.string("TD_comConnect"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintConnect"));
			this.canvas = canvas;
			this.entity = entity;
		}

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
	 * @deprecated This class is not maintained. It might be used in future releases of the Template Design plugin.
	 * @author Lenko Grigorov
	 *
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

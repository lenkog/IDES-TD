package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.diagram.actions.DiagramUndoableEdits;
import templates.model.TemplateComponent;
import templates.model.Validator;

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
			AssignEventsDialog.showAndAssign(canvas, connector);
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
					TemplateComponent.TYPE_MODULE).actionPerformed(evt);
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
					TemplateComponent.TYPE_CHANNEL).actionPerformed(evt);
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
			if(!Validator.canComputeSup(canvas.getDiagram().getModel(),channel.getComponent().getId()))
			{
				Hub.displayAlert(Hub.string("TD_cantComputeSup"));
			}
		}
	}
}

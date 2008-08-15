package templates.presentation;

import ides.api.core.Hub;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.diagram.actions.DiagramUndoableEdits;

public class UIActions
{
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

		public AssignFSAAction(TemplateEditableCanvas canvas,
				Entity entity)
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
			AssignFSADialog.showAndAssign(canvas,entity);
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
			this.connector=connector;
		}

		public void actionPerformed(ActionEvent evt)
		{
			CompoundEdit allEdits=new CompoundEdit();
			new DiagramActions.RemoveLinksAction(allEdits,canvas.getDiagram(),connector,connector.getLinks()).execute();
			allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(Hub.string("TD_comDeleteAllLinks")));
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
			this.connector=connector;
		}

		public void actionPerformed(ActionEvent evt)
		{
			new DiagramActions.MatchEventsAction(canvas.getDiagram(),connector).execute();
		}
	}
}

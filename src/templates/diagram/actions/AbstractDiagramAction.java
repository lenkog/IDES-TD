package templates.diagram.actions;

import ides.api.core.Hub;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import templates.diagram.TemplateDiagram;

public abstract class AbstractDiagramAction extends AbstractAction
{
	private static final long serialVersionUID = 7619598556267497405L;

	protected CompoundEdit parentEdit = null;

	protected boolean usePluralDescription = false;

	public AbstractDiagramAction()
	{
		super();
	}

	public AbstractDiagramAction(String arg0, Icon arg1)
	{
		super(arg0, arg1);
	}

	public AbstractDiagramAction(String arg0)
	{
		super(arg0);
	}

	protected void postEdit(UndoableEdit edit)
	{
		if (usePluralDescription && edit instanceof AbstractDiagramUndoableEdit)
		{
			((AbstractDiagramUndoableEdit)edit).setLastOfMultiple(true);
		}
		if (parentEdit != null)
		{
			parentEdit.addEdit(edit);
		}
		else
		{
			Hub.getUndoManager().addEdit(edit);
		}
	}

	protected void postEditAdjustCanvas(TemplateDiagram diagram,
			UndoableEdit edit)
	{
		postEdit(addBoundsAdjust(diagram, edit));
	}

	protected UndoableEdit addBoundsAdjust(TemplateDiagram diagram,
			UndoableEdit edit)
	{
		CompoundEdit adjEdit = new CompoundEdit();
		adjEdit.addEdit(edit);
		new DiagramActions.ShiftDiagramInViewAction(adjEdit, diagram).execute();
		adjEdit.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(edit
				.getPresentationName()));
		adjEdit.end();
		return adjEdit;
	}

	public void setLastOfMultiple(boolean b)
	{
		usePluralDescription = b;
	}

	public void execute()
	{
		actionPerformed(null);
	}
}

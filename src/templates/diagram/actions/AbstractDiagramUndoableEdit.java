package templates.diagram.actions;

import javax.swing.undo.AbstractUndoableEdit;

public abstract class AbstractDiagramUndoableEdit extends AbstractUndoableEdit
{
	private static final long serialVersionUID = -7752002813977055908L;
	
	protected boolean usePluralDescription = false;

	public void setLastOfMultiple(boolean b)
	{
		usePluralDescription = b;
	}
}

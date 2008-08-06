package templates.presentation;

import javax.swing.JPopupMenu;

import templates.diagram.Entity;

public class EntityPopup extends JPopupMenu
{
	private static final long serialVersionUID = -2569273570320479180L;

	public EntityPopup(TemplateEditableCanvas canvas, Entity entity)
	{
		super();
		add(new UIActions.LabelAction(canvas, entity));
		add(new UIActions.DeleteAction(canvas, entity));
		pack();
	}

}

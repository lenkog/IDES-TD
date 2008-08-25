package templates.presentation;

import javax.swing.JPopupMenu;

import templates.diagram.Entity;
import templates.model.TemplateComponent;

public class EntityPopup extends JPopupMenu
{
	private static final long serialVersionUID = -2569273570320479180L;

	public EntityPopup(TemplateEditableCanvas canvas, Entity entity)
	{
		super();
		add(new UIActions.OpenModelAction(canvas, entity));
		add(new UIActions.LabelAction(canvas, entity));
		add(new UIActions.AssignFSAAction(canvas, entity));
		addSeparator();
		if (entity.getComponent().getType() == TemplateComponent.TYPE_CHANNEL)
		{
			add(new UIActions.SetModuleAction(canvas, entity));
		}
		else
		{
			add(new UIActions.SetChannelAction(canvas, entity));
		}
		addSeparator();
		add(new UIActions.DeleteAction(canvas, entity));
		pack();
	}

}

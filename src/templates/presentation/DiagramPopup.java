package templates.presentation;

import java.awt.Point;

import javax.swing.JPopupMenu;

public class DiagramPopup extends JPopupMenu
{
	private static final long serialVersionUID = 2751221686777325805L;

	public DiagramPopup(TemplateEditableCanvas canvas, Point location)
	{
		super();
		add(new UIActions.CreateEntityAction(canvas, location));
		pack();
	}
}

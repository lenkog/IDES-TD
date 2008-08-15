package templates.presentation;

import javax.swing.JPopupMenu;

import templates.diagram.Connector;

public class ConnectorPopup extends JPopupMenu
{
	private static final long serialVersionUID = -1486150739464614804L;

	public ConnectorPopup(TemplateEditableCanvas canvas, Connector connector)
	{
		super();
		add(new UIActions.AssignEventsAction(canvas, connector));
		add(new UIActions.MatchEventsAction(canvas,connector));
		addSeparator();
		add(new UIActions.DeleteAllLinksAction(canvas,connector));
		add(new UIActions.DeleteAction(canvas, connector));
		pack();
	}

}

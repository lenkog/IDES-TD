package templates.presentation;

import ides.api.core.Hub;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import templates.diagram.Entity;

public class EntityPopup extends JPopupMenu
{
	private static final long serialVersionUID = -2569273570320479180L;

	public EntityPopup(TemplateEditableCanvas canvas,Entity entity)
	{
		super();
		add(new UIActions.LabelAction(canvas,entity));
		add(new UIActions.DeleteAction(canvas,entity));
		pack();
	}
	
}

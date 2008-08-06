/**
 * 
 */
package templates.presentation;

import ides.api.core.Hub;
import ides.api.utilities.EscapeDialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;

/**
 * @author Lenko Grigorov
 */
public class EntityLabellingDialog extends EscapeDialog
{
	private static final long serialVersionUID = -9128357609584017316L;

	private static EntityLabellingDialog me = null;

	protected static Entity entity = null;

	protected static TemplateEditableCanvas canvas = null;

	// the main job will be handled by commitListener on focusLost
	protected Action enterListener = new AbstractAction()
	{
		private static final long serialVersionUID = 4258152153714537489L;

		public void actionPerformed(ActionEvent actionEvent)
		{
			canvas.setUIInteraction(false);
			setVisible(false);
		}
	};

	protected static FocusListener commitOnFocusLost = new FocusListener()
	{
		public void focusLost(FocusEvent e)
		{
			if (canvas != null && !area.getText().equals(entity.getLabel()))
			{
				new DiagramActions.LabelEntityAction(
						canvas.getDiagram(),
						entity,
						area.getText()).execute();
			}
			me.setVisible(false);
		}

		public void focusGained(FocusEvent e)
		{
		}
	};

	private EntityLabellingDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_entityLabellingTitle"));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				onEscapeEvent();
			}
		});
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Box mainBox = Box.createVerticalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		area = new JTextField(WIDTH);
		area.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke
				.getKeyStroke(KeyEvent.VK_ENTER, 0),
				this);
		area.getActionMap().put(this, enterListener);
		mainBox.add(area);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		getContentPane().add(mainBox);
		pack();
	}

	public static EntityLabellingDialog instance()
	{
		if (me == null)
		{
			me = new EntityLabellingDialog();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	protected static JTextField area;

	public static void showAndLabel(TemplateEditableCanvas canvas, Entity entity)
	{
		canvas.setUIInteraction(true);
		EntityLabellingDialog.canvas = canvas;
		EntityLabellingDialog.entity = entity;
		Point p = new Point(entity.getLocation().x, entity.getLocation().y);
		instance();
		me.pack();
		String label = entity.getLabel();
		boolean hasOurListener = false;
		for (int i = 0; i < area.getFocusListeners().length; ++i)
		{
			if (area.getFocusListeners()[i] == commitOnFocusLost)
			{
				hasOurListener = true;
			}
		}
		if (!hasOurListener)
		{
			area.addFocusListener(commitOnFocusLost);
		}
		area.setText(label);
		area.selectAll();
		p = canvas.localToScreen(p);
		if (p.x + me.getWidth() > Toolkit
				.getDefaultToolkit().getScreenSize().getWidth())
		{
			p.x = p.x - me.getWidth();
		}
		if (p.y + me.getHeight() > Toolkit
				.getDefaultToolkit().getScreenSize().getHeight())
		{
			p.y = p.y - me.getHeight();
		}
		me.setLocation(p);
		me.setVisible(true);
		area.requestFocus();
	}

	@Override
	public void onEscapeEvent()
	{
		area.removeFocusListener(commitOnFocusLost);
		canvas.setUIInteraction(false);
		setVisible(false);
	}
}

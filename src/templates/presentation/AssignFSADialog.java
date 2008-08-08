package templates.presentation;

import ides.api.core.Hub;
import ides.api.utilities.EscapeDialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import templates.diagram.Entity;

public class AssignFSADialog extends EscapeDialog
{
	private static final long serialVersionUID = 2530576123753377680L;
	
	private static AssignFSADialog me = null;

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

	protected static WindowListener onFocusLost = new WindowListener()
	{
		public void windowActivated(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void windowClosed(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void windowClosing(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void windowDeactivated(WindowEvent arg0)
		{
			me.onEscapeEvent();
		}

		public void windowDeiconified(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void windowIconified(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void windowOpened(WindowEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}
	};
	
	protected static class NewFSAAction extends AbstractAction
	{
		private static final long serialVersionUID = 8824881153311968903L;

		private static ImageIcon icon = new ImageIcon();
		
		public NewFSAAction()
		{
			super(Hub.string("TD_comAssignNewFSA"),icon);
			icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub.getLocalResource(AssignFSADialog.class,"images/icons/new_automaton.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignNewFSA"));
		}

		public void actionPerformed(ActionEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}
	}

	private AssignFSADialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_assignFSATitle"));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				onEscapeEvent();
			}
		});
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setMinimumSize(new Dimension(300,10));
		
		Box mainBox = Box.createHorizontalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Box newBox=Box.createVerticalBox();
		newBox.add(new JLabel(" "));
		newBox.add(new JButton(new NewFSAAction()));
		mainBox.add(newBox);
		
		mainBox.add(Box.createRigidArea(new Dimension(5,0)));
		
		openModels=new JComboBox();
		Box modelsBox=Box.createVerticalBox();
		Box titleBox=Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_openModels")));
		titleBox.add(Box.createHorizontalGlue());
		modelsBox.add(titleBox);
		modelsBox.add(openModels);
		mainBox.add(modelsBox);

		mainBox.add(Box.createRigidArea(new Dimension(5,0)));

		templates=new JComboBox();
		Box templatesBox=Box.createVerticalBox();
		titleBox=Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_templates")));
		titleBox.add(Box.createHorizontalGlue());
		templatesBox.add(titleBox);
		templatesBox.add(templates);
		mainBox.add(templatesBox);

		getContentPane().add(mainBox);
		
	}

	public static AssignFSADialog instance()
	{
		if (me == null)
		{
			me = new AssignFSADialog();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	protected static JComboBox openModels;
	protected static JComboBox templates;

	public static void showAndAssign(TemplateEditableCanvas canvas, Entity entity)
	{
		canvas.setUIInteraction(true);
		AssignFSADialog.canvas = canvas;
		AssignFSADialog.entity = entity;
		Point p = new Point(entity.getLocation().x, entity.getLocation().y);
		instance();
		me.pack();
		boolean hasOurListener = false;
		for (int i = 0; i < me.getWindowListeners().length; ++i)
		{
			if (me.getWindowListeners()[i] == onFocusLost)
			{
				hasOurListener = true;
			}
		}
		if (!hasOurListener)
		{
			me.addWindowListener(onFocusLost);
		}
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
	}

	@Override
	public void onEscapeEvent()
	{
		me.removeWindowListener(onFocusLost);
		canvas.setUIInteraction(false);
		setVisible(false);
	}

}

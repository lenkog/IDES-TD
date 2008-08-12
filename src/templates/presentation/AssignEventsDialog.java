package templates.presentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import templates.diagram.Connector;
import templates.diagram.Entity;
import templates.presentation.AssignFSADialog.JLabelListRenderer;
import templates.presentation.AssignFSADialog.NewFSAAction;
import ides.api.core.Hub;
import ides.api.utilities.EscapeDialog;

public class AssignEventsDialog extends EscapeDialog
{

	private static AssignEventsDialog me = null;

	protected static Connector connector= null;

	protected static TemplateEditableCanvas canvas = null;

	protected static WindowListener onFocusLost = new WindowListener()
	{
		public void windowActivated(WindowEvent arg0)
		{
			me.onEscapeEvent();
		}

		public void windowClosed(WindowEvent arg0)
		{
		}

		public void windowClosing(WindowEvent arg0)
		{
		}

		public void windowDeactivated(WindowEvent arg0)
		{
		}

		public void windowDeiconified(WindowEvent arg0)
		{
		}

		public void windowIconified(WindowEvent arg0)
		{
		}

		public void windowOpened(WindowEvent arg0)
		{
		}
	};
	
	protected static class EntityRenderer extends JComponent
	{
		private static final long serialVersionUID = 2564457321980021865L;
		
		protected Entity entity;
		
		public EntityRenderer(Entity entity)
		{
			this.entity=entity;
		}
		
		public Dimension getPreferredSize()
		{
			return new Dimension(entity.getBounds().width,entity.getBounds().height);
		}
		
		public void paint(Graphics g)
		{
			g.translate(-entity.getBounds().x, -entity.getBounds().y);
			entity.drawPlain((Graphics2D)g);
		}
	}

	private AssignEventsDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_assignEventsTitle"));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				onEscapeEvent();
			}
		});
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//		this.setMinimumSize(new Dimension(300, 10));

		Box mainBox = Box.createHorizontalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		mainBox.add(leftIcon);
		mainBox.add(Box.createRigidArea(new Dimension(5,0)));
		
		Box listBox = Box.createVerticalBox();
		
		Box newBox=Box.createHorizontalBox();
		newBox.add(new JComboBox());
		newBox.add(new JComboBox());
		newBox.add(new JButton("Add"));
		
		listBox.add(newBox);
		listBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		listBox.add(new JScrollPane(linker));
		mainBox.add(listBox);

		mainBox.add(Box.createRigidArea(new Dimension(5,0)));
		mainBox.add(rightIcon);
		
		getContentPane().add(mainBox);

	}

	public static AssignEventsDialog instance()
	{
		if (me == null)
		{
			me = new AssignEventsDialog();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}
	
	@Override
	public void onEscapeEvent()
	{
		Hub.getMainWindow().removeWindowListener(onFocusLost);
		canvas.setUIInteraction(false);
		setVisible(false);
		linker.removeAll();
		leftIcon.removeAll();
		rightIcon.removeAll();
	}

	protected static Entity leftEntity;
	protected static Entity rightEntity;
	
	protected static JPanel leftIcon=new JPanel();
	protected static JPanel rightIcon=new JPanel();
	
	protected static JPanel linker=new JPanel();
	
//	protected static 
	
	public static void showAndAssign(TemplateEditableCanvas canvas,Connector connector)
	{
		AssignEventsDialog.canvas=canvas;
		AssignEventsDialog.connector=connector;
		boolean isLeftLeft;
		if(connector.getLeftEntity().getLocation().x>connector.getRightEntity().getLocation().x)
		{
			isLeftLeft=false;
		}
		else
		{
			isLeftLeft=true;
		}
		linker.removeAll();
		leftIcon.removeAll();
		rightIcon.removeAll();
		linker.add(new EventLinker(connector));
		if(isLeftLeft)
		{
			leftIcon.add(new EntityRenderer(connector.getLeftEntity()));
			rightIcon.add(new EntityRenderer(connector.getRightEntity()));
		}
		else
		{
			leftIcon.add(new EntityRenderer(connector.getRightEntity()));
			rightIcon.add(new EntityRenderer(connector.getLeftEntity()));
		}
		instance().pack();
		instance().setLocation(Hub.getCenteredLocationForDialog(new Dimension(instance().getWidth(),instance().getHeight())));
		Hub.getMainWindow().addWindowListener(onFocusLost);
		instance().setVisible(true);
	}

}

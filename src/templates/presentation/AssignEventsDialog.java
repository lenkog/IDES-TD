package templates.presentation;

import ides.api.core.Hub;
import ides.api.utilities.EscapeDialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import templates.diagram.Connector;
import templates.diagram.Entity;

public class AssignEventsDialog extends EscapeDialog
{
	private static final long serialVersionUID = 6519071057585162972L;

	private static AssignEventsDialog me = null;

	protected static Connector connector = null;

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
			this.entity = entity;
		}

		public Dimension getPreferredSize()
		{
			return new Dimension(
					entity.getBounds().width,
					entity.getBounds().height);
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

		Box mainBox = Box.createVerticalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box newBox = Box.createHorizontalBox();
		leftName.setMaximumSize(new Dimension(
				leftName.getMaximumSize().width,
				leftName.getMinimumSize().height));
		rightName.setMaximumSize(new Dimension(
				rightName.getMaximumSize().width,
				rightName.getMinimumSize().height));
		leftName.addFocusListener(new FocusListener()
		{

			public void focusGained(FocusEvent arg0)
			{
				instance().getRootPane().setDefaultButton(leftAdd);
			}

			public void focusLost(FocusEvent arg0)
			{
				instance().getRootPane().setDefaultButton(null);
			}
		});
		leftAdd = new JButton(Hub.string("TD_addEvent"));
		leftAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!"".equals(leftName.getText()))
				{
					if (isLeftLeft)
					{
						linker.addExtraLeftEvent(leftName.getText());
					}
					else
					{
						linker.addExtraRightEvent(leftName.getText());
					}
					leftName.setText("");
				}
			}
		});
		newBox.add(leftAdd);
		newBox.add(leftName);
		newBox.add(Box.createRigidArea(new Dimension(10, 0)));
		rightAdd = new JButton(Hub.string("TD_addEvent"));
		rightAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!"".equals(rightName.getText()))
				{
					if (!isLeftLeft)
					{
						linker.addExtraLeftEvent(rightName.getText());
					}
					else
					{
						linker.addExtraRightEvent(rightName.getText());
					}
					rightName.setText("");
				}
			}
		});
		rightName.addFocusListener(new FocusListener()
		{

			public void focusGained(FocusEvent arg0)
			{
				instance().getRootPane().setDefaultButton(rightAdd);
			}

			public void focusLost(FocusEvent arg0)
			{
				instance().getRootPane().setDefaultButton(null);
			}
		});
		newBox.add(rightName);
		newBox.add(rightAdd);

		mainBox.add(newBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		Box listBox = Box.createHorizontalBox();

		listBox.add(leftIcon);
		listBox.add(Box.createRigidArea(new Dimension(5, 0)));
		listBox.add(new JScrollPane(linkerPanel));
		listBox.add(Box.createRigidArea(new Dimension(5, 0)));
		listBox.add(rightIcon);

		mainBox.add(listBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(new JButton(new AbstractAction(Hub
				.string("TD_comDeleteAllLinks"))
		{
			private static final long serialVersionUID = 2680899991712228777L;

			public void actionPerformed(ActionEvent e)
			{
				linker.unlinkAll();
			}
		}));
		buttonBox.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonBox.add(new JButton(new AbstractAction(Hub
				.string("TD_comMatchEvents"))
		{
			private static final long serialVersionUID = 1783903218138278716L;

			public void actionPerformed(ActionEvent e)
			{
				linker.matchEvents();
			}
		}));
		buttonBox.add(Box.createHorizontalGlue());

		mainBox.add(buttonBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		Box infoBox = Box.createHorizontalBox();
		infoBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JLabel iconLabel = new JLabel();
		try
		{
			iconLabel.setIcon(new ImageIcon(ImageIO.read(Hub
					.getLocalResource(AssignEventsDialog.class,
							"images/icons/exclamation.gif"))));
		}
		catch (IOException e)
		{
		}
		iconLabel.setAlignmentY(JComponent.TOP_ALIGNMENT);
		infoBox.add(iconLabel);
		JTextArea area = new JTextArea();
		area.setFont(iconLabel.getFont());
		area.setBackground(iconLabel.getBackground());
		area.setText(Hub.string("TD_eventIconExplain1") + " "
				+ Hub.string("TD_eventIconExplain2") + " "
				+ Hub.string("TD_eventAssignExplain"));
		area.setLineWrap(true);
		area.setEditable(false);
		area.setWrapStyleWord(true);
		area.setAlignmentY(JComponent.TOP_ALIGNMENT);
		infoBox.add(area);

		mainBox.add(infoBox);

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
		setVisible(false);
		linker.commitChanges();
		canvas.setUIInteraction(false);
		linkerPanel.removeAll();
		linker = null;
		leftIcon.removeAll();
		rightIcon.removeAll();
	}

	protected static JPanel leftIcon = new JPanel();

	protected static JPanel rightIcon = new JPanel();

	protected static JPanel linkerPanel = new JPanel();

	protected static EventLinker linker;

	protected static JTextField leftName = new JTextField(10);

	protected static JButton leftAdd;

	protected static JTextField rightName = new JTextField(10);

	protected static JButton rightAdd;

	protected static boolean isLeftLeft;

	public static void showAndAssign(TemplateEditableCanvas canvas,
			Connector connector)
	{
		AssignEventsDialog.canvas = canvas;
		AssignEventsDialog.connector = connector;
		if (connector.getLeftEntity().getLocation().x > connector
				.getRightEntity().getLocation().x)
		{
			isLeftLeft = false;
		}
		else
		{
			isLeftLeft = true;
		}
		linkerPanel.removeAll();
		leftIcon.removeAll();
		rightIcon.removeAll();
		linker = new EventLinker(canvas.getDiagram(), connector);
		linkerPanel.add(linker);
		if (isLeftLeft)
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
		instance().setLocation(Hub.getCenteredLocationForDialog(new Dimension(
				instance().getWidth(),
				instance().getHeight())));
		Hub.getMainWindow().addWindowListener(onFocusLost);
		instance().setVisible(true);
	}

}

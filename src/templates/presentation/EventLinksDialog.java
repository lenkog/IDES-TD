/*
 * Copyright (c) 2009, Lenko Grigorov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package templates.presentation;

import ides.api.core.Hub;
import ides.api.utilities.EscapeDialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.SwingUtilities;

import templates.diagram.Connector;
import templates.diagram.Entity;
import templates.model.TemplateComponent;

/**
 * The UI dialog which allows the linking of events between components in a
 * template design.
 * 
 * @author Lenko Grigorov
 */
public class EventLinksDialog extends EscapeDialog
{
	private static final long serialVersionUID = 6519071057585162972L;

	/**
	 * Singleton instance.
	 */
	private static EventLinksDialog me = null;

	/**
	 * The {@link Connector} between the {@link TemplateComponent}s whose events
	 * are linked.
	 */
	protected static Connector connector = null;

	/**
	 * The canvas which contains the connector between the
	 * {@link TemplateComponent}s whose events are linked.
	 */
	protected static TemplateEditableCanvas canvas = null;

	/**
	 * Handler of focus for the main window of IDES. When the user clicks
	 * outside the event linking dialog (i.e., the main window gets activated),
	 * commit the changes and close.
	 */
	protected static WindowListener commitOnFocusLost = new WindowListener()
	{
		/**
		 * When the main window of IDES is activated because the user clicked on
		 * it, commit the changes to the linking of events and close the event
		 * linking dialog.
		 */
		public void windowActivated(WindowEvent arg0)
		{
			if (arg0.getOppositeWindow() != null
					&& !Hub
							.getUserInterface()
							.isWindowActivationAfterNoticePopup(arg0))
			{
				instance().commitAndClose();
			}
			else
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						instance().requestFocus();
					}
				});
			}
		}

		/**
		 * Do nothing.
		 */
		public void windowClosed(WindowEvent arg0)
		{
		}

		/**
		 * Do nothing.
		 */
		public void windowClosing(WindowEvent arg0)
		{
		}

		/**
		 * Do nothing.
		 */
		public void windowDeactivated(WindowEvent arg0)
		{
		}

		/**
		 * Do nothing.
		 */
		public void windowDeiconified(WindowEvent arg0)
		{
		}

		/**
		 * Do nothing.
		 */
		public void windowIconified(WindowEvent arg0)
		{
		}

		/**
		 * Do nothing.
		 */
		public void windowOpened(WindowEvent arg0)
		{
		}
	};

	/**
	 * Class used to render the icons of the {@link Entity}s which contain the
	 * {@link TemplateComponent}s whose events are linked.
	 * 
	 * @author Lenko Grigorov
	 */
	protected static class EntityRenderer extends JComponent
	{
		private static final long serialVersionUID = 2564457321980021865L;

		/**
		 * The {@link Entity} to be rendered.
		 */
		protected Entity entity;

		/**
		 * Construct an object to render the given {@link Entity}.
		 * 
		 * @param entity
		 *            the {@link Entity} whose icon is to be rendered
		 */
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

	/**
	 * Construct the assign events dialog and set up the generic layout.
	 */
	private EventLinksDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_assignEventsTitle"));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				commitAndClose();
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
					pack();
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
					pack();
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
		listBox.add(new JScrollPane(
				linkerPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
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
					.getLocalResource(EventLinksDialog.class,
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

		JButton OKButton = new JButton(Hub.string("OK"));
		OKButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				commitAndClose();
			}
		});
		JButton cancelButton = new JButton(Hub.string("cancel"));
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onEscapeEvent();
			}
		});
		JPanel p = new JPanel(new FlowLayout());
		p.add(OKButton);
		p.add(cancelButton);

		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));
		mainBox.add(p);

		getContentPane().add(mainBox);

		pack();
		OKButton.setPreferredSize(new Dimension(Math.max(OKButton.getWidth(),
				cancelButton.getWidth()), OKButton.getHeight()));
		OKButton.invalidate();
		cancelButton
				.setPreferredSize(new Dimension(Math.max(OKButton.getWidth(),
						cancelButton.getWidth()), cancelButton.getHeight()));
		cancelButton.invalidate();
	}

	/**
	 * Access the singleton instance of the assign events dialog.
	 * 
	 * @return the singleton instance of the assign events dialog
	 */
	protected static EventLinksDialog instance()
	{
		if (me == null)
		{
			me = new EventLinksDialog();
		}
		return me;
	}

	/**
	 * @throws RuntimeException
	 *             cloning is not allowed
	 */
	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	/**
	 * Called to cancel the changes made to the linking of events (e.g., when
	 * the user presses the <code>Esc</code> key).
	 */
	@Override
	public void onEscapeEvent()
	{
		Hub.getMainWindow().removeWindowListener(commitOnFocusLost);
		setVisible(false);
		// linker.commitChanges();
		canvas.setUIInteraction(false);
		linkerPanel.removeAll();
		linker = null;
		leftIcon.removeAll();
		rightIcon.removeAll();
	}

	/**
	 * Commit the changes to the event links made by the user and close the
	 * dialog.
	 */
	public void commitAndClose()
	{
		linker.commitChanges();
		onEscapeEvent();
	}

	/**
	 * The UI element displaying the icon of the left {@link Entity} linked by
	 * the {@link Connector}.
	 */
	protected static JPanel leftIcon = new JPanel();

	/**
	 * The UI element displaying the icon of the right {@link Entity} linked by
	 * the {@link Connector}.
	 */
	protected static JPanel rightIcon = new JPanel();

	/**
	 * The UI element displaying the event links.
	 */
	protected static JPanel linkerPanel = new JPanel();

	/**
	 * The UI element which handles the event linking.
	 */
	protected static EventLinker linker;

	/**
	 * Field to type event names for the left {@link Entity}.
	 */
	protected static JTextField leftName = new JTextField(10);

	/**
	 * Button to add new events to the event list for the left {@link Entity}.
	 */
	protected static JButton leftAdd;

	/**
	 * Field to type event names for the right {@link Entity}.
	 */
	protected static JTextField rightName = new JTextField(10);

	/**
	 * Button to add new events to the event list for the right {@link Entity}.
	 */
	protected static JButton rightAdd;

	/**
	 * Set to <code>true</code> if the "left" {@link Entity} linked by the
	 * {@link Connector} appears to the left of the "right" {@link Entity};
	 * otherwise set to <code>false</code>.
	 */
	protected static boolean isLeftLeft;

	/**
	 * Show the assign events dialog to let the user link events between the
	 * {@link TemplateComponent}s contained by the {@link Entity}s connected by
	 * the given {@link Connector}.
	 * 
	 * @param canvas
	 *            the canvas which contains the given connector
	 * @param connector
	 *            the {@link Connector} connecting the {@link Entity}s which
	 *            contain the {@link TemplateComponent}s whose events the user
	 *            will be linking
	 */
	public static void showAndAssign(TemplateEditableCanvas canvas,
			Connector connector)
	{
		instance();
		EventLinksDialog.canvas = canvas;
		EventLinksDialog.connector = connector;
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
		linkerPanel.getPreferredSize();
		instance().pack();
		instance().setLocation(Hub.getCenteredLocationForDialog(new Dimension(
				instance().getWidth(),
				instance().getHeight())));
		boolean hasOurListener = false;
		for (int i = 0; i < Hub.getMainWindow().getWindowListeners().length; ++i)
		{
			if (Hub.getMainWindow().getWindowListeners()[i] == commitOnFocusLost)
			{
				hasOurListener = true;
			}
		}
		if (!hasOurListener)
		{
			Hub.getMainWindow().addWindowListener(commitOnFocusLost);
		}
		instance().setVisible(true);
	}

}

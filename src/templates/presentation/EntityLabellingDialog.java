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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;

/**
 * The UI dialog to label components in the template design.
 * 
 * @author Lenko Grigorov
 */
public class EntityLabellingDialog extends EscapeDialog
{
	private static final long serialVersionUID = -9128357609584017316L;

	private static EntityLabellingDialog me = null;

	protected static Entity entity = null;

	protected static TemplateEditableCanvas canvas = null;

	protected Action enterListener = new AbstractAction()
	{
		private static final long serialVersionUID = 4258152153714537489L;

		public void actionPerformed(ActionEvent actionEvent)
		{
			commitAndClose();
		}
	};

	protected static WindowListener commitOnFocusLost = new WindowListener()
	{
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
						area.requestFocus();
					}
				});
			}
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

	private EntityLabellingDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_entityLabellingTitle"));
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
		Hub.getMainWindow().removeWindowListener(commitOnFocusLost);
		canvas.setUIInteraction(false);
		setVisible(false);
	}

	protected void commitAndClose()
	{
		if (canvas != null && !area.getText().equals(entity.getLabel()))
		{
			new DiagramActions.LabelEntityAction(
					canvas.getDiagram(),
					entity,
					area.getText()).execute();
		}
		onEscapeEvent();
	}
}

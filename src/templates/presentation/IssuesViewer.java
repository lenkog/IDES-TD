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
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.TemplateDiagramMessage;
import templates.diagram.TemplateDiagramSubscriber;

/**
 * The UI element displaying the list of consistency issues in template designs.
 * 
 * @author Lenko Grigorov
 */
public class IssuesViewer implements Presentation, TemplateDiagramSubscriber,
		MouseListener
{

	/**
	 * The UI element which displays an individual consistency issue.
	 * 
	 * @author Lenko Grigorov
	 */
	protected class IssueUI extends JPanel
	{
		private static final long serialVersionUID = -5757502501077317407L;

		/**
		 * Buttons for the actions which can fix the consistency issue.
		 */
		protected Collection<JButton> buttons = new HashSet<JButton>();

		/**
		 * Box to contain the buttons for the fix actions.
		 */
		protected Box fixBox;

		/**
		 * The consistency issue descriptor.
		 */
		public IssueDescriptor descriptor;

		/**
		 * Construct a new UI element to display the given consistency issue.
		 * 
		 * @param id
		 *            the description of the consistency issue
		 */
		public IssueUI(IssueDescriptor id)
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			descriptor = id;
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			String[] lines = id.message.split("\n");
			JLabel title = new JLabel(lines[0]);
			if (id.type == IssueDescriptor.TYPE_ERROR)
			{
				title.setIcon(new ImageIcon(Toolkit
						.getDefaultToolkit().createImage(Hub
								.getLocalResource(this.getClass(),
										"images/icons/issue_error.gif"))));
			}
			else if (id.type == IssueDescriptor.TYPE_WARNING)
			{
				title.setIcon(new ImageIcon(Toolkit
						.getDefaultToolkit().createImage(Hub
								.getLocalResource(this.getClass(),
										"images/icons/issue_warn.gif"))));
			}
			title.setAlignmentX(Component.LEFT_ALIGNMENT);
			add(title);
			for (int i = 1; i < lines.length; ++i)
			{
				JLabel label = new JLabel(lines[i]);
				if (i == 1)
				{
					label.setFont(label.getFont().deriveFont(Font.BOLD));
				}
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				add(label);
			}
			add(Box.createRigidArea(new Dimension(0, 5)));

			fixBox = Box.createHorizontalBox();
			for (Action fix : id.fixes)
			{
				if (fixBox.getComponentCount() > 0)
				{
					fixBox.add(Box.createRigidArea(new Dimension(2, 0)));
				}
				JButton button = new JButton(fix);
				buttons.add(button);
				fixBox.add(button);
			}
			fixBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			add(fixBox);
		}

		/**
		 * Called when the user clicks in the area of this UI element. If the
		 * user has clicks on a button, the corresponding fix action gets
		 * executed.
		 * 
		 * @param p
		 *            the point where the user clicked
		 */
		public void clickOn(Point p)
		{
			for (JButton b : buttons)
			{
				Point temp = new Point(p.x - fixBox.getLocation().x, p.y
						- fixBox.getLocation().y);
				if (b.getBounds().contains(temp))
				{
					b.doClick();
					break;
				}
			}
		}
	}

	/**
	 * Renderer of UI elements for consistency issues when shown as items in a
	 * list.
	 * 
	 * @author Lenko Grigorov
	 */
	protected class IssueRenderer implements ListCellRenderer
	{
		/**
		 * Retrieve the UI element for the list item.
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			if (!(value instanceof IssueUI))
			{
				return new JLabel(value.toString());
			}
			IssueUI issue = (IssueUI)value;
			if (isSelected)
			{
				issue.setBackground(DiagramElement.COLOR_SELECT_INCONSIST);
			}
			else
			{
				issue.setBackground(SystemColor.control);
			}
			return issue;
		}
	}

	/**
	 * The template diagram with the design whose consistency issues are
	 * displayed.
	 */
	protected TemplateDiagram diagram;

	/**
	 * The scroll pane which contains the list of consistency issues.
	 */
	private JScrollPane sp;

	/**
	 * The list of consistency issues.
	 */
	protected JList issues = new JList();

	/**
	 * The {@link ListModel} used to display the list of consistency issues.
	 */
	protected DefaultListModel data = new DefaultListModel();

	/**
	 * Construct and set up a UI element to display the list of consistency
	 * issues in the template design from the given template diagram.
	 * 
	 * @param diagram
	 *            the template diagram with the design whose consistency issues
	 *            will be displayed
	 */
	public IssuesViewer(TemplateDiagram diagram)
	{
		this.diagram = diagram;
		issues.setName(Hub.string("TD_titleIssues"));
		issues.setModel(data);
		issues.setCellRenderer(new IssueRenderer());
		issues.addMouseListener(this);
		issues.setBackground(SystemColor.control);
		sp = new JScrollPane(issues);
		refreshIssueList();
		diagram.addSubscriber(this);
	}

	/**
	 * Subscribe a listener for changes to the list of consistency issues.
	 * 
	 * @param ldl
	 *            the listener to be subscribed
	 */
	public void addIssuesListener(ListDataListener ldl)
	{
		data.addListDataListener(ldl);
	}

	/**
	 * Cancel the subscription of a listener for changes to the list of
	 * consistency issues.
	 * 
	 * @param ldl
	 *            the listener whose subscription is to be cancelled
	 */
	public void removeIssuesListener(ListDataListener ldl)
	{
		data.removeListDataListener(ldl);
	}

	/**
	 * Retrieve the number of consistency issues in the list.
	 * 
	 * @return the number of consistency issues in the list
	 */
	public int getIssueCount()
	{
		return data.getSize();
	}

	public void forceRepaint()
	{
		issues.repaint();
	}

	public JComponent getGUI()
	{
		return sp;
	}

	public DESModel getModel()
	{
		return diagram.getModel();
	}

	public String getName()
	{
		return Hub.string("TD_titleIssues");
	}

	public void release()
	{
		diagram.removeSubscriber(this);
	}

	public void setTrackModel(boolean arg0)
	{
	}

	/**
	 * Update the list of consistency issues to reflect the current state of the
	 * template design. As well, update the consistency status of the template
	 * diagram elements.
	 */
	protected void refreshIssueList()
	{
		List<IssueDescriptor> ids = IssuesWrapper.getIssues(diagram);
		data.clear();
		for (Entity e : diagram.getEntities())
		{
			e.setInconsistent(false);
		}
		for (Connector c : diagram.getConnectors())
		{
			c.setInconsistent(false);
		}
		for (IssueDescriptor id : ids)
		{
			for (DiagramElement e : id.elements)
			{
				e.setInconsistent(true);
			}
			data.addElement(new IssueUI(id));
		}
	}

	/**
	 * Update the list of consistency issues after a change to the template
	 * diagram.
	 */
	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		refreshIssueList();
	}

	/**
	 * After a change to the selection of diagram elements, in the list of
	 * consistency issues select those consistency issues which stem from the
	 * diagram elements selected by the user. This lets the user easily
	 * determine which consistency issues depend on a given diagram element.
	 */
	public void templateDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		Collection<Integer> selectedIdx = new HashSet<Integer>();
		for (DiagramElement e : diagram.getSelection())
		{
			for (int i = 0; i < data.getSize(); ++i)
			{
				if (((IssueUI)data.elementAt(i)).descriptor.elements
						.contains(e))
				{
					selectedIdx.add(i);
				}
			}
		}
		int[] indices = new int[selectedIdx.size()];
		int i = 0;
		for (Integer idx : selectedIdx)
		{
			indices[i++] = idx;
		}
		issues.setSelectedIndices(indices);
	}

	/**
	 * When the user clicks inside the list of consistency issues, select in the
	 * template diagram those diagram elements which contribute to the
	 * occurrence of the consistency issue under the mouse cursor. As well,
	 * handle the clicking of the mouse over a button with a fix action.
	 * <p>
	 * In case there is no consistency issue item under the mouse cursor, do
	 * nothing.
	 */
	public void mouseClicked(MouseEvent e)
	{
		TemplateConsistencyCanvas canvas = Hub
				.getWorkspace()
				.getPresentationsOfType(TemplateConsistencyCanvas.class)
				.iterator().next();
		Hub.getWorkspace().setActivePresentation(canvas.getName());
		int idx = issues.locationToIndex(e.getPoint());
		if (idx < 0)
		{
			return;
		}
		IssueUI iui = (IssueUI)data.elementAt(idx);
		diagram.setSelection(iui.descriptor.elements);
		canvas.scrollTo(iui.descriptor.elements);
		iui.clickOn(new Point(e.getX(), e.getY()
				- issues.indexToLocation(idx).y));
	}

	/**
	 * Do nothing.
	 */
	public void mouseEntered(MouseEvent e)
	{
	}

	/**
	 * Do nothing.
	 */
	public void mouseExited(MouseEvent e)
	{
	}

	/**
	 * Do nothing.
	 */
	public void mousePressed(MouseEvent e)
	{
	}

	/**
	 * Do nothing.
	 */
	public void mouseReleased(MouseEvent e)
	{
	}

}

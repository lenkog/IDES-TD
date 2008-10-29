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
import javax.swing.event.ListDataListener;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.TemplateDiagramMessage;
import templates.diagram.TemplateDiagramSubscriber;

public class IssuesViewer implements Presentation, TemplateDiagramSubscriber,
		MouseListener
{

	protected class IssueUI extends JPanel
	{
		private static final long serialVersionUID = -5757502501077317407L;

		protected Collection<JButton> buttons = new HashSet<JButton>();

		protected Box fixBox;

		public IssueDescriptor descriptor;

		public IssueUI(IssueDescriptor id)
		{
//			super(BoxLayout.Y_AXIS);
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
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

	protected class IssueRenderer implements ListCellRenderer
	{
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

	protected TemplateDiagram diagram;

	private JScrollPane sp;

	protected JList issues = new JList();

	protected DefaultListModel data = new DefaultListModel();

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
	
	public void addIssuesListener(ListDataListener ldl)
	{
		data.addListDataListener(ldl);
	}
	
	public void removeIssuesListener(ListDataListener ldl)
	{
		data.removeListDataListener(ldl);
	}
	
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

	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		refreshIssueList();
	}

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

	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

}

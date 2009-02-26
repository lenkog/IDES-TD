package templates.library;

import ides.api.core.Annotable;
import ides.api.core.Hub;
import ides.api.core.WorkspaceMessage;
import ides.api.core.WorkspaceSubscriber;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.presentation.Presentation;
import ides.api.utilities.GeneralUtils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

public class LibraryUI extends Box implements Presentation,
		TemplateLibraryListener, MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = -666343525812865685L;

	public static class AddTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = 1033418973771323762L;

		// private static ImageIcon icon = new ImageIcon();

		public AddTemplateAction()
		{
			super(Hub.string("TD_comAddTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAddTemplate"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			AddTemplateDialog.addTemplate(TemplateManager
					.instance().getMainLibrary());
		}
	}

	public class DeleteTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = -4293547969708851728L;

		// private static ImageIcon icon = new ImageIcon();

		public DeleteTemplateAction()
		{
			super(Hub.string("TD_comDeleteTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDeleteTemplate"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			Object[] templates = list.getSelectedValues();
			if (JOptionPane.showConfirmDialog(Hub.getMainWindow(),
					GeneralUtils.JOptionPaneKeyBinder.messageLabel(Hub
							.string("TD_confirmDelTemplate")),
					Hub.string("TD_confirmDelTemplateTitle"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			{
				return;
			}
			String errors = "";
			for (Object template : templates)
			{
				try
				{
					TemplateManager
							.instance().getMainLibrary()
							.removeTemplate(((Template)template).getName());
				}
				catch (IOException e)
				{
					errors += Hub.string("TD_errorRemovingTemplate") + " \'"
							+ ((Template)template).getName() + "\' ["
							+ e.getMessage() + "]\n";
				}
			}
			if (!"".equals(errors))
			{
				Hub.displayAlert(errors);
			}
		}
	}

	public class EditTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = -3546061467454314196L;

		// private static ImageIcon icon = new ImageIcon();

		public EditTemplateAction()
		{
			super(Hub.string("TD_comEditTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintEditTemlate"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			Template template = (Template)list.getSelectedValue();
			if (template != null)
			{
				AddTemplateDialog.editTemplate(TemplateManager
						.instance().getMainLibrary(), template);
			}
		}
	}

	public class ViewTemplateAction extends AbstractAction
	{
		private static final long serialVersionUID = -7055962471632256236L;

		// private static ImageIcon icon = new ImageIcon();

		public ViewTemplateAction()
		{
			super(Hub.string("TD_comViewTemplate"));
			// icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
			// .getResource("images/icons/edit_delete.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintViewTemplate"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			Template template = (Template)list.getSelectedValue();
			if (template != null)
			{
				if (template.getModel() != Hub.getWorkspace().getModel(template
						.getModel().getName()))
				{
					TemplateUpdater updater = new TemplateUpdater(template
							.getName(), template.getModel(), (File)template
							.getModel().getAnnotation(Annotable.FILE));
					template.getModel().addSubscriber(updater);
					Hub.getWorkspace().addModel(template.getModel());
					Hub.getWorkspace().addSubscriber(updater);
				}
				Hub
						.getWorkspace().setActiveModel(template
								.getModel().getName());
			}
		}
	}

	private class TemplateUpdater implements DESModelSubscriber,
			WorkspaceSubscriber
	{
		protected String template;

		protected DESModel model;

		protected File file;

		public TemplateUpdater(String template, DESModel model, File file)
		{
			this.model = model;
			this.template = template;
			this.file = file;
		}

		public void modelNameChanged(DESModelMessage arg0)
		{
		}

		public void saveStatusChanged(DESModelMessage arg0)
		{
			// try
			// {
			// throw new RuntimeException();
			// } catch(Exception e)
			// {
			// e.printStackTrace();
			// }
			if (!file.equals(arg0.getSource().getAnnotation(Annotable.FILE)))
			{
				unsubscribeAndReload();
			}
		}

		public void aboutToRearrangeWorkspace()
		{
		}

		public void modelCollectionChanged(WorkspaceMessage arg0)
		{
			boolean found = false;
			for (Iterator<DESModel> i = Hub.getWorkspace().getModels(); i
					.hasNext();)
			{
				if (model == i.next())
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				unsubscribeAndReload();
			}
		}

		public void modelSwitched(WorkspaceMessage arg0)
		{
		}

		public void repaintRequired()
		{
		}

		protected void unsubscribeAndReload()
		{
			model.removeSubscriber(this);
			Hub.getWorkspace().removeSubscriber(this);
			try
			{
				TemplateManager
						.instance().getMainLibrary().reloadTemplate(template);
			}
			catch (IOException e)
			{
				Hub.displayAlert(Hub.string("TD_errorReloadingTemplate") + " "
						+ template + ".\n"
						+ GeneralUtils.truncateMessage(e.getMessage()));
			}
		}
	}

	private static class TemplateListRenderer extends JLabel implements
			ListCellRenderer
	{
		private static final long serialVersionUID = -4843903577071299167L;

		public TemplateListRenderer()
		{
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			setBackground(SystemColor.text);
			if (value == null)
			{
				setText("");
			}
			else if (value instanceof Template)
			{
				setText(TemplateDescriptor.shortDescription(((Template)value)
						.getDescription()));
				setIcon(((Template)value).getIcon());
			}
			else
			{
				setText(value.toString());
			}
			if (isSelected)
			{
				setBackground(SystemColor.textHighlight);
				setForeground(SystemColor.textHighlightText);

			}
			else
			{
				setBackground(SystemColor.text);
				setForeground(SystemColor.textText);
			}
			return this;
		}
	}

	protected JList list;

	protected DefaultListModel model;

	protected ViewTemplateAction viewAction;

	public LibraryUI()
	{
		super(BoxLayout.Y_AXIS);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		list = new JList();
		list.setCellRenderer(new TemplateListRenderer());
		model = new DefaultListModel();
		updateList();
		list.setModel(model);
		TemplateManager.instance().getMainLibrary().addListener(this);
		list.addMouseMotionListener(this);
		list.addMouseListener(this);
		list.setTransferHandler(new LibraryTransferHandler());
		list.setDragEnabled(true);

		viewAction = new ViewTemplateAction();

		Box titleBox = Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_avaliableTemplates")));
		titleBox.add(Box.createHorizontalGlue());
		add(titleBox);

		add(Box.createRigidArea(new Dimension(0, 5)));

		JScrollPane sp = new JScrollPane(list);
		add(sp);

		add(Box.createRigidArea(new Dimension(0, 5)));

		Box buttonBox = Box.createHorizontalBox();
		JButton addBut = new JButton(new AddTemplateAction());
		buttonBox.add(addBut);
		buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton delBut = new JButton(new DeleteTemplateAction());
		buttonBox.add(delBut);
		buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton editBut = new JButton(new EditTemplateAction());
		buttonBox.add(editBut);
		buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton viewBut = new JButton(viewAction);
		buttonBox.add(viewBut);
		add(buttonBox);
	}

	protected void updateList()
	{
		Set<Template> templates = new TreeSet<Template>(
				new Comparator<Template>()
				{

					public int compare(Template o1, Template o2)
					{
						return o1.getName().compareTo(o2.getName());
					}

				});
		for (Template t : TemplateManager
				.instance().getMainLibrary().getTemplates())
		{
			templates.add(t);
		}
		model.removeAllElements();
		for (Template t : templates)
		{
			model.addElement(t);
		}
	}

	public void forceRepaint()
	{
	}

	public JComponent getGUI()
	{
		return this;
	}

	public DESModel getModel()
	{
		return Hub.getWorkspace().getActiveModel();
	}

	public String getName()
	{
		return Hub.string("TD_libraryTitle");
	}

	public void release()
	{
	}

	public void setTrackModel(boolean arg0)
	{
	}

	public void templateCollectionChanged(TemplateLibrary source)
	{
		updateList();
	}

	public void mouseDragged(MouseEvent arg0)
	{
	}

	public void mouseMoved(MouseEvent arg0)
	{
		int idx = list.locationToIndex(arg0.getPoint());
		if (idx >= 0 && list.getCellBounds(idx, idx).contains(arg0.getPoint()))
		{
			Template t = (Template)model.getElementAt(idx);
			list.setToolTipText(t.getDescription());
		}
		else
		{
			list.setToolTipText(null);
		}
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if (arg0.getClickCount() == 2)
		{
			int idx = list.locationToIndex(arg0.getPoint());
			if (idx >= 0
					&& list.getCellBounds(idx, idx).contains(arg0.getPoint()))
			{
				list.setSelectedIndex(idx);
				viewAction.actionPerformed(null);
			}
		}
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}

	public void mousePressed(MouseEvent arg0)
	{
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

}

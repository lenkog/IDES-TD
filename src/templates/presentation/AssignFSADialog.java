package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.utilities.EscapeDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.library.Template;
import templates.library.TemplateDescriptor;
import templates.library.TemplateManager;
import templates.model.TemplateComponent;
import templates.utils.EntityIcon;

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

	protected static WindowListener commitOnFocusLost = new WindowListener()
	{
		public void windowActivated(WindowEvent arg0)
		{
			if (arg0.getOppositeWindow() != null
					&& !Hub
							.getUserInterface()
							.isWindowActivationAfterNoticePopup(arg0))
			{
				instance().onEscapeEvent();
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

	protected static ActionListener onSelectModel = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!(openModelsCombo.getSelectedItem() instanceof FSACell))
			{
				return;
			}
			FSACell cell = (FSACell)openModelsCombo.getSelectedItem();
			FSAModel fsa = cell.fsa.clone();
			me.onEscapeEvent();
			EntityIcon icon = null;
			if (cell.icon != null && (cell.icon instanceof EntityIcon))
			{
				icon = (EntityIcon)cell.icon;
				if (cell.fsa.hasAnnotation(Entity.FLAG_MARK))
				{
					fsa.setAnnotation(Entity.FLAG_MARK, new Object());
				}
			}
			new DiagramActions.AssignFSAAction(
					canvas.getDiagram(),
					entity,
					fsa,
					icon).execute();
		}
	};

	protected static ActionListener onSelectTemplate = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!(templatesCombo.getSelectedItem() instanceof TemplateCell))
			{
				return;
			}
			Template template = ((TemplateCell)templatesCombo.getSelectedItem()).template;
			FSAModel fsa = template.instantiate();
			me.onEscapeEvent();
			new DiagramActions.AssignFSAAction(
					canvas.getDiagram(),
					entity,
					fsa,
					template.getIcon().clone()).execute();
		}
	};

	protected static class NewFSAAction extends AbstractAction
	{
		private static final long serialVersionUID = 8824881153311968903L;

		private static ImageIcon icon = new ImageIcon();

		public NewFSAAction()
		{
			super(Hub.string("TD_comAssignNewFSA"), icon);
			icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
					.getLocalResource(AssignFSADialog.class,
							"images/icons/new_automaton.gif")));
			putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignNewFSA"));
		}

		public void actionPerformed(ActionEvent arg0)
		{
			me.onEscapeEvent();
			FSAModel[] model = new FSAModel[1];
			new DiagramActions.AssignNewFSAAction(
					canvas.getDiagram(),
					entity,
					model).execute();
		}
	}

	protected static class FSACell extends JLabel
	{
		private static final long serialVersionUID = -3672946556518152880L;

		public FSAModel fsa;

		public Icon icon = null;

		public FSACell(FSAModel fsa)
		{
			super(fsa.getName());
			this.fsa = fsa;
			setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
		}

		public FSACell(FSAModel fsa, Icon icon)
		{
			super(icon);
			this.icon = icon;
			setText(fsa.getName());
			this.fsa = fsa;
			setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
		}
	}

	protected static class TemplateCell extends JLabel
	{
		private static final long serialVersionUID = -8477312997483571474L;

		public Template template;

		public TemplateCell(Template template)
		{
			super(template.getIcon());
			setText(TemplateDescriptor.shortDescription(template
					.getDescription()));
			this.template = template;
			setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
		}
	}

	protected class JLabelListRenderer extends Box implements ListCellRenderer
	{
		private static final long serialVersionUID = -4858000916109104619L;

		protected JLabel defaultLabel = new JLabel();

		public JLabelListRenderer()
		{
			super(BoxLayout.X_AXIS);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			JLabel label;
			if (!(value instanceof JLabel))
			{
				defaultLabel.setText(value != null ? value.toString() : "");
				label = defaultLabel;
			}
			else
			{
				label = (JLabel)value;
			}
			if (isSelected)
			{
				setBackground(SystemColor.textHighlight);
				setOpaque(true);
			}
			else
			{
				setBackground(SystemColor.control);
				setOpaque(false);
			}
			removeAll();
			add(label);
			add(Box.createHorizontalGlue());
			return this;
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
		this.setMinimumSize(new Dimension(300, 10));

		Box mainBox = Box.createHorizontalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Box newBox = Box.createVerticalBox();
		newBox.add(new JLabel(" "));
		newBox.add(new JButton(new NewFSAAction()));
		mainBox.add(newBox);

		mainBox.add(Box.createRigidArea(new Dimension(5, 0)));

		openModelsCombo = new JComboBox();
		openModelsCombo.setRenderer(new JLabelListRenderer());
		Box modelsBox = Box.createVerticalBox();
		Box titleBox = Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_openModels")));
		titleBox.add(Box.createHorizontalGlue());
		modelsBox.add(titleBox);
		modelsBox.add(openModelsCombo);
		mainBox.add(modelsBox);

		mainBox.add(Box.createRigidArea(new Dimension(5, 0)));

		templatesCombo = new JComboBox();
		templatesCombo.setRenderer(new JLabelListRenderer());
		Box templatesBox = Box.createVerticalBox();
		titleBox = Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_templates")));
		titleBox.add(Box.createHorizontalGlue());
		templatesBox.add(titleBox);
		templatesBox.add(templatesCombo);
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

	protected static JComboBox openModelsCombo;

	protected static JComboBox templatesCombo;

	public static void showAndAssign(TemplateEditableCanvas canvas,
			Entity entity)
	{
		canvas.setUIInteraction(true);
		AssignFSADialog.canvas = canvas;
		AssignFSADialog.entity = entity;
		instance();

		// prepare ComboBox with loaded FSAs
		Set<FSAModel> openModels = new HashSet<FSAModel>(Hub
				.getWorkspace().getModelsOfType(FSAModel.class));
		Set<FSAModel> designModels = new HashSet<FSAModel>();
		for (TemplateComponent c : canvas
				.getDiagram().getModel().getComponents())
		{
			if (c.hasModel())
			{
				openModels.add(c.getModel());
				designModels.add(c.getModel());
			}
		}
		if (entity.getComponent().hasModel())
		{
			openModels.remove(entity.getComponent().getModel());
		}
		Vector<FSAModel> sortedModels = new Vector<FSAModel>(openModels);
		Collections.sort(sortedModels, new Comparator<FSAModel>()
		{

			public int compare(FSAModel o1, FSAModel o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		openModelsCombo.removeActionListener(onSelectModel);
		for (FSAModel fsa : sortedModels)
		{
			if (designModels.contains(fsa))
			{
				openModelsCombo.addItem(new FSACell(fsa, canvas
						.getDiagram().getEntityWithFSA(fsa).getIcon()));
			}
			else
			{
				openModelsCombo.addItem(new FSACell(fsa, new ImageIcon(fsa
						.getModelType().getIcon())));
			}
		}
		openModelsCombo.setSelectedIndex(-1);
		openModelsCombo.addActionListener(onSelectModel);

		// prepare ComboBox with templates
		Vector<Template> templates = new Vector<Template>(TemplateManager
				.instance().getMainLibrary().getTemplates());
		Collections.sort(templates, new Comparator<Template>()
		{

			public int compare(Template arg0, Template arg1)
			{
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		templatesCombo.removeActionListener(onSelectTemplate);
		for (Template t : templates)
		{
			templatesCombo.addItem(new TemplateCell(t));
		}
		templatesCombo.setSelectedIndex(-1);
		templatesCombo.addActionListener(onSelectTemplate);

		me.pack();
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
		Point p = new Point(entity.getLocation().x, entity.getLocation().y);
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
		Hub.getMainWindow().removeWindowListener(commitOnFocusLost);
		openModelsCombo.removeActionListener(onSelectModel);
		openModelsCombo.removeAllItems();
		templatesCombo.removeActionListener(onSelectTemplate);
		templatesCombo.removeAllItems();
		canvas.setUIInteraction(false);
		setVisible(false);
	}

}

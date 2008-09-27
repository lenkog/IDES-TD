package templates.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.util.Comparator;
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
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;

public class LibraryUI extends Box implements Presentation
{
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
			AddTemplateDialog.addTemplate(TemplateManager.instance().getMainLibrary());
		}
	}

	public static class DeleteTemplateAction extends AbstractAction
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
		}
	}

	public static class EditTemplateAction extends AbstractAction
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
		}
	}

	private static class TemplateListRenderer extends JLabel implements ListCellRenderer {
	     public TemplateListRenderer() {
	         setOpaque(true);
	     }
	     private Border inactiveBorder=BorderFactory.createEmptyBorder(1,1,1,1);
	     private Border activeBorder=BorderFactory.createLineBorder(Color.BLACK);
	     
	     public Component getListCellRendererComponent(
	         JList list,
	         Object value,
	         int index,
	         boolean isSelected,
	         boolean cellHasFocus)
	     {
	    	 setBackground(SystemColor.text);
	    	 if(value == null)
	    	 {
	    		 setText("");
	    	 }
	    	 else if(value instanceof Template)
	    	 {
	    		 setText(((Template)value).getDescription());
	    		 setIcon(((Template)value).getIcon());
//	    		 this.getGraphics().setColor(Color.BLACK);
	    	 }
	    	 else
	    	 {
	    		 setText(value.toString());
	    	 }
	    	 if(isSelected)
	    	 {
	    		 setBorder(activeBorder);
	    	 }
	    	 else
	    	 {
	    		 setBorder(inactiveBorder);
	    	 }
	         return this;
	     }
	 }
	
	protected JList list;
	protected DefaultListModel model;
	
	public LibraryUI()
	{
		super(BoxLayout.Y_AXIS);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		list=new JList();
		list.setCellRenderer(new TemplateListRenderer());
		model=new DefaultListModel();
		updateList();
		list.setModel(model);
		
		Box titleBox=Box.createHorizontalBox();
		titleBox.add(new JLabel(Hub.string("TD_avaliableTemplates")));
		titleBox.add(Box.createHorizontalGlue());
		add(titleBox);

		add(Box.createRigidArea(new Dimension(0,5)));

		JScrollPane sp=new JScrollPane(list);
		add(sp);
		
		add(Box.createRigidArea(new Dimension(0,5)));
		
		Box buttonBox=Box.createHorizontalBox();
		JButton addBut=new JButton(new AddTemplateAction());
		buttonBox.add(addBut);
		buttonBox.add(Box.createRigidArea(new Dimension(5,0)));
		JButton delBut=new JButton(new DeleteTemplateAction());
		buttonBox.add(delBut);
		buttonBox.add(Box.createRigidArea(new Dimension(5,0)));
		JButton editBut=new JButton(new EditTemplateAction());
		buttonBox.add(editBut);
		add(buttonBox);
	}
	
	protected void updateList()
	{
		Set<Template> templates=new TreeSet<Template>(new Comparator<Template>()
				{

					public int compare(Template o1, Template o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
			
				});
		for(Template t:TemplateManager.instance().getMainLibrary().getTemplates())
		{
			templates.add(t);
		}
		model.removeAllElements();
		for(Template t:templates)
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

}

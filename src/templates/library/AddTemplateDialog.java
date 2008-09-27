package templates.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import templates.diagram.Entity;
import templates.model.TemplateComponent;
import templates.model.TemplateModel;
import templates.presentation.AssignFSADialog;
import templates.presentation.TemplateEditableCanvas;
import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESModel;
import ides.api.utilities.EscapeDialog;

public class AddTemplateDialog extends EscapeDialog
{
	private static final long serialVersionUID = -2871252921508560702L;

	private static AddTemplateDialog me = null;

	private AddTemplateDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_addTemplateTitle"), true);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				onEscapeEvent();
			}
		});
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		 this.setMinimumSize(new Dimension(250, 350));

		Box mainBox = Box.createVerticalBox();
		mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box specBox = Box.createHorizontalBox();
		Box tagBox = Box.createHorizontalBox();
		tagBox.setBorder(BorderFactory.createTitledBorder(Hub
				.string("TD_tagBoxTitle")));
		tagField = new JTextField(5);
		tagField.setMaximumSize(new Dimension(tagField.getMaximumSize().width,tagField.getMinimumSize().height));
		tagBox.add(tagField);
		specBox.add(tagBox);
		specBox.add(Box.createRigidArea(new Dimension(10, 0)));
		Box colorBox = Box.createHorizontalBox();
		colorBox.setBorder(BorderFactory.createTitledBorder(Hub
				.string("TD_colorBoxTitle")));
		colorIcon=new ColorIcon();
		JButton colorButton = new JButton(colorIcon);
		colorBox.add(colorButton);
		specBox.add(colorBox);
		mainBox.add(specBox);

		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		selectModelBox = Box.createHorizontalBox();
		selectModelBox.setBorder(BorderFactory.createTitledBorder(Hub
				.string("TD_modelBoxTitle")));
		modelsCombo = new JComboBox();
		modelsCombo.setMaximumSize(new Dimension(modelsCombo.getMaximumSize().width,modelsCombo.getPreferredSize().height));
		modelsCombo.setRenderer(new FSARenderer());
		selectModelBox.add(modelsCombo);
		mainBox.add(selectModelBox);

		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		Box descBox = Box.createHorizontalBox();
		descBox.setBorder(BorderFactory.createTitledBorder(Hub
				.string("TD_descBoxTitle")));
		descArea = new JTextArea();
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		descBox.add(new JScrollPane(descArea));
		mainBox.add(descBox);

		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		JButton okButton = new JButton(Hub.string("TD_OK"));
		okButton.addActionListener(onCommit);
		buttonBox.add(okButton);
		buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton cancelButton = new JButton(Hub.string("TD_cancel"));
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onEscapeEvent();
			}
		});
		buttonBox.add(cancelButton);
		buttonBox.add(Box.createHorizontalGlue());
		mainBox.add(buttonBox);

		getContentPane().add(mainBox);

		//resize OK button
		pack();
		okButton.setPreferredSize(new Dimension(Math.max(okButton.getWidth(),cancelButton.getWidth()),
				Math.max(okButton.getHeight(),cancelButton.getHeight())));
		cancelButton.setPreferredSize(new Dimension(Math.max(okButton.getWidth(),cancelButton.getWidth()),
				Math.max(okButton.getHeight(),cancelButton.getHeight())));
		okButton.invalidate();
		cancelButton.invalidate();
	}

	public static AddTemplateDialog instance()
	{
		if (me == null)
		{
			me = new AddTemplateDialog();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	protected static Box selectModelBox;

	protected static JComboBox modelsCombo;

	protected static ColorIcon colorIcon;

	protected static JTextField tagField;

	protected static JTextArea descArea;
	
	protected static TemplateLibrary library;
	
	public static void addTemplate(TemplateLibrary library)
	{
		Set<FSAModel> openModels = new HashSet<FSAModel>();
		for (Iterator<DESModel> i = Hub.getWorkspace().getModels(); i.hasNext();)
		{
			DESModel model = i.next();
			if (model instanceof FSAModel)
			{
				openModels.add((FSAModel)model);
			}
		}
		DESModel activeModel = Hub.getWorkspace().getActiveModel();
		if (activeModel != null && activeModel instanceof TemplateModel)
		{
			for (TemplateComponent c : ((TemplateModel)activeModel)
					.getComponents())
			{
				if (c.getModel() != null)
				{
					openModels.add(c.getModel());
				}
			}
		}
		Vector<FSAModel> sortedModels = new Vector<FSAModel>(openModels);
		Collections.sort(sortedModels, new Comparator<FSAModel>()
		{
			public int compare(FSAModel o1, FSAModel o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		addTemplate(library,sortedModels);
	}
	
	public static void addTemplate(TemplateLibrary library,FSAModel model)
	{
		addTemplate(library,Arrays.asList(new FSAModel[]{model}));
	}

	protected static void addTemplate(TemplateLibrary library, Collection<FSAModel> models)
	{
		if(models.isEmpty())
		{
			Hub.displayAlert(Hub.string("TD_noModels4Template"));
			return;
		}
		
		instance();
		me.library=library;

		modelsCombo.removeAllItems();
		for (FSAModel fsa : models)
		{
			modelsCombo.addItem(fsa);
		}
		if(models.size()==1)
		{
			modelsCombo.setSelectedIndex(0);
		}
		else
		{
			modelsCombo.setSelectedIndex(-1);
		}

		colorIcon.setColor(Color.WHITE);
		tagField.setText("");
		descArea.setText("");

		me.pack();
		me.setLocation(Hub.getCenteredLocationForDialog(me.getSize()));
		me.setVisible(true);
	}

	@Override
	public void onEscapeEvent()
	{
		setVisible(false);
	}
	
	protected static class ColorIcon implements Icon
	{
		protected static final int size=20;
		
		Color color=Color.WHITE;
		
		public int getIconHeight()
		{
			return size;
		}

		public int getIconWidth()
		{
			return size;
		}

		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Color old=g.getColor();
			g.setColor(color);
			g.fillRect(x,y,size,size);
			g.setColor(old);
		}
		
		public void setColor(Color color)
		{
			this.color=color;
		}

		public Color getColor()
		{
			return color;
		}
	}
	
	private static class FSARenderer extends JLabel implements ListCellRenderer {
	     public FSARenderer() {
	         setOpaque(true);
	     }
	     public Component getListCellRendererComponent(
	         JList list,
	         Object value,
	         int index,
	         boolean isSelected,
	         boolean cellHasFocus)
	     {
	    	 if(value == null)
	    	 {
	    		 setText("");
	    	 }
	    	 else if(value instanceof FSAModel)
	    	 {
	    		 setText(((FSAModel)value).getName());
	    	 }
	    	 else
	    	 {
	    		 setText(value.toString());
	    	 }
	         setBackground(isSelected ? SystemColor.textHighlight : SystemColor.text);
	         setForeground(isSelected ? SystemColor.textHighlightText : SystemColor.textText);
	         return this;
	     }
	 }

	
	protected static ActionListener onCommit=new ActionListener()
	{
		public void actionPerformed(ActionEvent arg0)
		{
			if(tagField.getText().length()==0||descArea.getText().length()==0||modelsCombo.getSelectedIndex()<0)
			{
				Hub.displayAlert(Hub.string("TD_incompleteTemplateInfo"));
				return;
			}
			if(library.getTemplate(tagField.getText())!=null)
			{
				Hub.displayAlert(Hub.string("TD_duplicateTag"));
				return;
			}
			TemplateDescriptor td=new TemplateDescriptor();
			td.tag=tagField.getText();
			td.color=colorIcon.getColor();
			td.description=descArea.getText();
			try
			{
				library.addTemplate(td,(FSAModel)modelsCombo.getSelectedItem());
			}
			catch(IOException e)
			{
				Hub.displayAlert(Hub.string("TD_errorCreatingTemplate")+" ["+
						e.getMessage()+"]");
				return;
			}
			me.onEscapeEvent();
		}
	};
}
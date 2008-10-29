package templates.presentation;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAEvent;
import ides.api.model.fsa.FSAMessage;
import ides.api.model.fsa.FSAModel;
import ides.api.utilities.EscapeDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramUndoableEdits;

public class ControllabilityDialog extends EscapeDialog
{
	private static final long serialVersionUID = -2875089939362805966L;

	private static ControllabilityDialog me = null;

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

	protected static ControllabilityDialog instance()
	{
		if (me == null)
		{
			me = new ControllabilityDialog();
		}
		return me;
	}

	@Override
	public Object clone()
	{
		throw new RuntimeException("Cloning of " + this.getClass().toString()
				+ " not supported.");
	}

	private ControllabilityDialog()
	{
		super(Hub.getMainWindow(), Hub.string("TD_controllabilityTitle"));
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

		eventList = new JList();
		eventList.setCellRenderer(new JCheckBoxListRenderer());
		eventList.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0)
			{
				int idx=eventList.locationToIndex(arg0.getPoint());
				if(idx>=0&&eventList.getCellBounds(idx,idx).contains(arg0.getPoint())&&
						eventList.getModel().getElementAt(idx) instanceof JCheckBox)
				{
					((JCheckBox)eventList.getModel().getElementAt(idx)).setSelected(!
							((JCheckBox)eventList.getModel().getElementAt(idx)).isSelected());
				}
				eventList.repaint();
			}

			public void mouseEntered(MouseEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}
		});

		mainBox.add(new JScrollPane(eventList));
		getContentPane().add(mainBox);
	}

	protected static JList eventList;
	protected static CompoundEdit edits;

	public static void showAndModify(TemplateEditableCanvas canvas,
			Entity channel)
	{
//		canvas.setUIInteraction(true);
		ControllabilityDialog.canvas=canvas;
		instance();
		edits=new CompoundEdit();
		
		List<FSAEvent> events=new LinkedList<FSAEvent>();
		for(Iterator<FSAEvent> i=channel.getComponent().getModel().getEventIterator();i.hasNext();)
		{
			events.add(i.next());
		}
		Collections.sort(events);
		DefaultListModel listModel = new DefaultListModel();
		for(FSAEvent event:events)
		{
			JCheckBox box=new JCheckBox();
			box.setText(event.getSymbol());
			box.setSelected(event.isControllable());
			box.addItemListener(new ControllabilitySetter(channel.getComponent().getModel(),event.getId(),edits));
			listModel.addElement(box);
		}
		if(events.isEmpty())
		{
			listModel.addElement(Hub.string("TD_noEventsInModel"));
		}
		eventList.setModel(listModel);
		
		instance().pack();
		Point p = canvas.localToScreen(channel.getLocation());
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
		instance().setLocation(p);
		Hub.getMainWindow().addWindowListener(onFocusLost);
		instance().setVisible(true);
	}

	@Override
	public void onEscapeEvent()
	{
		Hub.getMainWindow().removeWindowListener(onFocusLost);
		setVisible(false);
		edits.end();
		Hub.getUndoManager().addEdit(edits);
		canvas.setUIInteraction(false);
		eventList.setListData(new Object[0]);
	}

	protected class JCheckBoxListRenderer extends Box implements ListCellRenderer
	{

		private static final long serialVersionUID = -8828783426676456157L;

		protected JLabel label;
		
		public JCheckBoxListRenderer()
		{
			super(BoxLayout.X_AXIS);
			label=new JLabel();
			label.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			removeAll();
			if (value instanceof JCheckBox)
			{
				add((JCheckBox)value);
				((JCheckBox)value).setOpaque(true);
				((JCheckBox)value).setBackground(list.getBackground());
			}
			else
			{
				label.setText(value.toString());
				add(label);
			}
			// if (isSelected)
			// {
			// setBackground(SystemColor.textHighlight);
			// setOpaque(true);
			// }
			// else
			// {
			// setBackground(SystemColor.control);
			// setOpaque(false);
			// }
			// removeAll();
			// add(label);
			add(Box.createHorizontalGlue());
			return this;
		}
	}

	private static class ControllabilitySetter implements ItemListener
	{
		protected FSAModel model;
		protected long eventID;
		protected CompoundEdit allEdits;
		
		public ControllabilitySetter(FSAModel model, long eventID, CompoundEdit edit)
		{
			this.model=model;
			this.eventID=eventID;
			this.allEdits=edit;
		}

		public void itemStateChanged(ItemEvent e)
		{
			JCheckBox box=(JCheckBox)e.getSource();
			UndoableEdit edit=new DiagramUndoableEdits.SetControllabilityEdit(model,eventID,box.isSelected());
			edit.redo();
			allEdits.addEdit(edit);
		}
	}
}
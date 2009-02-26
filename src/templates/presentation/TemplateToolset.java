package templates.presentation;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.presentation.Presentation;
import ides.api.plugin.presentation.Toolset;
import ides.api.plugin.presentation.UIDescriptor;
import ides.api.plugin.presentation.UnsupportedModelException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import templates.library.LibraryUI;
import templates.model.TemplateModel;
import templates.model.TemplateModelSubscriber;

public class TemplateToolset implements Toolset
{
	protected static LibraryUI library = null;

	protected static TransferHandler transferHandler = new CanvasTransferHandler();

	protected class TemplateUID implements UIDescriptor
	{
		protected TemplateModel model;

		protected TemplateEditableCanvas canvas;

		protected TemplateConsistencyCanvas consistency;

		protected IssuesViewer issues;

		protected Presentation statusBar;

		public TemplateUID(TemplateModel model)
		{
			if (library == null)
			{
				library = new LibraryUI();
			}
			this.model = model;
			canvas = new TemplateEditableCanvas(model);
			canvas.setName(Hub.string("TD_modelCanvasTitle"));
			canvas.setTransferHandler(transferHandler);
			consistency = new TemplateConsistencyCanvas(model);
			consistency.setName(Hub.string("TD_consistencyCanvasTitle"));
			consistency.setTransferHandler(transferHandler);
			issues = new IssuesViewer(canvas.getDiagram());
			statusBar = new TemplateStatusBar(model, issues);
		}

		public Presentation[] getLeftPanePresentations()
		{
			// TODO Auto-generated method stub
			return new Presentation[] {};
		}

		public Presentation[] getMainPanePresentations()
		{
			return new Presentation[] { canvas, consistency };
		}

		public JMenu[] getMenus()
		{
			// TODO Auto-generated method stub
			return new JMenu[] {};
		}

		public Presentation[] getRightPanePresentations()
		{
			return new Presentation[] { library, issues };
		}

		public Presentation getStatusBar()
		{
			return statusBar;
		}

		public JToolBar getToolbar()
		{
			return new JToolBar();
		}

		public boolean supportsZoom()
		{
			return true;
		}

	}

	public Presentation getModelThumbnail(DESModel model, int width, int height)
			throws UnsupportedModelException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new UnsupportedModelException();
		}
		return new TemplateCanvas((TemplateModel)model);
	}

	public UIDescriptor getUIElements(DESModel model)
			throws UnsupportedModelException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new UnsupportedModelException();
		}
		return new TemplateUID((TemplateModel)model);
	}

	protected static class TemplateStatusBar extends JLabel implements
			Presentation, DESModelSubscriber, ListDataListener
	{
		private static final long serialVersionUID = 341918799019344384L;

		private boolean trackModel = true;

		protected TemplateModel model;

		protected IssuesViewer issueList;

		public TemplateStatusBar(TemplateModel model, IssuesViewer issueList)
		{
			this.model = model;
			this.issueList = issueList;
			model.addSubscriber((DESModelSubscriber)this);
			issueList.addIssuesListener(this);
			refresh();
		}

		public void refresh()
		{
			String issues = "";
			if (issueList.getIssueCount() > 0)
			{
				if (issueList.getIssueCount() == 1)
				{
					issues = " " + Hub.string("TD_statusBarIssues1a") + " "
							+ issueList.getIssueCount() + " "
							+ Hub.string("TD_statusBarIssues2a");
				}
				else
				{
					issues = " " + Hub.string("TD_statusBarIssues1b") + " "
							+ issueList.getIssueCount() + " "
							+ Hub.string("TD_statusBarIssues2b");
				}
			}
			setText(model.getName() + ":  " + +model.getComponentCount() + " "
					+ Hub.string("TD_statsBarEntities") + "." + issues);
		}

		public void forceRepaint()
		{
			refresh();
			repaint();
		}

		public JComponent getGUI()
		{
			return this;
		}

		public DESModel getModel()
		{
			return model;
		}

		public void release()
		{
			issueList.removeIssuesListener(this);
			model.removeSubscriber((DESModelSubscriber)this);
		}

		public void setTrackModel(boolean b)
		{
			if (trackModel != b)
			{
				trackModel = b;
				if (trackModel)
				{
					model.addSubscriber((DESModelSubscriber)this);
					issueList.addIssuesListener(this);
				}
				else
				{
					issueList.removeIssuesListener(this);
					model.removeSubscriber((TemplateModelSubscriber)this);
				}
			}
		}

		public void modelNameChanged(DESModelMessage arg0)
		{
			refresh();
		}

		public void saveStatusChanged(DESModelMessage arg0)
		{
		}

		public void contentsChanged(ListDataEvent arg0)
		{
			refresh();
		}

		public void intervalAdded(ListDataEvent arg0)
		{
			refresh();
		}

		public void intervalRemoved(ListDataEvent arg0)
		{
			refresh();
		}
	}
}

package templates.presentation;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;
import ides.api.plugin.presentation.Toolset;
import ides.api.plugin.presentation.UIDescriptor;
import ides.api.plugin.presentation.UnsupportedModelException;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import templates.library.LibraryUI;
import templates.model.TemplateModel;

public class TemplateToolset implements Toolset
{
	protected static LibraryUI library=null;

	protected class TemplateUID implements UIDescriptor
	{
		protected TemplateModel model;

		protected TemplateEditableCanvas canvas;

		protected TemplateConsistencyCanvas consistency;

		protected IssuesViewer issues;

		public TemplateUID(TemplateModel model)
		{
			if(library==null)
			{
				library=new LibraryUI();
			}
			this.model = model;
			canvas = new TemplateEditableCanvas(model);
			canvas.setName(Hub.string("TD_modelCanvasTitle"));
			consistency = new TemplateConsistencyCanvas(model);
			consistency.setName(Hub.string("TD_consistencyCanvasTitle"));
			issues = new IssuesViewer(canvas.getDiagram());
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

		public JMenu getPopupMenu()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Presentation[] getRightPanePresentations()
		{
			return new Presentation[] { library, issues };
		}

		public JComponent getStatusBar()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public JToolBar getToolbar()
		{
			// TODO Auto-generated method stub
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

}

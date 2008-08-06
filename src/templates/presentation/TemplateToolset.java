package templates.presentation;

import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;
import ides.api.plugin.presentation.Toolset;
import ides.api.plugin.presentation.UIDescriptor;
import ides.api.plugin.presentation.UnsupportedModelException;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import templates.model.TemplateModel;

public class TemplateToolset implements Toolset
{
	protected class TemplateUID implements UIDescriptor
	{

		TemplateModel model;

		Presentation canvas;

		public TemplateUID(TemplateModel model)
		{
			this.model = model;
			canvas = new TemplateEditableCanvas(model);
		}

		public Presentation[] getLeftPanePresentations()
		{
			// TODO Auto-generated method stub
			return new Presentation[] {};
		}

		public Presentation[] getMainPanePresentations()
		{
			// TODO Auto-generated method stub
			return new Presentation[] { canvas };
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
			// TODO Auto-generated method stub
			return new Presentation[] {};
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

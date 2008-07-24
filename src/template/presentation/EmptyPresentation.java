package template.presentation;

import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class EmptyPresentation implements Presentation
{

	protected DESModel model;

	public EmptyPresentation(DESModel model)
	{
		this.model = model;
	}

	public void forceRepaint()
	{
		// TODO Auto-generated method stub

	}

	public JComponent getGUI()
	{
		// TODO Auto-generated method stub
		return new JPanel();
	}

	public DESModel getModel()
	{
		// TODO Auto-generated method stub
		return model;
	}

	public String getName()
	{
		// TODO Auto-generated method stub
		return model.getName();
	}

	public void release()
	{
		// TODO Auto-generated method stub

	}

	public void setTrackModel(boolean b)
	{
		// TODO Auto-generated method stub

	}

}

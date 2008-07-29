package template.presentation;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import template.model.TemplateModel;

public class TemplateEditableCanvas extends TemplateCanvas
{

	public TemplateEditableCanvas(TemplateModel model)
	{
		super(model);
	}
	
	public JComponent getGUI()
	{
		JScrollPane sp = new JScrollPane(
				this,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		return sp;
	}
}

package templates.library;

import ides.api.model.fsa.FSAModel;

import java.awt.datatransfer.DataFlavor;

import templates.utils.EntityIcon;

public interface Template
{
	public static final String TEMPLATE_DESC = "templates.library.TemplateDescriptor";

	public static final DataFlavor templateFlavor = new DataFlavor(
			Template.class,
			"template");

	public static final DataFlavor fsaFlavor = new DataFlavor(
			FSAModel.class,
			"FSA");

	public String getName();

	public EntityIcon getIcon();

	public String getDescription();

	public FSAModel getModel();

	public FSAModel instantiate();
}

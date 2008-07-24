package template.model;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESElement;

public interface TemplateModule extends DESElement
{
	public FSAModel getModel();

	public void setModel(FSAModel fsa);

	public boolean hasModel();
}

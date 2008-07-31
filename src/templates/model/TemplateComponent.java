package templates.model;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESElement;

public interface TemplateComponent extends DESElement
{
	public final int TYPE_INDETERMINATE=0;
	public final int TYPE_MODULE=1;
	public final int TYPE_CHANNEL=2;
	
	public int getType();
	
	public void setType(int type);
	
	public FSAModel getModel();

	public void setModel(FSAModel fsa);

	public boolean hasModel();
}

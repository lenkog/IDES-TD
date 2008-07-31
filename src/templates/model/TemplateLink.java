package templates.model;

import ides.api.model.fsa.FSAEvent;
import ides.api.plugin.model.DESElement;

public interface TemplateLink extends DESElement
{
	public TemplateComponent getLeftComponent();
	
	public TemplateComponent getRightComponent();

	public TemplateComponent[] getComponents();

	public boolean hasProperNeighbors();

	public TemplateComponent getModule();

	public TemplateComponent getChannel();
	
	public String getLeftEventName();

	public boolean existsLeftEvent();

	public FSAEvent getLeftEvent();

	public String getRightEventName();

	public boolean existsRightEvent();

	public FSAEvent getRightEvent();

	public void setLeftEventName(String name);

	public void setRightEventName(String name);
}

package templates.model;

import ides.api.plugin.model.DESElement;
import ides.api.plugin.model.DESEvent;

public interface TemplateLink extends DESElement
{
	public TemplateComponent getLeftComponent();

	public TemplateComponent getRightComponent();

	public TemplateComponent[] getComponents();

	public TemplateComponent getModule();

	public TemplateComponent getChannel();

	public String getLeftEventName();

	public boolean existsLeftEvent();

	public DESEvent getLeftEvent();

	public String getRightEventName();

	public boolean existsRightEvent();

	public DESEvent getRightEvent();

	public void setLeftEventName(String name);

	public void setRightEventName(String name);
}

package template.model;

import ides.api.model.fsa.FSAEvent;
import ides.api.plugin.model.DESElement;

public interface TemplateLink extends DESElement
{
	public TemplateModule getModule();

	public TemplateChannel getChannel();

	public String getModuleEventName();

	public boolean existsModuleEvent();

	public FSAEvent getModuleEvent();

	public String getChannelEventName();

	public boolean existsChannelEvent();

	public FSAEvent getChannelEvent();

	public void setModuleEventName(String name);

	public void setChannelEventName(String name);
}

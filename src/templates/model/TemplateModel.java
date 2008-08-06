package templates.model;

import ides.api.plugin.model.DESModel;

import java.util.Collection;

public interface TemplateModel extends DESModel, TemplateModelPublisher
{
	public Collection<TemplateComponent> getComponents();

	public Collection<TemplateComponent> getModules();

	public Collection<TemplateComponent> getChannels();

	public Collection<TemplateLink> getLinks();

	public TemplateComponent getComponent(long id);

	public TemplateLink getLink(long id);

	public TemplateComponent createComponent();

	public void addComponent(TemplateComponent component);

	public TemplateLink createLink(long leftId, long rightId);

	public void addLink(TemplateLink link);

	public void removeComponent(long id);

	public void removeLink(long id);

	public Collection<TemplateLink> getAdjacentLinks(long componentId);

	public Collection<TemplateComponent> getCover(long channelId);

	public boolean existsLink(long leftId, long rightId);

	public Collection<TemplateLink> getLinks(long leftId, long rightId);
}

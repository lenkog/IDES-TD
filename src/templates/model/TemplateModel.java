package templates.model;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ParentModel;

import java.util.Collection;

public interface TemplateModel extends ParentModel, TemplateModelPublisher
{
	public static String FSA_NAME_PREFIX = "TD:";

	public Collection<TemplateComponent> getComponents();

	public Collection<TemplateComponent> getModules();

	public Collection<TemplateComponent> getChannels();

	public Collection<TemplateLink> getLinks();

	public TemplateComponent getComponent(long id);

	public TemplateLink getLink(long id);

	public TemplateComponent assembleComponent();

	public TemplateComponent createComponent();

	public void addComponent(TemplateComponent component);

	public TemplateLink assembleLink(long leftId, long rightId);

	public TemplateLink createLink(long leftId, long rightId);

	public void addLink(TemplateLink link);

	public void removeComponent(long id);

	public void removeLink(long id);

	public Collection<TemplateLink> getAdjacentLinks(long componentId);

	public Collection<TemplateComponent> getCover(long channelId);

	public boolean existsLink(long leftId, long rightId);

	public Collection<TemplateLink> getLinks(long leftId, long rightId);

	public void assignFSA(long componentId, FSAModel fsa);

	public void removeFSA(long componentId);
}

package template.model.v3;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.model.DESModelType;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import template.model.InconsistentModificationException;
import template.model.TemplateComponent;
import template.model.TemplateLink;
import template.model.TemplateModel;
import template.model.TemplateModelMessage;
import template.model.TemplateModelSubscriber;

public class TemplateDesign implements TemplateModel
{
	protected Hashtable<String, Object> annotations = new Hashtable<String, Object>();

	public Object getAnnotation(String key)
	{
		return annotations.get(key);
	}

	public boolean hasAnnotation(String key)
	{
		return annotations.containsKey(key);
	}

	public void removeAnnotation(String key)
	{
		annotations.remove(key);
	}

	public void setAnnotation(String key, Object annotation)
	{
		if (annotation != null)
		{
			annotations.put(key, annotation);
		}
	}

	private ArrayList<DESModelSubscriber> modelSubscribers = new ArrayList<DESModelSubscriber>();

	/**
	 * Attaches the given subscriber to this publisher. The given subscriber
	 * will receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void addSubscriber(DESModelSubscriber subscriber)
	{
		modelSubscribers.add(subscriber);
	}

	/**
	 * Removes the given subscriber to this publisher. The given subscriber will
	 * no longer receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void removeSubscriber(DESModelSubscriber subscriber)
	{
		modelSubscribers.remove(subscriber);
	}

	/**
	 * Returns all current subscribers to this publisher.
	 * 
	 * @return all current subscribers to this publisher
	 */
	public DESModelSubscriber[] getDESModelSubscribers()
	{
		return modelSubscribers.toArray(new DESModelSubscriber[] {});
	}

	private ArrayList<TemplateModelSubscriber> templateSubscribers = new ArrayList<TemplateModelSubscriber>();

	public void addSubscriber(TemplateModelSubscriber subscriber)
	{
		templateSubscribers.add(subscriber);
	}

	public void removeSubscriber(TemplateModelSubscriber subscriber)
	{
		templateSubscribers.remove(subscriber);
	}

	public TemplateModelSubscriber[] getTemplateModelSubscribers()
	{
		return templateSubscribers.toArray(new TemplateModelSubscriber[]{});
	}

	public void fireTemplateModelStructureChanged(TemplateModelMessage message)
	{
		for(TemplateModelSubscriber subscriber:templateSubscribers)
		{
			subscriber.templateModelStructureChanged(message);
		}
	}

	protected boolean needsSave = false;

	public boolean needsSave()
	{
		return needsSave;
	}

	protected void setNeedsSave(boolean b)
	{
		if (b != needsSave)
		{
			needsSave = b;
			DESModelMessage message = new DESModelMessage(
					needsSave ? DESModelMessage.DIRTY : DESModelMessage.CLEAN,
					this);
			for (DESModelSubscriber s : modelSubscribers)
			{
				s.saveStatusChanged(message);
			}
		}
	}

	protected static class TemplateDesignDescriptor implements DESModelType
	{

		public TemplateModel createModel(String name)
		{
			return new TemplateDesign(name);
		}

		public String getDescription()
		{
			return "Template Design";
		}

		public Image getIcon()
		{
			return Toolkit.getDefaultToolkit().createImage(Hub
					.getResource("images/icons/model_template.gif"));
		}

		public Class<TemplateModel> getMainPerspective()
		{
			return TemplateModel.class;
		}

		public Class<?>[] getModelPerspectives()
		{
			return new Class[] { TemplateModel.class };
		}
	}

	public static TemplateDesignDescriptor myDescriptor = new TemplateDesignDescriptor();

	public DESModelType getModelType()
	{
		return myDescriptor;
	}

	protected String name;

	protected Set<TemplateComponent> components = new HashSet<TemplateComponent>();

	protected Set<TemplateLink> links = new HashSet<TemplateLink>();

	protected long freeComponentId = 0;

	protected long freeLinkId = 0;

	public TemplateDesign(String name)
	{
		this.name = name;
	}

	protected boolean containsComponentId(long id)
	{
		for (TemplateComponent component : components)
		{
			if (component.getId() == id)
			{
				return true;
			}
		}
		return false;
	}

	protected boolean containsLinkId(long id)
	{
		for (TemplateLink link : links)
		{
			if (link.getId() == id)
			{
				return true;
			}
		}
		return false;
	}

	public synchronized void addComponent(TemplateComponent component)
	{
		if (containsComponentId(component.getId()))
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyComponentId"));
		}
		if (freeComponentId <= component.getId())
		{
			freeComponentId = component.getId() + 1;
		}
		components.add(component);
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,component.getId(),
				TemplateModelMessage.ELEMENT_COMPONENT,TemplateModelMessage.OP_ADD));
		setNeedsSave(true);
	}

	public synchronized void addLink(TemplateLink link)
	{
		if (containsLinkId(link.getId()))
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinkId"));
		}
		if (!components.contains(link.getLeftComponent())
				|| !components.contains(link.getRightComponent()))
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinking"));
		}
//		Collection<TemplateLink> channelLinks = getChannelLinks(link
//				.getChannel().getId());
//		for (TemplateLink l : channelLinks)
//		{
//			if (l.getChannelEventName().equals(link.getChannelEventName())
//					|| (l.getModule() == link.getModule() && l
//							.getModuleEventName().equals(link
//									.getModuleEventName())))
//			{
//				throw new InconsistentModificationException(Hub
//						.string("TD_inconsistencyLinking"));
//			}
//		}
		if (freeLinkId <= link.getId())
		{
			freeLinkId = link.getId() + 1;
		}
		links.add(link);
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,link.getId(),
				TemplateModelMessage.ELEMENT_LINK,TemplateModelMessage.OP_ADD));
		setNeedsSave(true);
	}

	public synchronized TemplateLink createLink(long leftId, long rightId)
	{
		TemplateComponent left = getComponent(leftId);
		TemplateComponent right = getComponent(rightId);
		if (left == null || right == null)
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinkInit"));
		}
		Link link = new Link(freeLinkId, left, right);
		freeLinkId++;
		links.add(link);
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,link.getId(),
				TemplateModelMessage.ELEMENT_LINK,TemplateModelMessage.OP_ADD));
		setNeedsSave(true);
		return link;
	}

	public synchronized TemplateComponent createComponent()
	{
		Component component = new Component(freeComponentId);
		freeComponentId++;
		components.add(component);
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,component.getId(),
				TemplateModelMessage.ELEMENT_COMPONENT,TemplateModelMessage.OP_ADD));
		setNeedsSave(true);
		return component;
	}

	public Collection<TemplateComponent> getComponents()
	{
		return new HashSet<TemplateComponent>(components);
	}

	public Collection<TemplateLink> getLinks()
	{
		return new HashSet<TemplateLink>(links);
	}

	public TemplateComponent getComponent(long id)
	{
		for (TemplateComponent component : components)
		{
			if (component.getId() == id)
			{
				return component;
			}
		}
		return null;
	}

	public Collection<TemplateComponent> getModules()
	{
		Set<TemplateComponent> modules=new HashSet<TemplateComponent>();
		for (TemplateComponent component : components)
		{
			if (component.getType()==TemplateComponent.TYPE_MODULE)
			{
				modules.add(component);
			}
		}
		return modules;
	}

	public Collection<TemplateComponent> getChannels()
	{
		Set<TemplateComponent> channels=new HashSet<TemplateComponent>();
		for (TemplateComponent component : components)
		{
			if (component.getType()==TemplateComponent.TYPE_CHANNEL)
			{
				channels.add(component);
			}
		}
		return channels;
	}

	public TemplateLink getLink(long id)
	{
		for (TemplateLink link : links)
		{
			if (link.getId() == id)
			{
				return link;
			}
		}
		return null;
	}

	public synchronized void removeComponent(long id)
	{
		if (!containsComponentId(id))
		{
			return;
		}
		for(TemplateLink link:getAdjacentLinks(id))
		{
			removeLink(link.getId());
		}
		components.remove(getComponent(id));
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,id,
				TemplateModelMessage.ELEMENT_COMPONENT,TemplateModelMessage.OP_REMOVE));
		setNeedsSave(true);
	}

	public synchronized void removeLink(long id)
	{
		if (!containsLinkId(id))
		{
			return;
		}
		links.remove(getLink(id));
		fireTemplateModelStructureChanged(new TemplateModelMessage(this,id,
				TemplateModelMessage.ELEMENT_LINK,TemplateModelMessage.OP_REMOVE));
		setNeedsSave(true);
	}

	public String getName()
	{
		return name;
	}

	public void metadataChanged()
	{
		setNeedsSave(true);
	}

	public void modelSaved()
	{
		setNeedsSave(false);
	}

	public void setName(String name)
	{
		if (name != null && name.equals(this.name))
		{
			return;
		}
		this.name = name;
		DESModelMessage message = new DESModelMessage(
				DESModelMessage.NAME,
				this);
		for (DESModelSubscriber s : modelSubscribers)
		{
			s.modelNameChanged(message);
		}
		metadataChanged();
	}

	public boolean existsLink(long channelId, long moduleId)
	{
		for (TemplateLink link : links)
		{
			if (link.getChannel().getId() == channelId
					&& link.getModule().getId() == moduleId)
			{
				return true;
			}
		}
		return false;
	}

	public Collection<TemplateLink> getAdjacentLinks(long componentId)
	{
		Set<TemplateLink> ret = new HashSet<TemplateLink>();
		for (TemplateLink link : links)
		{
			if (link.getLeftComponent().getId() == componentId||link.getRightComponent().getId()==componentId)
			{
				ret.add(link);
			}
		}
		return ret;
	}

	public Collection<TemplateComponent> getCover(long channelId)
	{
		Set<TemplateComponent> ret = new HashSet<TemplateComponent>();
		if(getComponent(channelId).getType()==TemplateComponent.TYPE_CHANNEL)
		{
		for (TemplateLink link : getAdjacentLinks(channelId))
		{
			if (link.getModule()!=null)
			{
				ret.add(link.getModule());
			}
		}
		}
		return ret;
	}

	public Collection<TemplateLink> getLinks(long leftId, long rightId)
	{
		Set<TemplateLink> ret = new HashSet<TemplateLink>();
		for (TemplateLink link : links)
		{
			if ((link.getLeftComponent().getId() == leftId
					&& link.getRightComponent().getId() == rightId)||
					(link.getLeftComponent().getId() == rightId
							&& link.getRightComponent().getId() == leftId))
			{
				ret.add(link);
			}
		}
		return ret;
	}
}

package templates.model;

import ides.api.model.fsa.FSAEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validator
{
	public static class ValidatorResult
	{
		public static final int ERROR = 1;

		public static final int WARNING = 2;

		public String message;

		public List<TemplateComponent> components;

		public List<TemplateLink> links;

		public String event = "";

		public int type;

		public ValidatorResult(String message,
				Collection<TemplateComponent> components,
				Collection<TemplateLink> links, String event, int type)
		{
			this.message = message;
			this.components = new LinkedList<TemplateComponent>(components);
			this.links = new LinkedList<TemplateLink>(links);
			this.event = event;
			this.type = type;
		}

		public ValidatorResult(String message, TemplateComponent component,
				int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					new HashSet<TemplateLink>(),
					"",
					type);
		}

		public ValidatorResult(String message, TemplateComponent component,
				Collection<TemplateLink> links, String event, int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					links,
					event,
					type);
		}

		public ValidatorResult(String message, TemplateComponent component,
				Collection<TemplateLink> links, int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					links,
					"",
					type);
		}

		public ValidatorResult(String message, TemplateLink link, int type)
		{
			this(message, new HashSet<TemplateComponent>(), Arrays
					.asList(new TemplateLink[] { link }), "", type);
		}
	}

	private static class InjectionKey
	{
		public TemplateComponent component;

		public String event;

		public InjectionKey(TemplateComponent component, String event)
		{
			this.component = component;
			this.event = event;
		}

		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof InjectionKey))
			{
				return false;
			}
			return ((InjectionKey)o).component == component
					&& ((InjectionKey)o).event.equals(event);
		}

		public int hashCode()
		{
			return component.hashCode();
		}
	}

	public static final String ERROR_NO_MODEL = "TD_inconsistencyNoModel";

	public static final String ERROR_MODULE_CHANNEL = "TD_inconsistencyModuleChannel";

	public static final String ERROR_NO_EVENT = "TD_inconsistencyEventNotExists";

	public static final String ERROR_FORKED_EVENT = "TD_inconsistencyForkedEvent";

	public static final String ERROR_MERGED_EVENT = "TD_inconsistencyMergedEvent";

	public static final String WARNING_FREE_COMPONENT = "TD_issueFreeComponent";

	public static final String WARNING_FREE_EVENT = "TD_issueFreeChannelEvent";

	public static List<ValidatorResult> validate(TemplateModel model)
	{
		LinkedList<ValidatorResult> ret = new LinkedList<ValidatorResult>();
		for (TemplateComponent component : model.getComponents())
		{
			if (!component.hasModel())
			{
				ret.add(new ValidatorResult(
						ERROR_NO_MODEL,
						component,
						ValidatorResult.ERROR));
			}
			if (model.getAdjacentLinks(component.getId()).isEmpty())
			{
				ret.add(new ValidatorResult(
						WARNING_FREE_COMPONENT,
						component,
						ValidatorResult.WARNING));
			}
		}
		for (TemplateLink link : model.getLinks())
		{
			TemplateComponent module = link.getModule();
			TemplateComponent channel = link.getChannel();
			if (module == null || channel == null)
			{
				ret.add(new ValidatorResult(
						ERROR_MODULE_CHANNEL,
						link,
						ValidatorResult.ERROR));
			}
			if (!link.existsLeftEvent() || !link.existsRightEvent())
			{
				ret.add(new ValidatorResult(
						ERROR_NO_EVENT,
						link,
						ValidatorResult.ERROR));
			}
		}
		for (TemplateComponent component : model.getChannels())
		{
			Set<String> totalEvents = new HashSet<String>();
			Set<String> linkedEvents = new HashSet<String>();
			Set<String> doubleEvents = new HashSet<String>();
			// key=module event,value=channel events it links to
			Map<InjectionKey, Set<String>> injectionEvents = new HashMap<InjectionKey, Set<String>>();
			if (component.hasModel())
			{
				for (FSAEvent event : component.getModel().getEventSet())
				{
					totalEvents.add(event.getSymbol());
				}
			}
			for (TemplateLink link : model.getAdjacentLinks(component.getId()))
			{
				String event;
				TemplateComponent module = null;
				String moduleEvent = null;
				if (component == link.getLeftComponent())
				{
					event = link.getLeftEventName();
					if (link.getRightComponent().getType() == TemplateComponent.TYPE_MODULE)
					{
						module = link.getRightComponent();
						moduleEvent = link.getRightEventName();
					}
				}
				else
				{
					event = link.getRightEventName();
					if (link.getLeftComponent().getType() == TemplateComponent.TYPE_MODULE)
					{
						module = link.getLeftComponent();
						moduleEvent = link.getLeftEventName();
					}
				}
				if (linkedEvents.contains(event))
				{
					doubleEvents.add(event);
				}
				else
				{
					linkedEvents.add(event);
				}
				if (moduleEvent != null)
				{
					Set<String> channelEvents = injectionEvents
							.get(new InjectionKey(module, moduleEvent));
					if (channelEvents == null)
					{
						channelEvents = new HashSet<String>();
					}
					channelEvents.add(event);
					injectionEvents.put(new InjectionKey(module, moduleEvent),
							channelEvents);
				}
			}
			for (String event : doubleEvents)
			{
				ret.add(new ValidatorResult(
						ERROR_FORKED_EVENT,
						component,
						linksWithEvent(model, component, event),
						event,
						ValidatorResult.ERROR));
			}
			for (InjectionKey key : injectionEvents.keySet())
			{
				if (injectionEvents.get(key).size() > 1)
				{
					Set<TemplateLink> links = new HashSet<TemplateLink>();
					for (String event : injectionEvents.get(key))
					{
						links.addAll(linksWithEvent(model, component, event));
					}
					ret.add(new ValidatorResult(
							ERROR_MERGED_EVENT,
							Arrays.asList(new TemplateComponent[] { component,
									key.component }),
							links,
							key.event,
							ValidatorResult.ERROR));
				}
			}
			totalEvents.removeAll(linkedEvents);
			if (!totalEvents.isEmpty())
			{
				ret.add(new ValidatorResult(
						WARNING_FREE_EVENT,
						component,
						ValidatorResult.WARNING));
			}
		}
		return ret;
	}

	protected static Collection<TemplateLink> linksWithEvent(
			TemplateModel model, TemplateComponent component, String event)
	{
		Set<TemplateLink> links = new HashSet<TemplateLink>();
		for (TemplateLink link : model.getAdjacentLinks(component.getId()))
		{
			if (link.getLeftComponent() == component)
			{
				if (link.getLeftEventName().equals(event))
				{
					links.add(link);
				}
			}
			else
			{
				if (link.getRightEventName().equals(event))
				{
					links.add(link);
				}
			}
		}
		return links;
	}
}
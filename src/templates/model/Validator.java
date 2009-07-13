/*
 * Copyright (c) 2009, Lenko Grigorov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package templates.model;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validator of the consistency of {@link TemplateModel}s.
 * 
 * @author Lenko Grigorov
 */
public class Validator
{
	/**
	 * Encapsulates a consistency issue found by the validator.
	 * 
	 * @author Lenko Grigorov
	 */
	public static class ValidatorResult
	{
		/**
		 * Type "error".
		 */
		public static final int ERROR = 1;

		/**
		 * Type "warning".
		 */
		public static final int WARNING = 2;

		/**
		 * String descriptor of the message. Not human readable.
		 */
		public String message;

		/**
		 * {@link TemplateComponent}s contributing to the consistency issue, if
		 * any.
		 */
		public List<TemplateComponent> components;

		/**
		 * {@link TemplateLink}s contributing to the consistency issue, if any.
		 */
		public List<TemplateLink> links;

		/**
		 * The name of the event contributing to the consistency issue, if any.
		 */
		public String event = "";

		/**
		 * The type of the consistency issue ({@link #ERROR} or {@link #WARNING}
		 * ).
		 */
		public int type;

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param components
		 *            the {@link TemplateComponent}s contributing to the issue
		 * @param links
		 *            the {@link TemplateLink}s contributing to the issue
		 * @param event
		 *            the name of the event contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
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

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param component
		 *            the {@link TemplateComponent} contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message, TemplateComponent component,
				int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					new HashSet<TemplateLink>(),
					"",
					type);
		}

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param components
		 *            the {@link TemplateComponent}s contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message,
				Collection<TemplateComponent> components, int type)
		{
			this(message, components, new HashSet<TemplateLink>(), "", type);
		}

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param component
		 *            the {@link TemplateComponent} contributing to the issue
		 * @param links
		 *            the {@link TemplateLink}s contributing to the issue
		 * @param event
		 *            the name of the event contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message, TemplateComponent component,
				Collection<TemplateLink> links, String event, int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					links,
					event,
					type);
		}

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param component
		 *            the {@link TemplateComponent} contributing to the issue
		 * @param links
		 *            the {@link TemplateLink}s contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message, TemplateComponent component,
				Collection<TemplateLink> links, int type)
		{
			this(message,
					Arrays.asList(new TemplateComponent[] { component }),
					links,
					"",
					type);
		}

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param link
		 *            the {@link TemplateLink} contributing to the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message, TemplateLink link, int type)
		{
			this(message, new HashSet<TemplateComponent>(), Arrays
					.asList(new TemplateLink[] { link }), "", type);
		}

		/**
		 * Construct a descriptor of a consistency issue.
		 * 
		 * @param message
		 *            the string descriptor of the issue
		 * @param type
		 *            the type of the issue
		 */
		public ValidatorResult(String message, int type)
		{
			this(message,
					new HashSet<TemplateComponent>(),
					new HashSet<TemplateLink>(),
					"",
					type);
		}
	}

	/**
	 * Encapsulates the key used in a {@link Map} when checking for the
	 * injective property of links between a <i>module</i> and a <i>channel</i>.
	 * The key consists of a {@link TemplateComponent} and an event name.
	 * 
	 * @author Lenko Grigorov
	 */
	private static class InjectionKey
	{
		/**
		 * The {@link TemplateComponent} part of the key.
		 */
		public TemplateComponent component;

		/**
		 * The event name part of the key.
		 */
		public String event;

		/**
		 * Construct a new key with the given parameters.
		 * 
		 * @param component
		 *            the {@link TemplateComponent}
		 * @param event
		 *            the event name
		 */
		public InjectionKey(TemplateComponent component, String event)
		{
			this.component = component;
			this.event = event;
		}

		/**
		 * Check if the key equals another key. Equivalence is established when
		 * the {@link TemplateComponent}s in the two keys are identical and the
		 * event names are the same.
		 */
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

	/**
	 * No <i>module</i> is present in the model.
	 */
	public static final String ERROR_NO_MODULE = "TD_inconsistencyNoModule";

	/**
	 * A {@link TemplateComponent} does not have an assigned {@link FSAModel}.
	 */
	public static final String ERROR_NO_MODEL = "TD_inconsistencyNoModel";

	/**
	 * A link connects two <i>modules</i> or two <i>channels</i>.
	 */
	public static final String ERROR_MODULE_CHANNEL = "TD_inconsistencyModuleChannel";

	/**
	 * A linked event is not contained in the {@link FSAModel} of the
	 * corresponding {@link TemplateComponent}.
	 */
	public static final String ERROR_NO_EVENT = "TD_inconsistencyEventNotExists";

	/**
	 * A <i>channel</i> event is linked to multiple <i>module</i> events.
	 */
	public static final String ERROR_FORKED_EVENT = "TD_inconsistencyForkedEvent";

	/**
	 * A <i>module</i> event is linked to multiple events from the same
	 * <i>channel</i>.
	 */
	public static final String ERROR_MERGED_EVENT = "TD_inconsistencyMergedEvent";

	/**
	 * The {@link FSAModel}s of two or more {@link TemplateComponent}s share the
	 * same name. This becomes a problem when auto-renaming events to indicate
	 * which model they belong to.
	 */
	public static final String ERROR_NONUNIQUE_NAME = "TD_inconsistencyNonuniqueName";

	/**
	 * There is no <i>channel</i> in the model.
	 */
	public static final String WARNING_NO_CHANNEL = "TD_inconsistencyNoChannel";

	/**
	 * There is a {@link TemplateComponent} which is not connected.
	 */
	public static final String WARNING_FREE_COMPONENT = "TD_issueFreeComponent";

	/**
	 * A <i>channel</i> contains one or more events which are not linked.
	 */
	public static final String WARNING_FREE_EVENT = "TD_issueFreeChannelEvent";

	/**
	 * Validate the consistency of a given {@link TemplateModel} and return a
	 * collection of all consistency issues discovered.
	 * 
	 * @param model
	 *            the {@link TemplateModel} to be validated
	 * @return a collection of all consistency issues discovered
	 */
	public static List<ValidatorResult> validate(TemplateModel model)
	{
		LinkedList<ValidatorResult> ret = new LinkedList<ValidatorResult>();
		boolean hasModule = false;
		boolean hasChannel = false;
		Map<String, Set<TemplateComponent>> namesMap = new HashMap<String, Set<TemplateComponent>>();
		for (TemplateComponent component : model.getComponents())
		{
			if (component.getType() == TemplateComponent.TYPE_MODULE)
			{
				hasModule = true;
			}
			else if (component.getType() == TemplateComponent.TYPE_CHANNEL)
			{
				hasChannel = true;
			}
			if (!component.hasModel())
			{
				ret.add(new ValidatorResult(
						ERROR_NO_MODEL,
						component,
						ValidatorResult.ERROR));
			}
			else
			{
				Set<TemplateComponent> components = namesMap.get(component
						.getModel().getName());
				if (components == null)
				{
					components = new HashSet<TemplateComponent>();
				}
				components.add(component);
				namesMap.put(component.getModel().getName(), components);
			}
			if (model.getAdjacentLinks(component.getId()).isEmpty())
			{
				ret.add(new ValidatorResult(
						WARNING_FREE_COMPONENT,
						component,
						ValidatorResult.WARNING));
			}
		}
		if (!hasModule)
		{
			ret
					.add(new ValidatorResult(
							ERROR_NO_MODULE,
							ValidatorResult.ERROR));
		}
		if (!hasChannel)
		{
			ret.add(new ValidatorResult(
					WARNING_NO_CHANNEL,
					ValidatorResult.WARNING));
		}
		for (String name : namesMap.keySet())
		{
			Set<TemplateComponent> components = namesMap.get(name);
			if (components.size() != 1)
			{
				ret.add(new ValidatorResult(
						ERROR_NONUNIQUE_NAME,
						components,
						ValidatorResult.ERROR));
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
				for (DESEvent event : component.getModel().getEventSet())
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

	/**
	 * Checks if a it is safe to compute the local supervisor for a
	 * <i>channel</i>, i.e., if the part of the model involving the
	 * <i>channel</i> and the immediate neighbors of the <i>channel</i> contains
	 * any consistency issues of type {@link ValidatorResult#ERROR}.
	 * 
	 * @param model
	 *            the model containing the <i>channel</i>
	 * @param channelId
	 *            the id of the <i>channel</i> {@link TemplateComponent}
	 * @return <code>true</code> if the part of the model involving the
	 *         <i>channel</i> and the immediate neighbors of the <i>channel</i>
	 *         does not contain any consistency issues of type
	 *         {@link ValidatorResult#ERROR}; <code>false</code> otherwise
	 */
	public static boolean canComputeSup(TemplateModel model, long channelId)
	{
		TemplateComponent channel = model.getComponent(channelId);
		if (channel.getType() != TemplateComponent.TYPE_CHANNEL)
		{
			return false;
		}
		Collection<TemplateComponent> cover = model.getCover(channel.getId());
		Collection<TemplateLink> links = model
				.getAdjacentLinks(channel.getId());
		Collection<ValidatorResult> results = validate(model);
		boolean invalid = false;
		for (ValidatorResult result : results)
		{
			if (result.type == ValidatorResult.WARNING)
			{
				continue;
			}
			for (TemplateComponent c : result.components)
			{
				if (channel == c
						|| (cover.contains(c) && result.links.isEmpty()))
				{
					invalid = true;
					break;
				}
			}
			if (invalid)
			{
				break;
			}
			for (TemplateLink l : result.links)
			{
				if (links.contains(l))
				{
					invalid = true;
					break;
				}
			}
			if (invalid)
			{
				break;
			}
		}
		return !invalid;
	}

	/**
	 * Retrieve all {@link TemplateLink}s which link a given event of a given
	 * {@link TemplateComponent}.
	 * 
	 * @param model
	 *            the {@link TemplateModel} containing the
	 *            {@link TemplateComponent}
	 * @param component
	 *            the {@link TemplateComponent} for which links have to be
	 *            retrieved
	 * @param event
	 *            the event name for which links have to be retrieved
	 * @return all {@link TemplateLink}s which link the given event of the given
	 *         {@link TemplateComponent}
	 */
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

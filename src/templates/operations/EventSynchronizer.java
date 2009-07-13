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

package templates.operations;

import ides.api.model.fsa.FSAModel;
import ides.api.model.fsa.FSAState;
import ides.api.plugin.model.DESEvent;
import ides.api.plugin.model.DESEventSet;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;

/**
 * Implements operations commonly employed by the other template design operations.
 * 
 * @author Lenko Grigorov
 */
public class EventSynchronizer
{
	public static FSAModel[] synchronizeAndCompose(TemplateModel model,
			Collection<TemplateComponent> modules,
			Collection<TemplateComponent> channels)
	{
		if (modules.isEmpty())
		{
			throw new IllegalArgumentException();
		}
		Set<FSAModel> modulesFSA = new HashSet<FSAModel>();
		Map<TemplateComponent, Map<String, String>> eventRenaming = new HashMap<TemplateComponent, Map<String, String>>();
		for (TemplateComponent module : modules)
		{
			FSAModel fsa = module.getModel().clone();
			Map<String, String> eventMap = new HashMap<String, String>();
			for (DESEvent event : fsa.getEventSet())
			{
				String newName = getUniqueEventName(module, event.getId());
				eventMap.put(event.getSymbol(), newName);
				event.setSymbol(newName);
			}
			modulesFSA.add(fsa);
			eventRenaming.put(module, eventMap);
		}
		Operation sync = OperationManager.instance().getOperation("sync");
		FSAModel moduleFSA = (FSAModel)sync.perform(modulesFSA.toArray())[0];
		DESEventSet systemEvents = moduleFSA.getEventSet().copy();
		Set<FSAModel> channelsFSA = new HashSet<FSAModel>();
		for (TemplateComponent channel : channels)
		{
			Map<String, String> channelEventMap = new HashMap<String, String>();
			for (TemplateLink link : model.getAdjacentLinks(channel.getId()))
			{
				TemplateComponent module;
				String moduleEvent;
				String channelEvent;
				if (link.getLeftComponent() == channel)
				{
					module = link.getRightComponent();
					moduleEvent = link.getRightEventName();
					channelEvent = link.getLeftEventName();
				}
				else
				{
					module = link.getLeftComponent();
					moduleEvent = link.getLeftEventName();
					channelEvent = link.getRightEventName();
				}
				if (!eventRenaming.containsKey(module))
				{
					throw new IllegalArgumentException();
				}
				channelEventMap.put(channelEvent, eventRenaming
						.get(module).get(moduleEvent));
			}
			FSAModel fsa = channel.getModel().clone();
			for (DESEvent event : fsa.getEventSet())
			{
				if (channelEventMap.containsKey(event.getSymbol()))
				{
					event.setSymbol(channelEventMap.get(event.getSymbol()));
				}
				else
				{
					event.setSymbol(getUniqueEventName(channel, event.getId()));
				}
			}
			DESEventSet toSelfloop = systemEvents.subtract(fsa.getEventSet());
			fsa = (FSAModel)OperationManager
					.instance().getOperation("selfloop").perform(new Object[] {
							fsa, toSelfloop })[0];
			channelsFSA.add(fsa);
		}
		FSAModel channelFSA;
		if (!channelsFSA.isEmpty())
		{
			Operation product = OperationManager
					.instance().getOperation("product");
			channelFSA = (FSAModel)product.perform(channelsFSA.toArray())[0];
		}
		else
		{
			channelFSA = ModelManager.instance().createModel(FSAModel.class);
			FSAState s = channelFSA.assembleState();
			s.setInitial(true);
			s.setMarked(true);
			channelFSA.add(s);
			channelFSA = (FSAModel)OperationManager
					.instance().getOperation("selfloop").perform(new Object[] {
							channelFSA, systemEvents })[0];
		}
		return new FSAModel[] { moduleFSA, channelFSA };
	}

	public static void label4Humans(TemplateModel model,
			Collection<FSAModel> fsas)
	{
		for (FSAModel fsa : fsas)
		{
			for (DESEvent event : fsa.getEventSet())
			{
				long[] pointer = getEventPointer(event.getSymbol());
				FSAModel original = model.getComponent(pointer[0]).getModel();
				String fsaName = original.getName();
				if (fsaName.startsWith(TemplateModel.FSA_NAME_PREFIX))
				{
					fsaName = fsaName.substring(TemplateModel.FSA_NAME_PREFIX
							.length());
				}
				event.setSymbol(fsaName + ":"
						+ original.getEvent(pointer[1]).getSymbol());
			}
		}
	}

	protected static String getUniqueEventName(TemplateComponent c, long eventId)
	{
		return "" + c.getId() + ":" + eventId;
	}

	protected static long[] getEventPointer(String name)
	{
		String[] ids = name.split(":");
		return new long[] { Long.parseLong(ids[0]), Long.parseLong(ids[1]) };
	}

}

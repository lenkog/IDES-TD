package templates.operations;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAEvent;
import ides.api.model.fsa.FSAEventSet;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;

public class ChannelSup implements Operation
{
	protected List<String> warnings = new LinkedList<String>();

	public String getDescription()
	{
		return Hub.string("TD_chsupDesc");
	}

	public String[] getDescriptionOfInputs()
	{
		return new String[] { Hub.string("TD_modelDesc"),
				Hub.string("TD_channelDesc") };
	}

	public String[] getDescriptionOfOutputs()
	{
		return new String[] { Hub.string("TD_modulesDesc"),
				Hub.string("TD_adjChannel"), Hub.string("TD_supDesc") };
	}

	public String getName()
	{
		return "channelsup";
	}

	public int getNumberOfInputs()
	{
		return 2;
	}

	public int getNumberOfOutputs()
	{
		return 3;
	}

	public Class<?>[] getTypeOfInputs()
	{
		return new Class<?>[] { TemplateModel.class, Long.class };
	}

	public Class<?>[] getTypeOfOutputs()
	{
		return new Class<?>[] { FSAModel.class, FSAModel.class, FSAModel.class };
	}

	public List<String> getWarnings()
	{
		return warnings;
	}

	public Object[] perform(Object[] arg0)
	{
		warnings.clear();
		if (arg0.length != 2)
		{
			throw new IllegalArgumentException();
		}
		if (!(arg0[0] instanceof TemplateModel) || !(arg0[1] instanceof Long))
		{
			throw new IllegalArgumentException();
		}
		TemplateModel model = (TemplateModel)arg0[0];
		TemplateComponent channel = model.getComponent((Long)arg0[1]);
		Set<FSAModel> modulesFSA = new HashSet<FSAModel>();
		Set<TemplateComponent> modules = new HashSet<TemplateComponent>();
		Map<TemplateComponent, Map<String, String>> eventRenaming = new HashMap<TemplateComponent, Map<String, String>>();
		for (TemplateLink link : model.getAdjacentLinks(channel.getId()))
		{
			modules.add(link.getLeftComponent() == channel ? link
					.getRightComponent() : link.getLeftComponent());
		}
		if (modules.isEmpty())
		{
			Hub.getNoticeManager().postWarningTemporary(Hub
					.string("TD_unconnectedChannel"),
					Hub.string("TD_unconnectedChannel1") + " \'"
							+ channel.getModel().getName() + "\' "
							+ Hub.string("TD_unconnectedChannel2"));
			warnings.add(Hub.string("TD_unconnectedChannel"));
			return new Object[] {
					ModelManager.instance().createModel(FSAModel.class),
					channel.getModel().clone(),
					ModelManager.instance().createModel(FSAModel.class) };
		}
		for (TemplateComponent module : modules)
		{
			FSAModel fsa = module.getModel().clone();
			Map<String, String> eventMap = new HashMap<String, String>();
			for (FSAEvent event : fsa.getEventSet())
			{
				String newName = getUniqueEventName(module, event.getId());
				eventMap.put(event.getSymbol(), newName);
				event.setSymbol(newName);
			}
			modulesFSA.add(fsa);
			eventRenaming.put(module, eventMap);
		}
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
			channelEventMap.put(channelEvent, eventRenaming
					.get(module).get(moduleEvent));
		}
		FSAModel channelFSA = channel.getModel().clone();
		for (FSAEvent event : channelFSA.getEventSet())
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
		Operation sync = OperationManager.instance().getOperation("sync");
		Iterator<FSAModel> i = modulesFSA.iterator();
		FSAModel moduleFSA = i.next();
		for (; i.hasNext();)
		{
			moduleFSA = (FSAModel)sync.perform(new Object[] { moduleFSA,
					i.next() })[0];
		}
		FSAEventSet toSelfloop = moduleFSA.getEventSet().copy();
		toSelfloop = toSelfloop.subtract(channelFSA.getEventSet());
		channelFSA = (FSAModel)OperationManager
				.instance().getOperation("selfloop").perform(new Object[] {
						channelFSA, toSelfloop })[0];
		FSAModel supFSA = (FSAModel)OperationManager
				.instance().getOperation("supcon").perform(new Object[] {
						moduleFSA, channelFSA })[0];
		for (FSAEvent event : moduleFSA.getEventSet())
		{
			long[] pointer = getEventPointer(event.getSymbol());
			FSAModel fsa = model.getComponent(pointer[0]).getModel();
			String fsaName = fsa.getName();
			if (fsaName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				fsaName = fsaName.substring(TemplateModel.FSA_NAME_PREFIX
						.length());
			}
			event.setSymbol(fsaName + ":"
					+ fsa.getEvent(pointer[1]).getSymbol());
		}
		for (FSAEvent event : channelFSA.getEventSet())
		{
			long[] pointer = getEventPointer(event.getSymbol());
			FSAModel fsa = model.getComponent(pointer[0]).getModel();
			String fsaName = fsa.getName();
			if (fsaName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				fsaName = fsaName.substring(TemplateModel.FSA_NAME_PREFIX
						.length());
			}
			event.setSymbol(fsaName + ":"
					+ fsa.getEvent(pointer[1]).getSymbol());
		}
		for (FSAEvent event : supFSA.getEventSet())
		{
			long[] pointer = getEventPointer(event.getSymbol());
			FSAModel fsa = model.getComponent(pointer[0]).getModel();
			String fsaName = fsa.getName();
			if (fsaName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				fsaName = fsaName.substring(TemplateModel.FSA_NAME_PREFIX
						.length());
			}
			event.setSymbol(fsaName + ":"
					+ fsa.getEvent(pointer[1]).getSymbol());
		}
		return new Object[] { moduleFSA, channelFSA, supFSA };
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

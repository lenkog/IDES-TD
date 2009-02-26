package templates.operations;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;
import ides.api.presentation.fsa.FSAStateLabeller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
		return "tdchannelsup";
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
		Set<TemplateComponent> modules = new HashSet<TemplateComponent>();
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
		FSAModel[] models = EventSynchronizer.synchronizeAndCompose(model,
				modules,
				Arrays.asList(new TemplateComponent[] { channel }));
		FSAModel moduleFSA = models[0];
		FSAModel channelFSA = models[1];
		FSAStateLabeller.labelCompositeStates(moduleFSA);
		FSAStateLabeller.labelCompositeStates(channelFSA);
		FSAModel supFSA = (FSAModel)OperationManager
				.instance().getOperation("supcon").perform(new Object[] {
						moduleFSA, channelFSA })[0];
		EventSynchronizer.label4Humans(model, Arrays.asList(new FSAModel[] {
				moduleFSA, channelFSA, supFSA }));
		FSAStateLabeller.labelCompositeStates(supFSA);
		return new Object[] { moduleFSA, channelFSA, supFSA };
	}
}

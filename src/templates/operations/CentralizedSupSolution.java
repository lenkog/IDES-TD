package templates.operations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import templates.model.TemplateComponent;
import templates.model.TemplateModel;
import templates.model.Validator;
import templates.model.Validator.ValidatorResult;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;

public class CentralizedSupSolution implements Operation
{

	private static final String[] description = {
			Hub.string("TD_sysDesc"),Hub.string("TD_specDesc"),Hub.string("TD_supDesc")
	};

	protected List<String> warnings = new LinkedList<String>();

	public String getDescription()
	{
		return Hub.string("TD_centralsupDesc");
	}

	public String[] getDescriptionOfInputs()
	{
		return new String[] { Hub.string("TD_modelDesc") };
	}

	public String[] getDescriptionOfOutputs()
	{
		return description;
	}

	public String getName()
	{
		return "tdcentralsup";
	}

	public int getNumberOfInputs()
	{
		return 1;
	}

	public int getNumberOfOutputs()
	{
		return 3;
	}

	public Class<?>[] getTypeOfInputs()
	{
		return new Class<?>[] { TemplateModel.class };
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
		if (arg0.length != 1)
		{
			throw new IllegalArgumentException();
		}
		if (!(arg0[0] instanceof TemplateModel))
		{
			throw new IllegalArgumentException();
		}
		TemplateModel model = (TemplateModel)arg0[0];
		for (ValidatorResult r : Validator.validate(model))
		{
			if (r.type == ValidatorResult.ERROR)
			{
				Hub.getNoticeManager().postErrorTemporary(Hub
						.string("TD_errorsInModel"),
						Hub.string("TD_errorsInModel1") + " \'"
								+ model.getName() + "\' "
								+ Hub.string("TD_errorsInModel2"));
				warnings.add(Hub.string("TD_errorsInModel"));
				return new Object[] {
						ModelManager.instance().createModel(FSAModel.class),
						ModelManager.instance().createModel(FSAModel.class),
						ModelManager.instance().createModel(FSAModel.class)
						};
			}
		}
		FSAModel[] models=EventSynchronizer.synchronizeAndCompose(model,model.getModules(),model.getChannels());
		FSAModel moduleFSA = models[0];
		FSAModel channelFSA = models[1];
		FSAModel supFSA = (FSAModel)OperationManager
				.instance().getOperation("supcon").perform(new Object[] {
						moduleFSA, channelFSA })[0];
		EventSynchronizer.label4Humans(model,Arrays.asList(new FSAModel[]{moduleFSA,channelFSA,supFSA}));
		return new Object[] { moduleFSA, channelFSA, supFSA };
	}
}

package templates.operations;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;

import java.util.LinkedList;
import java.util.List;

import templates.model.TemplateComponent;
import templates.model.TemplateModel;
import templates.model.Validator;
import templates.model.Validator.ValidatorResult;

public class ModularSupSolution implements Operation
{

	private static final String[] STD_DESC=new String[]{
			Hub.string("TD_checklmDesc")
	};
	private static final String[] ERROR_DESC=new String[]{
		Hub.string("TD_cantComputeSups")
};
	private static final String[] NO_OUTPUT_DESC=new String[]{
		Hub.string("TD_noOutputSups")
};
	
	private String[] description=STD_DESC;
	
	protected List<String> warnings = new LinkedList<String>();

	public String getDescription()
	{
		return Hub.string("TD_modsupDesc");
	}

	public String[] getDescriptionOfInputs()
	{
		return new String[]{Hub.string("TD_modelDesc")};
	}

	public String[] getDescriptionOfOutputs()
	{
		return description;
	}

	public String getName()
	{
		return "tdmodularsup";
	}

	public int getNumberOfInputs()
	{
		return 1;
	}

	public int getNumberOfOutputs()
	{
		return -1;
	}

	public Class<?>[] getTypeOfInputs()
	{
		return new Class<?>[]{TemplateModel.class};
	}

	public Class<?>[] getTypeOfOutputs()
	{
		return new Class<?>[]{Boolean.class};
	}

	public List<String> getWarnings()
	{
		return warnings;
	}

	public Object[] perform(Object[] arg0)
	{
		warnings.clear();
		description=STD_DESC;
		if (arg0.length != 1)
		{
			throw new IllegalArgumentException();
		}
		if (!(arg0[0] instanceof TemplateModel))
		{
			throw new IllegalArgumentException();
		}
		TemplateModel model = (TemplateModel)arg0[0];
		for(ValidatorResult r:Validator.validate(model))
		{
			if(r.type==ValidatorResult.ERROR)
			{
				Hub.getNoticeManager().postErrorTemporary(Hub
						.string("TD_errorsInModel"),
						Hub.string("TD_errorsInModel1") + " \'"
								+ model.getName() + "\' "
								+ Hub.string("TD_errorsInModel2"));
				warnings.add(Hub.string("TD_errorsInModel"));
				description=ERROR_DESC;
				return new Object[] {new Boolean(true)};
			}
		}
		Operation channelsup=OperationManager
		.instance().getOperation("tdchannelsup");
		List<FSAModel> models=new LinkedList<FSAModel>();
		List<FSAModel> sups=new LinkedList<FSAModel>();
		List<String> descriptions=new LinkedList<String>();
		for(TemplateComponent channel:model.getChannels())
		{
			Object[] result=channelsup.perform(new Object[]{model,channel.getId()});
			String channelName = channel.getModel().getName();
			if (channelName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				channelName = channelName.substring(TemplateModel.FSA_NAME_PREFIX
						.length());
			}
			((FSAModel)result[0]).setName("M_" + channelName);
			((FSAModel)result[1]).setName("C_" + channelName);
			((FSAModel)result[2]).setName("S_" + channelName);
			models.add((FSAModel)result[0]);
			models.add((FSAModel)result[1]);
			models.add((FSAModel)result[2]);
			sups.add((FSAModel)result[2]);
			descriptions.add(Hub.string("TD_modulesDesc")+" \""+channelName+"\"");
			descriptions.add(Hub.string("TD_adjChannel")+" \""+channelName+"\"");
			descriptions.add(Hub.string("TD_supDesc")+" \""+channelName+"\"");
			warnings.addAll(channelsup.getWarnings());
		}
		if(sups.isEmpty())
		{
			warnings.add(NO_OUTPUT_DESC[0]);
			description=NO_OUTPUT_DESC;
			return new Object[]{true};
		}
		Operation lm=OperationManager.instance().getOperation("localmodular");
		Boolean isLM=(Boolean)lm.perform(sups.toArray())[0];
		description=new String[models.size()+1];
		System.arraycopy(descriptions.toArray(),0,description,0,descriptions.size());
		if(isLM)
		{
			description[description.length-1]=Hub.string("TD_checklmPos");
		}
		else
		{
			description[description.length-1]=Hub.string("TD_checklmNeg");
		}
		Object[] ret=new Object[models.size()+1];
		System.arraycopy(models.toArray(),0,ret,0,models.size());
		ret[ret.length-1]=isLM;
		return ret;
	}

}

package templates.operations;

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

public class SupSolution implements Operation
{

	private static final String[] STD_DESC=new String[]{
			Hub.string("TD_checklmDesc")
//			Hub.string("TD_modulesDesc"),
//			Hub.string("TD_supDesc")
	};
	
	private String[] description=STD_DESC;
	
	protected List<String> warnings = new LinkedList<String>();

	public String getDescription()
	{
		return Hub.string("TD_supsolDesc");
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
		return "supcontd";
	}

	public int getNumberOfInputs()
	{
		return 1;
	}

	public int getNumberOfOutputs()
	{
//		return -1;
		return 1;
	}

	public Class<?>[] getTypeOfInputs()
	{
		return new Class<?>[]{TemplateModel.class};
	}

	public Class<?>[] getTypeOfOutputs()
	{
//		return new Class<?>[]{Boolean.class,FSAModel.class,FSAModel.class};
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
				return new Object[] {
						new Boolean(true),
						ModelManager.instance().createModel(FSAModel.class),
						ModelManager.instance().createModel(FSAModel.class) };
			}
		}
		Operation channelsup=OperationManager.instance().getOperation("channelsup");
		List<FSAModel> models=new LinkedList<FSAModel>();
		for(TemplateComponent channel:model.getChannels())
		{
			Object[] result=channelsup.perform(new Object[]{model,channel.getId()});
			models.add((FSAModel)result[0]);
			models.add((FSAModel)result[2]);
			warnings.addAll(channelsup.getWarnings());
		}
		List<FSAModel> sups=new LinkedList<FSAModel>();
		for(Iterator<FSAModel> i=models.iterator();i.hasNext();)
		{
			i.next();
			sups.add(i.next());
		}
		Operation lm=OperationManager.instance().getOperation("localmodular");
		Boolean isLM=(Boolean)lm.perform(sups.toArray())[0];
//		description=new String[models.size()+1];
//		Object[] ret=new Object[models.size()+1];
		if(isLM)
		{
			description[0]=Hub.string("TD_checklmPos");
		}
		else
		{
			description[0]=Hub.string("TD_checklmNeg");
		}
//		ret[0]=isLM;
		for(FSAModel m:models)
//		Iterator<FSAModel> modelsIter=models.iterator();
//		for(int i=1;i<description.length;i=i+2)
		{
//			description[i]=Hub.string("TD_modulesDesc");
//			description[i+1]=Hub.string("TD_supDesc");
//			ret[i]=modelsIter.next();
//			ret[i+1]=modelsIter.next();
			Hub.getWorkspace().addModel(m);
		}
//		return ret;
		return new Object[]{isLM};
	}

}

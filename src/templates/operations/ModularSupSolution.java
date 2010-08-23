/*
 * Copyright (c) 2010, Lenko Grigorov
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

/**
 * Computes the modular supervisory solution for a {@link TemplateModel}.
 * <p>
 * Inputs:
 * <ul>
 * <li>template design [{@link TemplateModel}]
 * </ul>
 * <p>
 * Outputs:<br>
 * For each channel in the template design:
 * <ul>
 * <li>composition of the modules linked to the channel [{@link FSAModel}]
 * <li>the synchronized channel [{@link FSAModel}]
 * <li>supervisor for the channel [{@link FSAModel}]
 * </ul>
 * and
 * <ul>
 * <li>are all the supervisors locally modular [{@link Boolean}]
 * </ul>
 * 
 * @author Lenko Grigorov
 */
public class ModularSupSolution implements Operation
{
	/**
	 * Standard description of the output.
	 */
	private static final String[] STD_DESC = new String[] { Hub
			.string("TD_checklmDesc") };

	/**
	 * Description of the output when there was an error computing the
	 * supervisory solution (e.g., due to model inconsistencies).
	 */
	private static final String[] ERROR_DESC = new String[] { Hub
			.string("TD_cantComputeSups") };

	/**
	 * Description of the output when no supervisors were produced (e.g., when
	 * there are no channels in a template design).
	 */
	private static final String[] NO_OUTPUT_DESC = new String[] { Hub
			.string("TD_noOutputSups") };

	/**
	 * The description of the outputs.
	 */
	private String[] description = STD_DESC;

	/**
	 * Collection of warnings accumulated while performing the operation.
	 */
	protected List<String> warnings = new LinkedList<String>();

	public String getDescription()
	{
		return Hub.string("TD_modsupDesc");
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
		return new Class<?>[] { TemplateModel.class };
	}

	public Class<?>[] getTypeOfOutputs()
	{
		return new Class<?>[0];
	}

	public List<String> getWarnings()
	{
		return warnings;
	}

	public Object[] perform(Object[] arg0)
	{
		warnings.clear();
		description = STD_DESC;
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
				description = ERROR_DESC;
				return new Object[] { true };
			}
		}
		Operation channelsup = OperationManager
				.instance().getOperation("tdchannelsup");
		List<FSAModel> models = new LinkedList<FSAModel>();
		List<FSAModel> sups = new LinkedList<FSAModel>();
		List<String> descriptions = new LinkedList<String>();
		for (TemplateComponent channel : model.getChannels())
		{
			Object[] result = channelsup.perform(new Object[] { model,
					channel.getId() });
			String channelName = channel.getModel().getName();
			if (channelName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				channelName = channelName
						.substring(TemplateModel.FSA_NAME_PREFIX.length());
			}
			((FSAModel)result[0]).setName("M_" + channelName);
			((FSAModel)result[1]).setName("C_" + channelName);
			((FSAModel)result[2]).setName("S_" + channelName);
			models.add((FSAModel)result[0]);
			models.add((FSAModel)result[1]);
			models.add((FSAModel)result[2]);
			sups.add((FSAModel)result[2]);
			descriptions.add(Hub.string("TD_modulesDesc") + " \"" + channelName
					+ "\"");
			descriptions.add(Hub.string("TD_adjChannel") + " \"" + channelName
					+ "\"");
			descriptions.add(Hub.string("TD_supDesc") + " \"" + channelName
					+ "\"");
			warnings.addAll(channelsup.getWarnings());
		}
		if (sups.isEmpty())
		{
			warnings.add(NO_OUTPUT_DESC[0]);
			description = NO_OUTPUT_DESC;
			return new Object[] { true };
		}
		Operation lm = OperationManager.instance().getOperation("localmodular");
		Boolean isLM = (Boolean)lm.perform(sups.toArray())[0];
		warnings.addAll(lm.getWarnings());
		description = new String[models.size() + 1];
		System.arraycopy(descriptions.toArray(),
				0,
				description,
				0,
				descriptions.size());
		if (isLM)
		{
			description[description.length - 1] = Hub.string("TD_checklmPos");
		}
		else
		{
			description[description.length - 1] = Hub.string("TD_checklmNeg");
		}
		Object[] ret = new Object[models.size() + 1];
		System.arraycopy(models.toArray(), 0, ret, 0, models.size());
		ret[ret.length - 1] = isLM;
		return ret;
	}

}

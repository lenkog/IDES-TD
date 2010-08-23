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

/**
 * Computes the supervisor for a channel with respect to the modules connected
 * to it.
 * <p>
 * Inputs:
 * <ul>
 * <li>template design [{@link TemplateModel}]
 * <li>channel ID [{@link Long}]
 * </ul>
 * <p>
 * Outputs:
 * <ul>
 * <li>composition of the modules linked to the channel [{@link FSAModel}]
 * <li>the synchronized channel [{@link FSAModel}]
 * <li>supervisor [{@link FSAModel}]
 * </ul>
 * 
 * @author Lenko Grigorov
 */
public class ChannelSup implements Operation
{
	/**
	 * Collection of warnings accumulated while performing the operation.
	 */
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
		warnings.addAll(EventSynchronizer.getWarnings());
		FSAModel moduleFSA = models[0];
		FSAModel channelFSA = models[1];
		FSAStateLabeller.labelCompositeStates(moduleFSA);
		FSAStateLabeller.labelCompositeStates(channelFSA);
		EventSynchronizer.copyControllability(moduleFSA, channelFSA);
		Operation supcon = OperationManager.instance().getOperation("supcon");
		FSAModel supFSA = (FSAModel)supcon.perform(new Object[] { moduleFSA,
				channelFSA })[0];
		warnings.addAll(supcon.getWarnings());
		EventSynchronizer.label4Humans(model, Arrays.asList(new FSAModel[] {
				moduleFSA, channelFSA, supFSA }));
		FSAStateLabeller.labelCompositeStates(supFSA);
		return new Object[] { moduleFSA, channelFSA, supFSA };
	}
}

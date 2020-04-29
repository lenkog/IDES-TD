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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;
import ides.api.presentation.fsa.FSAStateLabeller;
import templates.model.TemplateModel;
import templates.model.Validator;
import templates.model.Validator.ValidatorResult;

/**
 * Computes the centralized (monolithic) supervisory solution for a
 * {@link TemplateModel}.
 * <p>
 * Inputs:
 * <ul>
 * <li>template design [{@link TemplateModel}]
 * </ul>
 * <p>
 * Outputs:
 * <ul>
 * <li>composition of all modules in the template design [{@link FSAModel}]
 * <li>composition of all synchronized channels in the template design [
 * {@link FSAModel}]
 * <li>supervisor [{@link FSAModel}]
 * </ul>
 * 
 * @author Lenko Grigorov
 */
public class CentralizedSupSolution implements Operation {
    /**
     * Collection of warnings accumulated while performing the operation.
     */
    protected List<String> warnings = new LinkedList<String>();

    public String getDescription() {
        return Hub.string("TD_centralsupDesc");
    }

    public String[] getDescriptionOfInputs() {
        return new String[] { Hub.string("TD_modelDesc") };
    }

    public String[] getDescriptionOfOutputs() {
        return new String[] { Hub.string("TD_sysDesc"), Hub.string("TD_specDesc"), Hub.string("TD_supDesc") };
    }

    public String getName() {
        return "tdcentralsup";
    }

    public int getNumberOfInputs() {
        return 1;
    }

    public int getNumberOfOutputs() {
        return 3;
    }

    public Class<?>[] getTypeOfInputs() {
        return new Class<?>[] { TemplateModel.class };
    }

    public Class<?>[] getTypeOfOutputs() {
        return new Class<?>[] { FSAModel.class, FSAModel.class, FSAModel.class };
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Object[] perform(Object[] arg0) {
        warnings.clear();
        if (arg0.length != 1) {
            throw new IllegalArgumentException();
        }
        if (!(arg0[0] instanceof TemplateModel)) {
            throw new IllegalArgumentException();
        }
        TemplateModel model = (TemplateModel) arg0[0];
        for (ValidatorResult r : Validator.validate(model)) {
            if (r.type == ValidatorResult.ERROR) {
                Hub.getNoticeManager().postErrorTemporary(Hub.string("TD_errorsInModel"),
                        Hub.string("TD_errorsInModel1") + " \'" + model.getName() + "\' "
                                + Hub.string("TD_errorsInModel2"));
                warnings.add(Hub.string("TD_errorsInModel"));
                return new Object[] { ModelManager.instance().createModel(FSAModel.class),
                        ModelManager.instance().createModel(FSAModel.class),
                        ModelManager.instance().createModel(FSAModel.class) };
            }
        }
        FSAModel[] models = EventSynchronizer.synchronizeAndCompose(model, model.getModules(), model.getChannels());
        warnings.addAll(EventSynchronizer.getWarnings());
        FSAModel moduleFSA = models[0];
        FSAModel channelFSA = models[1];
        FSAStateLabeller.labelCompositeStates(moduleFSA);
        FSAStateLabeller.labelCompositeStates(channelFSA);
        EventSynchronizer.copyControllability(moduleFSA, channelFSA);
        Operation supcon = OperationManager.instance().getOperation("supcon");
        FSAModel supFSA = (FSAModel) supcon.perform(new Object[] { moduleFSA, channelFSA })[0];
        warnings.addAll(supcon.getWarnings());
        EventSynchronizer.label4Humans(model, Arrays.asList(new FSAModel[] { moduleFSA, channelFSA, supFSA }));
        return new Object[] { moduleFSA, channelFSA, supFSA };
    }
}

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
import ides.api.plugin.model.DESElement;

/**
 * Describes a component in a template design, namely a <i>module</i> or a
 * <i>channel</i>.
 * 
 * @author Lenko Grigorov
 */
public interface TemplateComponent extends DESElement {
    /**
     * Specifies the type of components which are <i>modules</i>.
     */
    public final int TYPE_MODULE = 1;

    /**
     * Specifies the type of components which are <i>channels</i>.
     */
    public final int TYPE_CHANNEL = 2;

    /**
     * Retrieve the type of the component.
     * 
     * @return {@link #TYPE_MODULE} or {@link #TYPE_CHANNEL}
     */
    public int getType();

    /**
     * Sets the type of the component.
     * 
     * @param type the type of the component ({@link #TYPE_MODULE} or
     *             {@link #TYPE_CHANNEL})
     */
    public void setType(int type);

    /**
     * Retrieve the {@link FSAModel} associated with the component.
     * 
     * @return the {@link FSAModel} associated with the component; <code>null</code>
     *         if no {@link FSAModel} is associated
     */
    public FSAModel getModel();

    /**
     * Assign the {@link FSAModel} to be associated with the component.
     * 
     * @param fsa the {@link FSAModel} to be associated with the component
     */
    public void setModel(FSAModel fsa);

    /**
     * Checks if there is an {@link FSAModel} associated with the component.
     * 
     * @return <code>true</code> if the associated {@link FSAModel} is not
     *         <code>null</code>; <code>false</code> otherwise
     */
    public boolean hasModel();
}

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

import java.util.Collection;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ParentModel;

/**
 * Describes a template design (which includes <i>modules</i>, <i>channels</i>
 * and <i>links</i>).
 * 
 * @author Lenko Grigorov
 */
public interface TemplateModel extends ParentModel, TemplateModelPublisher {
    /**
     * Prefix for the names of the FSA models contained in
     * {@link TemplateComponent}s.
     */
    public static String FSA_NAME_PREFIX = "TD:";

    /**
     * Retrieve all the {@link TemplateComponent}s in the model.
     * 
     * @return a collection of all the {@link TemplateComponent}s in the model
     */
    public Collection<TemplateComponent> getComponents();

    /**
     * Retrieve all the {@link TemplateComponent}s in the model which are
     * <i>modules</i>.
     * 
     * @return a collection of all the {@link TemplateComponent}s in the model which
     *         are <i>modules</i>
     */
    public Collection<TemplateComponent> getModules();

    /**
     * Retrieve all the {@link TemplateComponent}s in the model which are
     * <i>channels</i>.
     * 
     * @return a collection of all the {@link TemplateComponent}s in the model which
     *         are <i>channels</i>
     */
    public Collection<TemplateComponent> getChannels();

    /**
     * Return the number of {@link TemplateComponent}s in the model.
     * 
     * @return the number of {@link TemplateComponent}s in the model
     */
    public int getComponentCount();

    /**
     * Retrieve all the {@link TemplateLink}s in the model.
     * 
     * @return a collection of all the {@link TemplateLink}s in the model
     */
    public Collection<TemplateLink> getLinks();

    /**
     * Return the {@link TemplateComponent} with the given id.
     * 
     * @param id the id of the {@link TemplateComponent}
     * @return the {@link TemplateComponent} with the given id; <code>null</code> if
     *         there is no {@link TemplateComponent} with such an id
     */
    public TemplateComponent getComponent(long id);

    /**
     * Return the {@link TemplateLink} with the given id.
     * 
     * @param id the id of the {@link TemplateLink}
     * @return the {@link TemplateLink} with the given id; <code>null</code> if
     *         there is no {@link TemplateLink} with such an id
     */
    public TemplateLink getLink(long id);

    /**
     * Create a new {@link TemplateComponent} which can be then added to the model
     * without breaking the model consistency (e.g., it has a unique id).
     * 
     * @return a new {@link TemplateComponent} which can be then added to the model
     *         without breaking the model consistency
     */
    public TemplateComponent assembleComponent();

    /**
     * Create a new {@link TemplateComponent} and add it to the model.
     * 
     * @return the new {@link TemplateComponent} which was added to the model
     */
    public TemplateComponent createComponent();

    /**
     * Add a {@link TemplateComponent} to the model.
     * 
     * @param component the {@link TemplateComponent} to be added
     * @throws InconsistentModificationException if the model already contain a
     *                                           {@link TemplateComponent} with the
     *                                           same id
     */
    public void addComponent(TemplateComponent component);

    /**
     * Create a new {@link TemplateLink} which can be then added to the model
     * without breaking the model consistency (e.g., it has a unique id).
     * 
     * @param leftId  the id of the first {@link TemplateComponent} to be linked
     * @param rightId the id of the second {@link TemplateComponent} to be linked
     * @return a new {@link TemplateLink} which can be then added to the model
     *         without breaking the model consistency
     * @throws InconsistentModificationException if the model does not contain
     *                                           {@link TemplateComponent}s with the
     *                                           given ids
     */
    public TemplateLink assembleLink(long leftId, long rightId);

    /**
     * Create a new {@link TemplateLink} and add it to the model.
     * 
     * @param leftId  the id of the first {@link TemplateComponent} to be linked
     * @param rightId the id of the second {@link TemplateComponent} to be linked
     * @return the new {@link TemplateLink} which was added to the model
     * @throws InconsistentModificationException if the model does not contain
     *                                           {@link TemplateComponent}s with the
     *                                           given ids
     */
    public TemplateLink createLink(long leftId, long rightId);

    /**
     * Add a {@link TemplateLink} to the model.
     * 
     * @param link the {@link TemplateLink} to be added
     * @throws InconsistentModificationException if the model already contain a
     *                                           {@link TemplateLink} with the same
     *                                           id or if the model does not contain
     *                                           the {@link TemplateComponent}s
     *                                           linked by the {@link TemplateLink}
     */
    public void addLink(TemplateLink link);

    /**
     * Remove the {@link TemplateComponent} with the given id.
     * 
     * @param id the id of the {@link TemplateComponent} to be removed
     */
    public void removeComponent(long id);

    /**
     * Remove the {@link TemplateLink} with the given id.
     * 
     * @param id the id of the {@link TemplateLink} to be removed
     */
    public void removeLink(long id);

    /**
     * Retrieve all the {@link TemplateLink}s connected to the
     * {@link TemplateComponent} with the given id.
     * 
     * @param componentId the id of the {@link TemplateComponent}
     * @return all the {@link TemplateLink}s connected to the
     *         {@link TemplateComponent} with the given id
     */
    public Collection<TemplateLink> getAdjacentLinks(long componentId);

    /**
     * Retrieve all the <i>modules</i> linked to the <i>channel</i> with the given
     * id.
     * 
     * @param channelId the id of the <i>channel</i> {@link TemplateComponent}
     * @return all the <i>modules</i> linked to the <i>channel</i> with the given
     *         id; an empty collection if the {@link TemplateComponent} with the
     *         given id is not a <i>channel</i>
     */
    public Collection<TemplateComponent> getCover(long channelId);

    /**
     * Check if there is a {@link TemplateLink} between the
     * {@link TemplateComponent}s with the given ids.
     * 
     * @param leftId  the id of the first {@link TemplateComponent}
     * @param rightId the id of the second {@link TemplateComponent}
     * @return <code>true</code> if there is a {@link TemplateLink} between the
     *         {@link TemplateComponent}s with the given ids; <code>false</code>
     *         otherwise
     */
    public boolean existsLink(long leftId, long rightId);

    /**
     * Retrieve the {@link TemplateLink}s between the {@link TemplateComponent}s
     * with the given ids.
     * 
     * @param leftId  the id of the first {@link TemplateComponent}
     * @param rightId the id of the second {@link TemplateComponent}
     * @return all the {@link TemplateLink}s between the {@link TemplateComponent}s
     *         with the given ids
     */
    public Collection<TemplateLink> getLinks(long leftId, long rightId);

    /**
     * Assign an {@link FSAModel} to the {@link TemplateComponent} with the given
     * id.
     * 
     * @param componentId the id of the {@link TemplateComponent}
     * @param fsa         the {@link FSAModel} to be assigned
     * @throws InconsistentModificationException if the model already contains a
     *                                           {@link TemplateComponent} with the
     *                                           given {@link FSAModel}
     */
    public void assignFSA(long componentId, FSAModel fsa);

    /**
     * Remove the {@link FSAModel} assigned to the {@link TemplateComponent} with
     * the given id.
     * 
     * @param componentId the id of the {@link TemplateComponent} whose
     *                    {@link FSAModel} is to be removed
     */
    public void removeFSA(long componentId);

    /**
     * Set the type (<i>module</i> or <i>channel</i>) of the
     * {@link TemplateComponent} with the given id.
     * 
     * @param componentId the id of the {@link TemplateComponent} whose type is to
     *                    be changed
     * @param type        the type to be set ({@link TemplateComponent#TYPE_MODULE}
     *                    or {@link TemplateComponent#TYPE_CHANNEL})
     */
    public void setComponentType(long componentId, int type);
}

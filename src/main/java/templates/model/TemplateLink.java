/*
 * Copyright (c) 2009-2020, Lenko Grigorov
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
import ides.api.plugin.model.DESEvent;

/**
 * Describes a <i>link</i> between two components in a template design. A link
 * is symmetric so it makes no difference which {@link TemplateComponent} is
 * added to the "left" or to the "right". "Left" and "right" are used only to
 * enable addressing the two {@link TemplateComponent}s separately.
 * 
 * @author Lenko Grigorov
 */
public interface TemplateLink extends DESElement {
    /**
     * Retrieve the first {@link TemplateComponent} linked by the link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the first {@link TemplateComponent} linked by the link
     */
    public TemplateComponent getLeftComponent();

    /**
     * Retrieve the second {@link TemplateComponent} linked by the link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the second {@link TemplateComponent} linked by the link
     */
    public TemplateComponent getRightComponent();

    /**
     * Retrieve the two {@link TemplateComponent}s linked by the link.
     * 
     * @return the two {@link TemplateComponent}s linked by the link
     */
    public TemplateComponent[] getComponents();

    /**
     * Retrieve the <i>module</i> linked by the link, if any.
     * 
     * @return the <i>module</i> linked by the link (if both
     *         {@link TemplateComponent}s are <i>modules</i>, returns arbitrarily
     *         one of them); <code>null</code> if no <i>module</i> is linked
     */
    public TemplateComponent getModule();

    /**
     * Retrieve the <i>channel</i> linked by the link, if any.
     * 
     * @return the <i>channel</i> linked by the link (if both
     *         {@link TemplateComponent}s are <i>channels</i>, returns arbitrarily
     *         one of them); <code>null</code> if no <i>channel</i> is linked
     */
    public TemplateComponent getChannel();

    /**
     * Retrieve the name of the linked event from the first
     * {@link TemplateComponent}.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the name of the linked event from the first {@link TemplateComponent}
     */
    public String getLeftEventName();

    /**
     * Checks if the {@link FSAModel} of the first {@link TemplateComponent}
     * contains the linked event.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return <code>true</code> if the first linked {@link TemplateComponent} has
     *         an assigned {@link FSAModel} and if this {@link FSAModel} contains
     *         the linked event; <code>false</code> otherwise
     */
    public boolean existsLeftEvent();

    /**
     * Retrieves the linked {@link DESEvent} from the first
     * {@link TemplateComponent}.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the linked {@link DESEvent} from the {@link FSAModel} of the first
     *         {@link TemplateComponent}, if the {@link TemplateComponent} has an
     *         assigned {@link FSAModel} and if this {@link FSAModel} contains the
     *         linked event; <code>null</code> otherwise
     */
    public DESEvent getLeftEvent();

    /**
     * Retrieve the name of the linked event from the second
     * {@link TemplateComponent}.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the name of the linked event from the second
     *         {@link TemplateComponent}
     */
    public String getRightEventName();

    /**
     * Checks if the {@link FSAModel} of the second {@link TemplateComponent}
     * contains the linked event.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return <code>true</code> if the second linked {@link TemplateComponent} has
     *         an assigned {@link FSAModel} and if this {@link FSAModel} contains
     *         the linked event; <code>false</code> otherwise
     */
    public boolean existsRightEvent();

    /**
     * Retrieves the linked {@link DESEvent} from the second
     * {@link TemplateComponent}.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @return the linked {@link DESEvent} from the {@link FSAModel} of the second
     *         {@link TemplateComponent}, if the {@link TemplateComponent} has an
     *         assigned {@link FSAModel} and if this {@link FSAModel} contains the
     *         linked event; <code>null</code> otherwise
     */
    public DESEvent getRightEvent();

    /**
     * Set the name of the linked event from the first {@link TemplateComponent} .
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @param name the name of the linked event from the first
     *             {@link TemplateComponent}
     */
    public void setLeftEventName(String name);

    /**
     * Set the name of the linked event from the second {@link TemplateComponent}.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @param name the name of the linked event from the second
     *             {@link TemplateComponent}
     */
    public void setRightEventName(String name);
}

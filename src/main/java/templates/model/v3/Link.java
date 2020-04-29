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

package templates.model.v3;

import java.util.Hashtable;

import ides.api.core.Hub;
import ides.api.plugin.model.DESEvent;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

/**
 * Implementation of {@link TemplateLink}.
 * 
 * @author Lenko Grigorov
 */
public class Link implements TemplateLink {
    /**
     * A map with the annotations of this element.
     */
    protected Hashtable<String, Object> annotations = new Hashtable<String, Object>();

    public Object getAnnotation(String key) {
        return annotations.get(key);
    }

    public boolean hasAnnotation(String key) {
        return annotations.containsKey(key);
    }

    public void removeAnnotation(String key) {
        annotations.remove(key);
    }

    public void setAnnotation(String key, Object annotation) {
        if (annotation != null) {
            annotations.put(key, annotation);
        }
    }

    /**
     * The id of the link.
     */
    protected long id;

    /**
     * The first {@link TemplateComponent} linked by the link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     */
    protected TemplateComponent left;

    /**
     * The second {@link TemplateComponent} linked by the link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     */
    protected TemplateComponent right;

    /**
     * The event (of the first {@link TemplateComponent}) which is linked by the
     * link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two events separately.
     */
    protected String leftEvent = "";

    /**
     * The event (of the second {@link TemplateComponent}) which is linked by the
     * link.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two events separately.
     */
    protected String rightEvent = "";

    /**
     * Construct a new link with the given parameters.
     * <p>
     * A link is symmetric. "Left" and "right" are used only to enable addressing
     * the two {@link TemplateComponent}s separately.
     * 
     * @param id    the id of the link
     * @param left  the first {@link TemplateComponent} linked by the link
     * @param right the second {@link TemplateComponent} linked by the link
     */
    public Link(long id, TemplateComponent left, TemplateComponent right) {
        if (right == null || left == null) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
        }
        this.id = id;
        this.left = left;
        this.right = right;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TemplateComponent getChannel() {
        if (left.getType() == TemplateComponent.TYPE_CHANNEL) {
            return left;
        } else if (right.getType() == TemplateComponent.TYPE_CHANNEL) {
            return right;
        }
        return null;
    }

    public DESEvent getRightEvent() {
        if (!right.hasModel()) {
            return null;
        }
        for (DESEvent event : right.getModel().getEventSet()) {
            if (event.getSymbol().equals(rightEvent)) {
                return event;
            }
        }
        return null;
    }

    public String getRightEventName() {
        return rightEvent;
    }

    public TemplateComponent getModule() {
        if (left.getType() == TemplateComponent.TYPE_MODULE) {
            return left;
        } else if (right.getType() == TemplateComponent.TYPE_MODULE) {
            return right;
        }
        return null;
    }

    public DESEvent getLeftEvent() {
        if (!left.hasModel()) {
            return null;
        }
        for (DESEvent event : left.getModel().getEventSet()) {
            if (event.getSymbol().equals(leftEvent)) {
                return event;
            }
        }
        return null;
    }

    public String getLeftEventName() {
        return leftEvent;
    }

    public boolean existsRightEvent() {
        return getRightEvent() != null;
    }

    public boolean existsLeftEvent() {
        return getLeftEvent() != null;
    }

    public void setRightEventName(String name) {
        if (name == null) {
            name = "";
        }
        rightEvent = name;
    }

    public void setLeftEventName(String name) {
        if (name == null) {
            name = "";
        }
        leftEvent = name;
    }

    public TemplateComponent[] getComponents() {
        return new TemplateComponent[] { left, right };
    }

    public TemplateComponent getLeftComponent() {
        return left;
    }

    public TemplateComponent getRightComponent() {
        return right;
    }

}

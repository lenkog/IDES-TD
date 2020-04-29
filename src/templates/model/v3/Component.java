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

package templates.model.v3;

import java.util.Hashtable;

import ides.api.model.fsa.FSAModel;
import templates.model.TemplateComponent;

/**
 * Implementation of {@link TemplateComponent}.
 * 
 * @author Lenko Grigorov
 */
public class Component implements TemplateComponent {
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
     * The id of the component.
     */
    protected long id;

    /**
     * The type of the component. By default is <i>module</i>.
     */
    protected int type = TemplateComponent.TYPE_MODULE;

    /**
     * The {@link FSAModel} associated with the component.
     */
    protected FSAModel fsa = null;

    /**
     * Construct a new component with the given parameters.
     * 
     * @param id the id of the component
     */
    public Component(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public FSAModel getModel() {
        return fsa;
    }

    public boolean hasModel() {
        return fsa != null;
    }

    public void setModel(FSAModel fsa) {
        this.fsa = fsa;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if (type == TYPE_CHANNEL) {
            this.type = type;
        } else {
            this.type = TYPE_MODULE;
        }
    }
}

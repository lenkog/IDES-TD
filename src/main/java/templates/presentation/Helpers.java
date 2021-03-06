/*
 * Copyright (c) 2010-2020, Lenko Grigorov
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

package templates.presentation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ides.api.model.supeventset.SupervisoryEvent;
import templates.diagram.Connector;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

/**
 * Helper methods for some UI elements.
 * 
 * @author Lenko Grigorov
 */
public class Helpers {
    /**
     * Compute the set of event names which appear in both
     * {@link TemplateComponent}s connected by the given connector. If a template
     * component has an underlying model, the names of events from the model
     * alphabet are compared, otherwise the events specified by the
     * {@link TemplateLink}s in the connector are compared.
     * 
     * @param c the connector
     * @return the set of event names which appear in both
     *         {@link TemplateComponent}s connected by the given connector
     */
    public static Set<String> matchEvents(Connector c) {
        Set<String> leftEvents = new HashSet<String>();
        Set<String> rightEvents = new HashSet<String>();
        if (c.getLeftEntity().getComponent().hasModel()) {
            for (Iterator<SupervisoryEvent> i = c.getLeftEntity().getComponent().getModel().getEventIterator(); i
                    .hasNext();) {
                leftEvents.add(i.next().getSymbol());
            }
        } else {
            for (TemplateLink link : c.getLinks()) {
                if (link.getLeftComponent() == c.getLeftEntity().getComponent()) {
                    leftEvents.add(link.getLeftEventName());
                } else {
                    leftEvents.add(link.getRightEventName());
                }
            }
        }
        if (c.getRightEntity().getComponent().hasModel()) {
            for (Iterator<SupervisoryEvent> i = c.getRightEntity().getComponent().getModel().getEventIterator(); i
                    .hasNext();) {
                rightEvents.add(i.next().getSymbol());
            }
        } else {
            for (TemplateLink link : c.getLinks()) {
                if (link.getRightComponent() == c.getLeftEntity().getComponent()) {
                    rightEvents.add(link.getLeftEventName());
                } else {
                    rightEvents.add(link.getRightEventName());
                }
            }
        }
        return matchEvents(leftEvents, rightEvents);
    }

    /**
     * Compute the set of event names which appear in both event name sets given.
     * 
     * @param leftEvents  the first event name set
     * @param rightEvents the second event name set
     * @return the set of event names which appear in both event name sets given
     */
    public static Set<String> matchEvents(Set<String> leftEvents, Set<String> rightEvents) {
        Set<String> ret = new HashSet<String>(leftEvents);
        ret.retainAll(rightEvents);
        return ret;
    }
}

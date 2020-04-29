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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ides.api.model.fsa.FSAModel;
import ides.api.model.fsa.FSAState;
import ides.api.model.supeventset.SupervisoryEvent;
import ides.api.plugin.model.DESEvent;
import ides.api.plugin.model.DESEventSet;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.Operation;
import ides.api.plugin.operation.OperationManager;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;

/**
 * Implements operations commonly employed by the other template design
 * operations.
 * 
 * @author Lenko Grigorov
 */
public class EventSynchronizer {
    /**
     * Collection of warnings accumulated while performing
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}. The
     * collection is reset every time
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)} is
     * called.
     */
    protected static List<String> warnings = new LinkedList<String>();

    /**
     * Retrieve the warnings accumulated during the latest call to
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}.
     * 
     * @return the warnings accumulated during the latest call to
     *         {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}
     */
    public static List<String> getWarnings() {
        return warnings;
    }

    /**
     * Composes a list of modules and synchronizes and composes a list of channels.
     * Modules are composed using the "sync" operation. The events of the channels
     * are synchronized with the corresponding events from the linked modules; the
     * irrelevant events of each channel are self-looped; and the channels are
     * composed using the "product" operation. If the list of channels is empty,
     * instead of the channel composition the output contains a single-state
     * automaton with all events in a self-loop.
     * <p>
     * Note: as different modules may have the same event names (and as all
     * alphabets are assumed to be disjunct), the event names in the outputs are
     * replaced with unique strings. To convert these unique string to a
     * human-readable form, use {@link #label4Humans(TemplateModel, Collection)}.
     * 
     * @param model    the template model containing the modules and channels
     * @param modules  the modules to be composed
     * @param channels the channels to be synchronized and composed
     * @return an array of two {@link FSAModel}s; the first model is the composition
     *         of the given modules; the second model is the composition of the
     *         synchronized versions of the given channels (if no channels were
     *         given, the second model is a single-state automaton where all events
     *         are self-looped)
     * @throws IllegalArgumentException when the list of modules is empty
     * @see #label4Humans(TemplateModel, Collection)
     */
    public static FSAModel[] synchronizeAndCompose(TemplateModel model, Collection<TemplateComponent> modules,
            Collection<TemplateComponent> channels) {
        warnings.clear();
        if (modules.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Set<FSAModel> modulesFSA = new HashSet<FSAModel>();
        Map<TemplateComponent, Map<String, String>> eventRenaming = new HashMap<TemplateComponent, Map<String, String>>();
        for (TemplateComponent module : modules) {
            FSAModel fsa = module.getModel().clone();
            Map<String, String> eventMap = new HashMap<String, String>();
            for (DESEvent event : fsa.getEventSet()) {
                String newName = getUniqueEventName(module, event.getId());
                eventMap.put(event.getSymbol(), newName);
                event.setSymbol(newName);
            }
            modulesFSA.add(fsa);
            eventRenaming.put(module, eventMap);
        }
        Operation sync = OperationManager.instance().getOperation("sync");
        FSAModel moduleFSA = (FSAModel) sync.perform(modulesFSA.toArray())[0];
        warnings.addAll(sync.getWarnings());
        DESEventSet systemEvents = moduleFSA.getEventSet().copy();
        Set<FSAModel> channelsFSA = new HashSet<FSAModel>();
        for (TemplateComponent channel : channels) {
            Map<String, String> channelEventMap = new HashMap<String, String>();
            for (TemplateLink link : model.getAdjacentLinks(channel.getId())) {
                TemplateComponent module;
                String moduleEvent;
                String channelEvent;
                if (link.getLeftComponent() == channel) {
                    module = link.getRightComponent();
                    moduleEvent = link.getRightEventName();
                    channelEvent = link.getLeftEventName();
                } else {
                    module = link.getLeftComponent();
                    moduleEvent = link.getLeftEventName();
                    channelEvent = link.getRightEventName();
                }
                if (!eventRenaming.containsKey(module)) {
                    throw new IllegalArgumentException();
                }
                channelEventMap.put(channelEvent, eventRenaming.get(module).get(moduleEvent));
            }
            FSAModel fsa = channel.getModel().clone();
            for (DESEvent event : fsa.getEventSet()) {
                if (channelEventMap.containsKey(event.getSymbol())) {
                    event.setSymbol(channelEventMap.get(event.getSymbol()));
                } else {
                    event.setSymbol(getUniqueEventName(channel, event.getId()));
                }
            }
            DESEventSet toSelfloop = systemEvents.subtract(fsa.getEventSet());
            Operation selfloop = OperationManager.instance().getOperation("selfloop");
            fsa = (FSAModel) selfloop.perform(new Object[] { fsa, toSelfloop })[0];
            warnings.addAll(selfloop.getWarnings());
            channelsFSA.add(fsa);
        }
        FSAModel channelFSA;
        if (!channelsFSA.isEmpty()) {
            Operation product = OperationManager.instance().getOperation("product");
            channelFSA = (FSAModel) product.perform(channelsFSA.toArray())[0];
            warnings.addAll(product.getWarnings());
        } else {
            channelFSA = ModelManager.instance().createModel(FSAModel.class);
            FSAState s = channelFSA.assembleState();
            s.setInitial(true);
            s.setMarked(true);
            channelFSA.add(s);
            Operation selfloop = OperationManager.instance().getOperation("selfloop");
            channelFSA = (FSAModel) selfloop.perform(new Object[] { channelFSA, systemEvents })[0];
            warnings.addAll(selfloop.getWarnings());
        }
        return new FSAModel[] { moduleFSA, channelFSA };
    }

    /**
     * Relabel the events of the outputs of
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)} with
     * names which are human-readable. The existing labels are assumed to be unique
     * string identifiers as generated by
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}. This
     * method may produce unexpected results if the template design has been
     * modified in between the invocations of
     * {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)} and
     * this method.
     * 
     * @param model the template design containing the inputs to
     *              {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}
     * @param fsas  the outputs of
     *              {@link #synchronizeAndCompose(TemplateModel, Collection, Collection)}
     * @see #synchronizeAndCompose(TemplateModel, Collection, Collection)
     */
    public static void label4Humans(TemplateModel model, Collection<FSAModel> fsas) {
        for (FSAModel fsa : fsas) {
            for (DESEvent event : fsa.getEventSet()) {
                long[] pointer = getEventPointer(event.getSymbol());
                FSAModel original = model.getComponent(pointer[0]).getModel();
                String fsaName = original.getName();
                if (fsaName.startsWith(TemplateModel.FSA_NAME_PREFIX)) {
                    fsaName = fsaName.substring(TemplateModel.FSA_NAME_PREFIX.length());
                }
                event.setSymbol(fsaName + ":" + original.getEvent(pointer[1]).getSymbol());
            }
        }
    }

    /**
     * Generate a unique event name string for the given event.
     * 
     * @param c       the template design component
     * @param eventId the id of the event in the underlying model of the given
     *                component
     * @return a unique event name string
     */
    protected static String getUniqueEventName(TemplateComponent c, long eventId) {
        return "" + c.getId() + ":" + eventId;
    }

    /**
     * Retrieve the ids of the template design component and the event in the
     * underlying model from the unique event name string generated by
     * {@link #getUniqueEventName(TemplateComponent, long)}.
     * 
     * @param name the unique event name
     * @return an array of two elements: the id of the {@link TemplateComponent} and
     *         the id of the event in the underlying model of the
     *         {@link TemplateComponent}.
     */
    protected static long[] getEventPointer(String name) {
        String[] ids = name.split(":");
        return new long[] { Long.parseLong(ids[0]), Long.parseLong(ids[1]) };
    }

    /**
     * Copies the controllability setting from the events of the given source model
     * to the events of the given destination model. The controllability is copied
     * only for events that are present in both models.
     * 
     * @param source the source model
     * @param dest   the destination model
     */
    public static void copyControllability(FSAModel source, FSAModel dest) {
        HashMap<String, Boolean> sourceMap = new HashMap<String, Boolean>();
        for (Iterator<SupervisoryEvent> i = source.getEventIterator(); i.hasNext();) {
            SupervisoryEvent event = i.next();
            sourceMap.put(event.getSymbol(), event.isControllable());
        }
        for (Iterator<SupervisoryEvent> i = dest.getEventIterator(); i.hasNext();) {
            SupervisoryEvent event = i.next();
            if (sourceMap.containsKey(event.getSymbol())) {
                event.setControllable(sourceMap.get(event.getSymbol()));
            }
        }
    }
}

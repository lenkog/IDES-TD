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

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESEventSet;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.model.DESModelType;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.model.ParentModel;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.model.TemplateModelMessage;
import templates.model.TemplateModelSubscriber;

/**
 * Implementation of {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class TemplateDesign implements TemplateModel, DESModelSubscriber {
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
     * The collection of {@link DESModelSubscriber}s registered with the model.
     */
    private ArrayList<DESModelSubscriber> modelSubscribers = new ArrayList<DESModelSubscriber>();

    public void addSubscriber(DESModelSubscriber subscriber) {
        modelSubscribers.add(subscriber);
    }

    public void removeSubscriber(DESModelSubscriber subscriber) {
        modelSubscribers.remove(subscriber);
    }

    public DESModelSubscriber[] getDESModelSubscribers() {
        return modelSubscribers.toArray(new DESModelSubscriber[] {});
    }

    /**
     * The collection of {@link TemplateModelSubscriber}s registered with the model.
     */
    private ArrayList<TemplateModelSubscriber> templateSubscribers = new ArrayList<TemplateModelSubscriber>();

    public void addSubscriber(TemplateModelSubscriber subscriber) {
        templateSubscribers.add(subscriber);
    }

    public void removeSubscriber(TemplateModelSubscriber subscriber) {
        templateSubscribers.remove(subscriber);
    }

    public TemplateModelSubscriber[] getTemplateModelSubscribers() {
        return templateSubscribers.toArray(new TemplateModelSubscriber[] {});
    }

    public void fireTemplateModelStructureChanged(TemplateModelMessage message) {
        for (TemplateModelSubscriber subscriber : templateSubscribers) {
            subscriber.templateModelStructureChanged(message);
        }
    }

    /**
     * Keeps track if the model is "dirty", i.e., needs to be saved.
     */
    protected boolean needsSave = false;

    public boolean needsSave() {
        return needsSave;
    }

    /**
     * Set the "dirty" state of the model and announce to listeners if the state
     * changed.
     * 
     * @param b the new "dirty" state of the model
     */
    protected void setNeedsSave(boolean b) {
        if (b != needsSave) {
            needsSave = b;
            DESModelMessage message = new DESModelMessage(needsSave ? DESModelMessage.DIRTY : DESModelMessage.CLEAN,
                    this);
            for (DESModelSubscriber s : modelSubscribers.toArray(new DESModelSubscriber[] {})) {
                s.saveStatusChanged(message);
            }
        }
    }

    /**
     * Descriptor of the template design type of model.
     * 
     * @author Lenko Grigorov
     */
    protected static class TemplateDesignDescriptor implements DESModelType {

        public TemplateModel createModel(String name) {
            return new TemplateDesign(name);
        }

        public String getDescription() {
            return "Template Design";
        }

        public Image getIcon() {
            return Toolkit.getDefaultToolkit().createImage(
                    Hub.getLocalResource(TemplateDesignDescriptor.class, "images/icons/model_template.gif"));
        }

        public Class<TemplateModel> getMainPerspective() {
            return TemplateModel.class;
        }

        public Class<?>[] getModelPerspectives() {
            return new Class[] { TemplateModel.class };
        }
    }

    /**
     * Maintains an instance of the descriptor of the template design type.
     */
    public static TemplateDesignDescriptor myDescriptor = new TemplateDesignDescriptor();

    public DESModelType getModelType() {
        return myDescriptor;
    }

    /**
     * The name of the model.
     */
    protected String name;

    /**
     * The set of {@link TemplateComponent}s in the model.
     */
    protected Set<TemplateComponent> components = new HashSet<TemplateComponent>();

    /**
     * The set of {@link TemplateLink}s in the model.
     */
    protected Set<TemplateLink> links = new HashSet<TemplateLink>();

    /**
     * Next available id for {@link TemplateComponent}s.
     */
    protected long freeComponentId = 0;

    /**
     * Next available id for {@link TemplateLink}s.
     */
    protected long freeLinkId = 0;

    /**
     * Construct a new template design with the given name.
     * 
     * @param name the name for the new template design
     */
    public TemplateDesign(String name) {
        this.name = name;
    }

    /**
     * Checks if the model contains a {@link TemplateComponent} with the given id.
     * 
     * @param id the id of the component
     * @return <code>true</code> if the model contains a {@link TemplateComponent}
     *         with the given id; <code>false</code> otherwise
     */
    protected boolean containsComponentId(long id) {
        for (TemplateComponent component : components) {
            if (component.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the model contains a {@link TemplateLink} with the given id.
     * 
     * @param id the id of the link
     * @return <code>true</code> if the model contains a {@link TemplateLink} with
     *         the given id; <code>false</code> otherwise
     */
    protected boolean containsLinkId(long id) {
        for (TemplateLink link : links) {
            if (link.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addComponent(TemplateComponent component) {
        if (containsComponentId(component.getId())) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyComponentId"));
        }
        if (freeComponentId <= component.getId()) {
            freeComponentId = component.getId() + 1;
        }
        components.add(component);
        if (component.getModel() != null) {
            component.getModel().addSubscriber(this);
        }
        fireTemplateModelStructureChanged(new TemplateModelMessage(this, component.getId(),
                TemplateModelMessage.ELEMENT_COMPONENT, TemplateModelMessage.OP_ADD));
        setNeedsSave(true);
    }

    public synchronized void addLink(TemplateLink link) {
        if (containsLinkId(link.getId())) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkId"));
        }
        if (!components.contains(link.getLeftComponent()) || !components.contains(link.getRightComponent())) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinking"));
        }
        // Collection<TemplateLink> channelLinks = getChannelLinks(link
        // .getChannel().getId());
        // for (TemplateLink l : channelLinks)
        // {
        // if (l.getChannelEventName().equals(link.getChannelEventName())
        // || (l.getModule() == link.getModule() && l
        // .getModuleEventName().equals(link
        // .getModuleEventName())))
        // {
        // throw new InconsistentModificationException(Hub
        // .string("TD_inconsistencyLinking"));
        // }
        // }
        if (freeLinkId <= link.getId()) {
            freeLinkId = link.getId() + 1;
        }
        links.add(link);
        fireTemplateModelStructureChanged(new TemplateModelMessage(this, link.getId(),
                TemplateModelMessage.ELEMENT_LINK, TemplateModelMessage.OP_ADD));
        setNeedsSave(true);
    }

    public synchronized TemplateLink createLink(long leftId, long rightId) {
        TemplateLink link = assembleLink(leftId, rightId);
        links.add(link);
        fireTemplateModelStructureChanged(new TemplateModelMessage(this, link.getId(),
                TemplateModelMessage.ELEMENT_LINK, TemplateModelMessage.OP_ADD));
        setNeedsSave(true);
        return link;
    }

    public synchronized TemplateComponent createComponent() {
        TemplateComponent component = assembleComponent();
        components.add(component);
        fireTemplateModelStructureChanged(new TemplateModelMessage(this, component.getId(),
                TemplateModelMessage.ELEMENT_COMPONENT, TemplateModelMessage.OP_ADD));
        setNeedsSave(true);
        return component;
    }

    public Collection<TemplateComponent> getComponents() {
        return new HashSet<TemplateComponent>(components);
    }

    public Collection<TemplateLink> getLinks() {
        return new HashSet<TemplateLink>(links);
    }

    public TemplateComponent getComponent(long id) {
        for (TemplateComponent component : components) {
            if (component.getId() == id) {
                return component;
            }
        }
        return null;
    }

    public Collection<TemplateComponent> getModules() {
        Set<TemplateComponent> modules = new HashSet<TemplateComponent>();
        for (TemplateComponent component : components) {
            if (component.getType() == TemplateComponent.TYPE_MODULE) {
                modules.add(component);
            }
        }
        return modules;
    }

    public Collection<TemplateComponent> getChannels() {
        Set<TemplateComponent> channels = new HashSet<TemplateComponent>();
        for (TemplateComponent component : components) {
            if (component.getType() == TemplateComponent.TYPE_CHANNEL) {
                channels.add(component);
            }
        }
        return channels;
    }

    public TemplateLink getLink(long id) {
        for (TemplateLink link : links) {
            if (link.getId() == id) {
                return link;
            }
        }
        return null;
    }

    public int getComponentCount() {
        return components.size();
    }

    public synchronized void removeComponent(long id) {
        if (!containsComponentId(id)) {
            return;
        }
        for (TemplateLink link : getAdjacentLinks(id)) {
            removeLink(link.getId());
        }
        TemplateComponent c = getComponent(id);
        if (c.getModel() != null) {
            c.getModel().removeSubscriber(this);
        }
        components.remove(c);
        fireTemplateModelStructureChanged(new TemplateModelMessage(this, id, TemplateModelMessage.ELEMENT_COMPONENT,
                TemplateModelMessage.OP_REMOVE));
        setNeedsSave(true);
    }

    public synchronized void removeLink(long id) {
        if (!containsLinkId(id)) {
            return;
        }
        links.remove(getLink(id));
        fireTemplateModelStructureChanged(
                new TemplateModelMessage(this, id, TemplateModelMessage.ELEMENT_LINK, TemplateModelMessage.OP_REMOVE));
        setNeedsSave(true);
    }

    public String getName() {
        return name;
    }

    public void metadataChanged() {
        setNeedsSave(true);
    }

    public void modelSaved() {
        for (TemplateComponent c : components) {
            if (c.hasModel()) {
                c.getModel().removeSubscriber(this);
                c.getModel().modelSaved();
                c.getModel().addSubscriber(this);
            }
        }
        setNeedsSave(false);
    }

    public void setName(String name) {
        if (name != null && name.equals(this.name)) {
            return;
        }
        this.name = name;
        DESModelMessage message = new DESModelMessage(DESModelMessage.NAME, this);
        for (DESModelSubscriber s : modelSubscribers.toArray(new DESModelSubscriber[] {})) {
            s.modelNameChanged(message);
        }
        metadataChanged();
    }

    public boolean existsLink(long channelId, long moduleId) {
        for (TemplateLink link : links) {
            if (link.getChannel().getId() == channelId && link.getModule().getId() == moduleId) {
                return true;
            }
        }
        return false;
    }

    public Collection<TemplateLink> getAdjacentLinks(long componentId) {
        Set<TemplateLink> ret = new HashSet<TemplateLink>();
        for (TemplateLink link : links) {
            if (link.getLeftComponent().getId() == componentId || link.getRightComponent().getId() == componentId) {
                ret.add(link);
            }
        }
        return ret;
    }

    public Collection<TemplateComponent> getCover(long channelId) {
        Set<TemplateComponent> ret = new HashSet<TemplateComponent>();
        if (getComponent(channelId).getType() == TemplateComponent.TYPE_CHANNEL) {
            for (TemplateLink link : getAdjacentLinks(channelId)) {
                if (link.getModule() != null) {
                    ret.add(link.getModule());
                }
            }
        }
        return ret;
    }

    public Collection<TemplateLink> getLinks(long leftId, long rightId) {
        Set<TemplateLink> ret = new HashSet<TemplateLink>();
        for (TemplateLink link : links) {
            if ((link.getLeftComponent().getId() == leftId && link.getRightComponent().getId() == rightId)
                    || (link.getLeftComponent().getId() == rightId && link.getRightComponent().getId() == leftId)) {
                ret.add(link);
            }
        }
        return ret;
    }

    /**
     * Retrieve the {@link TemplateComponent} containing the given {@link FSAModel}.
     * 
     * @param fsa the {@link FSAModel} to be used in the search
     * @return the {@link TemplateComponent} containing the given {@link FSAModel}
     *         (if more than one {@link TemplateComponent}s contains the
     *         {@link FSAModel}, returns one of them arbitrarily); <code>null</code>
     *         if not {@link TemplateComponent} contains the given {@link FSAModel}
     */
    protected TemplateComponent getComponentWithFSA(FSAModel fsa) {
        if (fsa == null) {
            return null;
        }
        for (TemplateComponent component : components) {
            if (component.getModel() == fsa) {
                return component;
            }
        }
        return null;
    }

    public void assignFSA(long componentId, FSAModel fsa) {
        if (getComponentWithFSA(fsa) != null) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyFSADup"));
        }
        TemplateComponent c = getComponent(componentId);
        if (c != null) {
            if (c.getModel() != null) {
                c.getModel().removeSubscriber(this);
                c.getModel().setParentModel(null);
            }
            if (fsa != null) {
                fsa.setParentModel(this);
            }
            c.setModel(fsa);
            if (fsa != null) {
                fsa.addSubscriber(this);
            }
            fireTemplateModelStructureChanged(new TemplateModelMessage(this, componentId,
                    TemplateModelMessage.ELEMENT_COMPONENT, TemplateModelMessage.OP_MODIFY));
            setNeedsSave(true);
        }
    }

    public void removeFSA(long componentId) {
        TemplateComponent c = getComponent(componentId);
        if (c != null) {
            if (c.getModel() != null) {
                c.getModel().removeSubscriber(this);
                c.getModel().setParentModel(null);
            }
            c.setModel(null);
            fireTemplateModelStructureChanged(new TemplateModelMessage(this, componentId,
                    TemplateModelMessage.ELEMENT_COMPONENT, TemplateModelMessage.OP_MODIFY));
            setNeedsSave(true);
        }
    }

    public TemplateComponent assembleComponent() {
        Component component = new Component(freeComponentId);
        freeComponentId++;
        return component;
    }

    public TemplateLink assembleLink(long leftId, long rightId) {
        TemplateComponent left = getComponent(leftId);
        TemplateComponent right = getComponent(rightId);
        if (left == null || right == null) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
        }
        Link link = new Link(freeLinkId, left, right);
        freeLinkId++;
        return link;
    }

    public DESModel getChildModel(String arg0) {
        long id;
        try {
            id = Long.parseLong(arg0);
        } catch (NumberFormatException e) {
            return null;
        }
        TemplateComponent component = null;
        for (TemplateComponent c : components) {
            if (c.getId() == id) {
                component = c;
                break;
            }
        }
        if (component == null || !component.hasModel()) {
            return null;
        }
        return component.getModel();
    }

    public String getChildModelId(DESModel arg0) throws IllegalArgumentException {
        TemplateComponent component = null;
        for (TemplateComponent c : components) {
            if (c.hasModel() && c.getModel() == arg0) {
                component = c;
                break;
            }
        }
        if (component == null) {
            throw new IllegalArgumentException();
        }
        return "" + component.getId();
    }

    /**
     * Maintains a pointer to the parent model of this model. Under normal
     * circumstances should be <code>null</code>.
     */
    protected ParentModel parent = null;

    public ParentModel getParentModel() {
        return parent;
    }

    public void setParentModel(ParentModel arg0) {
        parent = arg0;
    }

    public void modelNameChanged(DESModelMessage arg0) {
    }

    public void saveStatusChanged(DESModelMessage arg0) {
        if (arg0.getEventType() == DESModelMessage.DIRTY) {
            setNeedsSave(true);
        }
    }

    public void setComponentType(long componentId, int type) {
        TemplateComponent c = getComponent(componentId);
        if (c != null) {
            if (type == TemplateComponent.TYPE_CHANNEL) {
                c.setType(type);
            } else {
                c.setType(TemplateComponent.TYPE_MODULE);
            }
            fireTemplateModelStructureChanged(new TemplateModelMessage(this, componentId,
                    TemplateModelMessage.ELEMENT_COMPONENT, TemplateModelMessage.OP_MODIFY));
            setNeedsSave(true);
        }
    }

    public DESEventSet getEventSet() {
        return ModelManager.instance().createEmptyEventSet();
    }
}

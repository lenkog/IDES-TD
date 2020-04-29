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

package templates.diagram;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAMessage;
import ides.api.model.fsa.FSAModel;
import ides.api.model.fsa.FSASubscriber;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.ModelManager;
import templates.diagram.actions.DiagramActions;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.model.TemplateModelMessage;
import templates.model.TemplateModelSubscriber;
import templates.utils.EntityIcon;

/**
 * The class describing and maintaining the graphical representation of a
 * {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class TemplateDiagram implements TemplateModelSubscriber, FSASubscriber {
    /**
     * Collection of all {@link TemplateDiagramSubscriber}s registered to receive
     * messages from this template diagram.
     */
    private ArrayList<TemplateDiagramSubscriber> subscribers = new ArrayList<TemplateDiagramSubscriber>();

    /**
     * Attaches the given subscriber to this template diagram. The given subscriber
     * will receive notifications of changes from this template diagram.
     * 
     * @param subscriber the subscriber to add
     */
    public void addSubscriber(TemplateDiagramSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Removes the given subscriber to this template diagram. The given subscriber
     * will no longer receive notifications of changes from this template diagram.
     * 
     * @param subscriber the subscriber to remove
     */
    public void removeSubscriber(TemplateDiagramSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Returns all current subscribers to this template diagram.
     * 
     * @return all current subscribers to this template diagram
     */
    public TemplateDiagramSubscriber[] getDiagramSubscribers() {
        return subscribers.toArray(new TemplateDiagramSubscriber[] {});
    }

    /**
     * Notifies all subscribers that there has been a change to an element of this
     * template diagram. Also notifies the underlying {@link TemplateModel} that
     * layout information has changed.
     * 
     * @param message the description of the change
     */
    protected void fireDiagramChanged(TemplateDiagramMessage message) {
        model.metadataChanged();
        for (TemplateDiagramSubscriber s : subscribers) {
            s.templateDiagramChanged(message);
        }
    }

    /**
     * Notifies all subscribers that there has been a change to the set of selected
     * elements in this template diagram.
     * 
     * @param message the description of the new set of selected elements
     */
    protected void fireDiagramSelectionChanged(TemplateDiagramMessage message) {
        for (TemplateDiagramSubscriber s : subscribers) {
            s.templateDiagramSelectionChanged(message);
        }
    }

    /**
     * The inset of the diagram (how much empty space to leave from the border of
     * the canvas).
     */
    public static final int DESIRED_DIAGRAM_INSET = 10;

    /**
     * Collection of all the {@link Entity}s in the template diagram.
     */
    protected Set<Entity> entities = new HashSet<Entity>();

    /**
     * The {@link Entity}s in the temlpate diagram, indexed by their corresponding
     * {@link TemplateComponent}s.
     */
    protected Map<TemplateComponent, Entity> component2Entity = new HashMap<TemplateComponent, Entity>();

    /**
     * Collection of all the {@link Connector}s in the template diagram.
     */
    protected Set<Connector> connectors = new HashSet<Connector>();

    /**
     * The {@link Connector}s in the template diagram, indexed by the
     * {@link TemplateLink}s they represent.
     */
    protected Map<TemplateLink, Connector> link2Connector = new HashMap<TemplateLink, Connector>();

    /**
     * Keep track of the {@link FSAModel} associated with each
     * {@link TemplateComponent} in the template diagram. This is necessary in order
     * to be able to unsubscribe from an {@link FSAModel} if it gets replaced (and
     * thus no longer accessible through the {@link TemplateComponent}).
     */
    protected Map<TemplateComponent, FSAModel> component2FSA = new HashMap<TemplateComponent, FSAModel>();

    /**
     * The {@link TemplateComponent}s in the template diagram, indexed by the
     * {@link FSAModel}s they contain.
     */
    protected Map<FSAModel, TemplateComponent> FSA2component = new HashMap<FSAModel, TemplateComponent>();

    /**
     * The {@link TemplateModel} represented by this template diagram.
     */
    protected TemplateModel model;

    /**
     * Collection of the currently selected elements in the template diagram.
     */
    protected Collection<DiagramElement> selection = new HashSet<DiagramElement>();

    /**
     * Construct a template diagram for the given {@link TemplateModel}.
     * 
     * @param m the {@link TemplateModel} to be represented by the template diagram
     */
    public TemplateDiagram(TemplateModel m) {
        model = m;
        recoverLayout();
        m.addSubscriber((TemplateModelSubscriber) this);
    }

    /**
     * Retrieve the layout annotations from the underlying {@link TemplateModel}
     * elements (under the {@link EntityLayout#KEY} key) and build the corresponding
     * diagram elements. In essence, computes how to render the underlying
     * {@link TemplateModel}.
     */
    protected void recoverLayout() {
        if (DiagramElement.getGlobalFont() == null) {
            DiagramElement.setGlobalFont(new JLabel().getFont());
        }
        if (DiagramElement.getGlobalFontRenderer() == null) {
            DiagramElement.setGlobalFontRenderer(Hub.getMainWindow().getGraphics());
        }
        clearSelection();
        component2Entity.clear();
        entities.clear();
        link2Connector.clear();
        for (FSAModel fsa : component2FSA.values()) {
            fsa.removeSubscriber(this);
        }
        component2FSA.clear();
        FSA2component.clear();
        connectors.clear();
        boolean modelClean = !model.needsSave();
        for (TemplateComponent component : model.getComponents()) {
            EntityLayout layout = null;
            if (component.hasAnnotation(EntityLayout.KEY)) {
                layout = (EntityLayout) component.getAnnotation(EntityLayout.KEY);
            }
            if (layout == null) {
                layout = createLayout(component);
            }
            Entity newEntity = new Entity(component, layout);
            entities.add(newEntity);
            component2Entity.put(component, newEntity);
            if (component.hasModel()) {
                component.getModel().setName(TemplateModel.FSA_NAME_PREFIX + layout.label);
                component2FSA.put(component, component.getModel());
                FSA2component.put(component.getModel(), component);
                component.getModel().addSubscriber(this);
            }
        }
        if (modelClean) {
            model.modelSaved();
        }
        for (TemplateLink link : model.getLinks()) {
            Entity left = component2Entity.get(link.getLeftComponent());
            Entity right = component2Entity.get(link.getRightComponent());
            Connector c = getConnector(left, right);
            if (c == null) {
                c = new Connector(left, right, Arrays.asList(new TemplateLink[] {}));
                connectors.add(c);
            }
            c.addLink(link);
            link2Connector.put(link, c);
        }
        if (model.hasAnnotation(EmptyConnectorSet.KEY)) {
            EmptyConnectorSet emptyConnectors = (EmptyConnectorSet) model.getAnnotation(EmptyConnectorSet.KEY);
            for (EmptyConnector ec : emptyConnectors) {
                Entity left = component2Entity.get(model.getComponent(ec.leftComponent));
                Entity right = component2Entity.get(model.getComponent(ec.rightComponent));
                Connector c = new Connector(left, right, Arrays.asList(new TemplateLink[] {}));
                connectors.add(c);
            }
        }
        new DiagramActions.ShiftDiagramInViewAction(new CompoundEdit(), this).execute();
    }

    /**
     * Create new layout information for the given {@link TemplateComponent}. Called
     * internally when the layout annotation of the {@link TemplateComponent} is
     * missing.
     * 
     * @param component the {@link TemplateComponent} whose layout has to be
     *                  generated
     * @return the new layout information for the given {@link TemplateComponent}
     */
    protected EntityLayout createLayout(TemplateComponent component) {
        EntityLayout layout = new EntityLayout();
        if (component.hasModel()) {
            layout.label = component.getModel().getName();
            if (layout.label.startsWith(TemplateModel.FSA_NAME_PREFIX)) {
                layout.label = layout.label.substring(TemplateModel.FSA_NAME_PREFIX.length());
            }
        } else {
            layout.label = Hub.string("TD_untitledEntityPrefix") + " " + component.getId();
        }
        int row = entities.size() / 10;
        int col = entities.size() % 10;
        while (hasEntitiesAt(new Rectangle(col * 100, row * 100, 100, 100))) {
            col = (col + 1) % 10;
            if (col == 0) {
                row++;
            }
        }
        layout.location = new Point(col * 100 + 50, row * 100 + 50);
        return layout;
    }

    public void templateModelStructureChanged(TemplateModelMessage message) {
        if (message.getOperationType() == TemplateModelMessage.OP_MODIFY
                && message.getElementType() == TemplateModelMessage.ELEMENT_COMPONENT) {
            TemplateComponent component = model.getComponent(message.getElementId());
            FSAModel fsa = component2FSA.get(component);
            if (fsa != null) {
                fsa.removeSubscriber(this);
                FSA2component.remove(fsa);
            }
            component2FSA.remove(component);
            Entity entity = component2Entity.get(component);
            if (entity != null) {
                if (entity.getComponent().hasModel()) {
                    component2FSA.put(entity.getComponent(), entity.getComponent().getModel());
                    FSA2component.put(entity.getComponent().getModel(), entity.getComponent());
                    entity.getComponent().getModel().addSubscriber(this);
                }
                entity.update();
                fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { entity }),
                        TemplateDiagramMessage.OP_MODIFY));
                return;
            }
        }
        recoverLayout();
        updateEmptyConnectorList();
        Set<DiagramElement> elements = new HashSet<DiagramElement>();
        elements.addAll(entities);
        elements.addAll(connectors);
        fireDiagramChanged(new TemplateDiagramMessage(this, elements, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Retrieve the {@link TemplateModel} represented by this template diagram.
     * 
     * @return the {@link TemplateModel} represented by this template diagram
     */
    public TemplateModel getModel() {
        return model;
    }

    /**
     * Unsubscribe this template diagram from the underlying {@link TemplateModel}
     * and stop listening to notifications of changes to the {@link TemplateModel}.
     * <p>
     * This method should be called only just before disposing of this template
     * diagram.
     */
    public void release() {
        model.removeSubscriber((TemplateModelSubscriber) this);
    }

    /**
     * Create an {@link Entity} (and the underlying {@link TemplateComponent}) at
     * the given location and add it to the template diagram. The new
     * {@link TemplateComponent} will be of type <i>module</i>.
     * 
     * @param location the location for the new {@link Entity}
     * @return the newly created {@link Entity}
     */
    public Entity createEntity(Point location) {
        model.removeSubscriber((TemplateModelSubscriber) this);
        TemplateComponent component = model.createComponent();
        EntityLayout layout = new EntityLayout();
        layout.location = location;
        layout.label = Hub.string("TD_untitledEntityPrefix") + " " + component.getId();
        Entity entity = new Entity(component, layout);
        entities.add(entity);
        component2Entity.put(component, entity);
        FSAModel fsa = ModelManager.instance().createModel(FSAModel.class,
                TemplateModel.FSA_NAME_PREFIX + layout.label);
        fsa.setParentModel(model);
        model.assignFSA(component.getId(), fsa);
        component2FSA.put(component, fsa);
        FSA2component.put(fsa, component);
        fsa.addSubscriber(this);
        model.addSubscriber((TemplateModelSubscriber) this);
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { entity }),
                TemplateDiagramMessage.OP_ADD));
        return entity;
    }

    /**
     * Add an {@link Entity} to the template design.
     * 
     * @param entity the {@link Entity} to add
     */
    public void add(Entity entity) {
        if (entities.contains(entity)) {
            return;
        }
        model.removeSubscriber((TemplateModelSubscriber) this);
        try {
            model.addComponent(entity.getComponent());
            entities.add(entity);
            component2Entity.put(entity.getComponent(), entity);
            if (entity.getComponent().hasModel()) {
                component2FSA.put(entity.getComponent(), entity.getComponent().getModel());
                FSA2component.put(entity.getComponent().getModel(), entity.getComponent());
                entity.getComponent().getModel().addSubscriber(this);
            }
        } finally {
            model.addSubscriber((TemplateModelSubscriber) this);
        }
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { entity }),
                TemplateDiagramMessage.OP_ADD));
    }

    /**
     * Remove an {@link Entity} from the template design (and the corresponding
     * {@link TemplateComponent} from the underlying {@link TemplateModel}). All
     * {@link Connector}s linked to the {@link Entity} (and the corresponding
     * {@link TemplateLink}s) are removed as well. If the {@link Entity} is not a
     * part of the template design, the method does nothing.
     * 
     * @param entity the {@link Entity} to remove
     */
    public void remove(Entity entity) {
        if (!entities.contains(entity)) {
            return;
        }
        if (entity.getComponent().hasModel()) {
            DESModel fsa = Hub.getWorkspace().getModel(entity.getComponent().getModel().getName());
            if (entity.getComponent().getModel() == fsa) {
                Hub.getWorkspace().removeModel(entity.getComponent().getModel().getName());
            }
        }
        clearSelection();
        model.removeSubscriber((TemplateModelSubscriber) this);
        Collection<Connector> adjacent = getAdjacentConnectors(entity);
        for (Connector c : adjacent) {
            link2Connector.keySet().removeAll(c.getLinks());
            connectors.remove(c);
            for (TemplateLink link : c.getLinks()) {
                model.removeLink(link.getId());
            }
        }
        if (entity.getComponent().hasModel()) {
            entity.getComponent().getModel().removeSubscriber(this);
            FSA2component.remove(entity.getComponent().getModel());
            component2FSA.remove(entity.getComponent());
        }
        component2Entity.remove(entity.getComponent());
        entities.remove(entity);
        model.removeComponent(entity.getComponent().getId());
        updateEmptyConnectorList();
        model.addSubscriber((TemplateModelSubscriber) this);
        Collection<DiagramElement> removed = new HashSet<DiagramElement>(adjacent);
        removed.add(entity);
        fireDiagramChanged(new TemplateDiagramMessage(this, removed, TemplateDiagramMessage.OP_REMOVE));
    }

    /**
     * Label the given {@link Entity} with the given label.
     * 
     * @param entity the {@link Entity} to be labelled
     * @param label  the new label for the {@link Entity}
     */
    public void labelEntity(Entity entity, String label) {
        entity.setLabel(label);
        if (entity.getComponent().getModel() != null) {
            entity.getComponent().getModel().setName(TemplateModel.FSA_NAME_PREFIX + label);
        }
        model.metadataChanged();
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { entity }),
                TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Set the icon of the given {@link Entity}.
     * 
     * @param entity the {@link Entity} whose icon has to be set
     * @param icon   the new icon for the {@link Entity}
     */
    public void setEntityIcon(Entity entity, EntityIcon icon) {
        entity.setIcon(icon);
        model.metadataChanged();
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { entity }),
                TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Retrieve the {@link Entity} which contains the given point. If there is more
     * than one {@link Entity} which contains the point, returns one of these
     * {@link Entity}s arbitrarily. If there is no {@link Entity} which contains the
     * point, returns <code>null</code>.
     * 
     * @param location the point to be used to locate the {@link Entity}
     * @return one of the {@link Entity}s which contain the point; <code>null</code>
     *         if no {@link Entity} contains the point
     */
    public Entity getEntityAt(Point location) {
        for (Entity entity : entities) {
            if (entity.contains(location)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Check if there are {@link Entity}s in the given area. An {@link Entity} is
     * considered to be in the area if a part of it intersects the area.
     * 
     * @param area the area to examine for {@link Entity}s
     * @return <code>true</code> if a part of at least one {@link Entity} intersects
     *         the given area; <code>false</code> otherwise
     */
    public boolean hasEntitiesAt(Rectangle area) {
        for (Entity entity : entities) {
            if (entity.intersects(area)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve a collection of all the {@link Entity}s in the template diagram.
     * 
     * @return a collection of all the {@link Entity}s in the template diagram
     */
    public Collection<Entity> getEntities() {
        return new HashSet<Entity>(entities);
    }

    /**
     * Retrieve a collection of all the <i>module</i> {@link Entity}s in the
     * template diagram.
     * 
     * @return a collection of all the <i>module</i> {@link Entity}s in the template
     *         diagram
     */
    public Collection<Entity> getModules() {
        HashSet<Entity> vmodules = new HashSet<Entity>();
        for (Entity vcomponent : entities) {
            if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_MODULE) {
                vmodules.add(vcomponent);
            }
        }
        return vmodules;
    }

    /**
     * Retrieve a collection of all the <i>channel</i> {@link Entity}s in the
     * template diagram.
     * 
     * @return a collection of all the <i>channel</i> {@link Entity}s in the
     *         template diagram
     */
    public Collection<Entity> getChannels() {
        HashSet<Entity> vchannels = new HashSet<Entity>();
        for (Entity vcomponent : entities) {
            if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_CHANNEL) {
                vchannels.add(vcomponent);
            }
        }
        return vchannels;
    }

    /**
     * Retrieve a collection of all the {@link Connector}s linking the given
     * {@link Entity} to other {@link Entity}s.
     * 
     * @param entity the {@link Entity} whose {@link Connector}s should be retrieved
     * @return a collection of all the {@link Connector}s linking the given
     *         {@link Entity} to other {@link Entity}s
     */
    public Collection<Connector> getAdjacentConnectors(Entity entity) {
        Collection<Connector> adjacent = new HashSet<Connector>();
        for (Connector c : connectors) {
            if (entity == c.getLeftEntity() || entity == c.getRightEntity()) {
                adjacent.add(c);
            }
        }
        return adjacent;
    }

    /**
     * Retrieve the {@link Connector} between the given {@link Entity}s. If there is
     * more than one {@link Connector} between the given {@link Entity} s, return
     * one of these {@link Connector}s arbitrarily (note that this situation can
     * happen only in inconsistent template diagrams). If there is no
     * {@link Connector} between the given {@link Entity}s, return
     * <code>null</code>.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to distinguish
     * between the linked entities.
     * 
     * @param left  the first {@link Entity}
     * @param right the second {@link Entity}
     * @return the {@link Connector} between the given {@link Entity}s;
     *         <code>null</code> if no such {@link Connector} exists
     */
    public Connector getConnector(Entity left, Entity right) {
        for (Connector c : connectors) {
            if ((left == c.getLeftEntity() && right == c.getRightEntity())
                    || (left == c.getRightEntity() && right == c.getLeftEntity())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Create a {@link Connector} between the given {@link Entity}s and add it to
     * the template diagram. If a {@link Connector} between the given
     * {@link Entity}s already exists, do nothing.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to distinguish
     * between the linked entities.
     * 
     * @param left  the first {@link Entity} to be linked
     * @param right the second {@link Entity} to be linked
     * @return the newly created {@link Connector}, or the existing
     *         {@link Connector} if a {@link Connector} between the given
     *         {@link Entity}s already exists
     * @throws InconsistentModificationException if one or both of the given
     *                                           {@link Entity}s are not part of the
     *                                           template diagram
     */
    public Connector createConnector(Entity left, Entity right) {
        if (!(entities.contains(left) && entities.contains(right))) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
        }
        if (getConnector(left, right) != null) {
            return getConnector(left, right);
        }
        Connector c = new Connector(left, right, new HashSet<TemplateLink>());
        connectors.add(c);
        updateEmptyConnectorList();
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_ADD));
        return c;
    }

    /**
     * Add a {@link Connector} to the template diagram.
     * 
     * @param c the {@link Connector} to be added
     * @throws InconsistentModificationException if one or both of the
     *                                           {@link Entity}s linked by the
     *                                           {@link Connector} are not part of
     *                                           the template diagram, or if there
     *                                           is already a {@link Connector}
     *                                           between the {@link Entity}s linked
     *                                           by the given {@link Connector}
     */
    public void add(Connector c) {
        if (!(entities.contains(c.getLeftEntity()) && entities.contains(c.getRightEntity()))) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
        }
        if (getConnector(c.getLeftEntity(), c.getRightEntity()) != null) {
            throw new InconsistentModificationException(Hub.string("TD_inconsistencyConnectorDup"));
        }
        model.removeSubscriber((TemplateModelSubscriber) this);
        try {
            for (TemplateLink link : c.getLinks()) {
                model.addLink(link);
            }
            connectors.add(c);
            for (TemplateLink link : c.getLinks()) {
                link2Connector.put(link, c);
            }
            updateEmptyConnectorList();
        } finally {
            model.addSubscriber((TemplateModelSubscriber) this);
        }
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_ADD));
    }

    /**
     * Remove a {@link Connector} from the template diagram (and the associated
     * {@link TemplateLink}s from the underlying {@link TemplateModel}). If the
     * given {@link Connector} is not a part of the diagram, the method does
     * nothing.
     * 
     * @param c the {@link Connector} to remove
     */
    public void remove(Connector c) {
        if (!connectors.contains(c)) {
            return;
        }
        clearSelection();
        model.removeSubscriber((TemplateModelSubscriber) this);
        for (TemplateLink link : c.getLinks()) {
            model.removeLink(link.getId());
        }
        link2Connector.keySet().removeAll(c.getLinks());
        connectors.remove(c);
        updateEmptyConnectorList();
        model.addSubscriber((TemplateModelSubscriber) this);
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_REMOVE));
    }

    /**
     * Create a new {@link TemplateLink} within a given {@link Connector}, linking
     * the given events from the "left" and "right" {@link TemplateComponent}s,
     * correspondingly. If the given {@link Connector} is not a part of the template
     * diagram, the method does nothing and returns <code>null</code>.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to distinguish
     * between the linked components.
     * 
     * @param c          the connector to include the new {@link TemplateLink}
     * @param leftEvent  the event (from the "left" {@link TemplateComponent}) to be
     *                   linked
     * @param rightEvent the event (from the "right" {@link TemplateComponent}) to
     *                   be linked
     * @return the newly created {@link TemplateLink}, or <code>null</code> if the
     *         given connector is not a part of the template diagram
     */
    public TemplateLink createLink(Connector c, String leftEvent, String rightEvent) {
        if (!connectors.contains(c)) {
            return null;
        }
        model.removeSubscriber((TemplateModelSubscriber) this);
        TemplateLink link;
        try {
            link = model.createLink(c.getLeftEntity().getComponent().getId(),
                    c.getRightEntity().getComponent().getId());
            link.setLeftEventName(leftEvent);
            link.setRightEventName(rightEvent);
            c.addLink(link);
            link2Connector.put(link, c);
            updateEmptyConnectorList();
        } finally {
            model.addSubscriber((TemplateModelSubscriber) this);
        }
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_MODIFY));
        return link;
    }

    /**
     * Add a given {@link TemplateLink} to a given {@link Connector}. If the given
     * {@link Connector} is not a part of the template diagram, the method does
     * nothing.
     * 
     * @param c    the {@link Connector} to which to add the {@link TemplateLink}
     * @param link the {@link TemplateLink} to be added to the {@link Connector}
     * @throws InconsistentModificationException if the {@link TemplateLink} to be
     *                                           added does not link the same
     *                                           {@link TemplateComponent}s linked
     *                                           by the {@link Connector}
     */
    public void addLink(Connector c, TemplateLink link) {
        if (!connectors.contains(c)) {
            return;
        }
        model.removeSubscriber((TemplateModelSubscriber) this);
        try {
            model.addLink(link);
            c.addLink(link);
            link2Connector.put(link, c);
            updateEmptyConnectorList();
        } finally {
            model.addSubscriber((TemplateModelSubscriber) this);
        }
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Remove a given {@link TemplateLink} from a given {@link Connector}. If the
     * given {@link Connector} is not a part of the template diagram, or if the
     * {@link TemplateLink} is not contained in the {@link Connector}, the method
     * does nothing.
     * 
     * @param c    the {@link Connector} from which to remove the
     *             {@link TemplateLink}
     * @param link the {@link TemplateLink} to remove from the {@link Connector}
     */
    public void removeLink(Connector c, TemplateLink link) {
        if (!connectors.contains(c)) {
            return;
        }
        model.removeSubscriber((TemplateModelSubscriber) this);
        try {
            link2Connector.remove(link);
            c.removeLink(link);
            model.removeLink(link.getId());
            updateEmptyConnectorList();
        } finally {
            model.addSubscriber((TemplateModelSubscriber) this);
        }
        fireDiagramChanged(new TemplateDiagramMessage(this, Arrays.asList(new DiagramElement[] { c }),
                TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Retrieve a collection of all {@link Connector}s in the template diagram.
     * 
     * @return a collection of all {@link Connector}s in the template diagram
     */
    public Collection<Connector> getConnectors() {
        return new HashSet<Connector>(connectors);
    }

    /**
     * Retrieve the {@link Connector} which contains the given point. If there is
     * more than one {@link Connector} which contains the point, returns one of
     * these {@link Connector}s arbitrarily. If there is no {@link Connector} which
     * contains the point, returns <code>null</code>.
     * 
     * @param location the point to be used to locate the {@link Connector}
     * @return one of the {@link Connector}s which contain the point;
     *         <code>null</code> if no {@link Connector} contains the point
     */
    public Connector getConnectorAt(Point location) {
        for (Connector c : connectors) {
            if (c.contains(location)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Retrieve the bounds of the template diagram (the smallest rectangle which
     * contains all elements in the template diagram). If the template diagram is
     * empty, returns a rectangle at position (0,0) and dimensions of 0.
     * 
     * @return the bounds of the template diagram (the smallest rectangle which
     *         contains all elements in the template diagram), or a rectangle at
     *         position (0,0) and dimensions of 0 if the template diagram is empty
     */
    public Rectangle getBounds() {
        Rectangle bounds = getAnyElement() != null ? getAnyElement().getBounds() : new Rectangle();
        for (Entity module : entities) {
            bounds = bounds.union(module.getBounds());
        }
        for (Connector c : getConnectors()) {
            bounds = bounds.union(c.getBounds());
        }
        return bounds;
    }

    /**
     * Returns an arbitrary element from the template diagram, if one exists. If the
     * template diagram is empty, returns <code>null</code>.
     * 
     * @return an arbitrary element from the template diagram, or <code>null</code>
     *         if the template diagram is empty
     */
    protected DiagramElement getAnyElement() {
        if (!entities.isEmpty()) {
            return entities.iterator().next();
        }
        return null;
    }

    /**
     * Translates all elements in the diagram with the given displacement.
     * 
     * @param delta the displacement to be used for the translation
     */
    public void translate(Point delta) {
        for (Entity e : entities) {
            e.translate(delta);
        }
        for (Connector c : getConnectors()) {
            c.translate(delta);
        }
        model.metadataChanged();
        Set<DiagramElement> elements = new HashSet<DiagramElement>();
        elements.addAll(entities);
        elements.addAll(connectors);
        fireDiagramChanged(new TemplateDiagramMessage(this, elements, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Translates the given diagram elements with the given displacement and updates
     * the rest of the diagram elements to reflect the change.
     * 
     * @param elements the diagram elements to be translated
     * @param delta    the displacement to be used for the translation
     */
    public void translate(Collection<DiagramElement> elements, Point delta) {
        for (DiagramElement element : elements) {
            element.translate(delta);
        }
        for (DiagramElement element : elements) {
            if (element instanceof Entity) {
                for (Connector c : getAdjacentConnectors((Entity) element)) {
                    c.update();
                }
            }
        }
        model.metadataChanged();
        fireDiagramChanged(new TemplateDiagramMessage(this, elements, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Notifies listeners for changes to this template diagram that the given
     * {@link DiagramElement}s were translated, and adds the {@link UndoableEdit}
     * for the translation to the undo stack.
     * <p>
     * This method is to be called only if diagram elements are translated without
     * invoking the {@link #translate(Point)} and
     * {@link #translate(Collection, Point)} methods (e.g., when dragging elements
     * with the mouse), once after all related translations are complete (e.g., at
     * the end of mouse dragging).
     * 
     * @param elements the translated {@link DiagramElement}s
     * @param delta    the displacement of the translation
     */
    public void commitTranslation(Collection<DiagramElement> elements, Point delta) {
        new DiagramActions.MovedSelectionAction(this, elements, delta).execute();
        model.metadataChanged();
        fireDiagramChanged(new TemplateDiagramMessage(this, elements, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Render the template diagram in the given graphical context, disregarding the
     * inconsistency setting. I.e., inconsistent diagram elements should be rendered
     * as if they were consistent.
     * 
     * @param g2d the graphical context where the diagram element has to be rendered
     */
    public void draw(Graphics2D g2d) {
        draw(g2d, false);
    }

    /**
     * Render the diagram element in the given graphical context, according to the
     * choice of differentiating or not inconsistent diagram elements.
     * 
     * @param g2d               the graphical context where the diagram element has
     *                          to be rendered
     * @param showInconsistency choice for rendering inconsistent diagram elements
     *                          differently or not; if <code>true</code>,
     *                          inconsistent diagram elements have to be rendered
     *                          differently; if<code>false</code>, inconsistent
     *                          diagram elements should be rendered as if they were
     *                          consistent
     */
    public void draw(Graphics2D g2d, boolean showInconsistency) {
        g2d.setFont(DiagramElement.getGlobalFont());
        for (Connector c : getConnectors()) {
            c.draw(g2d, showInconsistency);
        }
        for (Entity e : entities) {
            e.draw(g2d, showInconsistency);
        }
    }

    /**
     * Set which {@link DiagramElement}s are selected.
     * 
     * @param selection the new collection of selected {@link DiagramElement}s (can
     *                  be empty)
     */
    public void setSelection(Collection<DiagramElement> selection) {
        if (this.selection.containsAll(selection) && selection.containsAll(this.selection)) {
            return;
        }
        for (DiagramElement element : this.selection) {
            element.setSelected(false);
        }
        this.selection.clear();
        this.selection.addAll(selection);
        for (DiagramElement element : this.selection) {
            element.setSelected(true);
        }
        fireDiagramSelectionChanged(new TemplateDiagramMessage(this, this.selection, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Retrieve the collection of currently selected {@link DiagramElement}s.
     * 
     * @return the collection of currently selected {@link DiagramElement}s
     */
    public Collection<DiagramElement> getSelection() {
        return new HashSet<DiagramElement>(selection);
    }

    /**
     * Deselect all {@link DiagramElement}s in the template diagram.
     */
    public void clearSelection() {
        if (selection.isEmpty()) {
            return;
        }
        for (DiagramElement element : selection) {
            element.setSelected(false);
        }
        selection.clear();
        fireDiagramSelectionChanged(new TemplateDiagramMessage(this, selection, TemplateDiagramMessage.OP_MODIFY));
    }

    /**
     * Retrieve the {@link Connector} which contains the given {@link TemplateLink}.
     * 
     * @param link the {@link TemplateLink} whose {@link Connector} is to be
     *             retrieved
     * @return the {@link Connector} which contains the given {@link TemplateLink},
     *         or <code>null</code> if no {@link Connector} in the template diagram
     *         contains the given {@link TemplateLink}
     */
    public Connector getConnectorFor(TemplateLink link) {
        return link2Connector.get(link);
    }

    /**
     * Retrieve the {@link Entity} which represents the given
     * {@link TemplateComponent}.
     * 
     * @param component the {@link TemplateComponent} whose {@link Entity} has to be
     *                  retrieved
     * @return the {@link Entity} which represents the given
     *         {@link TemplateComponent}, or <code>null</code> if no {@link Entity}
     *         in the template diagram represents the given
     *         {@link TemplateComponent}
     */
    public Entity getEntityFor(TemplateComponent component) {
        return component2Entity.get(component);
    }

    /**
     * Retrieve the {@link Entity} which represents a {@link TemplateComponent}
     * associated with the given {@link FSAModel}.
     * 
     * @param fsa the {@link FSAModel} to be used to retrieve the {@link Entity}
     * @return the {@link Entity} which represents a {@link TemplateComponent}
     *         associated with the given {@link FSAModel}, or <code>null</code> if
     *         no {@link Entity} in the template diagram represents a
     *         {@link TemplateComponent} which is associated with the given
     *         {@link FSAModel}
     */
    public Entity getEntityWithFSA(FSAModel fsa) {
        for (Entity e : entities) {
            if (e.getComponent().getModel() == fsa) {
                return e;
            }
        }
        return null;
    }

    /**
     * Update the list of empty connectors in the template diagram (i.e.,
     * {@link Connector}s which do not contain any {@link TemplateLink}s), and add
     * the information to the underlying {@link TemplateModel} as a layout
     * annotation. Such an additional annotation is necessary as no
     * {@link TemplateLink}s will carry the information about the empty connectors.
     */
    protected void updateEmptyConnectorList() {
        EmptyConnectorSet emptyConnectors = new EmptyConnectorSet();
        for (Connector c : connectors) {
            if (c.getLinks().isEmpty()) {
                emptyConnectors.add(new EmptyConnector(c.getLeftEntity().getComponent().getId(),
                        c.getRightEntity().getComponent().getId()));
            }
        }
        model.setAnnotation(EmptyConnectorSet.KEY, emptyConnectors);
    }

    /**
     * Flags the {@link TemplateComponent} associated with the {@link FSAModel}
     * (since the {@link FSAModel} has been modified).
     */
    public void fsaEventSetChanged(FSAMessage arg0) {
        flagModel(arg0.getSource());
    }

    /**
     * Flags the {@link TemplateComponent} associated with the {@link FSAModel}
     * (since the {@link FSAModel} has been modified).
     */
    public void fsaStructureChanged(FSAMessage arg0) {
        flagModel(arg0.getSource());
    }

    /**
     * Flags the {@link TemplateComponent} associated with the given
     * {@link FSAModel}. If no {@link TemplateComponent} is associated with the
     * given {@link FSAModel}, the method does nothing.
     */
    protected void flagModel(FSAModel model) {
        TemplateComponent component = FSA2component.get(model);
        if (component != null) {
            component.getModel().setAnnotation(Entity.FLAG_MARK, new Object());
            Entity entity = getEntityFor(component);
            if (entity != null) {
                entity.update();
            }
        }
    }
}

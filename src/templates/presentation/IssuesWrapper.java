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

package templates.presentation;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ides.api.core.Hub;
import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.actions.DiagramActions;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.Validator;
import templates.model.Validator.ValidatorResult;

/**
 * Processes the consistency issues produced by a {@link Validator} and wraps
 * them into {@link IssueDescriptor}s.
 * 
 * @author Lenko Grigorov
 */
public class IssuesWrapper {
    /**
     * Action to change the type ({@link TemplateComponent#TYPE_MODULE} or
     * {@link TemplateComponent#TYPE_CHANNEL}) of a template component.
     * 
     * @author Lenko Grigorov
     */
    protected static class ConvertAction extends AbstractAction {
        private static final long serialVersionUID = 1042009036397461571L;

        /**
         * The template diagram which contains the template component.
         */
        protected TemplateDiagram diagram;

        /**
         * The {@link Entity} for the template component.
         */
        protected Entity entity;

        /**
         * The new type ({@link TemplateComponent#TYPE_MODULE} or
         * {@link TemplateComponent#TYPE_CHANNEL}) of the component.
         */
        protected int type;

        /**
         * Construct a new action to change the type of a given template component.
         * 
         * @param label   the label for the action
         * @param diagram the template diagram which contains the template component
         * @param entity  the entity for the template component
         * @param type    the new type ({@link TemplateComponent#TYPE_MODULE} or
         *                {@link TemplateComponent#TYPE_CHANNEL}) of the component
         */
        public ConvertAction(String label, TemplateDiagram diagram, Entity entity, int type) {
            super(label);
            this.diagram = diagram;
            this.entity = entity;
            this.type = type;
        }

        /**
         * Perform the change of type.
         */
        public void actionPerformed(ActionEvent e) {
            new DiagramActions.SetTypeAction(diagram, entity, type).execute();
        }
    }

    /**
     * Action to relabel an entity.
     * 
     * @author Lenko Grigorov
     */
    protected static class RenameAction extends AbstractAction {
        private static final long serialVersionUID = -8501367478990804882L;

        /**
         * The entity to be relabelled.
         */
        protected Entity entity;

        /**
         * Construct a new action to relabel the given entity.
         * 
         * @param entity the entity to be relabelled
         */
        public RenameAction(Entity entity) {
            super(Hub.string("TD_comLabelEntity"));
            this.entity = entity;
        }

        /**
         * Ask the user for the new label and then relabel the entity.
         * 
         * @see EntityLabellingDialog
         */
        public void actionPerformed(ActionEvent e) {
            TemplateConsistencyCanvas canvas = Hub.getWorkspace()
                    .getPresentationsOfType(TemplateConsistencyCanvas.class).iterator().next();
            new UIActions.LabelAction(canvas, entity).actionPerformed(e);
        }
    }

    /**
     * Action to update the linking of events between the template components
     * connected by a {@link Connector}.
     * 
     * @author Lenko Grigorov
     */
    protected static class SetEventsAction extends AbstractAction {
        private static final long serialVersionUID = -6787216038250603204L;

        /**
         * The {@link Connector} connecting the template components between which the
         * event links will be updated.
         */
        protected Connector connector;

        /**
         * Construct a new action to update the event links between the template
         * components connected by the given connector.
         * 
         * @param connector the connector connecting the template components between
         *                  which the event links should be updated
         */
        public SetEventsAction(Connector connector) {
            super(Hub.string("TD_comAssignEvents"));
            this.connector = connector;
        }

        /**
         * Display the UI dialog for event linking to enable the modification of the
         * linking of events between the template components connected by the connector
         * given at initialization.
         * 
         * @see EventLinksDialog
         */
        public void actionPerformed(ActionEvent e) {
            TemplateConsistencyCanvas canvas = Hub.getWorkspace()
                    .getPresentationsOfType(TemplateConsistencyCanvas.class).iterator().next();
            new UIActions.EventLinksAction(canvas, connector).actionPerformed(e);
        }
    }

    /**
     * Action to assign an FSA model to a template component.
     * 
     * @author Lenko Grigorov
     */
    protected static class AssignFSAAction extends AbstractAction {
        private static final long serialVersionUID = -7713091978419119233L;

        /**
         * The entity for the template component to which an FSA model will be assigned.
         */
        protected Entity entity;

        /**
         * Construct a new action to assign an FSA model to the given template
         * component.
         * 
         * @param entity the entity for the template component to which an FSA model
         *               should be assigned
         */
        public AssignFSAAction(Entity entity) {
            super(Hub.string("TD_comAssignFSA"));
            this.entity = entity;
        }

        /**
         * Display the FSA model assignment dialog to let the user assign a new model to
         * the template component.
         * 
         * @see AssignFSADialog
         */
        public void actionPerformed(ActionEvent e) {
            TemplateConsistencyCanvas canvas = Hub.getWorkspace()
                    .getPresentationsOfType(TemplateConsistencyCanvas.class).iterator().next();
            new UIActions.AssignFSAAction(canvas, entity).actionPerformed(e);
        }
    }

    /**
     * Assemble a list of {@link IssueDescriptor}s for the consistency issues in a
     * {@link TemplateDiagram}.
     * 
     * @param diagram the template diagram whose consistency issues should be
     *                assembled
     * @return a list of {@link IssueDescriptor}s for the consistency issues in the
     *         template diagram
     * @see Validator
     */
    public static List<IssueDescriptor> getIssues(TemplateDiagram diagram) {
        LinkedList<IssueDescriptor> issues = new LinkedList<IssueDescriptor>();
        List<ValidatorResult> results = Validator.validate(diagram.getModel());

        Set<Connector> moduleChannels = new HashSet<Connector>();
        Set<Connector> noEvents = new HashSet<Connector>();

        for (Connector c : diagram.getConnectors()) {
            if (c.getLinks().isEmpty()) {
                issues.add(new IssueDescriptor(describe(c) + "\n" + Hub.string("TD_issueWNoLinks"),
                        IssueDescriptor.TYPE_ERROR, Arrays.asList(new DiagramElement[] { c }),
                        Arrays.asList(new Action[] { new SetEventsAction(c) })));
            }
        }
        for (ValidatorResult result : results) {
            Set<DiagramElement> elements = new HashSet<DiagramElement>();
            String message = "";
            if (!result.components.isEmpty()) {
                message += describe(diagram.getEntityFor(result.components.get(0))) + "\n";
            } else if (!result.links.isEmpty()) {
                message += describe(diagram.getConnectorFor(result.links.get(0))) + "\n";
            }
            int type = result.type == ValidatorResult.WARNING ? IssueDescriptor.TYPE_WARNING
                    : IssueDescriptor.TYPE_ERROR;
            for (TemplateComponent component : result.components) {
                elements.add(diagram.getEntityFor(component));
            }
            for (TemplateLink link : result.links) {
                elements.add(diagram.getConnectorFor(link));
            }
            List<Action> fixes = new LinkedList<Action>();
            try {
                if (result.message.equals(Validator.ERROR_MODULE_CHANNEL)) {
                    Connector c = diagram.getConnectorFor(result.links.get(0));
                    if (c == null) {
                        message += result.message;
                    } else if (!moduleChannels.contains(c)) {
                        moduleChannels.add(c);
                        message += Hub.string("TD_issueWModuleChannel") + "\n";
                        if (c.getLeftEntity().getComponent().getType() == TemplateComponent.TYPE_MODULE) {
                            message += Hub.string("TD_issueWConverChannel");
                            fixes.add(
                                    new ConvertAction(Hub.string("TD_fixConvert") + " " + c.getLeftEntity().getLabel(),
                                            diagram, c.getLeftEntity(), TemplateComponent.TYPE_CHANNEL));
                            fixes.add(
                                    new ConvertAction(Hub.string("TD_fixConvert") + " " + c.getRightEntity().getLabel(),
                                            diagram, c.getRightEntity(), TemplateComponent.TYPE_CHANNEL));
                        } else {
                            message += Hub.string("TD_issueWConverModule");
                            fixes.add(
                                    new ConvertAction(Hub.string("TD_fixConvert") + " " + c.getLeftEntity().getLabel(),
                                            diagram, c.getLeftEntity(), TemplateComponent.TYPE_MODULE));
                            fixes.add(
                                    new ConvertAction(Hub.string("TD_fixConvert") + " " + c.getRightEntity().getLabel(),
                                            diagram, c.getRightEntity(), TemplateComponent.TYPE_MODULE));
                        }
                    } else {
                        throw new SkipIssueException();
                    }
                } else if (result.message.equals(Validator.WARNING_FREE_EVENT)) {
                    Entity e = diagram.getEntityFor(result.components.get(0));
                    if (e == null) {
                        message += result.message;
                    } else {
                        message += Hub.string("TD_issueWFreeEvent");
                    }
                } else if (result.message.equals(Validator.ERROR_NO_EVENT)) {
                    Connector c = diagram.getConnectorFor(result.links.get(0));
                    if (c == null) {
                        message += result.message;
                    } else if (!noEvents.contains(c)) {
                        noEvents.add(c);
                        message += Hub.string("TD_issueWNoEvent");
                        fixes.add(new SetEventsAction(c));
                    } else {
                        throw new SkipIssueException();
                    }
                } else if (result.message.equals(Validator.WARNING_FREE_COMPONENT)) {
                    Entity e = diagram.getEntityFor(result.components.get(0));
                    if (e == null) {
                        message += result.message;
                    } else if (diagram.getAdjacentConnectors(e).isEmpty()) {
                        message += Hub.string("TD_issueWFreeComponent");
                    } else {
                        throw new SkipIssueException();
                    }
                } else if (result.message.equals(Validator.WARNING_NO_CHANNEL)) {
                    message += Hub.string("TD_issueWNoChannel");
                } else if (result.message.equals(Validator.ERROR_FORKED_EVENT)) {
                    Entity e = diagram.getEntityFor(result.components.get(0));
                    if (e == null) {
                        message += result.message;
                    } else {
                        message += Hub.string("TD_issueWForkedEvent1") + " \'" + result.event + "\' "
                                + Hub.string("TD_issueWForkedEvent2");
                    }
                } else if (result.message.equals(Validator.ERROR_MERGED_EVENT)) {
                    if (result.components.size() < 2) {
                        message += result.message;
                    } else {
                        Entity e = diagram.getEntityFor(result.components.get(1));
                        message += Hub.string("TD_issueWMergedEvent1") + " \'" + result.event + "\' "
                                + Hub.string("TD_issueWMergedEvent2") + " \'" + e.getLabel() + "\'.";
                    }
                } else if (result.message.equals(Validator.ERROR_NO_MODULE)) {
                    message += Hub.string("TD_issueWNoModule");
                } else if (result.message.equals(Validator.ERROR_NO_MODEL)) {
                    Entity e = diagram.getEntityFor(result.components.get(0));
                    if (e == null) {
                        message += result.message;
                    } else {
                        message += Hub.string("TD_issueWNoModel");
                        fixes.add(new AssignFSAAction(e));
                    }
                } else if (result.message.equals(Validator.ERROR_NONUNIQUE_NAME)) {
                    Entity e = diagram.getEntityFor(result.components.get(0));
                    if (e == null) {
                        message += result.message;
                    } else {
                        message += Hub.string("TD_issueWNonuniqueName") + "\n" + Hub.string("TD_issueWRenameEntity");
                        // fixes.add(new RenameAction(e));
                    }
                } else {
                    message += result.message;
                }
            } catch (SkipIssueException e) {
                continue;
            }
            issues.add(new IssueDescriptor(message, type, elements, fixes));
        }
        return issues;
    }

    protected static String describe(Connector c) {
        if (c == null) {
            return "";
        }
        return Hub.string("TD_describeConnector1") + " \'" + c.getLeftEntity().getLabel() + "\' "
                + Hub.string("TD_describeConnector2") + " \'" + c.getRightEntity().getLabel() + "\'";
    }

    protected static String describe(Entity e) {
        if (e == null) {
            return "";
        }
        return Hub.string("TD_describeEntity") + " \'" + e.getLabel() + "\'";
    }
}

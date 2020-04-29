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

package templates.presentation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.utilities.EscapeDialog;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.library.Template;
import templates.library.TemplateDescriptor;
import templates.library.TemplateManager;
import templates.model.TemplateComponent;
import templates.utils.EntityIcon;

/**
 * The UI dialog which allows the user to assign an FSA model to a component in
 * a template design.
 * 
 * @author Lenko Grigorov
 */
public class AssignFSADialog extends EscapeDialog {
    private static final long serialVersionUID = 2530576123753377680L;

    /**
     * Singleton instance.
     */
    private static AssignFSADialog me = null;

    /**
     * The {@link Entity} to whose {@link TemplateComponent} a new FSA model will be
     * assigned.
     */
    protected static Entity entity = null;

    /**
     * The canvas which contains the entity to whose {@link TemplateComponent} a new
     * FSA model will be assigned.
     */
    protected static TemplateEditableCanvas canvas = null;

    /**
     * Listener for the <code>Enter</code> key. This event will be interpreted as
     * the closing of the FSA model assignment dialog by the user.
     */
    protected Action enterListener = new AbstractAction() {
        private static final long serialVersionUID = 4258152153714537489L;

        /**
         * Close the FSA model assignment dialog.
         */
        public void actionPerformed(ActionEvent actionEvent) {
            canvas.setUIInteraction(false);
            setVisible(false);
        }
    };

    /**
     * Handler of focus for the main window of IDES. When the user clicks outside
     * the FSA model assignment dialog (i.e., the main window gets activated),
     * cancel the model assignment and close.
     */
    protected static WindowListener cancelOnFocusLost = new WindowListener() {
        /**
         * When the main window of IDES is activated because the user clicked on it,
         * cancel the FSA model assignment and close the FSA model assignment dialog.
         */
        public void windowActivated(WindowEvent arg0) {
            if (arg0.getOppositeWindow() != null && !Hub.getUserInterface().isWindowActivationAfterNoticePopup(arg0)) {
                instance().onEscapeEvent();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        instance().requestFocus();
                    }
                });
            }
        }

        /**
         * Do nothing.
         */
        public void windowClosed(WindowEvent arg0) {
        }

        /**
         * Do nothing.
         */
        public void windowClosing(WindowEvent arg0) {
        }

        /**
         * Do nothing.
         */
        public void windowDeactivated(WindowEvent arg0) {
        }

        /**
         * Do nothing.
         */
        public void windowDeiconified(WindowEvent arg0) {
        }

        /**
         * Do nothing.
         */
        public void windowIconified(WindowEvent arg0) {
        }

        /**
         * Do nothing.
         */
        public void windowOpened(WindowEvent arg0) {
        }
    };

    /**
     * Listener for the selection of an FSA model from the combo box with FSA
     * models.
     */
    protected static ActionListener onSelectModel = new ActionListener() {
        /**
         * Assign a copy of the selected FSA model to the {@link TemplateComponent} of
         * the {@link Entity} for which this dialog was opened.
         */
        public void actionPerformed(ActionEvent e) {
            if (!(openModelsCombo.getSelectedItem() instanceof FSACell)) {
                return;
            }
            FSACell cell = (FSACell) openModelsCombo.getSelectedItem();
            FSAModel fsa = cell.fsa.clone();
            me.onEscapeEvent();
            EntityIcon icon = null;
            if (cell.icon != null && (cell.icon instanceof EntityIcon)) {
                icon = (EntityIcon) cell.icon;
                if (cell.fsa.hasAnnotation(Entity.FLAG_MARK)) {
                    fsa.setAnnotation(Entity.FLAG_MARK, new Object());
                }
            }
            new DiagramActions.AssignFSAAction(canvas.getDiagram(), entity, fsa, icon).execute();
        }
    };

    /**
     * Listener for the selection of a template from the combo box with templates.
     */
    protected static ActionListener onSelectTemplate = new ActionListener() {
        /**
         * Assign a new instance of the selected template to the
         * {@link TemplateComponent} of the {@link Entity} for which this dialog was
         * opened.
         */
        public void actionPerformed(ActionEvent e) {
            if (!(templatesCombo.getSelectedItem() instanceof TemplateCell)) {
                return;
            }
            Template template = ((TemplateCell) templatesCombo.getSelectedItem()).template;
            FSAModel fsa = template.instantiate();
            me.onEscapeEvent();
            new DiagramActions.AssignFSAAction(canvas.getDiagram(), entity, fsa, template.getIcon().clone()).execute();
        }
    };

    /**
     * Action to assign an empty FSA model to the {@link TemplateComponent} of the
     * {@link Entity} for which this dialog was opened.
     * 
     * @author Lenko Grigorov
     */
    protected static class NewFSAAction extends AbstractAction {
        private static final long serialVersionUID = 8824881153311968903L;

        /**
         * The icon associated with the action.
         */
        private static ImageIcon icon = new ImageIcon();

        /**
         * Set up the action.
         */
        public NewFSAAction() {
            super(Hub.string("TD_comAssignNewFSA"), icon);
            icon.setImage(Toolkit.getDefaultToolkit()
                    .createImage(Hub.getLocalResource(AssignFSADialog.class, "images/icons/new_automaton.gif")));
            putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAssignNewFSA"));
        }

        /**
         * Assign an empty FSA model to the {@link TemplateComponent} of the
         * {@link Entity} for which this dialog was opened.
         */
        public void actionPerformed(ActionEvent arg0) {
            me.onEscapeEvent();
            FSAModel[] model = new FSAModel[1];
            new DiagramActions.AssignNewFSAAction(canvas.getDiagram(), entity, model).execute();
        }
    }

    /**
     * Component to display an {@link FSAModel} as an item in the combo box with FSA
     * models.
     * 
     * @author Lenko Grigorov
     */
    protected static class FSACell extends JLabel {
        private static final long serialVersionUID = -3672946556518152880L;

        /**
         * The {@link FSAModel} which is displayed.
         */
        public FSAModel fsa;

        /**
         * The icon for the FSA model.
         */
        public Icon icon = null;

        /**
         * Construct a component to display an empty icon and the name of the given FSA
         * model.
         * 
         * @param fsa the {@link FSAModel} to be displayed as an item
         */
        public FSACell(FSAModel fsa) {
            super(fsa.getName());
            this.fsa = fsa;
            setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
        }

        /**
         * Construct a component to display the given icon and the name of the given FSA
         * model.
         * 
         * @param fsa  the {@link FSAModel} to be displayed as an item
         * @param icon the icon to be displayed
         */
        public FSACell(FSAModel fsa, Icon icon) {
            super(icon);
            this.icon = icon;
            setText(fsa.getName());
            this.fsa = fsa;
            setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
        }
    }

    /**
     * Component to display a {@link Template} as an item in the combo box with
     * templates.
     * 
     * @author Lenko Grigorov
     */
    protected static class TemplateCell extends JLabel {
        private static final long serialVersionUID = -8477312997483571474L;

        /**
         * The {@link Template} which is displayed.
         */
        public Template template;

        /**
         * Construct a component to display the icon and name of the given template.
         * 
         * @param template the {@link Template} to be displayed as an item
         */
        public TemplateCell(Template template) {
            super(template.getIcon());
            setText(TemplateDescriptor.shortDescription(template.getDescription()));
            this.template = template;
            setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
        }
    }

    /**
     * Renderer of {@link JLabel}s when shown as items in a list. Used to render the
     * items in the combo boxes with FSA models and templates.
     * 
     * @see FSACell
     * @see TemplateCell
     * @author Lenko Grigorov
     */
    protected class JLabelListRenderer extends Box implements ListCellRenderer {
        private static final long serialVersionUID = -4858000916109104619L;

        /**
         * A default {@link JLabel} used to contain the string description of a rendered
         * item in case the item is not a {@link JLabel}.
         */
        protected JLabel defaultLabel = new JLabel();

        /**
         * Initialize the renderer.
         */
        public JLabelListRenderer() {
            super(BoxLayout.X_AXIS);
        }

        /**
         * Retrieve the {@link JLabel} for the list item.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label;
            if (!(value instanceof JLabel)) {
                defaultLabel.setText(value != null ? value.toString() : "");
                label = defaultLabel;
            } else {
                label = (JLabel) value;
            }
            if (isSelected) {
                setBackground(SystemColor.textHighlight);
                setOpaque(true);
            } else {
                setBackground(SystemColor.control);
                setOpaque(false);
            }
            removeAll();
            add(label);
            add(Box.createHorizontalGlue());
            return this;
        }
    }

    /**
     * Construct and set up the FSA model assignment dialog.
     */
    private AssignFSADialog() {
        super(Hub.getMainWindow(), Hub.string("TD_assignFSATitle"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onEscapeEvent();
            }
        });
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(300, 10));

        Box mainBox = Box.createHorizontalBox();
        mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Box newBox = Box.createVerticalBox();
        newBox.add(new JLabel(" "));
        newBox.add(new JButton(new NewFSAAction()));
        mainBox.add(newBox);

        mainBox.add(Box.createRigidArea(new Dimension(5, 0)));

        openModelsCombo = new JComboBox();
        openModelsCombo.setRenderer(new JLabelListRenderer());
        Box modelsBox = Box.createVerticalBox();
        Box titleBox = Box.createHorizontalBox();
        titleBox.add(new JLabel(Hub.string("TD_openModels")));
        titleBox.add(Box.createHorizontalGlue());
        modelsBox.add(titleBox);
        modelsBox.add(openModelsCombo);
        mainBox.add(modelsBox);

        mainBox.add(Box.createRigidArea(new Dimension(5, 0)));

        templatesCombo = new JComboBox();
        templatesCombo.setRenderer(new JLabelListRenderer());
        Box templatesBox = Box.createVerticalBox();
        titleBox = Box.createHorizontalBox();
        titleBox.add(new JLabel(Hub.string("TD_templates")));
        titleBox.add(Box.createHorizontalGlue());
        templatesBox.add(titleBox);
        templatesBox.add(templatesCombo);
        mainBox.add(templatesBox);

        getContentPane().add(mainBox);

    }

    /**
     * Access the singleton instance of the dialog for the assignment of an FSA
     * model.
     * 
     * @return the singleton instance of the dialog
     */
    public static AssignFSADialog instance() {
        if (me == null) {
            me = new AssignFSADialog();
        }
        return me;
    }

    /**
     * @throws RuntimeException cloning is not allowed
     */
    @Override
    public Object clone() {
        throw new RuntimeException("Cloning of " + this.getClass().toString() + " not supported.");
    }

    /**
     * The combo box with FSA models.
     */
    protected static JComboBox openModelsCombo;

    /**
     * The combo box with templates.
     */
    protected static JComboBox templatesCombo;

    /**
     * Refresh the content of the combo boxes with FSA models and templates, display
     * the FSA model assignment dialog, and wait for user input.
     * 
     * @param canvas the canvas which contains the given entity
     * @param entity the {@link Entity} to whose {@link TemplateComponent} a new FSA
     *               model will be assigned
     */
    public static void showAndAssign(TemplateEditableCanvas canvas, Entity entity) {
        canvas.setUIInteraction(true);
        AssignFSADialog.canvas = canvas;
        AssignFSADialog.entity = entity;
        instance();

        // prepare ComboBox with loaded FSAs
        Set<FSAModel> openModels = new HashSet<FSAModel>(Hub.getWorkspace().getModelsOfType(FSAModel.class));
        Set<FSAModel> designModels = new HashSet<FSAModel>();
        for (TemplateComponent c : canvas.getDiagram().getModel().getComponents()) {
            if (c.hasModel()) {
                openModels.add(c.getModel());
                designModels.add(c.getModel());
            }
        }
        if (entity.getComponent().hasModel()) {
            openModels.remove(entity.getComponent().getModel());
        }
        Vector<FSAModel> sortedModels = new Vector<FSAModel>(openModels);
        Collections.sort(sortedModels, new Comparator<FSAModel>() {

            public int compare(FSAModel o1, FSAModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        openModelsCombo.removeActionListener(onSelectModel);
        for (FSAModel fsa : sortedModels) {
            if (designModels.contains(fsa)) {
                openModelsCombo.addItem(new FSACell(fsa, canvas.getDiagram().getEntityWithFSA(fsa).getIcon()));
            } else {
                openModelsCombo.addItem(new FSACell(fsa, new ImageIcon(fsa.getModelType().getIcon())));
            }
        }
        openModelsCombo.setSelectedIndex(-1);
        openModelsCombo.addActionListener(onSelectModel);

        // prepare ComboBox with templates
        Vector<Template> templates = new Vector<Template>(TemplateManager.instance().getMainLibrary().getTemplates());
        Collections.sort(templates, new Comparator<Template>() {

            public int compare(Template arg0, Template arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        templatesCombo.removeActionListener(onSelectTemplate);
        for (Template t : templates) {
            templatesCombo.addItem(new TemplateCell(t));
        }
        templatesCombo.setSelectedIndex(-1);
        templatesCombo.addActionListener(onSelectTemplate);

        me.pack();
        boolean hasOurListener = false;
        for (int i = 0; i < Hub.getMainWindow().getWindowListeners().length; ++i) {
            if (Hub.getMainWindow().getWindowListeners()[i] == cancelOnFocusLost) {
                hasOurListener = true;
            }
        }
        if (!hasOurListener) {
            Hub.getMainWindow().addWindowListener(cancelOnFocusLost);
        }
        Point p = new Point(entity.getLocation().x, entity.getLocation().y);
        p = canvas.localToScreen(p);
        if (p.x + me.getWidth() > Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
            p.x = p.x - me.getWidth();
        }
        if (p.y + me.getHeight() > Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            p.y = p.y - me.getHeight();
        }
        me.setLocation(p);
        me.setVisible(true);
    }

    /**
     * Called to cancel the assignment of an FSA model (e.g., when the user presses
     * the <code>Esc</code> key).
     */
    @Override
    public void onEscapeEvent() {
        Hub.getMainWindow().removeWindowListener(cancelOnFocusLost);
        openModelsCombo.removeActionListener(onSelectModel);
        openModelsCombo.removeAllItems();
        templatesCombo.removeActionListener(onSelectTemplate);
        templatesCombo.removeAllItems();
        canvas.setUIInteraction(false);
        setVisible(false);
    }

}

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

package templates.library;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import ides.api.core.Hub;
import ides.api.core.WorkspaceMessage;
import ides.api.core.WorkspaceSubscriber;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.presentation.Presentation;
import ides.api.utilities.GeneralUtils;
import templates.model.TemplateModel;

/**
 * The UI for the template library. Displays a list of the templates in the
 * library, as well as buttons to edit the content of the library.
 * 
 * @author Lenko Grigorov
 */
public class LibraryUI extends Box
        implements Presentation, TemplateLibraryListener, MouseMotionListener, MouseListener {
    private static final long serialVersionUID = -666343525812865685L;

    /**
     * The UI action to add a new template to the template library.
     * 
     * @author Lenko Grigorov
     */
    public static class AddTemplateAction extends AbstractAction {
        private static final long serialVersionUID = 1033418973771323762L;

        // private static ImageIcon icon = new ImageIcon();

        /**
         * Initialize the action.
         */
        public AddTemplateAction() {
            super(Hub.string("TD_comAddTemplate"));
            // icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
            // .getResource("images/icons/edit_delete.gif")));
            putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintAddTemplate"));
        }

        /**
         * Display the dialog for adding a new template.
         * 
         * @see AddTemplateDialog
         */
        public void actionPerformed(ActionEvent evt) {
            AddTemplateDialog.addTemplate(TemplateManager.instance().getMainLibrary());
        }
    }

    /**
     * The UI action to remove a template from the template library.
     * 
     * @author Lenko Grigorov
     */
    public class DeleteTemplateAction extends AbstractAction {
        private static final long serialVersionUID = -4293547969708851728L;

        // private static ImageIcon icon = new ImageIcon();

        /**
         * Initialize the action.
         */
        public DeleteTemplateAction() {
            super(Hub.string("TD_comDeleteTemplate"));
            // icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
            // .getResource("images/icons/edit_delete.gif")));
            putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintDeleteTemplate"));
        }

        /**
         * Remove the selected template. Asks for confirmation before removing.
         */
        public void actionPerformed(ActionEvent evt) {
            Object[] templates = list.getSelectedValues();
            if (JOptionPane.showConfirmDialog(Hub.getMainWindow(),
                    GeneralUtils.JOptionPaneKeyBinder.messageLabel(Hub.string("TD_confirmDelTemplate")),
                    Hub.string("TD_confirmDelTemplateTitle"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            String errors = "";
            for (Object template : templates) {
                try {
                    TemplateManager.instance().getMainLibrary().removeTemplate(((Template) template).getName());
                } catch (IOException e) {
                    errors += Hub.string("TD_errorRemovingTemplate") + " \'" + ((Template) template).getName() + "\' ["
                            + e.getMessage() + "]\n";
                }
            }
            if (!"".equals(errors)) {
                Hub.displayAlert(errors);
            }
        }
    }

    /**
     * The UI action to edit the properties of a template in the template library.
     * 
     * @author Lenko Grigorov
     */
    public class EditTemplateAction extends AbstractAction {
        private static final long serialVersionUID = -3546061467454314196L;

        // private static ImageIcon icon = new ImageIcon();

        /**
         * Initialize the action.
         */
        public EditTemplateAction() {
            super(Hub.string("TD_comEditTemplate"));
            // icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
            // .getResource("images/icons/edit_delete.gif")));
            putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintEditTemlate"));
        }

        /**
         * Display the dialog for the editing of the properties of the selected
         * template.
         * 
         * @see AddTemplateDialog
         */
        public void actionPerformed(ActionEvent evt) {
            Template template = (Template) list.getSelectedValue();
            if (template != null) {
                AddTemplateDialog.editTemplate(TemplateManager.instance().getMainLibrary(), template);
            }
        }
    }

    /**
     * The UI action to load the underlying model of a template into the workspace.
     * 
     * @author Lenko Grigorov
     */
    public class ViewTemplateAction extends AbstractAction {
        private static final long serialVersionUID = -7055962471632256236L;

        // private static ImageIcon icon = new ImageIcon();

        /**
         * Initialize the action.
         */
        public ViewTemplateAction() {
            super(Hub.string("TD_comViewTemplate"));
            // icon.setImage(Toolkit.getDefaultToolkit().createImage(Hub
            // .getResource("images/icons/edit_delete.gif")));
            putValue(SHORT_DESCRIPTION, Hub.string("TD_comHintViewTemplate"));
        }

        /**
         * Load the underlying model of the selected model into the workspace. If the
         * model is already loaded, simply activate the model.
         */
        public void actionPerformed(ActionEvent evt) {
            Template template = (Template) list.getSelectedValue();
            if (template != null) {
                if (template.getModel() != Hub.getWorkspace().getModel(template.getModel().getName())) {
                    TemplateUpdater updater = new TemplateUpdater(template.getName(), template.getModel(),
                            Hub.getIOSubsystem().getFileOfModel(template.getModel()));
                    template.getModel().addSubscriber(updater);
                    Hub.getWorkspace().addModel(template.getModel());
                    Hub.getWorkspace().addSubscriber(updater);
                }
                Hub.getWorkspace().setActiveModel(template.getModel().getName());
            }
        }
    }

    /**
     * Listener to respond to events while the underlying model of a template is
     * loaded in the workspace. If the user saves the template model in a different
     * file, the model loaded in the library has to be reloaded from the original
     * file.
     * 
     * @author Lenko Grigorov
     */
    private class TemplateUpdater implements DESModelSubscriber, WorkspaceSubscriber {
        /**
         * The "ID" of the template.
         */
        protected String template;

        /**
         * The underlying model of the template.
         */
        protected DESModel model;

        /**
         * The original file where the template is stored.
         */
        protected File file;

        /**
         * Create a new listener for the given template.
         * 
         * @param template the "ID" of the template
         * @param model    the underlying model of the template
         * @param file     the original file where the template is stored
         */
        public TemplateUpdater(String template, DESModel model, File file) {
            this.model = model;
            this.template = template;
            this.file = file;
        }

        /**
         * Do nothing.
         */
        public void modelNameChanged(DESModelMessage arg0) {
        }

        /**
         * If the underlying model was saved in a different file, stop listening to the
         * events from the model and reload the template from the original file.
         */
        public void saveStatusChanged(DESModelMessage arg0) {
            if (!file.equals(Hub.getIOSubsystem().getFileOfModel(arg0.getSource()))) {
                unsubscribeAndReload();
            }
        }

        /**
         * Do nothing.
         */
        public void aboutToRearrangeWorkspace() {
        }

        /**
         * If the underlying model was removed from the workspace, stop listening to the
         * events from the model.
         */
        public void modelCollectionChanged(WorkspaceMessage arg0) {
            boolean found = false;
            for (Iterator<DESModel> i = Hub.getWorkspace().getModels(); i.hasNext();) {
                if (model == i.next()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unsubscribeAndReload();
            }
        }

        /**
         * Do nothing.
         */
        public void modelSwitched(WorkspaceMessage arg0) {
        }

        /**
         * Do nothing.
         */
        public void repaintRequired() {
        }

        /**
         * Stop listening to events from the underlying model and the workspace and
         * reload the template from the original file. If changes to the underlying
         * model were saved, the reloaded template will contain these changes.
         */
        protected void unsubscribeAndReload() {
            model.removeSubscriber(this);
            Hub.getWorkspace().removeSubscriber(this);
            try {
                TemplateManager.instance().getMainLibrary().reloadTemplate(template);
            } catch (IOException e) {
                Hub.displayAlert(Hub.string("TD_errorReloadingTemplate") + " " + template + ".\n"
                        + GeneralUtils.truncateMessage(e.getMessage()));
            }
        }
    }

    /**
     * Renderer of templates when shown as items in a list. Displays the icons and
     * descriptions of templates. Used to render the templates in the template
     * library.
     * 
     * @author Lenko Grigorov
     */
    private static class TemplateListRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = -4843903577071299167L;

        /**
         * Initialize the renderer.
         */
        public TemplateListRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        /**
         * Retrieve the icon and description of the given template and set up the
         * rendering component.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            setBackground(SystemColor.text);
            if (value == null) {
                setText("");
            } else if (value instanceof Template) {
                setText(TemplateDescriptor.shortDescription(((Template) value).getDescription()));
                setIcon(((Template) value).getIcon());
            } else {
                setText(value.toString());
            }
            if (isSelected) {
                setBackground(SystemColor.textHighlight);
                setForeground(SystemColor.textHighlightText);

            } else {
                setBackground(SystemColor.text);
                setForeground(SystemColor.textText);
            }
            return this;
        }
    }

    /**
     * The list of templates in the template library.
     */
    protected JList list;

    /**
     * The {@link ListModel} used to display the list of templates.
     */
    protected DefaultListModel model;

    /**
     * The action to load the underlying model of a template into the workspace
     */
    protected ViewTemplateAction viewAction;

    /**
     * Set up the UI for the template library.
     */
    public LibraryUI() {
        super(BoxLayout.Y_AXIS);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list = new JList();
        list.setCellRenderer(new TemplateListRenderer());
        model = new DefaultListModel();
        updateList();
        list.setModel(model);
        TemplateManager.instance().getMainLibrary().addListener(this);
        list.addMouseMotionListener(this);
        list.addMouseListener(this);
        list.setTransferHandler(new LibraryTransferHandler());
        list.setDragEnabled(true);

        viewAction = new ViewTemplateAction();

        Box titleBox = Box.createHorizontalBox();
        titleBox.add(new JLabel(Hub.string("TD_avaliableTemplates")));
        titleBox.add(Box.createHorizontalGlue());
        add(titleBox);

        add(Box.createRigidArea(new Dimension(0, 5)));

        JScrollPane sp = new JScrollPane(list);
        add(sp);

        add(Box.createRigidArea(new Dimension(0, 5)));

        Box buttonBox = Box.createHorizontalBox();
        JButton addBut = new JButton(new AddTemplateAction());
        buttonBox.add(addBut);
        buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton delBut = new JButton(new DeleteTemplateAction());
        buttonBox.add(delBut);
        buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton editBut = new JButton(new EditTemplateAction());
        buttonBox.add(editBut);
        buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton viewBut = new JButton(viewAction);
        buttonBox.add(viewBut);
        add(buttonBox);
    }

    /**
     * Update the list of templates to be displayed according to the current content
     * of the template library.
     */
    protected void updateList() {
        Set<Template> templates = new TreeSet<Template>(new Comparator<Template>() {

            public int compare(Template o1, Template o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });
        for (Template t : TemplateManager.instance().getMainLibrary().getTemplates()) {
            templates.add(t);
        }
        model.removeAllElements();
        for (Template t : templates) {
            model.addElement(t);
        }
    }

    /**
     * Do nothing.
     */
    public void forceRepaint() {
    }

    public JComponent getGUI() {
        return this;
    }

    /**
     * The {@link LibraryUI} is a presentation shared by all {@link TemplateModel}s;
     * it is not model-dependent. This method simply returns the model currently
     * active in the workspace.
     */
    public DESModel getModel() {
        return Hub.getWorkspace().getActiveModel();
    }

    public String getName() {
        return Hub.string("TD_libraryTitle");
    }

    public void release() {
    }

    public void setTrackModel(boolean arg0) {
    }

    /**
     * Update the list of templates.
     */
    public void templateCollectionChanged(TemplateLibrary source) {
        updateList();
    }

    /**
     * Do nothing.
     */
    public void mouseDragged(MouseEvent arg0) {
    }

    /**
     * Set the tooltip to contain the description of a template when mouse moves
     * over this template in the list of template.
     */
    public void mouseMoved(MouseEvent arg0) {
        int idx = list.locationToIndex(arg0.getPoint());
        if (idx >= 0 && list.getCellBounds(idx, idx).contains(arg0.getPoint())) {
            Template t = (Template) model.getElementAt(idx);
            list.setToolTipText(t.getDescription());
        } else {
            list.setToolTipText(null);
        }
    }

    /**
     * Load the underlying model of a template when the user double-clicks this
     * template in the list of templates.
     */
    public void mouseClicked(MouseEvent arg0) {
        if (arg0.getClickCount() == 2) {
            int idx = list.locationToIndex(arg0.getPoint());
            if (idx >= 0 && list.getCellBounds(idx, idx).contains(arg0.getPoint())) {
                list.setSelectedIndex(idx);
                viewAction.actionPerformed(null);
            }
        }
    }

    /**
     * Do nothing.
     */
    public void mouseEntered(MouseEvent arg0) {
    }

    /**
     * Do nothing.
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /**
     * Do nothing.
     */
    public void mousePressed(MouseEvent arg0) {
    }

    /**
     * Do nothing.
     */
    public void mouseReleased(MouseEvent arg0) {
    }

}

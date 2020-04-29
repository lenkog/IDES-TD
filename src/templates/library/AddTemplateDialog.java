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

package templates.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESModel;
import ides.api.utilities.EscapeDialog;
import templates.model.TemplateComponent;
import templates.model.TemplateModel;

/**
 * The UI dialog for adding templates to the template library or for editing the
 * properties of existing templates. It lets the user select the FSA model to be
 * used as the base for the template, as well as the color, description, etc. of
 * the template.
 * 
 * @see #addTemplate(TemplateLibrary)
 * @see #addTemplate(TemplateLibrary, FSAModel)
 * @see #editTemplate(TemplateLibrary, Template)
 * @author Lenko Grigorov
 */
public class AddTemplateDialog extends EscapeDialog {
    private static final long serialVersionUID = -2871252921508560702L;

    /**
     * Singleton instance.
     */
    private static AddTemplateDialog me = null;

    /**
     * Set up the dialog for adding/editing templates.
     */
    private AddTemplateDialog() {
        super(Hub.getMainWindow(), Hub.string("TD_addTemplateTitle"), true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onEscapeEvent();
            }
        });
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(250, 350));

        Box mainBox = Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Box specBox = Box.createHorizontalBox();
        Box tagBox = Box.createHorizontalBox();
        tagBox.setBorder(BorderFactory.createTitledBorder(Hub.string("TD_tagBoxTitle")));
        tagField = new JTextField(5);
        tagField.setMaximumSize(new Dimension(tagField.getMaximumSize().width, tagField.getMinimumSize().height));
        tagBox.add(tagField);
        specBox.add(tagBox);
        specBox.add(Box.createRigidArea(new Dimension(10, 0)));
        Box colorBox = Box.createHorizontalBox();
        colorBox.setBorder(BorderFactory.createTitledBorder(Hub.string("TD_colorBoxTitle")));
        colorIcon = new ColorIcon();
        JButton colorButton = new JButton(colorIcon);
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = JColorChooser.showDialog(me, Hub.string("TD_colorChooserTitle"), colorIcon.getColor());
                if (newColor != null) {
                    colorIcon.setColor(newColor);
                }
            }
        });
        colorBox.add(colorButton);
        specBox.add(colorBox);
        mainBox.add(specBox);

        mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

        Box selectModelBox = Box.createHorizontalBox();
        selectModelBox.setBorder(BorderFactory.createTitledBorder(Hub.string("TD_modelBoxTitle")));
        modelsCombo = new JComboBox();
        modelsCombo.setMaximumSize(
                new Dimension(modelsCombo.getMaximumSize().width, modelsCombo.getPreferredSize().height));
        modelsCombo.setRenderer(new FSARenderer());
        selectModelBox.add(modelsCombo);
        mainBox.add(selectModelBox);

        mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

        Box descBox = Box.createHorizontalBox();
        descBox.setBorder(BorderFactory.createTitledBorder(Hub.string("TD_descBoxTitle")));
        descArea = new JTextArea();
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descBox.add(new JScrollPane(descArea));
        mainBox.add(descBox);

        mainBox.add(Box.createRigidArea(new Dimension(0, 5)));

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        commitButton = new JButton(Hub.string("TD_OK"));
        buttonBox.add(commitButton);
        buttonBox.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton cancelButton = new JButton(Hub.string("TD_cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEscapeEvent();
            }
        });
        buttonBox.add(cancelButton);
        buttonBox.add(Box.createHorizontalGlue());
        mainBox.add(buttonBox);

        getContentPane().add(mainBox);

        // resize OK button
        pack();
        commitButton.setPreferredSize(new Dimension(Math.max(commitButton.getWidth(), cancelButton.getWidth()),
                Math.max(commitButton.getHeight(), cancelButton.getHeight())));
        cancelButton.setPreferredSize(new Dimension(Math.max(commitButton.getWidth(), cancelButton.getWidth()),
                Math.max(commitButton.getHeight(), cancelButton.getHeight())));
        commitButton.invalidate();
        cancelButton.invalidate();
    }

    /**
     * Access the singleton instance of the dialog for adding/editing templates.
     * 
     * @return the singleton instance of the dialog
     */
    public static AddTemplateDialog instance() {
        if (me == null) {
            me = new AddTemplateDialog();
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
     * The combo box listing the FSA models for the template.
     */
    protected static JComboBox modelsCombo;

    /**
     * The icon which displays the color selected for the template.
     */
    protected static ColorIcon colorIcon;

    /**
     * The text field where the user can enter the "ID" of the template.
     */
    protected static JTextField tagField;

    /**
     * The text area for the description of the template.
     */
    protected static JTextArea descArea;

    /**
     * The "OK" button to commit the addition of the new template to the library or
     * to commit the changes made to an existing template.
     */
    protected static JButton commitButton;

    /**
     * The template library where the new template has to be added or where the
     * existing template is located.
     */
    protected static TemplateLibrary library;

    /**
     * The existing template whose properties will be modified.
     */
    protected static Template oldTemplate;

    /**
     * Display the dialog to enable the addition of a new template to the template
     * library.
     * 
     * @param library the template library where the template has to be added
     */
    public static void addTemplate(TemplateLibrary library) {
        Set<FSAModel> openModels = new HashSet<FSAModel>();
        for (Iterator<DESModel> i = Hub.getWorkspace().getModels(); i.hasNext();) {
            DESModel model = i.next();
            if (model instanceof FSAModel) {
                openModels.add((FSAModel) model);
            }
        }
        DESModel activeModel = Hub.getWorkspace().getActiveModel();
        if (activeModel != null && activeModel instanceof TemplateModel) {
            for (TemplateComponent c : ((TemplateModel) activeModel).getComponents()) {
                if (c.getModel() != null) {
                    openModels.add(c.getModel());
                }
            }
        }
        Vector<FSAModel> sortedModels = new Vector<FSAModel>(openModels);
        Collections.sort(sortedModels, new Comparator<FSAModel>() {
            public int compare(FSAModel o1, FSAModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        addTemplate(library, sortedModels);
    }

    /**
     * Display the dialog to enable the addition of a new template to the template
     * library. The FSA model to serve as the basis for the new template is
     * predefined.
     * 
     * @param library the template library where the template has to be added
     * @param model   the FSA model to serve as the basis of the template
     */
    public static void addTemplate(TemplateLibrary library, FSAModel model) {
        addTemplate(library, Arrays.asList(new FSAModel[] { model }));
    }

    /**
     * Display the dialog to enable the addition of a new template to the template
     * library. The list of FSA models available to the user to select as the basis
     * of the new template is predefined.
     * 
     * @param library the template library where the template has to be added
     * @param models  the list of FSA models which can serve as the basis of the
     *                template
     */
    protected static void addTemplate(TemplateLibrary library, Collection<FSAModel> models) {
        if (models.isEmpty()) {
            Hub.displayAlert(Hub.string("TD_noModels4Template"));
            return;
        }

        instance();
        AddTemplateDialog.library = library;
        commitButton.removeActionListener(commitAdd);
        commitButton.removeActionListener(commitEdit);
        commitButton.addActionListener(commitAdd);

        modelsCombo.setEnabled(true);
        modelsCombo.removeAllItems();
        for (FSAModel fsa : models) {
            modelsCombo.addItem(fsa);
        }
        if (models.size() == 1) {
            modelsCombo.setSelectedIndex(0);
        } else {
            modelsCombo.setSelectedIndex(-1);
        }

        colorIcon.setColor(Color.WHITE);
        tagField.setText("");
        descArea.setText("");

        me.pack();
        me.setLocation(Hub.getCenteredLocationForDialog(me.getSize()));
        me.setVisible(true);
    }

    /**
     * Display the dialog to enable the modification of the properties of an
     * existing template. The dialog is updated to reflect the current properties of
     * the template.
     * 
     * @param library  the template library which contains the template which will
     *                 be edited
     * @param template the template which will be edited
     */
    protected static void editTemplate(TemplateLibrary library, Template template) {
        instance();
        AddTemplateDialog.library = library;
        oldTemplate = template;

        commitButton.removeActionListener(commitAdd);
        commitButton.removeActionListener(commitEdit);
        commitButton.addActionListener(commitEdit);

        modelsCombo.setEnabled(false);
        modelsCombo.removeAllItems();

        colorIcon.setColor(template.getIcon().getColor());
        tagField.setText(template.getName());
        descArea.setText(template.getDescription());

        me.pack();
        me.setLocation(Hub.getCenteredLocationForDialog(me.getSize()));
        me.setVisible(true);
    }

    /**
     * Called to cancel the addition/editing of the template (e.g., when the user
     * presses the <code>Esc</code> key).
     */
    @Override
    public void onEscapeEvent() {
        oldTemplate = null;
        setVisible(false);
    }

    /**
     * Icon which displays a square with a selected background color.
     * 
     * @author Lenko Grigorov
     */
    protected static class ColorIcon implements Icon {
        /**
         * The length of the side of the icon (square).
         */
        protected static final int SIZE = 20;

        /**
         * The color of the background of the icon.
         */
        Color color = Color.WHITE;

        public int getIconHeight() {
            return SIZE;
        }

        public int getIconWidth() {
            return SIZE;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color old = g.getColor();
            g.setColor(color);
            g.fillRect(x, y, SIZE, SIZE);
            g.setColor(old);
        }

        /**
         * Set the background color of the icon.
         * 
         * @param color the new background color of the icon
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * Retrieve the background color of the icon.
         * 
         * @return the background color of the icon
         */
        public Color getColor() {
            return color;
        }
    }

    /**
     * Renderer of FSA models when shown as items in a list. Displays the names of
     * the models.
     * 
     * @author Lenko Grigorov
     */
    private static class FSARenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = 4274427026504609797L;

        /**
         * Initialize the renderer.
         */
        public FSARenderer() {
            setOpaque(true);
        }

        /**
         * Retrieve the name of the given model and set up the rendering component.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            if (value == null) {
                setText("");
            } else if (value instanceof FSAModel) {
                setText(((FSAModel) value).getName());
            } else {
                setText(value.toString());
            }
            setBackground(isSelected ? SystemColor.textHighlight : SystemColor.text);
            setForeground(isSelected ? SystemColor.textHighlightText : SystemColor.textText);
            return this;
        }
    }

    /**
     * Listener for the user action committing the addition of a new template to the
     * template library. This listener is attached to the "OK" button when the
     * dialog is displayed to add a new template.
     */
    protected static ActionListener commitAdd = new ActionListener() {
        /**
         * Performs the addition of a new template to the template library. The new
         * template is created according to the properties specified in the dialog.
         */
        public void actionPerformed(ActionEvent arg0) {
            if (tagField.getText().length() == 0 || descArea.getText().length() == 0
                    || modelsCombo.getSelectedIndex() < 0) {
                Hub.displayAlert(Hub.string("TD_incompleteTemplateInfo"));
                return;
            }
            if (library.getTemplate(tagField.getText()) != null) {
                Hub.displayAlert(Hub.string("TD_duplicateTag"));
                return;
            }
            TemplateDescriptor td = new TemplateDescriptor();
            td.tag = tagField.getText();
            td.color = colorIcon.getColor();
            td.description = descArea.getText();
            try {
                library.addTemplate(td, ((FSAModel) modelsCombo.getSelectedItem()).clone());
            } catch (IOException e) {
                Hub.displayAlert(Hub.string("TD_errorCreatingTemplate") + " [" + e.getMessage() + "]");
                return;
            }
            me.onEscapeEvent();
        }
    };

    /**
     * Listener for the user action committing the modifications of an existing
     * template. This listener is attached to the "OK" button when the dialog is
     * displayed to edit an existing template.
     */
    protected static ActionListener commitEdit = new ActionListener() {
        /**
         * Commits the modifications of the existing template. The new properties of the
         * template are specified in the dialog.
         */
        public void actionPerformed(ActionEvent arg0) {
            if (tagField.getText().length() == 0 || descArea.getText().length() == 0) {
                Hub.displayAlert(Hub.string("TD_incompleteTemplateInfo"));
                return;
            }
            TemplateDescriptor td = new TemplateDescriptor();
            td.tag = tagField.getText();
            td.color = colorIcon.getColor();
            td.description = descArea.getText();
            try {
                library.removeTemplate(oldTemplate.getName());
            } catch (IOException e) {
                Hub.displayAlert(Hub.string("TD_errorEditingTemplate") + " [" + e.getMessage() + "]");
                return;
            }
            try {
                library.addTemplate(td, oldTemplate.getModel());
            } catch (IOException e) {
                Hub.displayAlert(Hub.string("TD_errorEditingTemplate") + " [" + e.getMessage() + "]");
                try {
                    td.tag = oldTemplate.getName();
                    td.color = oldTemplate.getIcon().getColor();
                    td.description = oldTemplate.getDescription();
                    library.addTemplate(td, oldTemplate.getModel());
                } catch (IOException ex) {
                    // there's nothing more to try
                }
                return;
            }
            me.onEscapeEvent();
        }
    };
}

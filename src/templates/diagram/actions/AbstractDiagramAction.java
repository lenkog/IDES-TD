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

package templates.diagram.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;
import templates.diagram.TemplateDiagram;

/**
 * Provides functionality common to all undoable actions working on
 * {@link TemplateDiagram}s.
 * 
 * @author Lenko Grigorov
 */
public abstract class AbstractDiagramAction extends AbstractAction {
    private static final long serialVersionUID = 7619598556267497405L;

    /**
     * If this action is not independent, the {@link UndoableEdit} the action is
     * part of. <code>null</code> if this action is independent.
     */
    protected CompoundEdit parentEdit = null;

    /**
     * Specifies if the user-readable description of the {@link UndoableEdit} for
     * this action should use the plural form.
     */
    protected boolean usePluralDescription = false;

    /**
     * Calls <code>super</code>.
     */
    public AbstractDiagramAction() {
        super();
    }

    /**
     * Calls <code>super</code> with the given arguments.
     * 
     * @param arg0
     * @param arg1
     */
    public AbstractDiagramAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    /**
     * Calls <code>super</code> with the given arguments.
     * 
     * @param arg0
     */
    public AbstractDiagramAction(String arg0) {
        super(arg0);
    }

    /**
     * Posts the given {@link UndoableEdit} to the undo stack of the active
     * {@link DESModel} in the workspace (if this action is independent), or adds
     * the given {@link UndoableEdit} to the {@link UndoableEdit} this action is
     * part of. The setting for the use of a plural user-readable description is
     * respected.
     * <p>
     * This method is called internally by the action to post the information
     * required to undo the modifications made by the action.
     * 
     * @param edit the {@link UndoableEdit} to be posted
     */
    protected void postEdit(UndoableEdit edit) {
        postEdit(null, edit);
    }

    /**
     * Posts the given {@link UndoableEdit} to the undo stack of the given
     * {@link DESModel} (if this action is independent), or adds the given
     * {@link UndoableEdit} to the {@link UndoableEdit} this action is part of. The
     * setting for the use of a plural user-readable description is respected.
     * 
     * @param model the {@link DESModel} to whose undo stack the
     *              {@link UndoableEdit} should be added
     * @param edit  the {@link UndoableEdit} to be posted
     */
    protected void postEdit(DESModel model, UndoableEdit edit) {
        if (usePluralDescription && edit instanceof AbstractDiagramUndoableEdit) {
            ((AbstractDiagramUndoableEdit) edit).setLastOfMultiple(true);
        }
        if (parentEdit != null) {
            parentEdit.addEdit(edit);
        } else {
            if (model == null) {
                Hub.getUndoManager().addEdit(edit);
            } else {
                Hub.getUndoManager().addEdit(model, edit);
            }
        }
    }

    /**
     * Translate the given {@link TemplateDiagram} so that all of its elements are
     * completely in view (have positive co-ordinates), add the information
     * necessary to undo the translation to the given {@link UndoableEdit}, and post
     * the given {@link UndoableEdit} to the undo stack of the active
     * {@link DESModel} in the workspace (if this action is independent) or add the
     * given {@link UndoableEdit} to the {@link UndoableEdit} this action is part of
     * (if this action is not independent). The setting for the use of a plural
     * user-readable description is respected.
     * <p>
     * This method is called internally by the action to shift the
     * {@link TemplateDiagram} into view after executing the action and to post the
     * information required to undo the modifications made by the action.
     * 
     * @param diagram the {@link TemplateDiagram} to be shifted into view
     * @param edit    the {@link UndoableEdit} to be posted
     */
    protected void postEditAdjustCanvas(TemplateDiagram diagram, UndoableEdit edit) {
        postEditAdjustCanvas(null, diagram, edit);
    }

    /**
     * Translate the given {@link TemplateDiagram} so that all of its elements are
     * completely in view (have positive co-ordinates), add the information
     * necessary to undo the translation to the given {@link UndoableEdit}, and post
     * the given {@link UndoableEdit} to the undo stack of the given
     * {@link DESModel} (if this action is independent) or add the given
     * {@link UndoableEdit} to the {@link UndoableEdit} this action is part of (if
     * this action is not independent). The setting for the use of a plural
     * user-readable description is respected.
     * <p>
     * This method is called internally by the action to shift the
     * {@link TemplateDiagram} into view after executing the action and to post the
     * information required to undo the modifications made by the action.
     * 
     * @param model   the {@link DESModel} to whose undo stack the
     *                {@link UndoableEdit} should be added
     * @param diagram the {@link TemplateDiagram} to be shifted into view
     * @param edit    the {@link UndoableEdit} to be posted
     */
    protected void postEditAdjustCanvas(DESModel model, TemplateDiagram diagram, UndoableEdit edit) {
        postEdit(model, addBoundsAdjust(diagram, edit));
    }

    /**
     * Translate the given {@link TemplateDiagram} so that all of its elements are
     * completely in view (have positive co-ordinates) and add the information
     * necessary to undo the translation to the given {@link UndoableEdit}.
     * 
     * @param diagram the {@link TemplateDiagram} to be shifted into view
     * @param edit    the {@link UndoableEdit} where the addition undo information
     *                has to be included
     * @return the new {@link UndoableEdit} which includes both the original undo
     *         information from the given {@link UndoableEdit} and the new undo
     *         information from the {@link TemplateDiagram} translation
     */
    protected UndoableEdit addBoundsAdjust(TemplateDiagram diagram, UndoableEdit edit) {
        CompoundEdit adjEdit = new CompoundEdit();
        adjEdit.addEdit(edit);
        new DiagramActions.ShiftDiagramInViewAction(adjEdit, diagram).execute();
        adjEdit.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(edit.getPresentationName()));
        adjEdit.end();
        return adjEdit;
    }

    /**
     * Specify if the action is the last one of a multiple similar action, in effect
     * specifying if the the plural form of the user-readable description should be
     * used.
     * 
     * @param b <code>true</code> if this is the last action of a multiple and the
     *          plural form of the user-readable description has to be used;
     *          otherwise <code>false</code>
     */
    public void setLastOfMultiple(boolean b) {
        usePluralDescription = b;
    }

    /**
     * Perform the action.
     */
    public void execute() {
        actionPerformed(null);
    }
}

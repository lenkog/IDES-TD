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

import javax.swing.JPopupMenu;

import templates.diagram.Entity;
import templates.model.TemplateComponent;

/**
 * The context (right-click) pop-up menu for components in a
 * {@link TemplateEditableCanvas}.
 * 
 * @author Lenko Grigorov
 */
public class EntityPopup extends JPopupMenu {
    private static final long serialVersionUID = -2569273570320479180L;

    /**
     * Construct the context pop-up menu for the given entity.
     * 
     * @param canvas the canvas which contains the given entity
     * @param entity the entity for which the context menu is constructed
     */
    public EntityPopup(TemplateEditableCanvas canvas, Entity entity) {
        super();
        add(new UIActions.OpenModelAction(canvas, entity));
        add(new UIActions.AssignFSAAction(canvas, entity));
        if (entity.getComponent().hasModel()) {
            add(new UIActions.MakeTemplateAction(entity));
        }
        addSeparator();
        if (entity.getComponent().getType() == TemplateComponent.TYPE_CHANNEL) {
            add(new UIActions.SetModuleAction(canvas, entity));
            add(new UIActions.ShowSupAction(canvas, entity));
            // add(new UIActions.SetControllabilityAction(canvas,entity));
        } else {
            add(new UIActions.SetChannelAction(canvas, entity));
        }
        addSeparator();
        add(new UIActions.LabelAction(canvas, entity));
        add(new UIActions.ConnectAction(canvas, entity));
        add(new UIActions.SetColorAction(canvas, entity));
        add(new UIActions.ResetIconAction(canvas, entity));
        addSeparator();
        add(new UIActions.DeleteAction(canvas, entity));
        pack();
    }

}

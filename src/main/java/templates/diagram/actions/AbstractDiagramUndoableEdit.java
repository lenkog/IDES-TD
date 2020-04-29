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

package templates.diagram.actions;

import javax.swing.undo.AbstractUndoableEdit;

import templates.diagram.TemplateDiagram;

/**
 * Provides functionality common to all undoable edits of
 * {@link TemplateDiagram}s.
 * 
 * @author Lenko Grigorov
 */
public abstract class AbstractDiagramUndoableEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = -7752002813977055908L;

    /**
     * Specifies if the user-readable description of the undoable edit should use
     * the plural form.
     */
    protected boolean usePluralDescription = false;

    /**
     * Specify if the undoable edit is the last one of a multiple similar edits, in
     * effect specifying if the the plural form of the user-readable description
     * should be used.
     * 
     * @param b <code>true</code> if this is the last undoable edit of a multiple
     *          and the plural form of the user-readable description has to be used;
     *          otherwise <code>false</code>
     */
    public void setLastOfMultiple(boolean b) {
        usePluralDescription = b;
    }
}

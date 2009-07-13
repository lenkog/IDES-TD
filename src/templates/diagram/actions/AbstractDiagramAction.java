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

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import templates.diagram.TemplateDiagram;

/**
 * Provides functionality common to all undoable actions working on {@link TemplateDiagram}s. 
 * 
 * @author Lenko Grigorov
 */
public abstract class AbstractDiagramAction extends AbstractAction
{
	private static final long serialVersionUID = 7619598556267497405L;

	protected CompoundEdit parentEdit = null;

	protected boolean usePluralDescription = false;

	public AbstractDiagramAction()
	{
		super();
	}

	public AbstractDiagramAction(String arg0, Icon arg1)
	{
		super(arg0, arg1);
	}

	public AbstractDiagramAction(String arg0)
	{
		super(arg0);
	}

	protected void postEdit(UndoableEdit edit)
	{
		postEdit(null, edit);
	}

	protected void postEdit(DESModel model, UndoableEdit edit)
	{
		if (usePluralDescription && edit instanceof AbstractDiagramUndoableEdit)
		{
			((AbstractDiagramUndoableEdit)edit).setLastOfMultiple(true);
		}
		if (parentEdit != null)
		{
			parentEdit.addEdit(edit);
		}
		else
		{
			if (model == null)
			{
				Hub.getUndoManager().addEdit(edit);
			}
			else
			{
				Hub.getUndoManager().addEdit(model, edit);
			}
		}
	}

	protected void postEditAdjustCanvas(TemplateDiagram diagram,
			UndoableEdit edit)
	{
		postEditAdjustCanvas(null, diagram, edit);
	}

	protected void postEditAdjustCanvas(DESModel model,
			TemplateDiagram diagram, UndoableEdit edit)
	{
		postEdit(model, addBoundsAdjust(diagram, edit));
	}

	protected UndoableEdit addBoundsAdjust(TemplateDiagram diagram,
			UndoableEdit edit)
	{
		CompoundEdit adjEdit = new CompoundEdit();
		adjEdit.addEdit(edit);
		new DiagramActions.ShiftDiagramInViewAction(adjEdit, diagram).execute();
		adjEdit.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(edit
				.getPresentationName()));
		adjEdit.end();
		return adjEdit;
	}

	public void setLastOfMultiple(boolean b)
	{
		usePluralDescription = b;
	}

	public void execute()
	{
		actionPerformed(null);
	}
}

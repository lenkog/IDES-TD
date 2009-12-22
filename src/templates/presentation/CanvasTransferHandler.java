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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.library.Template;

/**
 * Supports the drop of a template from the template library onto the canvas
 * during a drag-and-drop.
 * 
 * @author Lenko Grigorov
 */
public class CanvasTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = -2766095276487056080L;

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		if (!(comp instanceof TemplateEditableCanvas))
		{
			return false;
		}
		boolean hasTemplateFlavor = false;
		for (DataFlavor df : transferFlavors)
		{
			if (Template.templateFlavor.equals(df))
			{
				hasTemplateFlavor = true;
				break;
			}
		}
		return hasTemplateFlavor;
	}

	public boolean importData(JComponent comp, Transferable t)
	{
		if (!(comp instanceof TemplateEditableCanvas))
		{
			return false;
		}
		if (!t.isDataFlavorSupported(Template.templateFlavor))
		{
			return false;
		}
		TemplateEditableCanvas canvas = (TemplateEditableCanvas)comp;
		Template template = null;
		try
		{
			template = (Template)t.getTransferData(Template.templateFlavor);
		}
		catch (IOException e)
		{
			return false;
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		Point p = canvas.getMousePosition();
		if (p == null)
		{
			return false;
		}
		p = canvas.componentToLocal(p);
		Entity entity = canvas.getDiagram().getEntityAt(p);
		if (entity == null)
		{
			new DiagramActions.CreateTemplateInstanceAction(
					canvas.getDiagram(),
					template,
					p).execute();
		}
		else
		{
			new DiagramActions.AssignFSAAction(
					canvas.getDiagram(),
					entity,
					template.instantiate(),
					template.getIcon()).execute();
		}
		return true;
	}
}

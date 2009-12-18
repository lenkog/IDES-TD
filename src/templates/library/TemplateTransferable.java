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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Wraps {@link Template}s during drag-and-drop operations.
 * 
 * @author Lenko Grigorov
 */
public class TemplateTransferable implements Transferable
{
	/**
	 * The template which is wrapped.
	 */
	protected Template template;

	/**
	 * Create a {@link Transferable} wrapper for a template.
	 * 
	 * @param template
	 *            the template to be wrapped
	 */
	public TemplateTransferable(Template template)
	{
		this.template = template;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if (!Template.templateFlavor.equals(flavor))
		{
			throw new UnsupportedFlavorException(flavor);
		}
		return template;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { Template.templateFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return Template.templateFlavor.equals(flavor);
	}

}

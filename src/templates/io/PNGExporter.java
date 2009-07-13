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

package templates.io;

import ides.api.core.Hub;
import ides.api.plugin.io.FormatTranslationException;
import ides.api.plugin.io.ImportExportPlugin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import templates.diagram.TemplateDiagram;
import templates.model.TemplateModel;

/**
 * Exporter of {@link TemplateDiagram}s to the PNG format.
 * 
 * @author Lenko Grigorov
 */
public class PNGExporter implements ImportExportPlugin
{
	protected static final String description = Hub.string("TD_pngName");

	protected static final String ext = "png";

	protected final static int BORDER_SIZE = 10;

	/**
	 * Exports a file to a different format
	 * 
	 * @param src
	 *            - the source file
	 * @param dst
	 *            - the destination
	 */
	public void exportFile(File src, File dst)
			throws FormatTranslationException
	{
		// Loading the model from the file:
		TemplateModel a = null;
		try
		{
			a = (TemplateModel)Hub.getIOSubsystem().load(src);
		}
		catch (IOException e)
		{
			throw new FormatTranslationException(e);
		}

		TemplateDiagram diagram = new TemplateDiagram(a);

		Rectangle bounds = diagram.getBounds();
		if (bounds.height == 0 || bounds.width == 0)
		{
			bounds = new Rectangle(0, 0, 1, 1);
		}
		bounds.height += BORDER_SIZE * 2;
		bounds.width += BORDER_SIZE * 2;
		BufferedImage image = new BufferedImage(
				bounds.width,
				bounds.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = image.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(Color.WHITE);
		g2D.fillRect(0, 0, bounds.width, bounds.height);
		g2D.translate(-bounds.x + BORDER_SIZE, -bounds.y + BORDER_SIZE);
		diagram.draw(g2D);
		g2D.dispose();
		try
		{
			ImageIO.write(image, "png", dst);
		}
		catch (IOException e)
		{
			throw new FormatTranslationException(e);
		}
	}

	/**
	 * Import a file from a different format to the IDES file system
	 * 
	 * @param importFile
	 *            - the source file
	 * @return
	 */
	public void importFile(File src, File dst)
	{
		return;
	}

	/**
	 * Return a human readable description of the plugin
	 */
	public String getFileDescription()
	{
		return description;
	}

	/**
	 * 
	 */
	public String getFileExtension()
	{
		return ext;
	}

}

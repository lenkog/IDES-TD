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

import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import templates.diagram.DiagramElement;
import templates.model.TemplateModel;

/**
 * A {@link TemplateEditableCanvas} which highlights the elements
 * where there are consistency issues.
 * 
 * @author Lenko Grigorov
 */
public class TemplateConsistencyCanvas extends TemplateEditableCanvas
{
	private static final long serialVersionUID = -5397805276469365742L;

	protected static final String CONSISTENCY_CANVAS_SETTINGS = "templateConsistencyCanvasSettings";

	public TemplateConsistencyCanvas(TemplateModel model)
	{
		super(model);
	}

	protected void autoZoom()
	{
		if (model.hasAnnotation(CONSISTENCY_CANVAS_SETTINGS))
		{
			Hub
					.getUserInterface().getZoomControl()
					.setZoom(((CanvasSettings)model
							.getAnnotation(CONSISTENCY_CANVAS_SETTINGS)).zoom);
		}
		else
		{
			Hub.getUserInterface().getZoomControl().setZoom(1);
		}
	}

	protected void autoScroll()
	{
		if (model.hasAnnotation(CONSISTENCY_CANVAS_SETTINGS))
		{
			scrollRectToVisible(((CanvasSettings)model
					.getAnnotation(CONSISTENCY_CANVAS_SETTINGS)).viewport);
			model.removeAnnotation(CONSISTENCY_CANVAS_SETTINGS);
		}
	}

	protected void storeCanvasInfo()
	{
		CanvasSettings canvasSettings = new CanvasSettings();
		canvasSettings.viewport = getVisibleRect();
		canvasSettings.zoom = scaleFactor;
		model.setAnnotation(CONSISTENCY_CANVAS_SETTINGS, canvasSettings);
	}

	protected void paintCore(Graphics2D g2d)
	{
		diagram.draw(g2d, true);
		if (hilitedElement != null)
		{
			hilitedElement.draw(g2d, true);
		}
	}

	public void scrollTo(Collection<DiagramElement> elements)
	{
		if (elements.isEmpty())
		{
			return;
		}
		Rectangle area = elements.iterator().next().getBounds();
		for (DiagramElement e : elements)
		{
			area = area.union(e.getBounds());
		}
		Point topLeft = localToComponent(new Point(area.getLocation()));
		Point bottomDown = localToComponent(new Point(
				(int)area.getMaxX(),
				(int)area.getMaxY()));
		scrollRectToVisible(new Rectangle(topLeft.x, topLeft.y, bottomDown.x
				- topLeft.x, bottomDown.y - topLeft.y));
	}
}

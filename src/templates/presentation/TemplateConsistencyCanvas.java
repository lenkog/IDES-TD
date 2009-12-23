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

import ides.api.core.Annotable;
import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import templates.diagram.DiagramElement;
import templates.model.TemplateModel;

/**
 * A {@link TemplateEditableCanvas} which highlights the elements which
 * contribute to consistency issues in the template design.
 * 
 * @author Lenko Grigorov
 */
public class TemplateConsistencyCanvas extends TemplateEditableCanvas
{
	private static final long serialVersionUID = -5397805276469365742L;

	/**
	 * The key to be used to annotate a {@link TemplateModel} with the
	 * appearance settings of the consistency canvas (zoom level and viewport
	 * position). This annotation is used to restore the last appearance of the
	 * template diagram when the user re-activates the diagram in the workspace.
	 * <p>
	 * There has to be an annotation separate from the annotation of the
	 * {@link TemplateEditableCanvas} associated with the model to prevent the
	 * settings overwriting.
	 * 
	 * @see Annotable
	 * @see TemplateEditableCanvas#CANVAS_SETTINGS
	 * @see TemplateEditableCanvas.CanvasSettings
	 */
	protected static final String CONSISTENCY_CANVAS_SETTINGS = "templateConsistencyCanvasSettings";

	/**
	 * Construct a new consistency canvas to enable the editing of the given
	 * template design and the visualization of consistency issues in the
	 * design.
	 * 
	 * @param model
	 *            the template design which the user will manipulate and whose
	 *            consistency issues will be visualized
	 */
	public TemplateConsistencyCanvas(TemplateModel model)
	{
		super(model);
	}

	/**
	 * Set the scaling factor according to the zoom level setting stored as an
	 * annotation in the template design. If this annotation cannot be found,
	 * set the scaling factor to <code>1</code>.
	 * 
	 * @see TemplateEditableCanvas.CanvasSettings
	 * @see Annotable
	 */
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

	/**
	 * Scroll the viewport of the canvas to the rectangle stored as an
	 * annotation in the template design. After the scrolling, the annotation is
	 * removed. If the annotation cannot be found, do nothing.
	 * 
	 * @see TemplateEditableCanvas.CanvasSettings
	 * @see Annotable
	 */
	protected void autoScroll()
	{
		if (model.hasAnnotation(CONSISTENCY_CANVAS_SETTINGS))
		{
			scrollRectToVisible(((CanvasSettings)model
					.getAnnotation(CONSISTENCY_CANVAS_SETTINGS)).viewport);
			model.removeAnnotation(CONSISTENCY_CANVAS_SETTINGS);
		}
	}

	/**
	 * Create a descriptor of the appearance settings of the consistency canvas
	 * and create an annotation with it in the template design.
	 * 
	 * @see TemplateEditableCanvas.CanvasSettings
	 * @see Annotable
	 */
	protected void storeCanvasInfo()
	{
		CanvasSettings canvasSettings = new CanvasSettings();
		canvasSettings.viewport = getVisibleRect();
		canvasSettings.zoom = scaleFactor;
		model.setAnnotation(CONSISTENCY_CANVAS_SETTINGS, canvasSettings);
	}

	/**
	 * Paint the template diagram so that diagram elements contributing to
	 * consistency issues are color-highlighted.
	 */
	protected void paintCore(Graphics2D g2d)
	{
		diagram.draw(g2d, true);
		if (hilitedElement != null)
		{
			hilitedElement.draw(g2d, true);
		}
	}

	/**
	 * Scroll the viewport of the consistency canvas so that the specified
	 * diagram elements come to view. If all elements do not fit in the
	 * viewport, scroll to the top-left corner of the area containing the
	 * element. If the list of elements is empty, do nothing.
	 * 
	 * @param elements
	 *            the list of diagram elements which should come into view
	 */
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

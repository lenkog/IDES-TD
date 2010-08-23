/*
 * Copyright (c) 2010, Lenko Grigorov
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
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;
import ides.api.plugin.presentation.ZoomablePresentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JLabel;

import templates.diagram.DiagramElement;
import templates.diagram.TemplateDiagram;
import templates.diagram.TemplateDiagramMessage;
import templates.diagram.TemplateDiagramSubscriber;
import templates.model.TemplateModel;

/**
 * The UI element which renders the graphical representation of a
 * {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class TemplateCanvas extends JComponent implements Presentation,
		TemplateDiagramSubscriber, ZoomablePresentation
{
	private static final long serialVersionUID = 8536845910460021585L;

	/**
	 * The key to be used to annotate a {@link TemplateModel} with the
	 * {@link TemplateDiagram} for the model.
	 * 
	 * @see Annotable
	 */
	protected static final String DIAGRAM = "templates.diagram.TemplateDiagram";

	/**
	 * The graphics context for rendering the template diagram.
	 */
	protected static Graphics graphics = null;

	/**
	 * The template design to be rendered.
	 */
	protected TemplateModel model;

	/**
	 * The template diagram for the {@link TemplateModel} rendered by this
	 * canvas.
	 */
	protected TemplateDiagram diagram;

	/**
	 * The scaling factor when rendering the diagram.
	 */
	protected float scaleFactor = 1;

	/**
	 * Information about whether to scale the diagram to fit the size of the
	 * component containing the canvas. Set to <code>true</code> if the diagram
	 * should be scaled, set to <code>false</code> otherwise.
	 */
	protected boolean scaleToFit = true;

	/**
	 * Construct and set up a new canvas to render the given template design.
	 * 
	 * @param model
	 *            the template design to be rendered
	 */
	public TemplateCanvas(TemplateModel model)
	{
		super();
		if (graphics == null)
		{
			setupGraphics();
		}
		this.model = model;
		DiagramElement.setGlobalFont(new JLabel().getFont());
		DiagramElement.setGlobalFontRenderer(graphics);
		diagram = retrieveDiagram(model);
		diagram.addSubscriber(this);
	}

	/**
	 * Retrieve the {@link TemplateDiagram} for the given {@link TemplateModel}.
	 * If the diagram cannot be found as an annotation of the model, create a
	 * new template diagram for the model and set it as an annotation.
	 * 
	 * @param model
	 *            the template design whose diagram should be retrieved
	 * @return the template diagram for the given model (potentially newly
	 *         created)
	 */
	private static TemplateDiagram retrieveDiagram(TemplateModel model)
	{
		TemplateDiagram diagram = null;
		if (!model.hasAnnotation(DIAGRAM))
		{
			diagram = new TemplateDiagram(model);
			model.setAnnotation(DIAGRAM, diagram);
		}
		else
		{
			diagram = (TemplateDiagram)model.getAnnotation(DIAGRAM);
		}
		return diagram;
	}

	/**
	 * Set up the graphics context for the rendering of the template diagram.
	 */
	private static void setupGraphics()
	{
		graphics = Hub.getMainWindow().getGraphics().create();
	}

	/**
	 * Retrieve the template diagram rendered by this canvas.
	 * 
	 * @return the template diagram rendered by this canvas
	 */
	public TemplateDiagram getDiagram()
	{
		return diagram;
	}

	public void forceRepaint()
	{
		refresh();
	}

	public JComponent getGUI()
	{
		return this;
	}

	public DESModel getModel()
	{
		return model;
	}

	public void release()
	{
		diagram.removeSubscriber(this);
		if (diagram.getDiagramSubscribers().length == 0)
		{
			diagram.release();
			model.removeAnnotation(DIAGRAM);
		}
	}

	public void setTrackModel(boolean arg0)
	{
	}

	public Dimension getPreferredSize()
	{
		Rectangle bounds = diagram.getBounds();
		return new Dimension(
				(int)((bounds.width + bounds.x + TemplateDiagram.DESIRED_DIAGRAM_INSET) * scaleFactor),
				(int)((bounds.height + bounds.y + TemplateDiagram.DESIRED_DIAGRAM_INSET) * scaleFactor));
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getBounds().width, getBounds().height);
		g2d.scale(scaleFactor, scaleFactor);
		paintCore(g2d);
	}

	/**
	 * Paint only the template diagram itself. This method can be used by
	 * subclasses to augment the graphics context before the painting of the
	 * diagram.
	 * 
	 * @param g2d
	 *            the graphics context where the template diagram should be
	 *            painted
	 */
	protected void paintCore(Graphics2D g2d)
	{
		diagram.draw(g2d);
	}

	/**
	 * Refresh the rendering of the diagram.
	 */
	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		refresh();
	}

	/**
	 * Refresh the rendering of the diagram.
	 */
	public void templateDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		repaint();
	}

	/**
	 * Refresh the rendering of the diagram. Compute the new scaling factor and
	 * repaint.
	 */
	public void refresh()
	{
		if (scaleToFit && getParent() != null)
		{
			Insets ins = getParent().getInsets();
			Rectangle diaBounds = new Rectangle(0, 0)
					.union(diagram.getBounds());
			float xScale = (float)(getParent().getWidth() - ins.left - ins.right)
					/ (float)(diaBounds.width + diaBounds.x + 2 * TemplateDiagram.DESIRED_DIAGRAM_INSET);
			float yScale = (float)(getParent().getHeight() - ins.top - ins.bottom)
					/ (float)(diaBounds.height + diaBounds.y + 2 * TemplateDiagram.DESIRED_DIAGRAM_INSET);
			scaleFactor = Math.min(xScale, yScale);
		}
		revalidate();
		repaint();
	}

	public void setScaleFactor(float arg0)
	{
		scaleFactor = arg0;
	}
}

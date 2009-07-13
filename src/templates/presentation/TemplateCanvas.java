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
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;

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
 * The UI element which renders the graphical representation of a {@link TemplateModel}.
 * 
 * @author Lenko Grigorov
 */
public class TemplateCanvas extends JComponent implements Presentation,
		TemplateDiagramSubscriber
{
	private static final long serialVersionUID = 8536845910460021585L;

	protected static final String DIAGRAM = "templates.diagram.TemplateDiagram";

	protected static Graphics graphics = null;

	protected TemplateModel model;

	protected TemplateDiagram diagram;

	protected float scaleFactor = 1;

	protected boolean scaleToFit = true;

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
		// refresh();
	}

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

	private static void setupGraphics()
	{
		graphics = Hub.getMainWindow().getGraphics().create();
	}

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
		// TODO Auto-generated method stub

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

	protected void paintCore(Graphics2D g2d)
	{
		diagram.draw(g2d);
	}

	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		refresh();
	}

	public void templateDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		repaint();
	}

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
}

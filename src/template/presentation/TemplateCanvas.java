package template.presentation;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import template.diagram.DiagramElement;
import template.diagram.TemplateDiagram;
import template.diagram.TemplateDiagramMessage;
import template.diagram.TemplateDiagramSubscriber;
import template.diagram.actions.DiagramActions;
import template.model.TemplateModel;

public class TemplateCanvas extends JComponent implements Presentation,
		TemplateDiagramSubscriber
{
	private static final long serialVersionUID = 8536845910460021585L;

	protected static final String DIAGRAM = "template.diagram.TemplateDiagram";

	protected static Graphics graphics = null;

	protected TemplateModel model;

	protected TemplateDiagram diagram;
	
	protected float scaleFactor=1;
	protected boolean scaleToFit=true;

	public TemplateCanvas(TemplateModel model)
	{
		super();
		if (graphics == null)
		{
			setupGraphics();
		}
		this.model = model;
		DiagramElement.setGlobalFontMetrics(graphics.getFontMetrics());
		diagram = retrieveDiagram(model);
		diagram.addSubscriber(this);
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
		// TODO Auto-generated method stub
System.out.println("foo");
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
		// TODO Auto-generated method stub

	}

	public void setTrackModel(boolean arg0)
	{
		// TODO Auto-generated method stub

	}

	public Dimension getPreferredSize()
	{
		Rectangle bounds = diagram.getBounds();
		return new Dimension((int)((bounds.width + bounds.x
				+ TemplateDiagram.DESIRED_DIAGRAM_INSET)*scaleFactor), (int)((bounds.height
				+ bounds.y + TemplateDiagram.DESIRED_DIAGRAM_INSET)*scaleFactor));
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getBounds().width, getBounds().height);
		
		g2d.scale(scaleFactor, scaleFactor);
		diagram.draw(g2d);
	}

	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		if (scaleToFit && getParent() != null)
		{
			Insets ins = getParent().getInsets();
			Rectangle diaBounds=new Rectangle(0,0).union(diagram.getBounds());
			float xScale = (float)(getParent().getWidth() - ins.left - ins.right)
					/ (float)(diaBounds.width + diaBounds.x + 2*TemplateDiagram.DESIRED_DIAGRAM_INSET);
			float yScale = (float)(getParent().getHeight() - ins.top - ins.bottom)
					/ (float)(diaBounds.height + diaBounds.y + 2*TemplateDiagram.DESIRED_DIAGRAM_INSET);
			scaleFactor=Math.min(xScale, yScale);
		}
		revalidate();
		repaint();
	}

	public void templateDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		// TODO Auto-generated method stub

	}


}

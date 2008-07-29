package template.presentation;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.presentation.Presentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import template.diagram.DiagramElement;
import template.diagram.TemplateDiagram;
import template.diagram.TemplateDiagramMessage;
import template.diagram.TemplateDiagramSubscriber;
import template.diagram.actions.DiagramActions;
import template.model.TemplateModel;

public class TemplateCanvas extends JComponent implements Presentation, TemplateDiagramSubscriber, MouseListener
{
	private static final long serialVersionUID = 8536845910460021585L;

	protected static final String DIAGRAM="template.diagram.TemplateDiagram";
	
	protected static Graphics graphics=null;
	
	protected TemplateModel model;
	protected TemplateDiagram diagram;
	
	public TemplateCanvas(TemplateModel model)
	{
		super();
		if(graphics==null)
		{
			setupGraphics();
		}
		this.model=model;
		DiagramElement.setGlobalFontMetrics(graphics.getFontMetrics());
		diagram=retrieveDiagram(model);
		diagram.addSubscriber(this);
		addMouseListener(this);
	}
	
	private static TemplateDiagram retrieveDiagram(TemplateModel model)
	{
		TemplateDiagram diagram=null;
		if(!model.hasAnnotation(DIAGRAM))
		{
			diagram=new TemplateDiagram(model);
			model.setAnnotation(DIAGRAM,diagram);
		}
		else
		{
			diagram=(TemplateDiagram)model.getAnnotation(DIAGRAM);
		}
		return diagram;
	}
	
	private static void setupGraphics()
	{
		graphics=Hub.getMainWindow().getGraphics().create();
	}
	
	public void forceRepaint()
	{
		// TODO Auto-generated method stub
		
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
		return new Dimension(diagram.getBounds().width+diagram.getBounds().x,diagram.getBounds().height+diagram.getBounds().y);
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g2d=(Graphics2D)g;
		diagram.draw(g2d);
	}

	public void templateDiagramChanged(TemplateDiagramMessage message)
	{
		revalidate();
		repaint();
	}

	public void templateDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(MouseEvent arg0)
	{
		System.out.println(arg0.toString());
		if(arg0.getClickCount()==1)
		{
			new DiagramActions.CreateComponentAction(diagram,arg0.getPoint()).execute();
		}
	}

	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
}

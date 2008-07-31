package templates.presentation;

import ides.api.core.Hub;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import templates.model.TemplateModel;

public class TemplateEditableCanvas extends TemplateCanvas implements MouseListener, MouseMotionListener
{
	protected static final String CANVAS_SETTINGS="templateCanvasSettings";
	
	protected static class CanvasSettings
	{
		public Rectangle viewport = new Rectangle(0, 0, 0, 0);
		public float zoom = 1;
	}
	
	protected MouseInterpreter interpreter;
	
	public TemplateEditableCanvas(TemplateModel model)
	{
		super(model);
		if(model.hasAnnotation(CANVAS_SETTINGS))
		{
			Hub.getUserInterface().getZoomControl().setZoom(((CanvasSettings)model.getAnnotation(CANVAS_SETTINGS)).zoom);
		}
		else
		{
			Hub.getUserInterface().getZoomControl().setZoom(1);
		}
		scaleFactor=Hub.getUserInterface().getZoomControl().getZoom();
		scaleToFit=false;
		interpreter=new MouseInterpreter(this);	
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public JComponent getGUI()
	{
		JScrollPane sp = new JScrollPane(
				this,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		return sp;
	}
	
	public void paint(Graphics g)
	{
		scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
		if(model.hasAnnotation(CANVAS_SETTINGS))
		{
			scrollRectToVisible(((CanvasSettings)model.getAnnotation(CANVAS_SETTINGS)).viewport);
			model.removeAnnotation(CANVAS_SETTINGS);
		}
		super.paint(g);
	}
	
	public void refresh()
	{
		scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
		super.refresh();
	}
	
	public void release()
	{
		CanvasSettings canvasSettings = new CanvasSettings();
		canvasSettings.viewport = getVisibleRect();
		canvasSettings.zoom = scaleFactor;
		model.setAnnotation(CANVAS_SETTINGS, canvasSettings);
		super.release();
	}
	
	public MouseEvent transformMouseCoords(MouseEvent me)
	{
		Point2D.Float p = new Point2D.Float(me.getX(), me.getY());
		p.x = p.x / scaleFactor;
		p.y = p.y / scaleFactor;
		return new MouseEvent(
				(Component)me.getSource(),
				me.getID(),
				me.getWhen(),
				me.getModifiersEx(),
				(int)p.x,
				(int)p.y,
				me.getClickCount(),
				me.isPopupTrigger(),
				me.getButton());
	}

	public void mouseClicked(MouseEvent arg0)
	{
		arg0=transformMouseCoords(arg0);
		interpreter.mouseClicked(arg0);
	}
	
	public void mouseEntered(MouseEvent arg0)
	{
		arg0=transformMouseCoords(arg0);
		interpreter.mouseEntered(arg0);
	}

	public void mouseExited(MouseEvent arg0)
	{
		arg0=transformMouseCoords(arg0);
		interpreter.mouseExited(arg0);
	}

	public void mousePressed(MouseEvent arg0)
	{
		arg0=transformMouseCoords(arg0);
		interpreter.mousePressed(arg0);
	}

	public void mouseReleased(MouseEvent arg0)
	{
		arg0=transformMouseCoords(arg0);
		interpreter.mouseReleased(arg0);
	}

	public void mouseDragged(MouseEvent e)
	{
		e=transformMouseCoords(e);
		interpreter.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e)
	{
		e=transformMouseCoords(e);
		interpreter.mouseMoved(e);
	}
}

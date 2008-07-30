package template.presentation;

import ides.api.core.Hub;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import template.diagram.actions.DiagramActions;
import template.model.TemplateModel;

public class TemplateEditableCanvas extends TemplateCanvas implements MouseListener, MouseMotionListener
{
	protected MouseInterpreter interpreter;
	
	public TemplateEditableCanvas(TemplateModel model)
	{
		super(model);
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
		super.paint(g);
	}
	
	public MouseEvent transformMouseCoords(MouseEvent me)
	{
		return me;
	}

	public void mouseClicked(MouseEvent arg0)
	{
		transformMouseCoords(arg0);
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

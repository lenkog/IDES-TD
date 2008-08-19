package templates.presentation;

import ides.api.core.Hub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.model.TemplateModel;

public class TemplateEditableCanvas extends TemplateCanvas implements
		MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = -6177488412629054011L;

	protected static final String CANVAS_SETTINGS = "templateCanvasSettings";

	protected static class CanvasSettings
	{
		public Rectangle viewport = new Rectangle(0, 0, 0, 0);

		public float zoom = 1;
	}

	protected static final Stroke SELECTIONBOX_STROKE = new BasicStroke(
			1,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10f,
			new float[] { 3, 3 },
			0f);

	protected boolean ignoreNextMouseEvent = false;

	protected MouseInterpreter interpreter;

	protected DiagramElement hilitedElement = null;

	protected Rectangle selectionBox = null;

	protected Point connectorOrigin = null;

	protected boolean drawConnector = false;

	public TemplateEditableCanvas(TemplateModel model)
	{
		super(model);
		if (model.hasAnnotation(CANVAS_SETTINGS))
		{
			Hub
					.getUserInterface().getZoomControl()
					.setZoom(((CanvasSettings)model
							.getAnnotation(CANVAS_SETTINGS)).zoom);
		}
		else
		{
			Hub.getUserInterface().getZoomControl().setZoom(1);
		}
		scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
		scaleToFit = false;
		interpreter = new MouseInterpreter(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		String escAction = "esc";
		String deleteAction = "deleteSelection";

		// Associating key strokes with action names:
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, 0),
				deleteAction);
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke
				.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				escAction);
		// Associating the action names with operations:
		getActionMap().put(deleteAction, new AbstractAction()
		{
			private static final long serialVersionUID = -2136095877399476978L;

			public void actionPerformed(ActionEvent e)
			{
				if (!diagram.getSelection().isEmpty())
				{
					new DiagramActions.DeleteElementsAction(diagram, diagram
							.getSelection()).execute();
				}
			}
		});
		// getActionMap().put(escAction, escapeCommand);
		refresh();
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
		if (model.hasAnnotation(CANVAS_SETTINGS))
		{
			scrollRectToVisible(((CanvasSettings)model
					.getAnnotation(CANVAS_SETTINGS)).viewport);
			model.removeAnnotation(CANVAS_SETTINGS);
		}
		Stroke oldStroke = ((Graphics2D)g).getStroke();
		super.paint(g);
		if (selectionBox != null)
		{
			((Graphics2D)g).setStroke(SELECTIONBOX_STROKE);
			g.setColor(Color.GRAY);
			g.drawRect(selectionBox.x,
					selectionBox.y,
					selectionBox.width,
					selectionBox.height);
		}
		if (drawConnector && getMousePosition(true) != null)
		{
			((Graphics2D)g).setStroke(oldStroke);
			g.setColor(Color.BLACK);
			Point origin = connectorOrigin;
			Point end = getMousePosition(true);
			g.drawLine(origin.x,
					origin.y,
					(int)(end.x / scaleFactor),
					(int)(end.y / scaleFactor));
		}
	}

	public void refresh()
	{
		scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
		super.refresh();
	}

	public void release()
	{
		removeHighlight();
		diagram.clearSelection();
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
		return new MouseEvent((Component)me.getSource(), me.getID(), me
				.getWhen(), me.getModifiersEx(), (int)p.x, (int)p.y, me
				.getClickCount(), me.isPopupTrigger(), me.getButton());
	}

	public Point localToScreen(Point p)
	{
		Point ret = localToComponent(p);
		SwingUtilities.convertPointToScreen(ret, this);
		return ret;
	}

	public Point localToComponent(Point p)
	{
		return new Point((int)(p.x * scaleFactor), (int)(p.y * scaleFactor));
	}

	public void setUIInteraction(boolean b)
	{
		ignoreNextMouseEvent = b;
	}

	public void setSelectionBox(Rectangle r)
	{
		selectionBox = r;
		if (selectionBox != null)
		{
			Collection<DiagramElement> selected = new HashSet<DiagramElement>();
			for (Connector c : diagram.getConnectors())
			{
				if (c.intersects(selectionBox))
				{
					selected.add(c);
				}
			}
			for (Entity e : diagram.getEntities())
			{
				if (e.intersects(selectionBox))
				{
					selected.add(e);
					selected.addAll(diagram.getAdjacentConnectors(e));
				}
			}
			diagram.setSelection(selected);
		}
	}

	public Rectangle getSelectionBox()
	{
		return selectionBox;
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		arg0 = transformMouseCoords(arg0);
		interpreter.mouseClicked(arg0);
	}

	public void mouseEntered(MouseEvent arg0)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		arg0 = transformMouseCoords(arg0);
		interpreter.mouseEntered(arg0);
	}

	public void mouseExited(MouseEvent arg0)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		arg0 = transformMouseCoords(arg0);
		interpreter.mouseExited(arg0);
	}

	public void mousePressed(MouseEvent arg0)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		requestFocus();
		arg0 = transformMouseCoords(arg0);
		interpreter.mousePressed(arg0);
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (ignoreNextMouseEvent)
		{
			ignoreNextMouseEvent = false;
			return;
		}
		arg0 = transformMouseCoords(arg0);
		interpreter.mouseReleased(arg0);
	}

	public void mouseDragged(MouseEvent e)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		e = transformMouseCoords(e);
		interpreter.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e)
	{
		if (ignoreNextMouseEvent)
		{
			return;
		}
		e = transformMouseCoords(e);
		interpreter.mouseMoved(e);
	}

	public void highlight(DiagramElement element)
	{
		if (hilitedElement != null)
		{
			hilitedElement.setHighlight(false);
		}
		hilitedElement = element;
		hilitedElement.setHighlight(true);
	}

	public DiagramElement getHighlightedElement()
	{
		return hilitedElement;
	}

	public void removeHighlight()
	{
		if (hilitedElement != null)
		{
			hilitedElement.setHighlight(false);
		}
		hilitedElement = null;
	}

	public void startConnector(Point origin)
	{
		connectorOrigin = origin;
		drawConnector = true;
	}

	public void finishConnector()
	{
		drawConnector = false;
	}
}

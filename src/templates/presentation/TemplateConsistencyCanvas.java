package templates.presentation;

import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import templates.diagram.DiagramElement;
import templates.model.TemplateModel;

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

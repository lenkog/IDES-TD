package templates.presentation;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import templates.diagram.Entity;
import templates.diagram.actions.DiagramActions;
import templates.library.Template;

public class CanvasTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = -2766095276487056080L;

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		if (!(comp instanceof TemplateEditableCanvas))
		{
			return false;
		}
		boolean hasTemplateFlavor = false;
		for (DataFlavor df : transferFlavors)
		{
			if (Template.templateFlavor.equals(df))
			{
				hasTemplateFlavor = true;
				break;
			}
		}
		return hasTemplateFlavor;
	}

	public boolean importData(JComponent comp, Transferable t)
	{
		if (!(comp instanceof TemplateEditableCanvas))
		{
			return false;
		}
		if (!t.isDataFlavorSupported(Template.templateFlavor))
		{
			return false;
		}
		TemplateEditableCanvas canvas = (TemplateEditableCanvas)comp;
		Template template = null;
		try
		{
			template = (Template)t.getTransferData(Template.templateFlavor);
		}
		catch (IOException e)
		{
			return false;
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		Point p = canvas.getMousePosition();
		if (p == null)
		{
			return false;
		}
		p = canvas.componentToLocal(p);
		Entity entity = canvas.getDiagram().getEntityAt(p);
		if (entity == null)
		{
			new DiagramActions.CreateTemplateInstanceAction(
					canvas.getDiagram(),
					template,
					p).execute();
		}
		else
		{
			new DiagramActions.AssignFSAAction(
					canvas.getDiagram(),
					entity,
					template.instantiate(),
					template.getIcon()).execute();
		}
		return true;
	}
}

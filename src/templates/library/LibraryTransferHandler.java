package templates.library;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class LibraryTransferHandler extends TransferHandler
{
	protected Transferable createTransferable(JComponent c)
	{
		if(!(c instanceof JList))
		{
			return null;
		}
		JList list=(JList)c;
		Object value=list.getSelectedValue();
		if(!(value instanceof Template))
		{
			return null;
		}
		return new TemplateTransferable((Template)value);
	}
	
	public int getSourceActions(JComponent c)
	{
		if(!(c instanceof JList))
		{
			return TransferHandler.NONE;
		}
		return TransferHandler.COPY;
	}
}

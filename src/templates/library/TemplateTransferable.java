package templates.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TemplateTransferable implements Transferable
{
	protected Template template;
	
	public TemplateTransferable(Template template)
	{
		this.template=template;
	}
	
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if(!Template.templateFlavor.equals(flavor))
		{
			throw new UnsupportedFlavorException(flavor);
		}
		return template;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[]{Template.templateFlavor};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return Template.templateFlavor.equals(flavor);
	}

}

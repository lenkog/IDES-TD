package templates.library;

import ides.api.core.Hub;
import ides.api.plugin.io.FileIOPlugin;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.io.FileSaveException;
import ides.api.plugin.io.UnsupportedVersionException;
import ides.api.plugin.model.DESModel;
import ides.api.utilities.HeadTailInputStream;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TemplateMetaIO implements FileIOPlugin
{
	protected static final String META_TAG = "template";

	protected static final String VERSION = "3";

	protected static final String ELEMENT_META = "meta";

	protected static final String ELEMENT_INFO = "template";

	protected static final String ELEMENT_DESC = "description";

	protected static final String ATTRIBUTE_COLOR = "color";

	protected static final String ATTRIBUTE_TAG = "tag";

	public String getIOTypeDescriptor()
	{
		throw new UnsupportedOperationException();
	}

	public Set<String> getMetaTags()
	{
		Set<String> tags = new HashSet<String>();
		tags.add(META_TAG);
		return tags;
	}

	public String getSaveDataVersion()
	{
		throw new UnsupportedOperationException();
	}

	public String getSaveMetaVersion(String arg0)
	{
		return VERSION;
	}

	public DESModel loadData(String arg0, InputStream arg1, String arg2)
			throws FileLoadException
	{
		throw new UnsupportedOperationException();
	}

	public void loadMeta(String version, InputStream stream, DESModel model,
			String tag) throws FileLoadException
	{
		if (!tag.equals(META_TAG))
		{
			throw new FileLoadException(Hub.string("TD_unsupportedMetaTag")
					+ " [" + tag + "]");
		}
		if (!VERSION.equals(version))
		{
			throw new UnsupportedVersionException(Hub
					.string("TD_unsupportedVersion"));
		}
		byte[] FILE_HEADER = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ System.getProperty("line.separator") + "<meta>" + System
				.getProperty("line.separator")).getBytes();
		HeadTailInputStream metaField = new HeadTailInputStream(
				stream,
				FILE_HEADER,
				"</meta>".getBytes());
		Document doc = null;
		try
		{
			DocumentBuilder parser = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			doc = parser.parse(metaField);
		}
		catch (ParserConfigurationException e)
		{
			throw new FileLoadException(e.getMessage());
		}
		catch (IOException e)
		{
			throw new FileLoadException(e.getMessage());
		}
		catch (SAXException e)
		{
			throw new FileLoadException(e.getMessage());
		}
		if (doc == null)
		{
			throw new FileLoadException(Hub.string("TD_ioCantParseFile"));
		}
		Node dataNode = null;
		for (int i = 0; i < doc.getChildNodes().getLength(); ++i)
		{
			if (doc.getChildNodes().item(i).getNodeName().equals(ELEMENT_META))
			{
				dataNode = doc.getChildNodes().item(i);
			}
		}
		if (dataNode == null)
		{
			throw new FileLoadException(Hub.string("TD_ioCantParseFile"));
		}
		String errors = "";
		NodeList children = dataNode.getChildNodes();
		try
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node.getNodeName().equals(ELEMENT_INFO))
				{
					NamedNodeMap attributes = node.getAttributes();
					TemplateDescriptor td = new TemplateDescriptor();
					td.tag = attributes
							.getNamedItem(ATTRIBUTE_TAG).getNodeValue();
					try
					{
						td.color = Color.decode(attributes
								.getNamedItem(ATTRIBUTE_COLOR).getNodeValue());
					}
					catch (NumberFormatException e)
					{
						errors += Hub.string("TD_ioCantParseFile")
								+ " ("
								+ attributes
										.getNamedItem(ATTRIBUTE_COLOR)
										.getNodeValue() + ")\n";
					}
					for (int j = 0; j < node.getChildNodes().getLength(); j++)
					{
						Node child = node.getChildNodes().item(j);
						if (child.getNodeName().equals(ELEMENT_DESC))
						{
							td.description = child.getTextContent();
							break;
						}
					}
					model.setAnnotation(Template.TEMPLATE_DESC, td);
					break;
				}
			}
			if (!"".equals(errors))
			{
				throw new RuntimeException(errors);
			}
		}
		catch (Exception e)
		{
			throw new FileLoadException(e.getMessage(), model);
		}
	}

	public void saveData(PrintStream arg0, DESModel arg1, String arg2)
			throws FileSaveException
	{
		throw new UnsupportedOperationException();
	}

	public void saveMeta(PrintStream stream, DESModel arg1, String arg2)
			throws FileSaveException
	{
		if (!META_TAG.equals(arg2))
		{
			throw new FileSaveException("TD_ioUnsupportedTag");
		}
		if (!arg1.hasAnnotation(Template.TEMPLATE_DESC))
		{
			return;
		}
		TemplateDescriptor td = (TemplateDescriptor)arg1
				.getAnnotation(Template.TEMPLATE_DESC);
		stream.println("\n\t<" + ELEMENT_INFO + " " + ATTRIBUTE_TAG + "=\""
				+ td.tag + "\" " + ATTRIBUTE_COLOR + "=\"#"
				+ (td.color.getRed() < 16 ? "0" : "")
				+ Integer.toHexString(td.color.getRed())
				+ (td.color.getGreen() < 16 ? "0" : "")
				+ Integer.toHexString(td.color.getGreen())
				+ (td.color.getBlue() < 16 ? "0" : "")
				+ Integer.toHexString(td.color.getBlue()) + "\">");
		stream.println("\t\t<" + ELEMENT_DESC + ">" + td.description + "</"
				+ ELEMENT_DESC + ">");
		stream.println("\t</" + ELEMENT_INFO + ">");
	}

}

/*
 * Copyright (c) 2010, Lenko Grigorov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package templates.io;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.io.FileIOPlugin;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.io.FileSaveException;
import ides.api.plugin.io.IOSubsytem;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.ModelManager;
import ides.api.utilities.GeneralUtils;
import ides.api.utilities.HeadTailInputStream;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import templates.diagram.Connector;
import templates.diagram.EmptyConnector;
import templates.diagram.EmptyConnectorSet;
import templates.diagram.Entity;
import templates.diagram.EntityLayout;
import templates.diagram.TemplateDiagram;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;

/**
 * Provides IO for {@link TemplateModel}s. Also stores and loads the graphical
 * layout information needed for the associated {@link TemplateDiagram}s, as
 * well as manages the storing and loading of the separate files with the models
 * of the {@link TemplateComponent}s in {@link TemplateModel}s.
 * 
 * @author Lenko Grigorov
 */
public class TemplateFileIO implements FileIOPlugin
{
	/**
	 * The string description of {@link TemplateModel}s.
	 */
	protected static final String TYPE = "TemplateDesign";

	/**
	 * The meta tag for the layout information of {@link TemplateDiagram}s.
	 */
	protected static final String META = "layout";

	/**
	 * The version of the file format supported by this class.
	 */
	protected static final String VERSION = "3";

	/**
	 * Key for the annotation of a {@link FSAModel} component of a
	 * {@link TemplateModel} with information about the location of the file
	 * where the component is saved.
	 */
	protected static final String FILE = "templateComponentFile";

	/**
	 * Key for the annotation of a {@link TemplateModel} with information about
	 * the location of the last file to which the model was saved.
	 */
	protected static final String LAST_SAVE_FILE = "templateLastSaveFile";

	/**
	 * "DATA" element of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Denotes the element which
	 * contains the description of the {@link TemplateModel}.
	 */
	protected static final String ELEMENT_DATA = "data";

	/**
	 * "COMPONENT" element of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}. Denotes
	 * the element which describes a {@link TemplateComponent} (<i>module</i> or
	 * <i>channel</i>) of a {@link TemplateModel}.
	 */
	protected static final String ELEMENT_COMPONENT = "component";

	/**
	 * "LINK" element of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Denotes the element which
	 * describes a {@link TemplateLink} between components of a
	 * {@link TemplateModel}.
	 */
	protected static final String ELEMENT_LINK = "link";

	/**
	 * "ENTITY" element of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}. Denotes
	 * the element which describes an {@link Entity} in a
	 * {@link TemplateDiagram}.
	 */
	protected static final String ELEMENT_ENTITY = "entity";

	/**
	 * "CONNECTOR" element of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}. Denotes
	 * the element which describes a {@link Connector} in a
	 * {@link TemplateDiagram}.
	 */
	protected static final String ELEMENT_CONNECTOR = "connector";

	/**
	 * "ID" attribute of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Contains the ID of an element
	 * in a {@link TemplateModel}.
	 */
	protected static final String ATTRIBUTE_ID = "id";

	/**
	 * "TYPE" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the type (<i>module</i> or <i>channel</i>) of a
	 * {@link TemplateComponent}.
	 */
	protected static final String ATTRIBUTE_TYPE = "type";

	/**
	 * "MODEL" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the name of the file where the {@link FSAModel} of
	 * {@link TemplateComponent} is saved.
	 */
	protected static final String ATTRIBUTE_FSA = "model";

	/**
	 * "COMPONENT1" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the ID of the "left" {@link TemplateComponent} in a
	 * {@link TemplateLink}.
	 */
	protected static final String ATTRIBUTE_LEFT = "component1";

	/**
	 * "COMPONENT2" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the ID of the "right" {@link TemplateComponent} in a
	 * {@link TemplateLink}.
	 */
	protected static final String ATTRIBUTE_RIGHT = "component2";

	/**
	 * "EVENT1" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the name of the event from the "left" {@link TemplateComponent}
	 * linked by a {@link TemplateLink}.
	 */
	protected static final String ATTRIBUTE_LEFTEVENT = "event1";

	/**
	 * "EVENT2" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the name of the event from the "right" {@link TemplateComponent}
	 * linked by a {@link TemplateLink}.
	 */
	protected static final String ATTRIBUTE_RIGHTEVENT = "event2";

	/**
	 * "COMPONENT" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the ID of the {@link TemplateComponent} whose layout information
	 * is maintained by the {@link Entity}.
	 */
	protected static final String ATTRIBUTE_COMPONENT = "component";

	/**
	 * "X" attribute of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Contains the x-axis location
	 * of an {@link Entity}.
	 */
	protected static final String ATTRIBUTE_X = "x";

	/**
	 * "Y" attribute of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Contains the y-axis location
	 * of an {@link Entity}.
	 */
	protected static final String ATTRIBUTE_Y = "y";

	/**
	 * "LABEL" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the label of an {@link Entity}.
	 */
	protected static final String ATTRIBUTE_LABEL = "label";

	/**
	 * "COLOR" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains the color of the icon representing an {@link Entity}.
	 */
	protected static final String ATTRIBUTE_COLOR = "color";

	/**
	 * "TAG" attribute of the XML structure used to save a {@link TemplateModel}
	 * and the associated {@link TemplateDiagram}. Contains the text inside the
	 * icon representing an {@link Entity}.
	 */
	protected static final String ATTRIBUTE_TAG = "tag";

	/**
	 * "FLAG" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Contains information if the icon of an {@link Entity} is "flagged"
	 */
	protected static final String ATTRIBUTE_FLAG = "flag";

	/**
	 * "ICON" attribute of the XML structure used to save a
	 * {@link TemplateModel} and the associated {@link TemplateDiagram}.
	 * Reserved attribute.
	 */
	protected static final String ATTRIBUTE_ICON = "icon";

	public String getIOTypeDescriptor()
	{
		return TYPE;
	}

	public Set<String> getMetaTags()
	{
		Set<String> tags = new HashSet<String>();
		tags.add(META);
		return tags;
	}

	/**
	 * Derive a name for the file where the model of a {@link TemplateComponent}
	 * will be saved, from the name of the model of the
	 * {@link TemplateComponent}.
	 * 
	 * @param parentFile
	 *            name of the file where the {@link TemplateModel} is saved
	 * @param component
	 *            the {@link TemplateComponent} for which to derive a file name
	 * @return the file name to be used for saving the model of the
	 *         {@link TemplateComponent}
	 */
	protected String component2File(File parentFile, TemplateComponent component)
	{
		StringBuffer name = new StringBuffer();
		if (component.hasModel())
		{
			name.append("_");
			String modelName = component.getModel().getName();
			if (modelName.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				modelName = modelName.substring(TemplateModel.FSA_NAME_PREFIX
						.length());
			}
			for (Character ch : modelName.toCharArray())
			{
				if (Character.isLetterOrDigit(ch))
				{
					name.append(ch);
				}
			}
		}
		String parentName = parentFile.getName();
		if (parentName.endsWith("." + IOSubsytem.MODEL_FILE_EXT))
		{
			parentName = parentName.substring(0, parentName.length()
					- IOSubsytem.MODEL_FILE_EXT.length() - 1);
		}
		return parentName + "_" + component.getId() + name + "."
				+ IOSubsytem.MODEL_FILE_EXT;
	}

	/**
	 * Derive a name for the model of a {@link TemplateComponent}, from the name
	 * of the file where the model of the {@link TemplateComponent} is saved.
	 * 
	 * @param parentFile
	 *            name of the file where the {@link TemplateModel} is saved
	 * @param file
	 *            the file where the model of the {@link TemplateComponent} is
	 *            saved
	 * @return the name to be used for the model of the
	 *         {@link TemplateComponent}
	 */
	protected String file2Component(File parentFile, File file)
	{
		String name = file.getName();
		if (name.endsWith("." + IOSubsytem.MODEL_FILE_EXT))
		{
			name = name.substring(0, name.length()
					- IOSubsytem.MODEL_FILE_EXT.length() - 1);
		}
		String parentName = parentFile.getName();
		if (parentName.endsWith("." + IOSubsytem.MODEL_FILE_EXT))
		{
			parentName = parentName.substring(0, parentName.length()
					- IOSubsytem.MODEL_FILE_EXT.length() - 1);
		}
		if (name.startsWith(parentName))
		{
			name = name.substring(parentName.length());
		}
		if (name.indexOf("_") >= 0)
		{
			name = name.substring(name.indexOf("_") + 1);
		}
		if (name.indexOf("_") >= 0)
		{
			name = name.substring(name.indexOf("_") + 1);
		}
		return name;
	}

	public void saveData(PrintStream stream, DESModel model, String file)
			throws FileSaveException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new FileSaveException(Hub.string("TD_ioWrongModelType"));
		}
		TemplateModel td = (TemplateModel)model;
		File outFile = new File(file);
		boolean savingToNewFile = !td.hasAnnotation(LAST_SAVE_FILE)
				|| !outFile.equals(td.getAnnotation(LAST_SAVE_FILE));
		Map<TemplateComponent, File> fileMap = new HashMap<TemplateComponent, File>();
		Set<File> overwritten = new TreeSet<File>();
		for (TemplateComponent component : td.getComponents())
		{
			if (component.hasModel())
			{
				fileMap.put(component, new File(outFile
						.getParentFile().getAbsolutePath()
						+ File.separator + component2File(outFile, component)));
				if (!component.getModel().hasAnnotation(FILE)
						|| !fileMap.get(component).equals(component
								.getModel().getAnnotation(FILE)))
				{
					if (fileMap.get(component).exists())
					{
						overwritten.add(fileMap.get(component));
					}
				}
			}
		}
		if (!overwritten.isEmpty())
		{
			String message = Hub.string("TD_ioWarnOverwrite1") + "\n";
			for (File overfile : overwritten)
			{
				message += overfile.getAbsolutePath() + "\n";
			}
			message += Hub.string("TD_ioWarnOverwrite2") + "\n"
					+ Hub.string("TD_ioWarnOverwrite3");
			int choice = JOptionPane.showConfirmDialog(Hub.getMainWindow(),
					GeneralUtils.JOptionPaneKeyBinder.messageLabel(message),
					Hub.string("TD_ioWarnOverwriteTitle"),
					JOptionPane.YES_NO_OPTION);
			if (choice != JOptionPane.YES_OPTION)
			{
				throw new FileSaveException(Hub.string("TD_ioDontOverwrite"));
			}
		}
		Set<File> filesToErase = new HashSet<File>();
		try
		{
			for (TemplateComponent component : fileMap.keySet())
			{
				Hub.getIOSubsystem().save(component.getModel(),
						fileMap.get(component));
				if (!savingToNewFile
						&& component.getModel().hasAnnotation(FILE)
						&& !fileMap.get(component).equals(component
								.getModel().getAnnotation(FILE)))
				{
					filesToErase.add((File)component
							.getModel().getAnnotation(FILE));
				}
				component
						.getModel().setAnnotation(FILE, fileMap.get(component));
			}
		}
		catch (IOException e)
		{
			throw new FileSaveException(e);
		}
		td.setAnnotation(LAST_SAVE_FILE, outFile);
		for (File f : filesToErase)
		{
			f.delete();
		}
		for (TemplateComponent component : td.getComponents())
		{
			stream.print("\t<" + ELEMENT_COMPONENT + " " + ATTRIBUTE_ID + "=\""
					+ component.getId() + "\" " + ATTRIBUTE_TYPE + "=\""
					+ component.getType() + "\"");
			if (component.hasModel())
			{
				stream.print(" "
						+ ATTRIBUTE_FSA
						+ "=\""
						+ ((File)component.getModel().getAnnotation(FILE))
								.getName() + "\"");
			}
			stream.println("/>");
		}
		for (TemplateLink link : td.getLinks())
		{
			stream.println("\t<" + ELEMENT_LINK + " " + ATTRIBUTE_ID + "=\""
					+ link.getId() + "\" " + ATTRIBUTE_LEFT + "=\""
					+ link.getLeftComponent().getId() + "\" " + ATTRIBUTE_RIGHT
					+ "=\"" + link.getRightComponent().getId() + "\" "
					+ ATTRIBUTE_LEFTEVENT + "=\"" + link.getLeftEventName()
					+ "\" " + ATTRIBUTE_RIGHTEVENT + "=\""
					+ link.getRightEventName() + "\"/>");
		}
	}

	public void saveMeta(PrintStream stream, DESModel arg1, String arg2)
			throws FileSaveException
	{
		if (!(arg1 instanceof TemplateModel))
		{
			throw new FileSaveException(Hub.string("TD_ioWrongModelType"));
		}
		if (!META.equals(arg2))
		{
			throw new FileSaveException("TD_ioUnsupportedTag");
		}
		TemplateModel td = (TemplateModel)arg1;
		for (TemplateComponent c : td.getComponents())
		{
			if (c.hasAnnotation(EntityLayout.KEY))
			{
				EntityLayout layout = (EntityLayout)c
						.getAnnotation(EntityLayout.KEY);
				boolean flag = c.hasModel()
						&& c.getModel().hasAnnotation(Entity.FLAG_MARK);
				stream.print("\t<" + ELEMENT_ENTITY + " " + ATTRIBUTE_COMPONENT
						+ "=\"" + c.getId() + "\" " + ATTRIBUTE_LABEL + "=\""
						+ layout.label + "\" " + ATTRIBUTE_X + "=\""
						+ layout.location.x + "\" " + ATTRIBUTE_Y + "=\""
						+ layout.location.y + "\" " + ATTRIBUTE_FLAG + "=\""
						+ flag + "\"");
				if (layout.color != null)
				{
					stream.print(" " + ATTRIBUTE_COLOR + "=\"#"
							+ (layout.color.getRed() < 16 ? "0" : "")
							+ Integer.toHexString(layout.color.getRed())
							+ (layout.color.getGreen() < 16 ? "0" : "")
							+ Integer.toHexString(layout.color.getGreen())
							+ (layout.color.getBlue() < 16 ? "0" : "")
							+ Integer.toHexString(layout.color.getBlue())
							+ "\"");
				}
				if (!"".equals(layout.tag))
				{
					stream.print(" " + ATTRIBUTE_TAG + "=\"" + layout.tag
							+ "\"");
				}
				stream.println("/>");
			}
		}
		if (td.hasAnnotation(EmptyConnectorSet.KEY))
		{
			EmptyConnectorSet emptyConnectors = (EmptyConnectorSet)td
					.getAnnotation(EmptyConnectorSet.KEY);
			for (EmptyConnector c : emptyConnectors)
			{
				stream.println("\t<" + ELEMENT_CONNECTOR + " " + ATTRIBUTE_LEFT
						+ "=\"" + c.leftComponent + "\" " + ATTRIBUTE_RIGHT
						+ "=\"" + c.rightComponent + "\"/>");
			}
		}
	}

	public String getSaveDataVersion()
	{
		return VERSION;
	}

	public String getSaveMetaVersion(String arg0)
	{
		if (META.equals(arg0))
		{
			return VERSION;
		}
		return "";
	}

	public DESModel loadData(String arg0, InputStream arg1, String file)
			throws FileLoadException
	{
		if (!VERSION.equals(arg0))
		{
			throw new FileLoadException(Hub.string("TD_ioUnsupportedVer"));
		}
		Document doc = null;
		try
		{
			byte[] FILE_HEADER = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ System.getProperty("line.separator") + "<data>" + System
					.getProperty("line.separator")).getBytes();
			HeadTailInputStream dataSection = new HeadTailInputStream(
					arg1,
					FILE_HEADER,
					"</data>".getBytes());
			DocumentBuilder parser = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			doc = parser.parse(dataSection);
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
			// System.out.println(doc.getChildNodes().item(i).getNodeName());
			if (doc.getChildNodes().item(i).getNodeName().equals(ELEMENT_DATA))
			{
				dataNode = doc.getChildNodes().item(i);
			}
		}
		if (dataNode == null)
		{
			throw new FileLoadException(Hub.string("TD_ioCantParseFile"));
		}
		String errors = "";
		TemplateModel model = ModelManager
				.instance().createModel(TemplateModel.class);
		model.setAnnotation(LAST_SAVE_FILE, new File(file));
		NodeList children = dataNode.getChildNodes();
		try
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				NamedNodeMap attributes = node.getAttributes();
				if (node.getNodeName().equals(ELEMENT_COMPONENT))
				{
					TemplateComponent component = model.assembleComponent();
					component.setId(Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_ID).getNodeValue()));
					component.setType(Integer.parseInt(attributes
							.getNamedItem(ATTRIBUTE_TYPE).getNodeValue()));
					if (attributes.getNamedItem(ATTRIBUTE_FSA) != null)
					{
						File f = new File(new File(file)
								.getParentFile().getAbsolutePath()
								+ File.separator
								+ attributes
										.getNamedItem(ATTRIBUTE_FSA)
										.getNodeValue());
						try
						{
							FSAModel fsa = (FSAModel)Hub
									.getIOSubsystem().load(f);
							fsa.setName(file2Component(new File(file), f));
							fsa.setAnnotation(FILE, f);
							fsa.setParentModel(model);
							component.setModel(fsa);
						}
						catch (IOException e)
						{
							errors += Hub.string("TD_ioCantLoadFSA")
									+ f.getAbsolutePath() + "\n";
						}
					}
					model.addComponent(component);
				}
				else if (node.getNodeName().equals(ELEMENT_LINK))
				{
					long leftId = Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_LEFT).getNodeValue());
					long rightId = Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_RIGHT).getNodeValue());
					TemplateLink link = model.assembleLink(leftId, rightId);
					link.setId(Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_ID).getNodeValue()));
					link.setLeftEventName(attributes
							.getNamedItem(ATTRIBUTE_LEFTEVENT).getNodeValue());
					link.setRightEventName(attributes
							.getNamedItem(ATTRIBUTE_RIGHTEVENT).getNodeValue());
					if (link.getLeftEventName() == null
							|| link.getRightEventName() == null)
					{
						throw new RuntimeException(Hub
								.string("TD_ioCantParseFile"));
					}
					model.addLink(link);
				}
				else if (node.getNodeType() == Node.TEXT_NODE)
				{
				}
				else
				{
					errors += Hub.string("TD_ioCantParseFile") + " ("
							+ node.getNodeName() + ")\n";
				}
			}
			if (!"".equals(errors))
			{
				throw new RuntimeException(errors);
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new FileLoadException(e.getMessage(), model);
		}
		return model;
	}

	public void loadMeta(String arg0, InputStream arg1, DESModel model,
			String arg3) throws FileLoadException
	{
		if (!VERSION.equals(arg0))
		{
			throw new FileLoadException(Hub.string("TD_ioUnsupportedVer"));
		}
		if (!META.equals(arg3))
		{
			throw new FileLoadException("TD_ioUnsupportedTag");
		}
		Document doc = null;
		try
		{
			byte[] FILE_HEADER = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ System.getProperty("line.separator") + "<data>" + System
					.getProperty("line.separator")).getBytes();
			HeadTailInputStream dataSection = new HeadTailInputStream(
					arg1,
					FILE_HEADER,
					"</data>".getBytes());
			DocumentBuilder parser = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			doc = parser.parse(dataSection);
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
			if (doc.getChildNodes().item(i).getNodeName().equals(ELEMENT_DATA))
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
				NamedNodeMap attributes = node.getAttributes();
				if (node.getNodeName().equals(ELEMENT_ENTITY))
				{
					EntityLayout layout = new EntityLayout();
					long id = Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_COMPONENT).getNodeValue());
					layout.label = attributes
							.getNamedItem(ATTRIBUTE_LABEL).getNodeValue();
					if (layout.label == null)
					{
						layout.label = "" + id;
					}
					layout.location = new Point(Integer.parseInt(attributes
							.getNamedItem(ATTRIBUTE_X).getNodeValue()), Integer
							.parseInt(attributes
									.getNamedItem(ATTRIBUTE_Y).getNodeValue()));
					boolean flag = Boolean.parseBoolean(attributes
							.getNamedItem(ATTRIBUTE_FLAG).getNodeValue());
					if (attributes.getNamedItem(ATTRIBUTE_COLOR) != null)
					{
						try
						{
							layout.color = Color.decode(attributes
									.getNamedItem(ATTRIBUTE_COLOR)
									.getNodeValue());
						}
						catch (NumberFormatException e)
						{
							errors += Hub.string("TD_ioCantParseFile")
									+ " ("
									+ attributes
											.getNamedItem(ATTRIBUTE_COLOR)
											.getNodeValue() + ")\n";
						}
					}
					if (attributes.getNamedItem(ATTRIBUTE_TAG) != null)
					{
						layout.tag = attributes
								.getNamedItem(ATTRIBUTE_TAG).getNodeValue();
					}
					if (layout.tag == null)
					{
						layout.tag = "";
					}
					TemplateComponent component = ((TemplateModel)model)
							.getComponent(id);
					if (component == null)
					{
						errors += Hub.string("TD_ioCantParseFile") + " (" + id
								+ ")\n";
					}
					else
					{
						component.setAnnotation(EntityLayout.KEY, layout);
						if (flag && component.hasModel())
						{
							component
									.getModel().setAnnotation(Entity.FLAG_MARK,
											new Object());
						}
					}
				}
				else if (node.getNodeName().equals(ELEMENT_CONNECTOR))
				{
					EmptyConnectorSet emptyConnectors = (EmptyConnectorSet)model
							.getAnnotation(EmptyConnectorSet.KEY);
					if (emptyConnectors == null)
					{
						emptyConnectors = new EmptyConnectorSet();
					}
					emptyConnectors.add(new EmptyConnector(Long
							.parseLong(attributes
									.getNamedItem(ATTRIBUTE_LEFT)
									.getNodeValue()), Long.parseLong(attributes
							.getNamedItem(ATTRIBUTE_RIGHT).getNodeValue())));
					model.setAnnotation(EmptyConnectorSet.KEY, emptyConnectors);
				}
				else if (node.getNodeType() == Node.TEXT_NODE)
				{
				}
				else
				{
					errors += Hub.string("TD_ioCantParseFile") + " ("
							+ node.getNodeName() + ")\n";
				}
			}
			if (!"".equals(errors))
			{
				throw new RuntimeException(errors);
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new FileLoadException(e.getMessage(), model);
		}
	}

}

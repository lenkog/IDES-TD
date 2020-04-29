/*
 * Copyright (c) 2009, Lenko Grigorov
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

package templates.library;

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

import ides.api.core.Hub;
import ides.api.plugin.io.FileIOPlugin;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.io.FileSaveException;
import ides.api.plugin.io.UnsupportedVersionException;
import ides.api.plugin.model.DESModel;
import ides.api.utilities.HeadTailInputStream;

/**
 * Stores and loads the descriptions of {@link Template}s when the models of the
 * templates are stored and loaded.
 * 
 * @author Lenko Grigorov
 */
public class TemplateMetaIO implements FileIOPlugin {
    /**
     * The "tag" for the meta-data associated with templates.
     */
    protected static final String META_TAG = "template";

    /**
     * The version of the meta-data format.
     */
    protected static final String VERSION = "3";

    /**
     * The "meta" XML element. It is used to encapsulate the meta-data.
     */
    protected static final String ELEMENT_META = "meta";

    /**
     * The "template" XML element. It is used to store the information pertaining to
     * a template.
     */
    protected static final String ELEMENT_INFO = "template";

    /**
     * The "description" XML element. It is used to store the description of a
     * template.
     */
    protected static final String ELEMENT_DESC = "description";

    /**
     * The "color" XML attribute. It is used to store the color of the icon of a
     * template.
     */
    protected static final String ATTRIBUTE_COLOR = "color";

    /**
     * The "tag" XML attribute. It is used to store the "ID" of a template.
     */
    protected static final String ATTRIBUTE_TAG = "tag";

    /**
     * @throws UnsupportedOperationException this method is not supported
     */
    public String getIOTypeDescriptor() {
        throw new UnsupportedOperationException();
    }

    public Set<String> getMetaTags() {
        Set<String> tags = new HashSet<String>();
        tags.add(META_TAG);
        return tags;
    }

    /**
     * @throws UnsupportedOperationException this method is not supported
     */
    public String getSaveDataVersion() {
        throw new UnsupportedOperationException();
    }

    public String getSaveMetaVersion(String arg0) {
        return VERSION;
    }

    /**
     * @throws UnsupportedOperationException this method is not supported
     */
    public DESModel loadData(String arg0, InputStream arg1, String arg2) throws FileLoadException {
        throw new UnsupportedOperationException();
    }

    public void loadMeta(String version, InputStream stream, DESModel model, String tag) throws FileLoadException {
        if (!tag.equals(META_TAG)) {
            throw new FileLoadException(Hub.string("TD_unsupportedMetaTag") + " [" + tag + "]");
        }
        if (!VERSION.equals(version)) {
            throw new UnsupportedVersionException(Hub.string("TD_unsupportedVersion"));
        }
        byte[] FILE_HEADER = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") + "<"
                + ELEMENT_META + ">" + System.getProperty("line.separator")).getBytes();
        HeadTailInputStream metaField = new HeadTailInputStream(stream, FILE_HEADER,
                ("</" + ELEMENT_META + ">").getBytes());
        Document doc = null;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = parser.parse(metaField);
        } catch (ParserConfigurationException e) {
            throw new FileLoadException(e.getMessage());
        } catch (IOException e) {
            throw new FileLoadException(e.getMessage());
        } catch (SAXException e) {
            throw new FileLoadException(e.getMessage());
        }
        if (doc == null) {
            throw new FileLoadException(Hub.string("TD_ioCantParseFile"));
        }
        Node dataNode = null;
        for (int i = 0; i < doc.getChildNodes().getLength(); ++i) {
            if (doc.getChildNodes().item(i).getNodeName().equals(ELEMENT_META)) {
                dataNode = doc.getChildNodes().item(i);
            }
        }
        if (dataNode == null) {
            throw new FileLoadException(Hub.string("TD_ioCantParseFile"));
        }
        String errors = "";
        NodeList children = dataNode.getChildNodes();
        try {
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeName().equals(ELEMENT_INFO)) {
                    NamedNodeMap attributes = node.getAttributes();
                    TemplateDescriptor td = new TemplateDescriptor();
                    td.tag = attributes.getNamedItem(ATTRIBUTE_TAG).getNodeValue();
                    try {
                        td.color = Color.decode(attributes.getNamedItem(ATTRIBUTE_COLOR).getNodeValue());
                    } catch (NumberFormatException e) {
                        errors += Hub.string("TD_ioCantParseFile") + " ("
                                + attributes.getNamedItem(ATTRIBUTE_COLOR).getNodeValue() + ")\n";
                    }
                    for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                        Node child = node.getChildNodes().item(j);
                        if (child.getNodeName().equals(ELEMENT_DESC)) {
                            td.description = child.getTextContent();
                            break;
                        }
                    }
                    model.setAnnotation(Template.TEMPLATE_DESC, td);
                    break;
                }
            }
            if (!"".equals(errors)) {
                throw new RuntimeException(errors);
            }
        } catch (Exception e) {
            throw new FileLoadException(e.getMessage(), model);
        }
    }

    /**
     * @throws UnsupportedOperationException this method is not supported
     */
    public void saveData(PrintStream arg0, DESModel arg1, String arg2) throws FileSaveException {
        throw new UnsupportedOperationException();
    }

    public void saveMeta(PrintStream stream, DESModel arg1, String arg2) throws FileSaveException {
        if (!META_TAG.equals(arg2)) {
            throw new FileSaveException("TD_ioUnsupportedTag");
        }
        if (!arg1.hasAnnotation(Template.TEMPLATE_DESC)) {
            return;
        }
        TemplateDescriptor td = (TemplateDescriptor) arg1.getAnnotation(Template.TEMPLATE_DESC);
        stream.println("\n\t<" + ELEMENT_INFO + " " + ATTRIBUTE_TAG + "=\"" + td.tag + "\" " + ATTRIBUTE_COLOR + "=\"#"
                + (td.color.getRed() < 16 ? "0" : "") + Integer.toHexString(td.color.getRed())
                + (td.color.getGreen() < 16 ? "0" : "") + Integer.toHexString(td.color.getGreen())
                + (td.color.getBlue() < 16 ? "0" : "") + Integer.toHexString(td.color.getBlue()) + "\">");
        stream.println("\t\t<" + ELEMENT_DESC + ">" + td.description + "</" + ELEMENT_DESC + ">");
        stream.println("\t</" + ELEMENT_INFO + ">");
    }

}

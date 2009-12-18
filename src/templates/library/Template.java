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

import ides.api.core.Annotable;
import ides.api.model.fsa.FSAModel;

import java.awt.datatransfer.DataFlavor;

import templates.utils.EntityIcon;

/**
 * Describes a template (available in a template library).
 * 
 * @author Lenko Grigorov
 */
public interface Template
{
	/**
	 * The key to be used when a {@link TemplateDescriptor} is added as an
	 * annotation.
	 * 
	 * @see Annotable
	 */
	public static final String TEMPLATE_DESC = "templates.library.TemplateDescriptor";

	/**
	 * The {@link DataFlavor} for a {@link Template}.
	 */
	public static final DataFlavor templateFlavor = new DataFlavor(
			Template.class,
			"template");

	/**
	 * The {@link DataFlavor} for an {@link FSAModel}.
	 */
	public static final DataFlavor fsaFlavor = new DataFlavor(
			FSAModel.class,
			"FSA");

	/**
	 * Retrieve the "ID" of the template. This is a short string which can be
	 * used to identify the template, e.g., by displaying it inside the template
	 * icon.
	 * 
	 * @return the "ID" of the template
	 */
	public String getName();

	/**
	 * Retrieve the icon of the template. This icon can be used when visualizing
	 * the template.
	 * 
	 * @return the icon of the template
	 */
	public EntityIcon getIcon();

	/**
	 * Retrieve the description of the template. This can be a longer piece of
	 * text including any notes about the template.
	 * 
	 * @return the description of the template
	 */
	public String getDescription();

	/**
	 * Retrieve the {@link FSAModel} on which the template is based.
	 * 
	 * @return the {@link FSAModel} on which the template is based
	 */
	public FSAModel getModel();

	/**
	 * Create a new instance of the template. The instance is a copy of the
	 * underlying model.
	 * 
	 * @return a new instance of the template
	 */
	public FSAModel instantiate();
}

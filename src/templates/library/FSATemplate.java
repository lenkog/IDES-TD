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

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import templates.diagram.SimpleIcon;
import templates.utils.EntityIcon;

/**
 * Implementation of a {@link Template} which is based on an {@link FSAModel}.
 * 
 * @author Lenko Grigorov
 */
public class FSATemplate implements Template
{
	/**
	 * The "ID" of the template. This is a short string which will appear in the
	 * icon of the template.
	 */
	protected String tag;

	/**
	 * The description of the template.
	 */
	protected String description;

	/**
	 * The {@link FSAModel} underlying the template.
	 */
	protected FSAModel model;

	/**
	 * The icon of the template.
	 */
	protected EntityIcon icon;

	/**
	 * Create a template based on an {@link FSAModel}, with the properties
	 * provided in the given {@link TemplateDescriptor}.
	 * 
	 * @param td
	 *            the descriptor with the properties for the new template
	 * @param model
	 *            the FSA model on which the template should be based
	 */
	public FSATemplate(TemplateDescriptor td, FSAModel model)
	{
		tag = td.tag;
		description = td.description;
		this.model = model;
		icon = new SimpleIcon(tag, td.color, Hub
				.getMainWindow().getGraphics().create());
	}

	public EntityIcon getIcon()
	{
		return icon;
	}

	public String getName()
	{
		return tag;
	}

	public FSAModel instantiate()
	{
		return model.clone();
	}

	public String getDescription()
	{
		return description;
	}

	public FSAModel getModel()
	{
		return model;
	}

}

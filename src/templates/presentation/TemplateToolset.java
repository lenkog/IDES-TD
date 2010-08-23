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

package templates.presentation;

import ides.api.core.Hub;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.presentation.Presentation;
import ides.api.plugin.presentation.Toolset;
import ides.api.plugin.presentation.UIDescriptor;
import ides.api.plugin.presentation.UnsupportedModelException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import templates.library.LibraryUI;
import templates.model.TemplateModel;
import templates.model.TemplateModelSubscriber;

/**
 * The toolset which describes the UI elements to be displayed when a
 * {@link TemplateModel} is activated in IDES.
 * 
 * @author Lenko Grigorov
 */
public class TemplateToolset implements Toolset
{
	/**
	 * The template library UI element. The same element is used for all
	 * template designs opened in IDES.
	 */
	protected static LibraryUI library = null;

	/**
	 * Supports the drop of a template from the template library onto the canvas
	 * during a drag-and-drop.
	 */
	protected static TransferHandler transferHandler = new CanvasTransferHandler();

	/**
	 * The {@link UIDescriptor} which specifies the UI elements to be displayed
	 * when a {@link TemplateModel} is activated in IDES.
	 * 
	 * @author Lenko Grigorov
	 */
	protected class TemplateUID implements UIDescriptor
	{
		/**
		 * The template design which is displayed by the {@link Presentation}s
		 * specified in this descriptor.
		 */
		protected TemplateModel model;

		/**
		 * The main canvas which lets the user edit the template design.
		 */
		protected TemplateEditableCanvas canvas;

		/**
		 * The canvas which highlights the consistency issues in the template
		 * design.
		 */
		protected TemplateConsistencyCanvas consistency;

		/**
		 * The {@link Presentation} which lists the consistency issues in the
		 * template design.
		 */
		protected IssuesViewer issues;

		/**
		 * The status bar to be displayed when the {@link TemplateModel} is
		 * activated in IDES.
		 */
		protected Presentation statusBar;

		/**
		 * Construct the {@link UIDescriptor} for the given
		 * {@link TemplateModel}.
		 * 
		 * @param model
		 *            the template design for which to construct a descriptor
		 */
		public TemplateUID(TemplateModel model)
		{
			if (library == null)
			{
				library = new LibraryUI();
			}
			this.model = model;
			canvas = new TemplateEditableCanvas(model);
			canvas.setName(Hub.string("TD_modelCanvasTitle"));
			canvas.setTransferHandler(transferHandler);
			consistency = new TemplateConsistencyCanvas(model);
			consistency.setName(Hub.string("TD_consistencyCanvasTitle"));
			consistency.setTransferHandler(transferHandler);
			issues = new IssuesViewer(canvas.getDiagram());
			statusBar = new TemplateStatusBar(model, issues);
		}

		public Presentation[] getLeftPanePresentations()
		{
			return new Presentation[] {};
		}

		public Presentation[] getMainPanePresentations()
		{
			return new Presentation[] { canvas, consistency };
		}

		public JMenu[] getMenus()
		{
			return new JMenu[] {};
		}

		public Presentation[] getRightPanePresentations()
		{
			return new Presentation[] { library, issues };
		}

		public Presentation getStatusBar()
		{
			return statusBar;
		}

		public JToolBar getToolbar()
		{
			return new JToolBar();
		}
	}

	public Presentation getModelThumbnail(DESModel model, int width, int height)
			throws UnsupportedModelException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new UnsupportedModelException();
		}
		return new TemplateCanvas((TemplateModel)model);
	}

	public UIDescriptor getUIElements(DESModel model)
			throws UnsupportedModelException
	{
		if (!(model instanceof TemplateModel))
		{
			throw new UnsupportedModelException();
		}
		return new TemplateUID((TemplateModel)model);
	}

	/**
	 * Status bar for {@link TemplateModel}s. Displays information the number of
	 * template components and the number of consistency issues in the template
	 * design.
	 * 
	 * @author Lenko Grigorov
	 */
	protected static class TemplateStatusBar extends JLabel implements
			Presentation, DESModelSubscriber, ListDataListener
	{
		private static final long serialVersionUID = 341918799019344384L;

		/**
		 * Information about whether to keep track of updates to the template
		 * design. Set to <code>true</code> to track updates, set to
		 * <code>false</code> otherwise.
		 */
		private boolean trackModel = true;

		/**
		 * The template design whose status information is displayed.
		 */
		protected TemplateModel model;

		/**
		 * The UI element listing the consistency issues in the template design.
		 * The consistency issues count is retrieved from this element.
		 */
		protected IssuesViewer issueList;

		/**
		 * Construct and set up a status bar for the given template design.
		 * 
		 * @param model
		 *            the template design whose status information will be
		 *            displayed
		 * @param issueList
		 *            the consistency issues viewer which provide intomation
		 *            about the count of consistency issues in the template
		 *            design
		 */
		public TemplateStatusBar(TemplateModel model, IssuesViewer issueList)
		{
			this.model = model;
			this.issueList = issueList;
			model.addSubscriber((DESModelSubscriber)this);
			issueList.addIssuesListener(this);
			refresh();
		}

		/**
		 * Update the information displayed in the status bar.
		 */
		public void refresh()
		{
			String issues = "";
			if (issueList.getIssueCount() > 0)
			{
				if (issueList.getIssueCount() == 1)
				{
					issues = " " + Hub.string("TD_statusBarIssues1a") + " "
							+ issueList.getIssueCount() + " "
							+ Hub.string("TD_statusBarIssues2a");
				}
				else
				{
					issues = " " + Hub.string("TD_statusBarIssues1b") + " "
							+ issueList.getIssueCount() + " "
							+ Hub.string("TD_statusBarIssues2b");
				}
			}
			setText(model.getName() + ":  " + +model.getComponentCount() + " "
					+ Hub.string("TD_statsBarEntities") + "." + issues);
		}

		public void forceRepaint()
		{
			refresh();
			repaint();
		}

		public JComponent getGUI()
		{
			return this;
		}

		public DESModel getModel()
		{
			return model;
		}

		public void release()
		{
			issueList.removeIssuesListener(this);
			model.removeSubscriber((DESModelSubscriber)this);
		}

		public void setTrackModel(boolean b)
		{
			if (trackModel != b)
			{
				trackModel = b;
				if (trackModel)
				{
					model.addSubscriber((DESModelSubscriber)this);
					issueList.addIssuesListener(this);
				}
				else
				{
					issueList.removeIssuesListener(this);
					model.removeSubscriber((TemplateModelSubscriber)this);
				}
			}
		}

		/**
		 * Update the information displayed in the status bar.
		 */
		public void modelNameChanged(DESModelMessage arg0)
		{
			refresh();
		}

		/**
		 * Do nothing.
		 */
		public void saveStatusChanged(DESModelMessage arg0)
		{
		}

		/**
		 * Update the information displayed in the status bar.
		 */
		public void contentsChanged(ListDataEvent arg0)
		{
			refresh();
		}

		/**
		 * Update the information displayed in the status bar.
		 */
		public void intervalAdded(ListDataEvent arg0)
		{
			refresh();
		}

		/**
		 * Update the information displayed in the status bar.
		 */
		public void intervalRemoved(ListDataEvent arg0)
		{
			refresh();
		}
	}
}

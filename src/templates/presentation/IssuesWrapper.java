package templates.presentation;

import ides.api.core.Hub;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagram;
import templates.diagram.actions.DiagramActions;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.Validator;
import templates.model.Validator.ValidatorResult;

public class IssuesWrapper
{
	protected static class ConvertAction extends AbstractAction
	{
		private static final long serialVersionUID = 1042009036397461571L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected int type;

		public ConvertAction(String label, TemplateDiagram diagram,
				Entity entity, int type)
		{
			super(label);
			this.diagram = diagram;
			this.entity = entity;
			this.type = type;
		}

		public void actionPerformed(ActionEvent e)
		{
			new DiagramActions.SetTypeAction(diagram, entity, type).execute();
		}
	}

	protected static class SetEventsAction extends AbstractAction
	{
		private static final long serialVersionUID = -6787216038250603204L;

		protected Connector connector;

		public SetEventsAction(Connector connector)
		{
			super(Hub.string("TD_comAssignEvents"));
			this.connector = connector;
		}

		public void actionPerformed(ActionEvent e)
		{
			TemplateConsistencyCanvas canvas = Hub
					.getWorkspace()
					.getPresentationsOfType(TemplateConsistencyCanvas.class)
					.iterator().next();
			new UIActions.AssignEventsAction(canvas, connector)
					.actionPerformed(e);
		}
	}

	protected static class AssignFSAAction extends AbstractAction
	{
		private static final long serialVersionUID = -7713091978419119233L;

		protected Entity entity;

		public AssignFSAAction(Entity entity)
		{
			super(Hub.string("TD_comAssignFSA"));
			this.entity = entity;
		}

		public void actionPerformed(ActionEvent e)
		{
			TemplateConsistencyCanvas canvas = Hub
					.getWorkspace()
					.getPresentationsOfType(TemplateConsistencyCanvas.class)
					.iterator().next();
			new UIActions.AssignFSAAction(canvas, entity).actionPerformed(e);
		}
	}

	public static List<IssueDescriptor> getIssues(TemplateDiagram diagram)
	{
		LinkedList<IssueDescriptor> issues = new LinkedList<IssueDescriptor>();
		List<ValidatorResult> results = Validator.validate(diagram.getModel());

		Set<Connector> moduleChannels = new HashSet<Connector>();
		Set<Connector> noEvents = new HashSet<Connector>();

		for (Connector c : diagram.getConnectors())
		{
			if (c.getLinks().isEmpty())
			{
				issues
						.add(new IssueDescriptor(
								describe(c) + "\n"
										+ Hub.string("TD_issueWNoLinks"),
								IssueDescriptor.TYPE_ERROR,
								Arrays.asList(new DiagramElement[] { c }),
								Arrays
										.asList(new Action[] { new SetEventsAction(
												c) })));
			}
		}
		for (ValidatorResult result : results)
		{
			Set<DiagramElement> elements = new HashSet<DiagramElement>();
			String message = "";
			if (!result.components.isEmpty())
			{
				message += describe(diagram.getEntityFor(result.components
						.get(0)))
						+ "\n";
			}
			else if (!result.links.isEmpty())
			{
				message += describe(diagram
						.getConnectorFor(result.links.get(0)))
						+ "\n";
			}
			int type = result.type == ValidatorResult.WARNING ? IssueDescriptor.TYPE_WARNING
					: IssueDescriptor.TYPE_ERROR;
			for (TemplateComponent component : result.components)
			{
				elements.add(diagram.getEntityFor(component));
			}
			for (TemplateLink link : result.links)
			{
				elements.add(diagram.getConnectorFor(link));
			}
			List<Action> fixes = new LinkedList<Action>();
			try
			{
				if (result.message.equals(Validator.ERROR_MODULE_CHANNEL))
				{
					Connector c = diagram.getConnectorFor(result.links.get(0));
					if (c == null)
					{
						message += result.message;
					}
					else if (!moduleChannels.contains(c))
					{
						moduleChannels.add(c);
						message += Hub.string("TD_issueWModuleChannel") + "\n";
						if (c.getLeftEntity().getComponent().getType() == TemplateComponent.TYPE_MODULE)
						{
							message += Hub.string("TD_issueWConverChannel");
							fixes.add(new ConvertAction(
									Hub.string("TD_fixConvert") + " "
											+ c.getLeftEntity().getLabel(),
									diagram,
									c.getLeftEntity(),
									TemplateComponent.TYPE_CHANNEL));
							fixes.add(new ConvertAction(
									Hub.string("TD_fixConvert") + " "
											+ c.getRightEntity().getLabel(),
									diagram,
									c.getRightEntity(),
									TemplateComponent.TYPE_CHANNEL));
						}
						else
						{
							message += Hub.string("TD_issueWConverModule");
							fixes.add(new ConvertAction(
									Hub.string("TD_fixConvert") + " "
											+ c.getLeftEntity().getLabel(),
									diagram,
									c.getLeftEntity(),
									TemplateComponent.TYPE_MODULE));
							fixes.add(new ConvertAction(
									Hub.string("TD_fixConvert") + " "
											+ c.getRightEntity().getLabel(),
									diagram,
									c.getRightEntity(),
									TemplateComponent.TYPE_MODULE));
						}
					}
					else
					{
						throw new SkipIssueException();
					}
				}
				else if (result.message.equals(Validator.WARNING_FREE_EVENT))
				{
					Entity e = diagram.getEntityFor(result.components.get(0));
					if (e == null)
					{
						message += result.message;
					}
					else
					{
						message += Hub.string("TD_issueWFreeEvent");
					}
				}
				else if (result.message.equals(Validator.ERROR_NO_EVENT))
				{
					Connector c = diagram.getConnectorFor(result.links.get(0));
					if (c == null)
					{
						message += result.message;
					}
					else if (!noEvents.contains(c))
					{
						noEvents.add(c);
						message += Hub.string("TD_issueWNoEvent");
						fixes.add(new SetEventsAction(c));
					}
					else
					{
						throw new SkipIssueException();
					}
				}
				else if (result.message
						.equals(Validator.WARNING_FREE_COMPONENT))
				{
					Entity e = diagram.getEntityFor(result.components.get(0));
					if (e == null)
					{
						message += result.message;
					}
					else if (diagram.getAdjacentConnectors(e).isEmpty())
					{
						message += Hub.string("TD_issueWFreeComponent");
					}
					else
					{
						throw new SkipIssueException();
					}
				}
				else if (result.message.equals(Validator.ERROR_FORKED_EVENT))
				{
					Entity e = diagram.getEntityFor(result.components.get(0));
					if (e == null)
					{
						message += result.message;
					}
					else
					{
						message += Hub.string("TD_issueWForkedEvent1") + " \'"
								+ result.event + "\' "
								+ Hub.string("TD_issueWForkedEvent2");
					}
				}
				else if (result.message.equals(Validator.ERROR_MERGED_EVENT))
				{
					if (result.components.size() < 2)
					{
						message += result.message;
					}
					else
					{
						Entity e = diagram.getEntityFor(result.components
								.get(1));
						message += Hub.string("TD_issueWMergedEvent1") + " \'"
								+ result.event + "\' "
								+ Hub.string("TD_issueWMergedEvent2") + " \'"
								+ e.getLabel() + "\'.";
					}
				}
				else if (result.message.equals(Validator.ERROR_NO_MODEL))
				{
					Entity e = diagram.getEntityFor(result.components.get(0));
					if (e == null)
					{
						message += result.message;
					}
					else
					{
						message += Hub.string("TD_issueWNoModel");
						fixes.add(new AssignFSAAction(e));
					}
				}
				else
				{
					message += result.message;
				}
			}
			catch (SkipIssueException e)
			{
				continue;
			}
			issues.add(new IssueDescriptor(message, type, elements, fixes));
		}
		return issues;
	}

	protected static String describe(Connector c)
	{
		if (c == null)
		{
			return "";
		}
		return Hub.string("TD_describeConnector1") + " \'"
				+ c.getLeftEntity().getLabel() + "\' "
				+ Hub.string("TD_describeConnector2") + " \'"
				+ c.getRightEntity().getLabel() + "\'";
	}

	protected static String describe(Entity e)
	{
		if (e == null)
		{
			return "";
		}
		return Hub.string("TD_describeEntity") + " \'" + e.getLabel() + "\'";
	}
}

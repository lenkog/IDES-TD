package templates.diagram.actions;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.SimpleIcon;
import templates.diagram.TemplateDiagram;
import templates.library.Template;
import templates.model.TemplateLink;
import templates.presentation.Helpers;
import templates.utils.EntityIcon;

public class DiagramActions
{

	public static class CreateEntityAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4318087259767201282L;

		protected TemplateDiagram diagram;

		protected Point location;

		protected Entity[] buffer;

		public CreateEntityAction(TemplateDiagram diagram, Point location)
		{
			this(null, diagram, location, null);
		}

		public CreateEntityAction(TemplateDiagram diagram, Point location,
				Entity[] buffer)
		{
			this(null, diagram, location, buffer);
		}

		public CreateEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Point location)
		{
			this(parent, diagram, location, null);
		}

		public CreateEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Point location, Entity[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.location = location;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.CreateEntityEdit edit = new DiagramUndoableEdits.CreateEntityEdit(
						diagram,
						location);
				edit.redo();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = edit.getEntity();
				}
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	public static class CreateTemplateInstanceAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -8369451029215842373L;

		protected TemplateDiagram diagram;

		protected Template template;
		
		protected Point location;

		protected Entity[] buffer;

		public CreateTemplateInstanceAction(TemplateDiagram diagram, Template template, Point location)
		{
			this(null, diagram, template, location, null);
		}

		public CreateTemplateInstanceAction(TemplateDiagram diagram, Template template, Point location,
				Entity[] buffer)
		{
			this(null, diagram, template, location, buffer);
		}

		public CreateTemplateInstanceAction(CompoundEdit parent, TemplateDiagram diagram,
				Template template, Point location)
		{
			this(parent, diagram, template, location, null);
		}

		public CreateTemplateInstanceAction(CompoundEdit parent, TemplateDiagram diagram,
				Template template, Point location, Entity[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.template=template;
			this.location = location;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits=new CompoundEdit();
				Entity[] entityBuf=new Entity[1];
				new DiagramActions.CreateEntityAction(allEdits,diagram,location,entityBuf).execute();
				new DiagramActions.AssignFSAAction(allEdits,diagram,entityBuf[0],template.instantiate(),template.getIcon().clone()).execute();
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(Hub.string("TD_undoCreateEntity")));
				allEdits.end();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = entityBuf[0];
				}
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	public static class CreateConnectorAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6870236352320831902L;

		protected TemplateDiagram diagram;

		protected Entity left;

		protected Entity right;

		protected Connector[] buffer;

		public CreateConnectorAction(TemplateDiagram diagram, Entity left,
				Entity right)
		{
			this(null, diagram, left, right, null);
		}

		public CreateConnectorAction(TemplateDiagram diagram, Entity left,
				Entity right, Connector[] buffer)
		{
			this(null, diagram, left, right, buffer);
		}

		public CreateConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right)
		{
			this(parent, diagram, left, right, null);
		}

		public CreateConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right,
				Connector[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.left = left;
			this.right = right;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.CreateConnectorEdit edit = new DiagramUndoableEdits.CreateConnectorEdit(
						diagram,
						left,
						right);
				edit.redo();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = edit.getConnector();
				}
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	public static class CreateAndMatchConnectorAction extends
			AbstractDiagramAction
	{
		private static final long serialVersionUID = 2328335456634040094L;

		protected TemplateDiagram diagram;

		protected Entity left;

		protected Entity right;

		protected Connector[] buffer;

		public CreateAndMatchConnectorAction(TemplateDiagram diagram,
				Entity left, Entity right)
		{
			this(null, diagram, left, right, null);
		}

		public CreateAndMatchConnectorAction(TemplateDiagram diagram,
				Entity left, Entity right, Connector[] buffer)
		{
			this(null, diagram, left, right, buffer);
		}

		public CreateAndMatchConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right)
		{
			this(parent, diagram, left, right, null);
		}

		public CreateAndMatchConnectorAction(CompoundEdit parent,
				TemplateDiagram diagram, Entity left, Entity right,
				Connector[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.left = left;
			this.right = right;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				Connector[] myBuffer = new Connector[1];
				new CreateConnectorAction(
						allEdits,
						diagram,
						left,
						right,
						myBuffer).execute();
				String undoLabel = allEdits.getPresentationName();
				new MatchEventsAction(allEdits, diagram, myBuffer[0]).execute();
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						undoLabel));
				allEdits.end();
				if (buffer != null && buffer.length > 0)
				{
					buffer[0] = myBuffer[0];
				}
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	public static class AddLinkAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -8377001331734586265L;

		protected TemplateDiagram diagram;

		protected Connector connector;

		protected String leftEvent;

		protected String rightEvent;

		public AddLinkAction(TemplateDiagram diagram, Connector connector,
				String leftEvent, String rightEvent)
		{
			this(null, diagram, connector, leftEvent, rightEvent);
		}

		public AddLinkAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector, String leftEvent, String rightEvent)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
			this.leftEvent = leftEvent;
			this.rightEvent = rightEvent;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.AddLinkEdit edit = new DiagramUndoableEdits.AddLinkEdit(
						diagram,
						connector,
						leftEvent,
						rightEvent);
				edit.redo();
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	public static class DeleteElementsAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 4993580265901392619L;

		protected TemplateDiagram diagram;

		protected Collection<DiagramElement> elements;

		public DeleteElementsAction(TemplateDiagram diagram,
				Collection<DiagramElement> elements)
		{
			this(null, diagram, elements);
		}

		public DeleteElementsAction(CompoundEdit parent,
				TemplateDiagram diagram, Collection<DiagramElement> elements)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.elements = elements;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits = new CompoundEdit();
				int connectors = 0;
				int entities = 0;
				for (DiagramElement element : elements)
				{
					if (element instanceof Connector)
					{
						DiagramUndoableEdits.RemoveConnectorEdit edit = new DiagramUndoableEdits.RemoveConnectorEdit(
								diagram,
								(Connector)element);
						edit.redo();
						allEdits.addEdit(edit);
						connectors++;
					}
				}
				for (DiagramElement element : elements)
				{
					if (element instanceof Entity)
					{
						DiagramUndoableEdits.RemoveEntityEdit edit = new DiagramUndoableEdits.RemoveEntityEdit(
								diagram,
								(Entity)element);
						edit.redo();
						allEdits.addEdit(edit);
						entities++;
					}
				}
				if (entities > 0 && connectors > 0)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveElements")));
				}
				else if (entities > 1)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveEntities")));
				}
				else if (connectors > 1)
				{
					allEdits
							.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
									Hub.string("TD_undoRemoveConnectors")));
				}
				allEdits.end();
				postEdit(allEdits);
			}
		}
	}

	public static class RemoveLinksAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1134740009176987043L;

		protected TemplateDiagram diagram;

		protected Connector connector;

		protected Collection<TemplateLink> links;

		public RemoveLinksAction(TemplateDiagram diagram, Connector connector,
				Collection<TemplateLink> links)
		{
			this(null, diagram, connector, links);
		}

		public RemoveLinksAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector, Collection<TemplateLink> links)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
			this.links = links;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null && !links.isEmpty())
			{
				CompoundEdit allEdits = new CompoundEdit();
				for (TemplateLink link : links)
				{
					DiagramUndoableEdits.RemoveLinkEdit edit = new DiagramUndoableEdits.RemoveLinkEdit(
							diagram,
							connector,
							link);
					edit.redo();
					allEdits.addEdit(edit);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	public static class MovedSelectionAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1222866680866778507L;

		protected TemplateDiagram diagram;

		protected Point delta;

		protected Collection<DiagramElement> selection;

		public MovedSelectionAction(TemplateDiagram diagram,
				Collection<DiagramElement> selection, Point delta)
		{
			this(null, diagram, selection, delta);
		}

		public MovedSelectionAction(CompoundEdit parent,
				TemplateDiagram diagram, Collection<DiagramElement> selection,
				Point delta)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.selection = selection;
			this.delta = delta;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.MovedSelectionEdit edit = new DiagramUndoableEdits.MovedSelectionEdit(
						diagram,
						selection,
						delta);
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	public static class LabelEntityAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6200645190959701337L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected String label;

		public LabelEntityAction(TemplateDiagram diagram, Entity entity,
				String label)
		{
			this(null, diagram, entity, label);
		}

		public LabelEntityAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, String label)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.label = label;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.LabelEntityEdit edit = new DiagramUndoableEdits.LabelEntityEdit(
						diagram,
						entity,
						label);
				edit.redo();
				postEditAdjustCanvas(diagram.getModel(), diagram, edit);
			}
		}
	}

	public static class MatchEventsAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 897761928605656221L;

		protected TemplateDiagram diagram;

		protected Connector connector;

		public MatchEventsAction(TemplateDiagram diagram, Connector connector)
		{
			this(null, diagram, connector);
		}

		public MatchEventsAction(CompoundEdit parent, TemplateDiagram diagram,
				Connector connector)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.connector = connector;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				Set<String> matches = Helpers.matchEvents(connector);
				CompoundEdit allEdits = new CompoundEdit();
				new RemoveLinksAction(allEdits, diagram, connector, connector
						.getLinks()).execute();
				for (String name : matches)
				{
					DiagramUndoableEdits.AddLinkEdit edit = new DiagramUndoableEdits.AddLinkEdit(
							diagram,
							connector,
							name,
							name);
					edit.redo();
					allEdits.addEdit(edit);
				}
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(
						Hub.string("TD_comMatchEvents")));
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	public static class ShiftDiagramInViewAction extends AbstractAction
	{
		private static final long serialVersionUID = 2907001062138002843L;

		protected CompoundEdit parentEdit = null;

		protected TemplateDiagram diagram;

		public ShiftDiagramInViewAction(TemplateDiagram diagram)
		{
			this(null, diagram);
		}

		public ShiftDiagramInViewAction(CompoundEdit parentEdit,
				TemplateDiagram diagram)
		{
			this.parentEdit = parentEdit;
			this.diagram = diagram;
		}

		public void actionPerformed(ActionEvent event)
		{
			if (diagram != null)
			{
				Rectangle bounds = diagram.getBounds();
				if (bounds.x < 0 || bounds.y < 0)
				{
					UndoableEdit translation = new DiagramUndoableEdits.TranslateDiagramEdit(
							diagram,
							new Point(
									-bounds.x
											+ TemplateDiagram.DESIRED_DIAGRAM_INSET,
									-bounds.y
											+ TemplateDiagram.DESIRED_DIAGRAM_INSET));
					translation.redo();
					if (parentEdit != null)
					{
						parentEdit.addEdit(translation);
					}
					else
					{
						Hub.getUndoManager().addEdit(translation);
					}
				}
			}
		}

		public void execute()
		{
			actionPerformed(null);
		}
	}

	public static class AssignNewFSAAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 9167035481992348194L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected FSAModel[] buffer;

		public AssignNewFSAAction(TemplateDiagram diagram, Entity entity)
		{
			this(null, diagram, entity, null);
		}

		public AssignNewFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel[] buffer)
		{
			this(null, diagram, entity, buffer);
		}

		public AssignNewFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity)
		{
			this(parent, diagram, entity, null);
		}

		public AssignNewFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel[] buffer)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.buffer = buffer;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				FSAModel newModel = ModelManager
						.instance().createModel(FSAModel.class);
				if (newModel == null)
				{
					Hub.getNoticeManager().postErrorTemporary(Hub
							.string("TD_shortFSAnotSupported"),
							Hub.string("TD_FSAnotSupported"));
				}
				else
				{
					// add all linked events
					for (Connector c : diagram.getAdjacentConnectors(entity))
					{
						for (TemplateLink link : c.getLinks())
						{
							String event = c.getLeftEntity() == entity ? link
									.getLeftEventName() : link
									.getRightEventName();
							newModel.add(newModel.assembleEvent(event));
						}
					}
					CompoundEdit allEdits=new CompoundEdit();
					DiagramUndoableEdits.AssignFSAEdit edit = new DiagramUndoableEdits.AssignFSAEdit(
							diagram,
							entity,
							newModel);
					edit.redo();
					allEdits.addEdit(edit);
					DiagramUndoableEdits.SetIconEdit iconEdit=new DiagramUndoableEdits.SetIconEdit(diagram,entity,new SimpleIcon());
					iconEdit.redo();
					allEdits.addEdit(iconEdit);
					allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(edit.getPresentationName()));
					allEdits.end();
					if (buffer != null && buffer.length > 0)
					{
						buffer[0] = newModel;
					}
					postEditAdjustCanvas(diagram,allEdits);
				}
			}
		}
	}

	public static class AssignFSAAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 7311186809287532347L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected FSAModel fsa;
		
		protected EntityIcon icon;

		public AssignFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel fsa)
		{
			this(null, diagram, entity, fsa);
		}

		public AssignFSAAction(TemplateDiagram diagram, Entity entity,
				FSAModel fsa, EntityIcon icon)
		{
			this(null, diagram, entity, fsa,icon);
		}

		public AssignFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel fsa)
		{
			this(parent,diagram,entity,fsa,null);
		}

		public AssignFSAAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, FSAModel fsa, EntityIcon icon)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.fsa = fsa;
			if(icon==null)
			{
				icon=new SimpleIcon();
			}
			this.icon=icon;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				CompoundEdit allEdits=new CompoundEdit();
				DiagramUndoableEdits.AssignFSAEdit edit = new DiagramUndoableEdits.AssignFSAEdit(
						diagram,
						entity,
						fsa);
				edit.redo();
				allEdits.addEdit(edit);
				DiagramUndoableEdits.SetIconEdit iconEdit=new DiagramUndoableEdits.SetIconEdit(diagram,entity,icon);
				iconEdit.redo();
				allEdits.addEdit(iconEdit);
				allEdits.addEdit(new DiagramUndoableEdits.UndoableDummyLabel(edit.getPresentationName()));
				allEdits.end();				
				postEditAdjustCanvas(diagram,allEdits);
			}
		}
	}

	public static class SetTypeAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = -1774285258523292831L;

		protected TemplateDiagram diagram;

		protected Entity entity;

		protected int type;

		public SetTypeAction(TemplateDiagram diagram, Entity entity, int type)
		{
			this(null, diagram, entity, type);
		}

		public SetTypeAction(CompoundEdit parent, TemplateDiagram diagram,
				Entity entity, int type)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entity = entity;
			this.type = type;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null)
			{
				DiagramUndoableEdits.SetTypeEdit edit = new DiagramUndoableEdits.SetTypeEdit(
						diagram,
						entity,
						type);
				edit.redo();
				postEditAdjustCanvas(diagram, edit);
			}
		}
	}

	public static class SetIconColorAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 8626414278661880945L;

		protected TemplateDiagram diagram;

		protected Collection<Entity> entities;
		
		protected Color color;

		public SetIconColorAction(TemplateDiagram diagram, Collection<Entity> entities, Color color)
		{
			this(null, diagram, entities, color);
		}

		public SetIconColorAction(CompoundEdit parent, TemplateDiagram diagram,
				Collection<Entity> entities, Color color)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entities = entities;
			this.color = color;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null&&!entities.isEmpty())
			{
				CompoundEdit allEdits=new CompoundEdit();
				DiagramUndoableEdits.SetIconEdit edit=null;
				for(Entity entity:entities)
				{
					EntityIcon icon=entity.getIcon().clone();
					icon.setColor(color);
					edit = new DiagramUndoableEdits.SetIconEdit(
						diagram,
						entity,
						icon);
					edit.redo();
					allEdits.addEdit(edit);
				}
				if(entities.size()>1)
				{
					edit.setLastOfMultiple(true);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}

	public static class DefaultIconAction extends AbstractDiagramAction
	{
		private static final long serialVersionUID = 6507813585595950277L;

		protected TemplateDiagram diagram;

		protected Collection<Entity> entities;

		public DefaultIconAction(TemplateDiagram diagram, Collection<Entity> entities)
		{
			this(null, diagram, entities);
		}

		public DefaultIconAction(CompoundEdit parent, TemplateDiagram diagram,
				Collection<Entity> entities)
		{
			this.parentEdit = parent;
			this.diagram = diagram;
			this.entities = entities;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (diagram != null&&!entities.isEmpty())
			{
				CompoundEdit allEdits=new CompoundEdit();
				DiagramUndoableEdits.SetIconEdit edit=null;
				for(Entity entity:entities)
				{
					edit = new DiagramUndoableEdits.SetIconEdit(
						diagram,
						entity,
						new SimpleIcon());
					edit.redo();
					allEdits.addEdit(edit);
				}
				if(entities.size()>1)
				{
					edit.setLastOfMultiple(true);
				}
				allEdits.end();
				postEditAdjustCanvas(diagram, allEdits);
			}
		}
	}
}

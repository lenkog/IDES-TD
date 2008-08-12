package templates.diagram;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;

import templates.diagram.actions.DiagramActions;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.model.TemplateModelMessage;
import templates.model.TemplateModelSubscriber;

public class TemplateDiagram implements TemplateModelSubscriber,
		DESModelSubscriber
{
	/**
	 * FSAGraphPublisher part which maintains a collection of, and sends change
	 * notifications to, all interested observers (subscribers).
	 */
	private ArrayList<TemplateDiagramSubscriber> subscribers = new ArrayList<TemplateDiagramSubscriber>();

	// //////////////////////////////////////////////////////////////////////

	/**
	 * Attaches the given subscriber to this publisher. The given subscriber
	 * will receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void addSubscriber(TemplateDiagramSubscriber subscriber)
	{
		subscribers.add(subscriber);
	}

	/**
	 * Removes the given subscriber to this publisher. The given subscriber will
	 * no longer receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void removeSubscriber(TemplateDiagramSubscriber subscriber)
	{
		subscribers.remove(subscriber);
	}

	/**
	 * Returns all current subscribers to this publisher.
	 * 
	 * @return all current subscribers to this publisher
	 */
	public TemplateDiagramSubscriber[] getDiagramSubscribers()
	{
		return subscribers.toArray(new TemplateDiagramSubscriber[] {});
	}

	/**
	 * Notifies all subscribers that there has been a change to an element of
	 * this graph publisher.
	 * 
	 * @param message
	 */
	protected void fireDiagramChanged(TemplateDiagramMessage message)
	{
		model.metadataChanged();
		for (TemplateDiagramSubscriber s : subscribers)
		{
			s.templateDiagramChanged(message);
		}
	}

	/**
	 * Notifies all subscribers that there has been a change to the elements
	 * currently selected in this graph publisher.
	 * 
	 * @param message
	 */
	protected void fireDiagramSelectionChanged(TemplateDiagramMessage message)
	{
		for (TemplateDiagramSubscriber s : subscribers)
		{
			s.templateDiagramSelectionChanged(message);
		}
	}

	public static final int DESIRED_DIAGRAM_INSET = 10;

	protected Set<Entity> entities = new HashSet<Entity>();

	protected Set<Connector> connectors = new HashSet<Connector>();

	protected TemplateModel model;

	protected Collection<DiagramElement> selection = new HashSet<DiagramElement>();

	public TemplateDiagram(TemplateModel m)
	{
		model = m;
		recoverLayout();
		m.addSubscriber((TemplateModelSubscriber)this);
	}

	protected void recoverLayout()
	{
		if (DiagramElement.getGlobalFont() == null)
		{
			DiagramElement.setGlobalFont(new JLabel().getFont());
		}
		if (DiagramElement.getGlobalFontRenderer() == null)
		{
			DiagramElement.setGlobalFontRenderer(Hub
					.getMainWindow().getGraphics());
		}
	}

	public void templateModelStructureChanged(TemplateModelMessage message)
	{
		recoverLayout();
		Set<DiagramElement> elements = new HashSet<DiagramElement>();
		elements.addAll(entities);
		elements.addAll(connectors);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				elements,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public TemplateModel getModel()
	{
		return model;
	}

	public void release()
	{
		model.removeSubscriber((TemplateModelSubscriber)this);
	}

	public Entity createEntity(Point location)
	{
		model.removeSubscriber((TemplateModelSubscriber)this);
		TemplateComponent component = model.createComponent();
		DiagramElementLayout layout = new DiagramElementLayout();
		layout.location = location;
		layout.label = Hub.string("TD_untitledEntityPrefix") + " "
				+ component.getId();
		Entity entity = new Entity(component, layout);
		entities.add(entity);
		model.addSubscriber((TemplateModelSubscriber)this);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_ADD));
		return entity;
	}

	public void add(Entity entity)
	{
		if (entities.contains(entity))
		{
			return;
		}
		model.removeSubscriber((TemplateModelSubscriber)this);
		try
		{
			model.addComponent(entity.getComponent());
			entities.add(entity);
			if (entity.getComponent().getModel() != null)
			{
				entity.getComponent().getModel().addSubscriber(this);
			}
		}
		finally
		{
			model.addSubscriber((TemplateModelSubscriber)this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_ADD));
	}

	public void remove(Entity entity)
	{
		if (!entities.contains(entity))
		{
			return;
		}
		clearSelection();
		model.removeSubscriber((TemplateModelSubscriber)this);
		Collection<Connector> adjacent = getAdjacentConnectors(entity);
		for (Connector c : adjacent)
		{
			connectors.remove(c);
			for (TemplateLink link : c.getLinks())
			{
				model.removeLink(link.getId());
			}
		}
		entities.remove(entity);
		model.removeComponent(entity.getComponent().getId());
		model.addSubscriber((TemplateModelSubscriber)this);
		Collection<DiagramElement> removed = new HashSet<DiagramElement>(
				adjacent);
		if (entity.getComponent().getModel() != null)
		{
			entity.getComponent().getModel().removeSubscriber(this);
		}
		removed.add(entity);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				removed,
				TemplateDiagramMessage.OP_REMOVE));
	}

	public void labelEntity(Entity entity, String label)
	{
		entity.setLabel(label);
		if (entity.getComponent().getModel() != null)
		{
			entity.getComponent().getModel().removeSubscriber(this);
			entity
					.getComponent().getModel()
					.setName(TemplateModel.FSA_NAME_PREFIX + label);
			entity.getComponent().getModel().addSubscriber(this);
		}
		model.metadataChanged();
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_MODIFY));
	}

	public Entity getEntityAt(Point location)
	{
		for (Entity entity : entities)
		{
			if (entity.contains(location))
			{
				return entity;
			}
		}
		return null;
	}

	public Collection<Entity> getEntities()
	{
		return new HashSet<Entity>(entities);
	}

	public Collection<Entity> getModules()
	{
		HashSet<Entity> vmodules = new HashSet<Entity>();
		for (Entity vcomponent : entities)
		{
			if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_MODULE)
			{
				vmodules.add(vcomponent);
			}
		}
		return vmodules;
	}

	public Collection<Entity> getChannels()
	{
		HashSet<Entity> vchannels = new HashSet<Entity>();
		for (Entity vcomponent : entities)
		{
			if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_CHANNEL)
			{
				vchannels.add(vcomponent);
			}
		}
		return vchannels;
	}

	public Collection<Connector> getAdjacentConnectors(Entity entity)
	{
		Collection<Connector> adjacent = new HashSet<Connector>();
		for (Connector c : connectors)
		{
			if (entity == c.getLeftEntity() || entity == c.getRightEntity())
			{
				adjacent.add(c);
			}
		}
		return adjacent;
	}

	public Connector getConnector(Entity left, Entity right)
	{
		for (Connector c : connectors)
		{
			if ((left == c.getLeftEntity() && right == c.getRightEntity())
					|| (left == c.getRightEntity() && right == c
							.getLeftEntity()))
			{
				return c;
			}
		}
		return null;
	}

	public Connector createConnector(Entity left, Entity right)
	{
		if (!(entities.contains(left) && entities.contains(right)))
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinkInit"));
		}
		if (getConnector(left, right) != null)
		{
			return getConnector(left, right);
		}
		DiagramElementLayout layout = new DiagramElementLayout();
		Connector c = new Connector(left, right, new HashSet<TemplateLink>(), layout);
		connectors.add(c);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_ADD));
		return c;
	}

	public void add(Connector c)
	{
		if (!(entities.contains(c.getLeftEntity()) && entities.contains(c
				.getRightEntity())))
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyLinkInit"));
		}
		if (getConnector(c.getLeftEntity(), c.getRightEntity()) != null)
		{
			throw new InconsistentModificationException(Hub
					.string("TD_inconsistencyConnectorDup"));
		}
		model.removeSubscriber((TemplateModelSubscriber)this);
		try
		{
			for (TemplateLink link : c.getLinks())
			{
				model.addLink(link);
			}
			connectors.add(c);
		}
		finally
		{
			model.addSubscriber((TemplateModelSubscriber)this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_ADD));
	}

	public void remove(Connector c)
	{
		if (!connectors.contains(c))
		{
			return;
		}
		clearSelection();
		model.removeSubscriber((TemplateModelSubscriber)this);
		for (TemplateLink link : c.getLinks())
		{
			model.removeLink(link.getId());
		}
		connectors.remove(c);
		model.addSubscriber((TemplateModelSubscriber)this);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_REMOVE));
	}

	public Collection<Connector> getConnectors()
	{
		return new HashSet<Connector>(connectors);
	}

	public Connector getConnectorAt(Point location)
	{
		for (Connector c : connectors)
		{
			if (c.contains(location))
			{
				return c;
			}
		}
		return null;
	}

	public Rectangle getBounds()
	{
		Rectangle bounds = getAnyElement() != null ? getAnyElement()
				.getBounds() : new Rectangle();
		for (Entity module : entities)
		{
			bounds = bounds.union(module.getBounds());
		}
		for (Connector c : getConnectors())
		{
			bounds = bounds.union(c.getBounds());
		}
		return bounds;
	}

	protected DiagramElement getAnyElement()
	{
		if (!entities.isEmpty())
		{
			return entities.iterator().next();
		}
		return null;
	}

	public void translate(Point delta)
	{
		for (Entity module : entities)
		{
			module.translate(delta);
		}
		for (Connector c : getConnectors())
		{
			c.translate(delta);
		}
		model.metadataChanged();
		Set<DiagramElement> elements = new HashSet<DiagramElement>();
		elements.addAll(entities);
		elements.addAll(connectors);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				elements,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public void translate(Collection<DiagramElement> elements, Point delta)
	{
		for (DiagramElement element : elements)
		{
			element.translate(delta);
		}
		for (DiagramElement element : elements)
		{
			if (element instanceof Entity)
			{
				for (Connector c : getAdjacentConnectors((Entity)element))
				{
					c.update();
				}
			}
		}
		model.metadataChanged();
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				elements,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public void commitTranslation(Collection<DiagramElement> elements,
			Point delta)
	{
		new DiagramActions.MovedSelectionAction(this, elements, delta)
				.execute();
		model.metadataChanged();
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				elements,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public void draw(Graphics2D g2d)
	{
		g2d.setFont(DiagramElement.getGlobalFont());
		for (Connector c : getConnectors())
		{
			c.draw(g2d);
		}
		for (Entity module : entities)
		{
			module.draw(g2d);
		}
	}

	public void setSelection(Collection<DiagramElement> selection)
	{
		if (this.selection.containsAll(selection)
				&& selection.containsAll(this.selection))
		{
			return;
		}
		for (DiagramElement element : this.selection)
		{
			element.setSelected(false);
		}
		this.selection.clear();
		this.selection.addAll(selection);
		for (DiagramElement element : this.selection)
		{
			element.setSelected(true);
		}
		fireDiagramSelectionChanged(new TemplateDiagramMessage(
				this,
				this.selection,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public Collection<DiagramElement> getSelection()
	{
		return new HashSet<DiagramElement>(selection);
	}

	public void clearSelection()
	{
		if (selection.isEmpty())
		{
			return;
		}
		for (DiagramElement element : selection)
		{
			element.setSelected(false);
		}
		selection.clear();
		fireDiagramSelectionChanged(new TemplateDiagramMessage(
				this,
				selection,
				TemplateDiagramMessage.OP_MODIFY));
	}

	public Entity getEntityWithFSA(FSAModel fsa)
	{
		for (Entity e : entities)
		{
			if (e.getComponent().getModel() == fsa)
			{
				return e;
			}
		}
		return null;
	}

	public void modelNameChanged(DESModelMessage arg0)
	{
		if (!(arg0.getSource() instanceof FSAModel))
		{
			return;
		}
		Entity e = getEntityWithFSA((FSAModel)arg0.getSource());
		if (e == null)
		{
			return;
		}
		new DiagramActions.LabelEntityAction(this, e, arg0
				.getSource().getName()
				.startsWith(TemplateModel.FSA_NAME_PREFIX) ? arg0
				.getSource().getName().substring(TemplateModel.FSA_NAME_PREFIX
						.length()) : arg0.getSource().getName()).execute();
	}

	public void saveStatusChanged(DESModelMessage arg0)
	{
	}
}

package templates.diagram;

import ides.api.core.Annotable;
import ides.api.core.Hub;
import ides.api.model.fsa.FSAMessage;
import ides.api.model.fsa.FSAModel;
import ides.api.model.fsa.FSASubscriber;
import ides.api.plugin.model.DESModel;
import ides.api.plugin.model.DESModelMessage;
import ides.api.plugin.model.DESModelSubscriber;
import ides.api.plugin.model.ModelManager;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.undo.CompoundEdit;

import templates.diagram.actions.DiagramActions;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.model.TemplateModelMessage;
import templates.model.TemplateModelSubscriber;
import templates.utils.EntityIcon;

public class TemplateDiagram implements TemplateModelSubscriber, FSASubscriber
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

	protected Map<TemplateComponent, Entity> component2Entity = new HashMap<TemplateComponent, Entity>();

	protected Set<Connector> connectors = new HashSet<Connector>();

	protected Map<TemplateLink, Connector> link2Connector = new HashMap<TemplateLink, Connector>();

	//keep track of previous FSA for a component
	protected Map<TemplateComponent, FSAModel> component2FSA = new HashMap<TemplateComponent, FSAModel>();
	
	//speed up retrieval of component for an FSA model
	protected Map<FSAModel, TemplateComponent> FSA2component = new HashMap<FSAModel,TemplateComponent>();

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
		clearSelection();
		component2Entity.clear();
		entities.clear();
		link2Connector.clear();
		for(FSAModel fsa:component2FSA.values())
		{
			fsa.removeSubscriber(this);
		}
		component2FSA.clear();
		FSA2component.clear();
		connectors.clear();
		for (TemplateComponent component : model.getComponents())
		{
			EntityLayout layout = null;
			if (component.hasAnnotation(Annotable.LAYOUT))
			{
				layout = (EntityLayout)component
						.getAnnotation(Annotable.LAYOUT);
			}
			if (layout == null)
			{
				layout = createLayout(component);
			}
			Entity newEntity = new Entity(component, layout);
			entities.add(newEntity);
			component2Entity.put(component, newEntity);
			if (component.hasModel())
			{
				component.getModel().setName(TemplateModel.FSA_NAME_PREFIX
						+ layout.label);
				component2FSA.put(component,component.getModel());
				FSA2component.put(component.getModel(),component);
				component.getModel().addSubscriber(this);
			}
		}
		for (TemplateLink link : model.getLinks())
		{
			Entity left = component2Entity.get(link.getLeftComponent());
			Entity right = component2Entity.get(link.getRightComponent());
			Connector c = getConnector(left, right);
			if (c == null)
			{
				c = new Connector(left, right, Arrays
						.asList(new TemplateLink[] {}));
				connectors.add(c);
			}
			c.addLink(link);
			link2Connector.put(link, c);
		}
		if (model.hasAnnotation(Annotable.LAYOUT))
		{
			EmptyConnectorSet emptyConnectors = (EmptyConnectorSet)model
					.getAnnotation(Annotable.LAYOUT);
			for (EmptyConnector ec : emptyConnectors)
			{
				Entity left = component2Entity.get(model
						.getComponent(ec.leftComponent));
				Entity right = component2Entity.get(model
						.getComponent(ec.rightComponent));
				Connector c = new Connector(left, right, Arrays
						.asList(new TemplateLink[] {}));
				connectors.add(c);
			}
		}
		new DiagramActions.ShiftDiagramInViewAction(new CompoundEdit(), this)
				.execute();
	}

	protected EntityLayout createLayout(TemplateComponent component)
	{
		EntityLayout layout = new EntityLayout();
		if (component.hasModel())
		{
			layout.label = component.getModel().getName();
			if (layout.label.startsWith(TemplateModel.FSA_NAME_PREFIX))
			{
				layout.label = layout.label
						.substring(TemplateModel.FSA_NAME_PREFIX.length());
			}
		}
		else
		{
			layout.label = Hub.string("TD_untitledEntityPrefix") + " "
					+ component.getId();
		}
		int row = entities.size() / 10;
		int col = entities.size() % 10;
		while (hasEntitiesAt(new Rectangle(col * 100, row * 100, 100, 100)))
		{
			col = (col + 1) % 10;
			if (col == 0)
			{
				row++;
			}
		}
		layout.location = new Point(col * 100 + 50, row * 100 + 50);
		return layout;
	}

	public void templateModelStructureChanged(TemplateModelMessage message)
	{
		if (message.getOperationType() == TemplateModelMessage.OP_MODIFY
				&& message.getElementType() == TemplateModelMessage.ELEMENT_COMPONENT)
		{
			TemplateComponent component=model.getComponent(message.getElementId());
			FSAModel fsa=component2FSA.get(component);
			if(fsa!=null)
			{
				fsa.removeSubscriber(this);
				FSA2component.remove(fsa);
			}
			component2FSA.remove(component);
			Entity entity = component2Entity.get(component);
			if (entity != null)
			{
				if(entity.getComponent().hasModel())
				{
					component2FSA.put(entity.getComponent(),entity.getComponent().getModel());
					FSA2component.put(entity.getComponent().getModel(),entity.getComponent());
					entity.getComponent().getModel().addSubscriber(this);
				}
				entity.update();
				fireDiagramChanged(new TemplateDiagramMessage(
						this,
						Arrays.asList(new DiagramElement[] { entity }),
						TemplateDiagramMessage.OP_MODIFY));
				return;
			}
		}
		recoverLayout();
		updateEmptyConnectorList();
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
		EntityLayout layout = new EntityLayout();
		layout.location = location;
		layout.label = Hub.string("TD_untitledEntityPrefix") + " "
				+ component.getId();
		Entity entity = new Entity(component, layout);
		entities.add(entity);
		component2Entity.put(component, entity);
		FSAModel fsa = ModelManager.instance().createModel(FSAModel.class,
				TemplateModel.FSA_NAME_PREFIX + layout.label);
		fsa.setParentModel(model);
		model.assignFSA(component.getId(), fsa);
		component2FSA.put(component,fsa);
		FSA2component.put(fsa,component);
		fsa.addSubscriber(this);
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
			component2Entity.put(entity.getComponent(), entity);
			if(entity.getComponent().hasModel())
			{
				component2FSA.put(entity.getComponent(),entity.getComponent().getModel());
				FSA2component.put(entity.getComponent().getModel(),entity.getComponent());
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
		if(entity.getComponent().hasModel())
		{
			DESModel fsa=Hub.getWorkspace().getModel(entity.getComponent().getModel().getName());
			if(entity.getComponent().getModel()==fsa)
			{
				Hub.getWorkspace().removeModel(entity.getComponent().getModel().getName());
			}
		}
		clearSelection();
		model.removeSubscriber((TemplateModelSubscriber)this);
		Collection<Connector> adjacent = getAdjacentConnectors(entity);
		for (Connector c : adjacent)
		{
			link2Connector.keySet().removeAll(c.getLinks());
			connectors.remove(c);
			for (TemplateLink link : c.getLinks())
			{
				model.removeLink(link.getId());
			}
		}
		if(entity.getComponent().hasModel())
		{
			entity.getComponent().getModel().removeSubscriber(this);
			FSA2component.remove(entity.getComponent().getModel());
			component2FSA.remove(entity.getComponent());
		}
		component2Entity.remove(entity.getComponent());
		entities.remove(entity);
		model.removeComponent(entity.getComponent().getId());
		updateEmptyConnectorList();
		model.addSubscriber((TemplateModelSubscriber)this);
		Collection<DiagramElement> removed = new HashSet<DiagramElement>(
				adjacent);
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
			entity
					.getComponent().getModel()
					.setName(TemplateModel.FSA_NAME_PREFIX + label);
		}
		model.metadataChanged();
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_MODIFY));
	}
	
	public void setEntityIcon(Entity entity, EntityIcon icon)
	{
		entity.setIcon(icon);
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

	public boolean hasEntitiesAt(Rectangle area)
	{
		for (Entity entity : entities)
		{
			if (entity.intersects(area))
			{
				return true;
			}
		}
		return false;
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
		Connector c = new Connector(left, right, new HashSet<TemplateLink>());
		connectors.add(c);
		updateEmptyConnectorList();
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
			for (TemplateLink link : c.getLinks())
			{
				link2Connector.put(link, c);
			}
			updateEmptyConnectorList();
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
		link2Connector.keySet().removeAll(c.getLinks());
		connectors.remove(c);
		updateEmptyConnectorList();
		model.addSubscriber((TemplateModelSubscriber)this);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_REMOVE));
	}

	public TemplateLink createLink(Connector c, String leftEvent,
			String rightEvent)
	{
		if (!connectors.contains(c))
		{
			return null;
		}
		model.removeSubscriber((TemplateModelSubscriber)this);
		TemplateLink link;
		try
		{
			link = model.createLink(c.getLeftEntity().getComponent().getId(), c
					.getRightEntity().getComponent().getId());
			link.setLeftEventName(leftEvent);
			link.setRightEventName(rightEvent);
			c.addLink(link);
			link2Connector.put(link, c);
			updateEmptyConnectorList();
		}
		finally
		{
			model.addSubscriber((TemplateModelSubscriber)this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_MODIFY));
		return link;
	}

	public void addLink(Connector c, TemplateLink link)
	{
		if (!connectors.contains(c))
		{
			return;
		}
		model.removeSubscriber((TemplateModelSubscriber)this);
		try
		{
			model.addLink(link);
			c.addLink(link);
			link2Connector.put(link, c);
			updateEmptyConnectorList();
		}
		finally
		{
			model.addSubscriber((TemplateModelSubscriber)this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_MODIFY));
	}

	public void removeLink(Connector c, TemplateLink link)
	{
		if (!connectors.contains(c))
		{
			return;
		}
		model.removeSubscriber((TemplateModelSubscriber)this);
		try
		{
			link2Connector.remove(link);
			c.removeLink(link);
			model.removeLink(link.getId());
			updateEmptyConnectorList();
		}
		finally
		{
			model.addSubscriber((TemplateModelSubscriber)this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_MODIFY));
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
		for (Entity e : entities)
		{
			e.translate(delta);
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
		draw(g2d, false);
	}

	public void draw(Graphics2D g2d, boolean showInconsistency)
	{
		g2d.setFont(DiagramElement.getGlobalFont());
		for (Connector c : getConnectors())
		{
			c.draw(g2d, showInconsistency);
		}
		for (Entity e : entities)
		{
			e.draw(g2d, showInconsistency);
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

	public Connector getConnectorFor(TemplateLink link)
	{
		return link2Connector.get(link);
	}

	public Entity getEntityFor(TemplateComponent component)
	{
		return component2Entity.get(component);
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

	protected void updateEmptyConnectorList()
	{
		EmptyConnectorSet emptyConnectors = new EmptyConnectorSet();
		for (Connector c : connectors)
		{
			if (c.getLinks().isEmpty())
			{
				emptyConnectors.add(new EmptyConnector(c
						.getLeftEntity().getComponent().getId(), c
						.getRightEntity().getComponent().getId()));
			}
		}
		model.setAnnotation(Annotable.LAYOUT, emptyConnectors);
	}

	public void fsaEventSetChanged(FSAMessage arg0)
	{
		flagModel(arg0.getSource());
	}

	public void fsaStructureChanged(FSAMessage arg0)
	{
		flagModel(arg0.getSource());
	}
	
	protected void flagModel(FSAModel model)
	{
		TemplateComponent component=FSA2component.get(model);
		if(component!=null)
		{
			component.getModel().setAnnotation(Entity.FLAG_MARK,new Object());
			Entity entity=getEntityFor(component);
			if(entity!=null)
			{
				entity.update();
			}
		}
	}
}

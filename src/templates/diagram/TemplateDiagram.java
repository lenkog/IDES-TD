package templates.diagram;

import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;

import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;
import templates.model.TemplateModel;
import templates.model.TemplateModelMessage;
import templates.model.TemplateModelSubscriber;

public class TemplateDiagram implements TemplateModelSubscriber
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
	public void fireDiagramChanged(TemplateDiagramMessage message)
	{
		// fsa.metadataChanged();
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
	public void fireDiagramSelectionChanged(TemplateDiagramMessage message)
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

	public TemplateDiagram(TemplateModel m)
	{
		model = m;
		recoverLayout();
		m.addSubscriber(this);
	}

	protected void recoverLayout()
	{
		if(DiagramElement.getGlobalFont()==null)
		{
			DiagramElement.setGlobalFont(new JLabel().getFont());
		}
		if (DiagramElement.getGlobalFontMetrics() == null)
		{
			DiagramElement.setGlobalFontMetrics(Hub
					.getMainWindow().getGraphics().getFontMetrics(DiagramElement.getGlobalFont()));
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
		model.removeSubscriber(this);
	}

	public Entity createEntity(Point location)
	{
		model.removeSubscriber(this);
		TemplateComponent component = model.createComponent();
		DiagramElementLayout layout = new DiagramElementLayout();
		layout.location = location;
		Entity entity = new Entity(component, layout);
		entities.add(entity);
		model.addSubscriber(this);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_ADD));
		return entity;
	}

	public void add(Entity entity)
	{
		if(entities.contains(entity))
		{
			return;
		}
		model.removeSubscriber(this);
		try
		{
			model.addComponent(entity.getComponent());
			entities.add(entity);
		}
		finally
		{
			model.addSubscriber(this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { entity }),
				TemplateDiagramMessage.OP_ADD));
	}

	public void remove(Entity entity)
	{
		if(!entities.contains(entity))
		{
			return;
		}
		model.removeSubscriber(this);
		Collection<Connector> adjacent=getAdjacentConnectors(entity);
		for(Connector c:adjacent)
		{
			connectors.remove(c);
			for(TemplateLink link:c.getLinks())
			{
				model.removeLink(link.getId());
			}
		}
		entities.remove(entity);
		model.removeComponent(entity.getComponent().getId());
		model.addSubscriber(this);
		Collection<DiagramElement> removed=new HashSet<DiagramElement>(adjacent);
		removed.add(entity);
		fireDiagramChanged(new TemplateDiagramMessage(this, removed, TemplateDiagramMessage.OP_REMOVE));		
	}

	public Entity getEntityAt(Point location)
	{
		for(Entity entity:entities)
		{
			if(entity.contains(location))
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
		Collection<Connector> adjacent=new HashSet<Connector>();
		for(Connector c:connectors)
		{
			if(entity==c.getLeftEntity()||entity==c.getRightEntity())
			{
				adjacent.add(c);
			}
		}
		return adjacent;
	}
	
	public Connector getConnector(Entity left, Entity right)
	{
		for(Connector c:connectors)
		{
			if((left==c.getLeftEntity()&&right==c.getRightEntity())||(left==c.getRightEntity()&&right==c.getLeftEntity()))
			{
				return c;
			}
		}
		return null;		
	}
	
	public Connector createConnector(Entity left, Entity right)
	{
		if(!(entities.contains(left)&&entities.contains(right)))
		{
			throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
		}
		if(getConnector(left,right)!=null)
		{
			return getConnector(left,right);
		}
		model.removeSubscriber(this);
		Connector c;
		try
		{
			TemplateLink link = model.createLink(left.getComponent().getId(),right.getComponent().getId());
			DiagramElementLayout layout = new DiagramElementLayout();
			c = new Connector(left,right,Arrays.asList(new TemplateLink[]{link}),layout);
			connectors.add(c);
		}
		finally
		{
			model.addSubscriber(this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_ADD));
		return c;
	}

	public void add(Connector c)
	{
		if(!(entities.contains(c.getLeftEntity())&&entities.contains(c.getRightEntity())))
		{
			throw new InconsistentModificationException(Hub.string("TD_inconsistencyLinkInit"));
		}
		if(getConnector(c.getLeftEntity(),c.getRightEntity())!=null)
		{
			throw new InconsistentModificationException(Hub.string("TD_inconsistencyConnectorDup"));
		}
		model.removeSubscriber(this);
		try
		{
			for(TemplateLink link:c.getLinks())
			{
				model.addLink(link);
			}
			connectors.add(c);
		}
		finally
		{
			model.addSubscriber(this);
		}
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { c }),
				TemplateDiagramMessage.OP_ADD));
	}

	public void remove(Connector c)
	{
		if(!connectors.contains(c))
		{
			return;
		}
		model.removeSubscriber(this);
		for(TemplateLink link:c.getLinks())
		{
			model.removeLink(link.getId());
		}
		connectors.remove(c);
		model.addSubscriber(this);
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
		for(Connector c:connectors)
		{
			if(c.contains(location))
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

	public void draw(Graphics2D g2d)
	{
		for (Connector c : getConnectors())
		{
			c.draw(g2d);
		}
		for (Entity module : entities)
		{
			module.draw(g2d);
		}
	}
}

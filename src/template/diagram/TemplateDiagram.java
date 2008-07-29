package template.diagram;

import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import template.model.TemplateComponent;
import template.model.TemplateModel;
import template.model.TemplateModelMessage;
import template.model.TemplateModelSubscriber;

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
	public TemplateDiagramSubscriber[] getFSAGraphSubscribers()
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

	protected Set<VisualComponent> components = new HashSet<VisualComponent>();

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
		if (DiagramElement.getGlobalFontMetrics() == null)
		{
			DiagramElement.setGlobalFontMetrics(Hub
					.getMainWindow().getGraphics().getFontMetrics());
		}
	}

	public void templateModelStructureChanged(TemplateModelMessage message)
	{
		recoverLayout();
		Set<DiagramElement> elements = new HashSet<DiagramElement>();
		elements.addAll(components);
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

	public VisualComponent createComponent(Point location)
	{
		model.removeSubscriber(this);
		TemplateComponent component = model.createComponent();
		DiagramElementLayout layout = new DiagramElementLayout();
		layout.location = location;
		VisualComponent vcomponent = new VisualComponent(component, layout);
		components.add(vcomponent);
		model.addSubscriber(this);
		fireDiagramChanged(new TemplateDiagramMessage(
				this,
				Arrays.asList(new DiagramElement[] { vcomponent }),
				TemplateDiagramMessage.OP_ADD));
		return vcomponent;
	}

	public void addComponent(VisualComponent component)
	{

	}

	public void removeComponent(VisualComponent module)
	{

	}

	public VisualComponent getComponentAt(Point location)
	{
		return null;
	}

	public Collection<VisualComponent> getComponents()
	{
		HashSet<VisualComponent> copy = new HashSet<VisualComponent>(components);
		return copy;
	}

	public Collection<VisualComponent> getModules()
	{
		HashSet<VisualComponent> vmodules = new HashSet<VisualComponent>();
		for (VisualComponent vcomponent : components)
		{
			if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_MODULE)
			{
				vmodules.add(vcomponent);
			}
		}
		return vmodules;
	}

	public Collection<VisualComponent> getChannels()
	{
		HashSet<VisualComponent> vchannels = new HashSet<VisualComponent>();
		for (VisualComponent vcomponent : components)
		{
			if (vcomponent.getComponent().getType() == TemplateComponent.TYPE_CHANNEL)
			{
				vchannels.add(vcomponent);
			}
		}
		return vchannels;
	}

	public Connector addConnector(VisualComponent left, VisualComponent right)
	{
		return null;
	}

	public void addConnector(Connector c)
	{

	}

	public void removeConnector(Connector c)
	{

	}

	public Collection<Connector> getConnectors()
	{
		return new HashSet<Connector>(connectors);
	}

	public Connector getConnectorAt(Point location)
	{
		Collection<Connector> connectors = getConnectors();
		return null;
	}

	public Rectangle getBounds()
	{
		Rectangle bounds = getAnyElement() != null ? getAnyElement()
				.getBounds() : new Rectangle();
		for (VisualComponent module : components)
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
		if (!components.isEmpty())
		{
			return components.iterator().next();
		}
		return null;
	}

	public void translate(Point delta)
	{
		for (VisualComponent module : components)
		{
			module.translate(delta);
		}
		for (Connector c : getConnectors())
		{
			c.translate(delta);
		}
	}

	public void draw(Graphics2D g2d)
	{
		for (Connector c : getConnectors())
		{
			c.draw(g2d);
		}
		for (VisualComponent module : components)
		{
			module.draw(g2d);
		}
	}
}

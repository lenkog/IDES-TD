package templates.diagram;

import ides.api.core.Annotable;
import ides.api.core.Hub;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;

import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

public class Connector extends DiagramElement
{
	protected Collection<TemplateLink> links=new HashSet<TemplateLink>();
	protected Entity left;
	protected Entity right;
	
	private Rectangle bounds;
	
	public Connector(Entity left, Entity right, Collection<TemplateLink> links) throws MissingLayoutException
	{
		layout=null;
		for(TemplateLink link:links)
		{
			if(link.hasAnnotation(Annotable.LAYOUT) &&
					link.getAnnotation(Annotable.LAYOUT) instanceof DiagramElementLayout)
			{
				layout=(DiagramElementLayout)link.getAnnotation(Annotable.LAYOUT);
				break;
			}
		}
		if(layout==null)
		{
			throw new MissingLayoutException();
		}
		for(TemplateLink link:links)
		{
			link.setAnnotation(Annotable.LAYOUT, layout);
		}
		this.left=left;
		this.right=right;
		this.links.addAll(links);
		update();
	}
	
	public Connector(Entity left, Entity right, Collection<TemplateLink> links, DiagramElementLayout layout)
	{
		this.layout=layout;
		for(TemplateLink link:links)
		{
			link.setAnnotation(Annotable.LAYOUT, layout);
		}
		this.left=left;
		this.right=right;
		this.links.addAll(links);
		update();
	}
	
	public Collection<TemplateLink> getLinks()
	{
		return new HashSet<TemplateLink>(links);
	}
	
	public Entity getLeftEntity()
	{
		return left;
	}

	public Entity getRightEntity()
	{
		return right;
	}
	
	public Entity[] getEntities()
	{
		return new Entity[]{left,right};
	}
	
	public void addLink(TemplateLink link)
	{
		if(!links.contains(link))
		{
			TemplateComponent[] endpoints=link.getComponents();
			if(!(left.getComponent()==endpoints[0]&&right.getComponent()==endpoints[1])&&!(left.getComponent()==endpoints[1]&&right.getComponent()==endpoints[0]))
			{
				throw new InconsistentModificationException(Hub.string("TD_inconsistencyConnecting"));
			}
			links.add(link);
		}
	}
	
	public void removeLink(TemplateLink link)
	{
		links.remove(link);
	}

	@Override
	public void draw(Graphics2D g2d)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	public void computeBounds()
	{
		bounds=new Rectangle();
	}
	
	public void update()
	{
		computeBounds();
	}

	@Override
	public boolean contains(Point p)
	{
		return false;
	}
}

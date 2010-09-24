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

package templates.diagram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.Vector;

import templates.model.TemplateComponent;
import templates.utils.EntityIcon;

/**
 * Class to maintain the graphical representation of {@link TemplateComponent}s.
 * 
 * @author Lenko Grigorov
 */
public class Entity extends DiagramElement
{
	/**
	 * Denotes if the FSA model for an entity has been modified after being
	 * assigned to the entity.
	 */
	public static final String FLAG_MARK = "templates.diagram.Entity.flag";

	/**
	 * Class to maintain the graphical representation of the label of the
	 * entity.
	 * 
	 * @author Lenko Grigorov
	 */
	protected class LabelBox extends Rectangle
	{
		private static final long serialVersionUID = -3359948297694123889L;

		/**
		 * List of the lines into which the label is split.
		 */
		protected Vector<String> lines = new Vector<String>();

		/**
		 * Construct a label box to render the lines of an {@link Entity} label.
		 * 
		 * @param lines
		 *            the lines into which the label is split
		 */
		public LabelBox(Vector<String> lines)
		{
			this.lines.addAll(lines);
			int maxWidth = 0;
			for (String line : lines)
			{
				maxWidth = Math.max(maxWidth, getGlobalFontMetrics()
						.stringWidth(line));
			}
			width = maxWidth;
			if (lines.size() == 1 && lines.firstElement().trim().length() == 0)
			{
				height = 0;
			}
			else
			{
				height = lines.size() * getGlobalFontMetrics().getHeight();
			}
		}

		/**
		 * Render the label box in the given graphical context.
		 * 
		 * @param g2d
		 *            the graphical context where the label box has to be
		 *            rendered
		 */
		public void draw(Graphics2D g2d)
		{
			for (int i = 0; i < lines.size(); ++i)
			{
				int deltaX = (width - getGlobalFontMetrics().stringWidth(lines
						.elementAt(i))) / 2;
				int deltaY = getGlobalFontMetrics().getHeight() * (i + 1)
						- getGlobalFontMetrics().getDescent();
				g2d.drawString(lines.elementAt(i), x + deltaX + 1, y + deltaY);
			}
		}
	}

	/**
	 * Offset of the label box in pixels, to produce spacing around it.
	 */
	protected static final int LABEL_SPACING = 5;

	/**
	 * The radius of the connector handles on the sides the entity, in pixels.
	 */
	public static final int PORT_RADIUS = 5;

	/**
	 * The radius of the "supervisor computation" shortcut icon for
	 * <i>channels</i>, in pixels.
	 */
	private static final int HALO_RADIUS = 8;

	/**
	 * Constant to say mouse cursor is not over any part of the entity.
	 * 
	 * @see #whereisPoint(Point)
	 */
	public final static int ON_NADA = 0;

	/**
	 * Constant to say mouse cursor is over the icon of the entity.
	 * 
	 * @see #whereisPoint(Point)
	 */
	public final static int ON_ICON = 1;

	/**
	 * Constant to say mouse cursor is over the label of the entity.
	 * 
	 * @see #whereisPoint(Point)
	 */
	public final static int ON_LABEL = 2;

	/**
	 * Constant to say mouse cursor is over one of the connector handles of the
	 * entity.
	 * 
	 * @see #whereisPoint(Point)
	 */
	public final static int ON_PORT = 4;

	/**
	 * Constant to say mouse cursor is over the "supervisor computation"
	 * shortcut icon (possible only if the entity is a <i>channel</i>).
	 * 
	 * @see #whereisPoint(Point)
	 */
	public final static int ON_SUP = 8;

	/**
	 * The {@link TemplateComponent} represented by the entity.
	 */
	protected TemplateComponent component;

	/**
	 * The layout information of the entity.
	 */
	protected EntityLayout layout;

	/**
	 * Cached bounds of the entity (smallest rectangle containing all parts of
	 * the entity).
	 */
	private Rectangle bounds;

	/**
	 * The icon of the entity.
	 */
	private EntityIcon icon = new SimpleIcon();

	/**
	 * The representation of the label.
	 */
	private LabelBox labelBox;

	/**
	 * The connector handles of the entity.
	 * <ul>
	 * <li>ports[0] is the left handle
	 * <li>ports[1] is the top handle
	 * <li>ports[2] is the right handle
	 * <li>ports[3] is the bottom handle
	 * </ul>
	 */
	private Ellipse2D[] ports = new Ellipse2D[4];

	/**
	 * The "supervisor computation" shortcut icon for <i>channels</i>. Can be
	 * <code>null</code> when the entity is a <i>module</i>.
	 */
	private Ellipse2D supHalo;

	/**
	 * The label inside the "supervisor computation" shortcut icon.
	 */
	private static final String HALO_LABEL = "S";

	/**
	 * The displacement in pixels of the label inside the
	 * "supervisor computation" shortcut icon from the center of the shortcut
	 * icon, in the X direction.
	 */
	private int haloDX;

	/**
	 * The displacement in pixels of the label inside the
	 * "supervisor computation" shortcut icon from the center of the shortcut
	 * icon, in the Y direction.
	 */
	private int haloDY;

	/**
	 * Construct an entity for the layout of the given {@link TemplateComponent}
	 * .
	 * 
	 * @param component
	 *            the {@link TemplateComponent} which the entity will represent
	 * @throws MissingLayoutException
	 *             if the {@link TemplateComponent} has no annotation with an
	 *             {@link EntityLayout} (under the {@link EntityLayout#KEY} key)
	 */
	public Entity(TemplateComponent component) throws MissingLayoutException
	{
		if (!component.hasAnnotation(EntityLayout.KEY)
				|| !(component.getAnnotation(EntityLayout.KEY) instanceof EntityLayout))
		{
			throw new MissingLayoutException();
		}
		this.component = component;
		layout = (EntityLayout)component.getAnnotation(EntityLayout.KEY);
		update();
	}

	/**
	 * Construct an entity for the given {@link TemplateComponent} with the
	 * given {@link EntityLayout}.
	 * 
	 * @param component
	 *            the {@link TemplateComponent} which the entity will represent
	 * @param layout
	 *            the layout information for the entity
	 */
	public Entity(TemplateComponent component, EntityLayout layout)
	{
		this.component = component;
		this.layout = layout;
		component.setAnnotation(EntityLayout.KEY, layout);
		update();
	}

	/**
	 * Computes the bounds of the entity (smallest rectangle containing all
	 * elements of the entity) and caches the result.
	 * 
	 * @see #bounds
	 */
	protected void computeBounds()
	{
		bounds = new Rectangle(
				layout.location.x - icon.getIconWidth() / 2,
				layout.location.y - icon.getIconHeight() / 2,
				icon.getIconWidth(),
				icon.getIconHeight())
				.union(labelBox)
				.union(ports[0].getBounds())
				.union(ports[1].getBounds())
				.union(ports[2].getBounds())
				.union(component.getType() == TemplateComponent.TYPE_CHANNEL ? ports[3]
						.getBounds().union(supHalo.getBounds())
						: ports[3].getBounds());
	}

	/**
	 * Retrieve the {@link TemplateComponent} represented by the entity.
	 * 
	 * @return the {@link TemplateComponent} represented by the entity
	 */
	public TemplateComponent getComponent()
	{
		return component;
	}

	public void draw(Graphics2D g2d)
	{
		draw(g2d, false);
	}

	@Override
	public void draw(Graphics2D g2d, boolean showInconsistency)
	{
		if (selected)
		{
			if (showInconsistency && inconsistent)
			{
				g2d.setColor(COLOR_SELECT_INCONSIST);
			}
			else
			{
				g2d.setColor(COLOR_SELECT);
			}
		}
		else
		{
			if (showInconsistency && inconsistent)
			{
				g2d.setColor(COLOR_INCONSIST);
			}
			else
			{
				g2d.setColor(COLOR_NORM);
			}
		}
		if (highlight)
		{
			Color temp = g2d.getColor();
			g2d.setColor(Color.WHITE);
			g2d.fillRect(labelBox.x - 1,
					labelBox.y - 1,
					labelBox.width + 2,
					labelBox.height + 2);
			g2d.setColor(temp);
		}
		drawCore(g2d);
		if (highlight)
		{
			if (component.getType() == TemplateComponent.TYPE_CHANNEL)
			{
				g2d.draw(supHalo);
				g2d.drawString(HALO_LABEL,
						(int)supHalo.getCenterX() + haloDX,
						(int)supHalo.getCenterY() + haloDY);
			}
			g2d.setStroke(MARKER_STROKE);
			g2d.drawRect(labelBox.x - 1,
					labelBox.y - 1,
					labelBox.width + 2,
					labelBox.height + 2);
			g2d.drawOval((int)ports[0].getMinX(),
					(int)ports[0].getMinY(),
					(int)ports[0].getWidth(),
					(int)ports[0].getHeight());
			g2d.drawOval((int)ports[1].getMinX(),
					(int)ports[1].getMinY(),
					(int)ports[1].getWidth(),
					(int)ports[1].getHeight());
			g2d.drawOval((int)ports[2].getMinX(),
					(int)ports[2].getMinY(),
					(int)ports[2].getWidth(),
					(int)ports[2].getHeight());
			g2d.drawOval((int)ports[3].getMinX(),
					(int)ports[3].getMinY(),
					(int)ports[3].getWidth(),
					(int)ports[3].getHeight());
		}
	}

	/**
	 * Renders only the icon and the label of the entity using the
	 * {@link DiagramElement#COLOR_NORM} color, in the given graphical context.
	 * This method is to be used to render representations of the entity outside
	 * of the drawing canvas, e.g., in dialog boxes.
	 * 
	 * @param g2d
	 *            the graphical context where the entity has to be rendered
	 */
	public void drawPlain(Graphics2D g2d)
	{
		g2d.setColor(COLOR_NORM);
		drawCore(g2d);
	}

	/**
	 * Renders the icon and the label of the entity in the given graphical
	 * context. This method is used internally by the other <code>draw...</code>
	 * methods.
	 * 
	 * @param g2d
	 *            the graphical context where the entity has to be rendered
	 */
	private void drawCore(Graphics2D g2d)
	{
		icon.paintIcon(g2d,
				layout.location.x - icon.getIconWidth() / 2,
				layout.location.y - icon.getIconHeight() / 2,
				g2d.getColor());
		labelBox.draw(g2d);
	}

	/**
	 * Retrieve the location of the entity.
	 * 
	 * @return the location of the entity
	 */
	public Point getLocation()
	{
		return layout.location;
	}

	/**
	 * Set the location of the entity.
	 * <p>
	 * NOTE: Do not use this method directly. Use
	 * {@link TemplateDiagram#translate(java.util.Collection, Point)} instead.
	 * 
	 * @param location
	 *            the new location of the entity
	 */
	public void setLocation(Point location)
	{
		layout.location = location;
		update();
	}

	/**
	 * NOTE: Do not use this method directly. Use
	 * {@link TemplateDiagram#translate(java.util.Collection, Point)} instead.
	 */
	public void translate(Point delta)
	{
		layout.location.x += delta.x;
		layout.location.y += delta.y;
		update();
	}

	/**
	 * Updates the entity to reflect any changes to the icon, label, location,
	 * type of entity, etc. In essence, recomputes how the entity should be
	 * rendered.
	 */
	public void update()
	{
		icon = new SimpleIcon(layout.tag, layout.color, DiagramElement
				.getGlobalFontRenderer());
		icon.setIsModule(component.getType() != TemplateComponent.TYPE_CHANNEL);
		icon.setFlagged(component.hasModel()
				&& component.getModel().hasAnnotation(FLAG_MARK));
		int maxWidth = 3 * icon.getIconWidth();
		String[] words = layout.label.split(" ");
		Vector<String> lines = new Vector<String>();
		int wordsConsumed = 0;
		while (wordsConsumed < words.length)
		{
			String line = "";
			int i;
			for (i = wordsConsumed; i < words.length; ++i)
			{
				line += words[i];
				if (getGlobalFontMetrics().stringWidth(line) > maxWidth)
				{
					break;
				}
				line += " ";
			}
			if (i == words.length) // all left-over words fit
			{
				line = line.substring(0, line.length() - 1);
				lines.add(line);
				wordsConsumed = words.length;
			}
			else
			// some left-over words didn't fit
			{
				if (i == wordsConsumed) // the first left-over word was too long
				{
					lines.add(line);
					wordsConsumed++;
				}
				else
				// at least one left-over word fit
				{
					line = line.substring(0, line.lastIndexOf(" "));
					lines.add(line);
					wordsConsumed = i;
				}
			}
		}
		labelBox = new LabelBox(lines);
		int deltaX = -(labelBox.width / 2);
		int deltaY = icon.getIconHeight() / 2 + LABEL_SPACING;
		labelBox.x = layout.location.x + deltaX;
		labelBox.y = layout.location.y + deltaY;
		ports[0] = new Ellipse2D.Float(
				layout.location.x - icon.getIconWidth() / 2 - 2 * PORT_RADIUS
						- 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[1] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				layout.location.y - icon.getIconHeight() / 2 - 2 * PORT_RADIUS
						- 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[2] = new Ellipse2D.Float(
				layout.location.x + icon.getIconWidth() / 2 + 1,
				layout.location.y - PORT_RADIUS,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		ports[3] = new Ellipse2D.Float(
				layout.location.x - PORT_RADIUS,
				(int)labelBox.getMaxY() + 1,
				2 * PORT_RADIUS,
				2 * PORT_RADIUS);
		if (component.getType() == TemplateComponent.TYPE_CHANNEL)
		{
			supHalo = new Ellipse2D.Float(
					layout.location.x + icon.getIconWidth() / 4,
					layout.location.y - icon.getIconHeight() / 4 - 2
							* HALO_RADIUS,
					2 * HALO_RADIUS,
					2 * HALO_RADIUS);
			haloDX = -getGlobalFontMetrics().stringWidth(HALO_LABEL) / 2 + 1;
			haloDY = getGlobalFontMetrics().getAscent() / 2;
		}
		computeBounds();
	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	@Override
	public boolean contains(Point p)
	{
		return labelBox.contains(p)
				|| ports[0].contains(p)
				|| ports[1].contains(p)
				|| ports[2].contains(p)
				|| ports[3].contains(p)
				|| (p.x >= layout.location.x - icon.getIconWidth() / 2
						&& p.x <= layout.location.x + icon.getIconWidth() / 2
						&& p.y >= layout.location.y - icon.getIconHeight() / 2 && p.y <= layout.location.y
						+ icon.getIconHeight() / 2)
				|| (component.getType() == TemplateComponent.TYPE_CHANNEL && supHalo
						.contains(p));
	}

	public boolean intersects(Rectangle r)
	{
		return labelBox.intersects(r)
				|| ports[0].intersects(r)
				|| ports[1].intersects(r)
				|| ports[2].intersects(r)
				|| ports[3].intersects(r)
				|| new Rectangle(
						layout.location.x - icon.getIconWidth() / 2,
						layout.location.y - icon.getIconHeight() / 2,
						icon.getIconWidth(),
						icon.getIconHeight()).intersects(r)
				|| (component.getType() == TemplateComponent.TYPE_CHANNEL && supHalo
						.intersects(r));
	}

	/**
	 * Checks on which part of the entity a given point lies. The answer can be
	 * the icon, the label, one of the connector handles, the
	 * "supervisor computation" shortcut icon (possible only if the entity
	 * represents <i>channel</i>), or nothing (i.e., the point does not lie on
	 * the connector).
	 * 
	 * @param p
	 *            the point to be checked
	 * @return which part of the connector the point lies on ({@link #ON_ICON},
	 *         {@link #ON_LABEL}, {@link #ON_PORT}, {@link #ON_SUP} or
	 *         {@link #ON_NADA})
	 */
	public int whereisPoint(Point p)
	{
		if (labelBox.contains(p))
		{
			return ON_LABEL;
		}
		if (ports[0].contains(p) || ports[1].contains(p)
				|| ports[2].contains(p) || ports[3].contains(p))
		{
			return ON_PORT;
		}
		if (component.getType() == TemplateComponent.TYPE_CHANNEL
				&& supHalo.contains(p))
		{
			return ON_SUP;
		}
		if (bounds.contains(p))
		{
			return ON_ICON;
		}
		return ON_NADA;
	}

	/**
	 * Retrieve the label of the entity.
	 * 
	 * @return the label of the entity
	 */
	public String getLabel()
	{
		return layout.label;
	}

	/**
	 * Set the label of the entity.
	 * <p>
	 * NOTE: Do not use this method directly. Use
	 * {@link TemplateDiagram#labelEntity(Entity, String)} instead.
	 * 
	 * @param label
	 *            the new label of the entity
	 */
	public void setLabel(String label)
	{
		if (label == null)
		{
			label = "";
		}
		if (layout.label.equals(label))
		{
			return;
		}
		layout.label = label;
		update();
	}

	/**
	 * Retrieve the center points of the connector handles.
	 * 
	 * @return the center points of the connector handles
	 * @see #ports
	 */
	public Point[] getPorts()
	{
		return new Point[] {
				new Point((int)ports[0].getCenterX(), (int)ports[0]
						.getCenterY()),
				new Point((int)ports[1].getCenterX(), (int)ports[1]
						.getCenterY()),
				new Point((int)ports[2].getCenterX(), (int)ports[2]
						.getCenterY()),
				new Point((int)ports[3].getCenterX(), (int)ports[3]
						.getCenterY()) };
	}

	/**
	 * Retrieve the icon of the entity.
	 * 
	 * @return the icon of the entity
	 */
	public EntityIcon getIcon()
	{
		return icon;
	}

	/**
	 * Set the icon of the entity.
	 * <p>
	 * NOTE: Do not use this method directly. Use
	 * {@link TemplateDiagram#setEntityIcon(Entity, EntityIcon)} instead.
	 * 
	 * @param icon
	 *            the new icon of the entity
	 */
	public void setIcon(EntityIcon icon)
	{
		layout.color = icon.getColor();
		layout.tag = icon.getTag();
		update();
	}
}

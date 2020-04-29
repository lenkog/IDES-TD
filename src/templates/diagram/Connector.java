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

package templates.diagram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import ides.api.core.Hub;
import templates.model.InconsistentModificationException;
import templates.model.TemplateComponent;
import templates.model.TemplateLink;

/**
 * Class to maintain the graphical representation of {@link TemplateLink}s.
 * 
 * @author Lenko Grigorov
 */
public class Connector extends DiagramElement {
    /**
     * Encapsulation of a graphical element which displays the events linked through
     * a {@link Connector}.
     * 
     * @author Lenko Grigorov
     */
    protected class EventBox extends Rectangle {
        private static final long serialVersionUID = 219703659050182848L;

        /**
         * The maximum number of event pairs to be displayed on a single
         * {@link Connector}.
         */
        private static final int EVENT_LIST_LIMIT = 3;

        /**
         * The string abbreviation of event pairs beyond the maximal number.
         */
        private static final String ELLIPSES = "...";

        /**
         * The symbol to be used to indicate that two events forma pair.
         */
        private static final String EVENT_BINDER = "=";

        /**
         * A pair of events.
         * 
         * @author Lenko Grigorov
         */
        protected class EventPair implements Comparable<EventPair> {
            /**
             * First event.
             */
            public String event1;

            /**
             * Second event.
             */
            public String event2;

            /**
             * Indicates if the event pair does not contain events but instead stands for an
             * abbreviation of event pairs. This is used when there are too many event
             * pairs.
             */
            public boolean isEllipses = false;

            /**
             * Construct an event pair.
             * 
             * @param event1 first event
             * @param event2 second event
             */
            public EventPair(String event1, String event2) {
                this.event1 = event1;
                this.event2 = event2;
            }

            /**
             * Construct an event pair which does not contain events but instead stands for
             * an abbreviation of event pairs.
             */
            public EventPair() {
                isEllipses = true;
            }

            public int compareTo(EventPair o) {
                if (isEllipses) {
                    if (o.isEllipses) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    return event1.compareTo(o.event1);
                }
            }
        }

        /**
         * Keeps track if the linked events on both sides of a connector have to be
         * shown. Some event boxes show only the list of linked events for only one
         * {@link TemplateComponent}.
         */
        protected boolean showBothEvents = false;

        /**
         * The pairs of linked events to be shown in the event box.
         */
        protected Vector<EventPair> events = new Vector<EventPair>();

        /**
         * Constructs an event box to display the pairs of linked events.
         */
        public EventBox() {
            showBothEvents = true;
            boolean isLeftLeft;
            if (left.getLocation().x == right.getLocation().x) {
                isLeftLeft = left.getLocation().y < right.getLocation().y;
            } else if (left.getLocation().x > right.getLocation().x) {
                isLeftLeft = false;
            } else {
                isLeftLeft = true;
            }

            for (TemplateLink link : getLinks()) {
                TemplateComponent component = isLeftLeft ? getLeftEntity().getComponent()
                        : getRightEntity().getComponent();
                events.add(link.getLeftComponent() == component
                        ? new EventPair(link.getLeftEventName(), link.getRightEventName())
                        : new EventPair(link.getRightEventName(), link.getLeftEventName()));
            }
            Collections.sort(events);
            if (events.size() > EVENT_LIST_LIMIT) {
                for (int i = 0; i < events.size() - EVENT_LIST_LIMIT; ++i) {
                    events.removeElementAt(EVENT_LIST_LIMIT);
                }
                events.add(new EventPair());
            }
            if (events.isEmpty()) {
                width = getGlobalFontMetrics().stringWidth(Hub.string("TD_noLinkEvents"));
                height = getGlobalFontMetrics().getHeight();
            } else {
                int maxWidth = 0;
                for (EventPair pair : events) {
                    if (pair.isEllipses) {
                        maxWidth = Math.max(maxWidth, getGlobalFontMetrics().stringWidth(ELLIPSES));
                    } else {
                        int combinedWidth = getGlobalFontMetrics()
                                .stringWidth(pair.event1 + EVENT_BINDER + pair.event2);
                        maxWidth = Math.max(maxWidth, combinedWidth);
                    }
                }
                width = maxWidth;
                height = getGlobalFontMetrics().getHeight() * events.size();
            }
        }

        /**
         * Constructs an event box to display the linked events from only one side of
         * the connector.
         * 
         * @param isSideLeft whether to display the events for the "left"
         *                   {@link TemplateComponent} linked by the connector (
         *                   <code>true</code>), or the events for the "right"
         *                   {@link TemplateComponent} (<code>false</code>)
         */
        public EventBox(boolean isSideLeft) {
            this.showBothEvents = false;
            for (TemplateLink link : getLinks()) {
                events.add(new EventPair(isSideLeft ? link.getLeftEventName() : link.getRightEventName(), ""));
            }
            Collections.sort(events);
            if (events.size() > EVENT_LIST_LIMIT) {
                for (int i = 0; i < events.size() - EVENT_LIST_LIMIT; ++i) {
                    events.removeElementAt(EVENT_LIST_LIMIT);
                }
                events.add(new EventPair());
            }
            if (events.isEmpty()) {
                width = 0;
                height = getGlobalFontMetrics().getHeight();
            } else {
                int maxWidth = 0;
                for (EventPair pair : events) {
                    if (pair.isEllipses) {
                        maxWidth = Math.max(maxWidth, getGlobalFontMetrics().stringWidth(ELLIPSES));
                    } else {
                        int combinedWidth = getGlobalFontMetrics().stringWidth(pair.event1);
                        maxWidth = Math.max(maxWidth, combinedWidth);
                    }
                }
                width = maxWidth;
                height = getGlobalFontMetrics().getHeight() * events.size();
            }
        }

        /**
         * Render the event box.
         * 
         * @param g2d the graphical context where the event box has to be rendered
         */
        public void draw(Graphics2D g2d) {
            if (events.isEmpty()) {
                if (showBothEvents) {
                    g2d.drawString(Hub.string("TD_noLinkEvents"), x + 1,
                            y + getGlobalFontMetrics().getHeight() - getGlobalFontMetrics().getDescent());
                }
            } else {
                for (int i = 0; i < events.size(); ++i) {
                    int deltaY = getGlobalFontMetrics().getHeight() * (i + 1) - getGlobalFontMetrics().getDescent();
                    if (events.elementAt(i).isEllipses) {
                        g2d.drawString(ELLIPSES, x, y + deltaY);
                    } else {
                        g2d.setFont(globalFont);
                        g2d.drawString(events.elementAt(i).event1, x, y + deltaY);
                        if (showBothEvents) {
                            int deltaX = getGlobalFontMetrics().stringWidth(events.elementAt(i).event1);
                            g2d.drawString(EVENT_BINDER + events.elementAt(i).event2, x + deltaX, y + deltaY);
                        }
                    }
                }
            }
        }
    }

    /**
     * Background color for the event boxes at the ends of the connector.
     */
    protected static Color BACKGROUND_COLOR = new Color(224, 224, 224, 200);

    /**
     * When approaching the connector with the mouse, how many pixels away should
     * the connector respond as if the mouse is over it.
     * 
     * @see #contains(Point)
     * @see #intersects(Rectangle)
     */
    private final static int SENSITIVITY = 4;

    /**
     * Offset of the event boxes in pixels, to produce spacing around the event
     * boxes.
     */
    protected static final int LABEL_SPACING = 5;

    /**
     * Constant to say mouse cursor is not over any part of the connector.
     * 
     * @see #whereisPoint(Point)
     */
    public final static int ON_NADA = 0;

    /**
     * Constant to say mouse cursor is over the line of the connector.
     * 
     * @see #whereisPoint(Point)
     */
    public final static int ON_LINE = 1;

    /**
     * Constant to say mouse cursor is over the event box in the center of the
     * connector.
     * 
     * @see #whereisPoint(Point)
     */
    public final static int ON_LABEL = 2;

    /**
     * The {@link TemplateLink}s visualized by this connector.
     */
    protected Collection<TemplateLink> links = new HashSet<TemplateLink>();

    /**
     * The "left" component linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    protected Entity left;

    /**
     * The "right" component linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    protected Entity right;

    /**
     * Cached bounds of the connector (smallest rectangle containing all parts of
     * the connector).
     */
    private Rectangle bounds;

    /**
     * The line that represents the connector.
     */
    protected Line2D line;

    /**
     * The event box listing the events of the "left" component which are linked by
     * the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    protected EventBox leftEventBox;

    /**
     * The event box listing the events of the "right" component which are linked by
     * the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    protected EventBox rightEventBox;

    /**
     * The event box in the center of the connector, displaying the event pairs
     * linked by the connector.
     */
    protected EventBox centerEventBox;

    /**
     * Construct a connector with the given parameters.
     * 
     * @param left  the "left" component to be linked by the connector
     * @param right the "right" component to be linked by the connector
     * @param links a collection of the {@link TemplateLink}s to be encapsulated by
     *              the connector (can be empty)
     *              <p>
     *              A connector is symmetric. "Left" and "right" are used only to
     *              enable addressing the two linked components separately.
     */
    public Connector(Entity left, Entity right, Collection<TemplateLink> links) {
        this.left = left;
        this.right = right;
        this.links.addAll(links);
        update();
    }

    /**
     * Retrieve the {@link TemplateLink}s encapsulated by the connector. If there
     * are no links associated with this connector, the returned collection is
     * empty.
     * 
     * @return the {@link TemplateLink}s encapsulated by the connector
     */
    public Collection<TemplateLink> getLinks() {
        return new HashSet<TemplateLink>(links);
    }

    /**
     * Retrieve the "left" {@link Entity} linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     * 
     * @return the "left" {@link Entity} linked by the connector
     */
    public Entity getLeftEntity() {
        return left;
    }

    /**
     * Retrieve the "right" {@link Entity} linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     * 
     * @return the "right" {@link Entity} linked by the connector
     */
    public Entity getRightEntity() {
        return right;
    }

    /**
     * Retrieve the two {@link Entity}s linked by the connector.
     * 
     * @return the two {@link Entity}s linked by the connector
     */
    public Entity[] getEntities() {
        return new Entity[] { left, right };
    }

    /**
     * Add a {@link TemplateLink} to the collection of links represented by the
     * connector. The link must be between the two {@link TemplateComponent}s
     * already linked by the connector.
     * <p>
     * NOTE: Do not use this method directly. Use
     * {@link TemplateDiagram#addLink(Connector, TemplateLink)} instead.
     * 
     * @param link the {@link TemplateLink} to be added to the collection of links
     *             represented by the connector
     * @throws InconsistentModificationException if the {@link TemplateLink} to be
     *                                           added does not link the same
     *                                           {@link TemplateComponent}s linked
     *                                           by the connector
     */
    public void addLink(TemplateLink link) {
        if (!links.contains(link)) {
            TemplateComponent[] endpoints = link.getComponents();
            if (!(left.getComponent() == endpoints[0] && right.getComponent() == endpoints[1])
                    && !(left.getComponent() == endpoints[1] && right.getComponent() == endpoints[0])) {
                throw new InconsistentModificationException(Hub.string("TD_inconsistencyConnecting"));
            }
            links.add(link);
            update();
        }
    }

    /**
     * Remove a {@link TemplateLink} from the collection of links represented by
     * this connector. If the link is not part of the collection, the method does
     * nothing.
     * <p>
     * NOTE: Do not use this method directly. Use
     * {@link TemplateDiagram#removeLink(Connector, TemplateLink)} instead.
     * 
     * @param link the {@link TemplateLink} to remove from the collection of links
     *             represented by this connector
     */
    public void removeLink(TemplateLink link) {
        if (links.contains(link)) {
            links.remove(link);
            update();
        }
    }

    public void draw(Graphics2D g2d) {
        draw(g2d, false);
    }

    @Override
    public void draw(Graphics2D g2d, boolean showInconsistency) {
        if (selected) {
            if (showInconsistency && inconsistent) {
                g2d.setColor(COLOR_SELECT_INCONSIST);
            } else {
                g2d.setColor(COLOR_SELECT);
            }
        } else {
            if (showInconsistency && inconsistent) {
                g2d.setColor(COLOR_INCONSIST);
            } else {
                g2d.setColor(COLOR_NORM);
            }
        }
        g2d.setStroke(LINE_STROKE);
        g2d.drawLine((int) line.getX1(), (int) line.getY1(), (int) line.getX2(), (int) line.getY2());
        if (highlight) {
            Color temp = g2d.getColor();
            g2d.setColor(BACKGROUND_COLOR);
            if (leftEventBox.width > 0) {
                g2d.fillRect(leftEventBox.x - 2, leftEventBox.y - 1, leftEventBox.width + 4,

                        leftEventBox.height + 2);
            }
            if (rightEventBox.width > 0) {
                g2d.fillRect(rightEventBox.x - 2, rightEventBox.y - 1, rightEventBox.width + 4,
                        rightEventBox.height + 2);
            }
            g2d.setColor(temp);
            leftEventBox.draw(g2d);
            rightEventBox.draw(g2d);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(centerEventBox.x - 1, centerEventBox.y - 1, centerEventBox.width + 2,
                    centerEventBox.height + 2);
            g2d.setColor(temp);
            g2d.setStroke(MARKER_STROKE);
            g2d.drawRect(centerEventBox.x - 1, centerEventBox.y - 1, centerEventBox.width + 2,
                    centerEventBox.height + 2);
            if (centerEventBox.y + centerEventBox.height
                    + 1 > (int) (line.getY1() + (line.getY2() - line.getY1()) / 2)) {
                if (centerEventBox.x > (int) (line.getX1() + (line.getX2() - line.getX1()) / 2)) {
                    g2d.drawLine(centerEventBox.x - 1, centerEventBox.y + centerEventBox.height / 2,
                            (int) (line.getX1() + (line.getX2() - line.getX1()) / 2),
                            (int) (line.getY1() + (line.getY2() - line.getY1()) / 2));
                } else {
                    g2d.drawLine(centerEventBox.x + centerEventBox.width + 1,
                            centerEventBox.y + centerEventBox.height / 2,
                            (int) (line.getX1() + (line.getX2() - line.getX1()) / 2),
                            (int) (line.getY1() + (line.getY2() - line.getY1()) / 2));
                }
            } else {
                g2d.drawLine(centerEventBox.x + centerEventBox.width / 2, centerEventBox.y + centerEventBox.height + 1,
                        (int) (line.getX1() + (line.getX2() - line.getX1()) / 2),
                        (int) (line.getY1() + (line.getY2() - line.getY1()) / 2));
            }
        }
        centerEventBox.draw(g2d);
        if (highlight) {
            // g2d.setStroke(MARKER_STROKE);
            // g2d.drawRect(rightEventBox.x - 1,
            // rightEventBox.y - 1,
            // rightEventBox.width + 2,
            // rightEventBox.height + 2);
        }
    }

    /**
     * NOTE: Do not use this method directly. Use
     * {@link TemplateDiagram#translate(Collection, Point)} instead.
     */
    public void translate(Point delta) {
        line = new Line2D.Float((float) line.getX1() + delta.x, (float) line.getY1() + delta.y,
                (float) line.getX2() + delta.x, (float) line.getY2() + delta.y);
        centerEventBox.translate(delta.x, delta.y);
        leftEventBox.translate(delta.x, delta.y);
        rightEventBox.translate(delta.x, delta.y);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Computes the bounds of the connector (smallest rectangle containing all
     * elements of the connector) and caches the result.
     * 
     * @see #bounds
     */
    protected void computeBounds() {
        bounds = line.getBounds().union(centerEventBox).union(leftEventBox).union(rightEventBox);
    }

    /**
     * Updates the connector to reflect any changes to the location of the linked
     * {@link Entity}s and/or the events being linked. In essence, recomputes how
     * the connector should be rendered. The layout of connectors is computed in its
     * entirety, no layout information needs to be preserved between sessions.
     */
    public void update() {
        Point location1 = left.getLocation();
        Point location2 = right.getLocation();
        float slope = (location1.x - location2.x) == 0 ? 2 * Math.signum(location1.y - location2.y)
                : ((float) location1.y - location2.y) / (location1.x - location2.x);
        // quad1
        // 0=entity Right left of entity Left,
        // 1=entity Right above entity Left,
        // 2=entity Right right of entity Left,
        // 3=entity Right below entity Left
        int quad1 = 0;
        if (Math.abs(slope) > 1) {
            if (location1.y - location2.y > 0) {
                quad1 = 1;
            } else {
                quad1 = 3;
            }
        } else {
            if (location1.x - location2.x > 0) {
                quad1 = 0;
            } else {
                quad1 = 2;
            }
        }
        int quad2 = (quad1 + 2) % 4;
        line = new Line2D.Float(left.getPorts()[quad1].x, left.getPorts()[quad1].y, right.getPorts()[quad2].x,
                right.getPorts()[quad2].y);
        centerEventBox = new EventBox();
        leftEventBox = new EventBox(true);
        rightEventBox = new EventBox(false);
        double midpointX = Math.min(line.getX1(), line.getX2()) + Math.abs(line.getX1() - line.getX2()) / 2;
        double midpointY = Math.min(line.getY1(), line.getY2()) + Math.abs(line.getY1() - line.getY2()) / 2;
        if (line.getY1() - line.getY2() == 0) {
            centerEventBox.x = (int) (midpointX - centerEventBox.width / 2);
            centerEventBox.y = (int) (midpointY - centerEventBox.height - LABEL_SPACING);
        } else if (line.getX1() - line.getX2() == 0) {
            centerEventBox.x = (int) (midpointX + LABEL_SPACING);
            centerEventBox.y = (int) (midpointY - centerEventBox.height / 2);
        } else {
            double lineS = (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
            double lineD = line.getY1() - lineS * line.getX1();
            double cornerX = midpointX - Math.signum(lineS) * (centerEventBox.getWidth() / 2 + LABEL_SPACING);
            double cornerY = midpointY + centerEventBox.getHeight() / 2 + LABEL_SPACING;
            double perpendicularS = -1 / lineS;
            double perpendicularD = cornerY - perpendicularS * cornerX;
            double intersectX = (perpendicularD - lineD) / (lineS - perpendicularS);
            double intersectY = lineS * intersectX + lineD;
            centerEventBox.x = (int) (midpointX + intersectX - cornerX - centerEventBox.width / 2);
            centerEventBox.y = (int) (midpointY + intersectY - cornerY - centerEventBox.height / 2);
        }
        if (quad1 == 0 || quad1 == 2) {
            boolean below;
            if (location1.y - location2.y > 0) {
                below = true;
            } else {
                below = false;
            }
            if (quad1 == 0) {
                leftEventBox.x = left.getPorts()[0].x - (below ? leftEventBox.width + LABEL_SPACING : -LABEL_SPACING);
                leftEventBox.y = left.getPorts()[0].y + LABEL_SPACING;
                rightEventBox.x = right.getPorts()[2].x
                        - (below ? rightEventBox.width + LABEL_SPACING : -LABEL_SPACING);
                rightEventBox.y = right.getPorts()[2].y + LABEL_SPACING;
            } else {
                leftEventBox.x = left.getPorts()[2].x - (below ? -LABEL_SPACING : leftEventBox.width + LABEL_SPACING);
                leftEventBox.y = left.getPorts()[2].y + LABEL_SPACING;
                rightEventBox.x = right.getPorts()[0].x
                        - (below ? -LABEL_SPACING : rightEventBox.width + LABEL_SPACING);
                rightEventBox.y = right.getPorts()[0].y + LABEL_SPACING;
            }
        } else {
            boolean onLeft;
            if (location1.x == location2.x) {
                onLeft = quad1 != 1;
            } else if (location1.x - location2.x > 0) {
                onLeft = false;
            } else {
                onLeft = true;
            }
            if (quad1 == 1) {
                leftEventBox.y = left.getPorts()[1].y + LABEL_SPACING;
                leftEventBox.x = left.getPorts()[1].x - (onLeft ? -LABEL_SPACING : leftEventBox.width + LABEL_SPACING);
                rightEventBox.y = right.getPorts()[3].y + LABEL_SPACING;
                rightEventBox.x = right.getPorts()[3].x
                        - (onLeft ? -LABEL_SPACING : rightEventBox.width + LABEL_SPACING);
            } else {
                leftEventBox.y = left.getPorts()[3].y + LABEL_SPACING;
                leftEventBox.x = left.getPorts()[3].x - (onLeft ? leftEventBox.width + LABEL_SPACING : -LABEL_SPACING);
                rightEventBox.y = right.getPorts()[1].y + LABEL_SPACING;
                rightEventBox.x = right.getPorts()[1].x
                        - (onLeft ? rightEventBox.width + LABEL_SPACING : -LABEL_SPACING);
            }
        }
        computeBounds();
    }

    @Override
    public boolean contains(Point p) {
        return centerEventBox.contains(p) || (line.getP1().distance(p) >= Entity.PORT_RADIUS
                && line.getP2().distance(p) >= Entity.PORT_RADIUS && line.ptSegDist(p) < SENSITIVITY);
    }

    public boolean intersects(Rectangle r) {
        return line.intersects(r) || centerEventBox.intersects(r)
                || line.ptSegDist(r.getMinX(), r.getMinY()) < SENSITIVITY
                || line.ptSegDist(r.getMinX(), r.getMaxY()) < SENSITIVITY
                || line.ptSegDist(r.getMaxX(), r.getMinY()) < SENSITIVITY
                || line.ptSegDist(r.getMaxX(), r.getMaxY()) < SENSITIVITY;
    }

    /**
     * Checks on which part of the connector a given point lies. The answer can be
     * the line representing the connector, the event box in the center of the
     * connector, or nothing (i.e., the point does not lie on the connector).
     * 
     * @param p the point to be checked
     * @return which part of the connector the point lies on ({@link #ON_LINE},
     *         {@link #ON_LABEL} or {@link #ON_NADA})
     */
    public int whereisPoint(Point p) {
        if (centerEventBox.contains(p)) {
            return ON_LABEL;
        } else if (line.ptSegDist(p) < SENSITIVITY) {
            return ON_LINE;
        }
        return ON_NADA;
    }

}

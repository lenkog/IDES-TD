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

package templates.presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import ides.api.core.Annotable;
import ides.api.core.Hub;
import templates.diagram.Connector;
import templates.diagram.DiagramElement;
import templates.diagram.Entity;
import templates.diagram.TemplateDiagramMessage;
import templates.diagram.actions.DiagramActions;
import templates.model.TemplateModel;

/**
 * A {@link TemplateCanvas} which allows the manipulation of a
 * {@link TemplateModel} by the user via the GUI.
 * 
 * @author Lenko Grigorov
 */
public class TemplateEditableCanvas extends TemplateCanvas implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = -6177488412629054011L;

    /**
     * The key to be used to annotate a {@link TemplateModel} with the appearance
     * settings of the canvas (zoom level and viewport position). This annotation is
     * used to restore the last appearance of the template diagram when the user
     * re-activates the diagram in the workspace.
     * 
     * @see Annotable
     */
    protected static final String CANVAS_SETTINGS = "templateCanvasSettings";

    /**
     * Canvas appearance settings.
     * 
     * @author Lenko Grigorov
     */
    protected static class CanvasSettings {
        /**
         * The portion of the diagram visible in the canvas.
         */
        public Rectangle viewport = new Rectangle(0, 0, 0, 0);

        /**
         * The zoom level.
         */
        public float zoom = 1;
    }

    /**
     * The stroke used to paint the boundary of the selection box when the user
     * drags the mouse around diagram components to select them.
     */
    protected static final Stroke SELECTIONBOX_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[] { 3, 3 }, 0f);

    /**
     * Toggle to turn on and off the processing of mouse events by the canvas when a
     * dialog appears over the canvas (e.g., when the user enters a new label for an
     * entity). Set to <code>true</code> to turn off the processing of mouse events,
     * set to <code>false</code> to turn on the processing of mouse events. The
     * mouse event of releasing the mouse button always sets this toggle to
     * <code>false</code> as the user is assumed to have started interacting with
     * the canvas directly.
     * 
     * @see #setUIInteraction(boolean)
     */
    protected boolean ignoreNextMouseEvent = false;

    /**
     * The interpreter of the mouse events for this canvas.
     */
    protected MouseInterpreter interpreter;

    /**
     * The diagram element which is highlighted. This is the element under the mouse
     * cursor. If there is no diagram element under the mouse cursor, the variable
     * is set to <code>null</code>.
     */
    protected DiagramElement hilitedElement = null;

    /**
     * The rectangle which defines the boundaries of the selection box when the user
     * drags the mouse around diagram components to select them. If the user is not
     * in the process of making such a selection, the variable is set to
     * <code>null</code>.
     */
    protected Rectangle selectionBox = null;

    /**
     * The point of origin for the new connector when the user is drawing a new
     * connector.
     */
    protected Point connectorOrigin = null;

    /**
     * Information about whether to paint the line representing the new connector
     * when the user is drawing a new connector. Set to <code>true</code> to paint
     * the connector, set to <code>false</code> otherwise.
     */
    protected boolean drawConnector = false;

    /**
     * Construct a new canvas to enable the editing of the given template design.
     * 
     * @param model the template design which the user will manipulate
     */
    public TemplateEditableCanvas(TemplateModel model) {
        super(model);
        autoZoom();
        scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
        scaleToFit = false;
        interpreter = new MouseInterpreter(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        String escAction = "esc";
        String deleteAction = "deleteSelection";

        // Associating key strokes with action names:
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escAction);
        // Associating the action names with operations:
        getActionMap().put(deleteAction, new AbstractAction() {
            private static final long serialVersionUID = -2136095877399476978L;

            public void actionPerformed(ActionEvent e) {
                if (!diagram.getSelection().isEmpty()) {
                    new DiagramActions.DeleteElementsAction(diagram, diagram.getSelection()).execute();
                }
            }
        });
        getActionMap().put(escAction, new AbstractAction() {
            private static final long serialVersionUID = -3238931531794626118L;

            public void actionPerformed(ActionEvent e) {
                if (isDrawingConnector()) {
                    finishConnector();
                    repaint();
                }
            }
        });
        refresh();
    }

    public JComponent getGUI() {
        JScrollPane sp = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        return sp;
    }

    public void paint(Graphics g) {
        scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
        autoScroll();
        Stroke oldStroke = ((Graphics2D) g).getStroke();
        super.paint(g);
        if (selectionBox != null) {
            ((Graphics2D) g).setStroke(SELECTIONBOX_STROKE);
            g.setColor(Color.GRAY);
            g.drawRect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
        }
        if (drawConnector && getMousePosition(true) != null) {
            ((Graphics2D) g).setStroke(oldStroke);
            g.setColor(Color.BLACK);
            Point origin = connectorOrigin;
            Point end = getMousePosition(true);
            g.drawLine(origin.x, origin.y, (int) (end.x / scaleFactor), (int) (end.y / scaleFactor));
        }
    }

    /**
     * In addition to painting the template diagram, also paint the highlighting of
     * the highlighted diagram element (if any).
     */
    protected void paintCore(Graphics2D g2d) {
        super.paintCore(g2d);
        if (hilitedElement != null) {
            hilitedElement.draw(g2d);
        }
    }

    /**
     * In addition to refreshing the rendering of the template diagram, update the
     * highlighting of diagram elements.
     */
    public void templateDiagramChanged(TemplateDiagramMessage message) {
        if (message.getOperationType() == TemplateDiagramMessage.OP_REMOVE
                && message.getElements().contains(hilitedElement)) {
            hilitedElement = null;
        }
        super.templateDiagramChanged(message);
    }

    /**
     * Set the scaling factor according to the zoom level setting stored as an
     * annotation in the template design. If this annotation cannot be found, set
     * the scaling factor to <code>1</code>.
     * 
     * @see CanvasSettings
     * @see Annotable
     */
    protected void autoZoom() {
        if (model.hasAnnotation(CANVAS_SETTINGS)) {
            Hub.getUserInterface().getZoomControl()
                    .setZoom(((CanvasSettings) model.getAnnotation(CANVAS_SETTINGS)).zoom);
        } else {
            Hub.getUserInterface().getZoomControl().setZoom(1);
        }
    }

    /**
     * Scroll the viewport of the canvas to the rectangle stored as an annotation in
     * the template design. After the scrolling, the annotation is removed. If the
     * annotation cannot be found, do nothing.
     * 
     * @see CanvasSettings
     * @see Annotable
     */
    protected void autoScroll() {
        if (model.hasAnnotation(CANVAS_SETTINGS)) {
            scrollRectToVisible(((CanvasSettings) model.getAnnotation(CANVAS_SETTINGS)).viewport);
            model.removeAnnotation(CANVAS_SETTINGS);
        }
    }

    /**
     * Create a descriptor of the appearance settings of the canvas and create an
     * annotation with it in the template design.
     * 
     * @see CanvasSettings
     * @see Annotable
     */
    protected void storeCanvasInfo() {
        CanvasSettings canvasSettings = new CanvasSettings();
        canvasSettings.viewport = getVisibleRect();
        canvasSettings.zoom = scaleFactor;
        model.setAnnotation(CANVAS_SETTINGS, canvasSettings);
    }

    public void refresh() {
        scaleFactor = Hub.getUserInterface().getZoomControl().getZoom();
        super.refresh();
    }

    /**
     * Before releasing the template design, annotate it with the current appearance
     * settings so that they can be reloaded if the user activates again the model
     * in the workspace.
     */
    public void release() {
        removeHighlight();
        diagram.clearSelection();
        storeCanvasInfo();
        super.release();
    }

    /**
     * Construct a new mouse event based on the given mouse event, where the mouse
     * cursor coordinates are transformed to account for the scaling factor used in
     * painting the template diagram.
     * 
     * @param me the original mouse event
     * @return the mouse event where the mouse cursor coordinates are transformed to
     *         account for the scaling factor used in painting the template diagram
     */
    public MouseEvent transformMouseCoords(MouseEvent me) {
        Point p = componentToLocal(me.getPoint());
        return new MouseEvent((Component) me.getSource(), me.getID(), me.getWhen(), me.getModifiersEx(), (int) p.x,
                (int) p.y, me.getClickCount(), me.isPopupTrigger(), me.getButton());
    }

    /**
     * Transform a point from the space of the template diagram to the space of the
     * screen, accounting for the scaling factor and the location of the canvas on
     * the screen.
     * 
     * @param p the point in the space of the template diagram
     * @return the point in the space of the screen
     */
    public Point localToScreen(Point p) {
        Point ret = localToComponent(p);
        SwingUtilities.convertPointToScreen(ret, this);
        return ret;
    }

    /**
     * Transform a point from the space of the template diagram to the space of
     * canvas component, accounting for the scaling factor.
     * 
     * @param p the point in the space of the template diagram
     * @return the point in the space of canvas component
     */
    public Point localToComponent(Point p) {
        return new Point((int) (p.x * scaleFactor), (int) (p.y * scaleFactor));
    }

    /**
     * Transform a point from the space of the canvas component to the space of the
     * template diagram, accounting for the scaling factor.
     * 
     * @param p the point in the space of the canvas component
     * @return the point in the space of the template diagram
     */
    public Point componentToLocal(Point p) {
        return new Point((int) (p.x / scaleFactor), (int) (p.y / scaleFactor));
    }

    /**
     * Specify whether the user is engaged in an interaction with a dialog which
     * appears over the canvas (e.g., when the user enters a new label for an
     * entity).
     * 
     * @param b set to <code>true</code> when the user engages in an interaction
     *          with a dialog which appears over the canvas; set to
     *          <code>false</code> when the user finishes interacting with a dialog
     *          which appears over the canvas
     * @see #ignoreNextMouseEvent
     */
    public void setUIInteraction(boolean b) {
        ignoreNextMouseEvent = b;
    }

    /**
     * Update the rectangle which defines the boundaries of the selection box when
     * the user drags the mouse around diagram components to select them.
     * 
     * @param r the new boundaries of the selection box
     */
    public void setSelectionBox(Rectangle r) {
        selectionBox = r;
        if (selectionBox != null) {
            Collection<DiagramElement> selected = new HashSet<DiagramElement>();
            for (Connector c : diagram.getConnectors()) {
                if (c.intersects(selectionBox)) {
                    selected.add(c);
                }
            }
            for (Entity e : diagram.getEntities()) {
                if (e.intersects(selectionBox)) {
                    selected.add(e);
                    selected.addAll(diagram.getAdjacentConnectors(e));
                }
            }
            diagram.setSelection(selected);
        }
    }

    /**
     * Retrieve the rectangle which defines the boundaries of the selection box when
     * the user drags the mouse around diagram components to select them.
     * 
     * @return the boundaries of the selection box
     */
    public Rectangle getSelectionBox() {
        return selectionBox;
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseClicked(MouseEvent arg0) {
        if (ignoreNextMouseEvent) {
            return;
        }
        arg0 = transformMouseCoords(arg0);
        interpreter.mouseClicked(arg0);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseEntered(MouseEvent arg0) {
        if (ignoreNextMouseEvent) {
            return;
        }
        arg0 = transformMouseCoords(arg0);
        interpreter.mouseEntered(arg0);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseExited(MouseEvent arg0) {
        if (ignoreNextMouseEvent) {
            return;
        }
        arg0 = transformMouseCoords(arg0);
        interpreter.mouseExited(arg0);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mousePressed(MouseEvent arg0) {
        if (ignoreNextMouseEvent) {
            return;
        }
        requestFocus();
        arg0 = transformMouseCoords(arg0);
        interpreter.mousePressed(arg0);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseReleased(MouseEvent arg0) {
        if (ignoreNextMouseEvent) {
            ignoreNextMouseEvent = false;
            return;
        }
        arg0 = transformMouseCoords(arg0);
        interpreter.mouseReleased(arg0);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseDragged(MouseEvent e) {
        if (ignoreNextMouseEvent) {
            return;
        }
        e = transformMouseCoords(e);
        interpreter.mouseDragged(e);
    }

    /**
     * Transform the mouse cursor coordinates accounting for the scaling factor and
     * forward the event to the mouse event interpreter.
     * 
     * @see #interpreter
     */
    public void mouseMoved(MouseEvent e) {
        if (ignoreNextMouseEvent) {
            return;
        }
        e = transformMouseCoords(e);
        interpreter.mouseMoved(e);
    }

    /**
     * Specify which diagram element should be highlighted.
     * 
     * @param element the diagram element to be highlighted
     */
    public void highlight(DiagramElement element) {
        if (hilitedElement != null) {
            hilitedElement.setHighlight(false);
        }
        hilitedElement = element;
        hilitedElement.setHighlight(true);
    }

    /**
     * Get the currently highlighted diagram element.
     * 
     * @return the currently highlighted diagram element, if an element is
     *         highlighted; <code>null</code> otherwise
     */
    public DiagramElement getHighlightedElement() {
        return hilitedElement;
    }

    /**
     * Stop highlighting the currently highlighted diagram element.
     */
    public void removeHighlight() {
        if (hilitedElement != null) {
            hilitedElement.setHighlight(false);
        }
        hilitedElement = null;
    }

    /**
     * Start painting a line to denote a new connector with the given origin. The
     * end-point of the line will be the tracking the location of the mouse cursor.
     * 
     * @param origin the origin of the line denoting a new connector
     */
    public void startConnector(Point origin) {
        connectorOrigin = origin;
        drawConnector = true;
    }

    /**
     * Stop painting a line to denote a new connector.
     */
    public void finishConnector() {
        drawConnector = false;
    }

    /**
     * Determine if the canvas is set to paint a line to denote a new connector.
     * 
     * @return <code>true</code> if the canvas is set to paint a line to denote a
     *         new connector; <code>false</code> otherwise
     */
    public boolean isDrawingConnector() {
        return drawConnector;
    }
}

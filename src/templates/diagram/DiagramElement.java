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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import templates.model.TemplateModel;

/**
 * Common functionality of graphical representations of {@link TemplateModel}
 * elements.
 * 
 * @author Lenko Grigorov
 */
public abstract class DiagramElement {
    /**
     * A pointer to the graphical context which will provide font rendering metrics.
     * <p>
     * Needs to be set before creating {@link TemplateDiagram}s.
     */
    protected static Graphics globalFontRenderer = null;

    /**
     * A pointer to the font to be used to render text on the display.
     * <p>
     * Needs to be set before creating {@link TemplateDiagram}s.
     */
    protected static Font globalFont = null;

    /**
     * Set the graphical context which will provide font rendering metrics.
     * <p>
     * Needs to be set before creating {@link TemplateDiagram}s.
     * 
     * @param g the graphical context which will provide font rendering metrics
     */
    public static void setGlobalFontRenderer(Graphics g) {
        globalFontRenderer = g;
    }

    /**
     * Retrieve the graphical context which provides font rendering metrics.
     * 
     * @return the graphical context which provides font rendering metrics
     */
    public static Graphics getGlobalFontRenderer() {
        return globalFontRenderer;
    }

    /**
     * Retrieve the font metrics for rendering text on the display.
     * 
     * @return the font metrics for rendering text on the display if both
     *         {@link #globalFontRenderer} and {@link #globalFont} are set;
     *         <code>null</code> otherwise
     */
    public static FontMetrics getGlobalFontMetrics() {
        if (globalFontRenderer == null || globalFont == null) {
            return null;
        }
        return globalFontRenderer.getFontMetrics(globalFont);
    }

    /**
     * Set the font to be used to render text on the display.
     * <p>
     * Needs to be set before creating {@link TemplateDiagram}s.
     * 
     * @param f the font to be used to render text on the display
     */
    public static void setGlobalFont(Font f) {
        globalFont = f;
    }

    /**
     * Retrieve the font to be used to render text on the display.
     * 
     * @return the font to be used to render text on the display
     */
    public static Font getGlobalFont() {
        return globalFont;
    }

    /**
     * Default color for rendering diagram elements.
     */
    public static final Color COLOR_NORM = Color.BLACK;

    /**
     * Color for rendering inconsistent diagram elements.
     */
    public static final Color COLOR_INCONSIST = new Color(255, 120, 0);

    /**
     * Color for rendering selected diagram elements.
     */
    public static final Color COLOR_SELECT = new Color(70, 100, 140);

    /**
     * Color for rendering selected inconsistent diagram elements.
     */
    public static final Color COLOR_SELECT_INCONSIST = Color.ORANGE;

    /**
     * Default stroke style for rendering diagram elements.
     */
    protected static final Stroke LINE_STROKE = new BasicStroke(1);

    /**
     * Stroke for rendering thicker lines.
     */
    protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);

    /**
     * Stroke for rendering selection marquees.
     */
    protected static final Stroke MARKER_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
            new float[] { 1, 2 }, 0f);

    /**
     * Specifies if the diagram element is highlighted.
     */
    protected boolean highlight = false;

    /**
     * Specifies if the diagram element is selected.
     */
    protected boolean selected = false;

    /**
     * Specifies if the diagram element is inconsistent.
     */
    protected boolean inconsistent = false;

    /**
     * Translate the diagram element by the given displacement in the x and y
     * direction.
     * 
     * @param delta the displacement in the x and y direction
     */
    public abstract void translate(Point delta);

    /**
     * Set if the diagram element should be rendered as highlighted or not.
     * 
     * @param b <code>true</code> if the diagram element should be rendered as
     *          highlighted; <code>false</code> otherwise
     */
    public void setHighlight(boolean b) {
        highlight = b;
    }

    /**
     * Set if the diagram element should be rendered as selected or not.
     * 
     * @param b <code>true</code> if the diagram element should be rendered as
     *          seelcted; <code>false</code> otherwise
     */
    public void setSelected(boolean b) {
        selected = b;
    }

    /**
     * Set if the diagram element should be rendered as inconsistent or not.
     * 
     * @param b <code>true</code> if the diagram element should be rendered as
     *          inconsistent; <code>false</code> otherwise
     */
    public void setInconsistent(boolean b) {
        inconsistent = b;
    }

    /**
     * Retrieve the bounds of the diagram element.
     * 
     * @return the smallest rectangle containing all parts of the diagram element
     */
    public abstract Rectangle getBounds();

    /**
     * Render the diagram element in the given graphical context, disregarding the
     * inconsistency setting. I.e., inconsistent diagram elements should be rendered
     * as if they were consistent.
     * 
     * @param g2d the graphical context where the diagram element has to be rendered
     */
    public abstract void draw(Graphics2D g2d);

    /**
     * Render the diagram element in the given graphical context, according to the
     * choice of differentiating or not inconsistent diagram elements.
     * 
     * @param g2d               the graphical context where the diagram element has
     *                          to be rendered
     * @param showInconsistency choice for rendering inconsistent diagram elements
     *                          differently or not; if <code>true</code>,
     *                          inconsistent diagram elements have to be rendered
     *                          differently; if<code>false</code>, inconsistent
     *                          diagram elements should be rendered as if they were
     *                          consistent
     */
    public abstract void draw(Graphics2D g2d, boolean showInconsistency);

    /**
     * Checks if the diagram element contains the given point.
     * <p>
     * With non-rectangular diagram elements, this is different from checking if the
     * point is contained within the bounds of the element. More specifically, a
     * point may be contained within the bounds, but this method may still return
     * <code>false</code>.
     * 
     * @param p the point to be checked
     * @return <code>true</code> if the diagram element contains the point;
     *         <code>false</code> otherwise
     */
    public abstract boolean contains(Point p);

    /**
     * Checks if the given rectangle intersects the diagram element.
     * <p>
     * With non-rectangular diagram elements, this is different from checking if the
     * rectangle intersects the bounds of the element. More specifically, a
     * rectangle may intersect the bounds, but this method may still return
     * <code>false</code>.
     * 
     * @param r the rectangle to be checked
     * @return <code>true</code> if the rectangle intersects the diagram element;
     *         <code>false</code> otherwise
     */
    public abstract boolean intersects(Rectangle r);
}

/*
 * Copyright (c) 2009-2020, Lenko Grigorov
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import templates.utils.EntityIcon;

/**
 * Simple implementation of the {@link EntityIcon} interface. The icon is either
 * rectangular or oval (depending on the type of rendered component), with
 * uniform background, containing the "tag" of the component.
 * 
 * @author Lenko Grigorov
 */
public class SimpleIcon implements EntityIcon {
    /**
     * Line stroke for the border of the icon.
     */
    protected static final Stroke FAT_LINE_STROKE = new BasicStroke(2);

    /**
     * Space to be added between the icon border and the text inside the icon (if
     * any), in pixels.
     */
    protected static final int SPACING = 2;

    /**
     * Background color of the icon.
     */
    protected Color bgColor = Color.WHITE;

    /**
     * Color of the text inside the icon (if any).
     */
    protected Color fgColor = Color.BLACK;

    /**
     * Text inside the icon. Can be the empty string.
     */
    protected String tag = "";

    /**
     * Text inside the icon when it is "flagged". Can be the empty string.
     */
    protected String flaggedTag = "";

    /**
     * Width of the icon.
     */
    protected int w = EntityIcon.BOX_DISTANCE;

    /**
     * Width of the icon when it is "flagged".
     */
    protected int flaggedW = EntityIcon.BOX_DISTANCE;

    /**
     * Height of the icon.
     */
    protected int h = EntityIcon.BOX_DISTANCE;

    /**
     * Height of the icon when it is "flagged".
     */
    protected int flaggedH = EntityIcon.BOX_DISTANCE;

    /**
     * The displacement of the text inside the icon, in the Y direction.
     */
    protected int deltaY = 0;

    /**
     * The displacement of the text inside the icon when it is "flagged", in the Y
     * direction.
     */
    protected int flaggedDeltaY = 0;

    /**
     * The displacement of the text inside the icon, in the X direction.
     */
    protected int deltaX = 0;

    /**
     * The displacement of the text inside the icon when it is "flagged", in the X
     * direction.
     */
    protected int flaggedDeltaX = 0;

    /**
     * Does the icon represent a <i>module</i> (<code>true</code>) or a
     * <i>channel</i> (<code>false</code>).
     */
    protected boolean isModule = true;

    /**
     * Is the icon "flagged".
     */
    protected boolean flag = false;

    /**
     * Construct a simple icon with the given parameters.
     * 
     * @param tag     the text inside the icon (can be the empty string)
     * @param color   the background color of the icon
     * @param context the graphical context to be used for font metrics information
     */
    public SimpleIcon(String tag, Color color, Graphics context) {
        this.tag = tag;
        flaggedTag = "".equals(tag) ? "" : tag + "^";
        setColor(color);
        w = Math.max(context.getFontMetrics().stringWidth(tag) + 2 * SPACING, BOX_DISTANCE);
        h = Math.max(context.getFontMetrics().getHeight() + 2 * SPACING, BOX_DISTANCE);
        deltaY = context.getFontMetrics().getAscent();
        deltaX = (w - context.getFontMetrics().stringWidth(tag)) / 2;
        flaggedW = Math.max(context.getFontMetrics().stringWidth(flaggedTag) + 2 * SPACING, BOX_DISTANCE);
        flaggedH = h;
        flaggedDeltaY = deltaY;
        flaggedDeltaX = (flaggedW - context.getFontMetrics().stringWidth(flaggedTag)) / 2;
    }

    /**
     * Construct a simple icon with default attributes.
     */
    public SimpleIcon() {
    }

    public int getIconHeight() {
        return flag ? flaggedH : h;
    }

    public int getIconWidth() {
        return flag ? flaggedW : w;
    }

    public void paintIcon(Component arg0, Graphics g, int x, int y) {
        paintIcon(g, x, y, Color.BLACK);
    }

    public void paintIcon(Graphics g, int x, int y, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(FAT_LINE_STROKE);
        Color oldColor = g2d.getColor();
        g2d.setColor(bgColor);
        if (isModule) {
            g2d.fillRect(x, y, flag ? flaggedW : w, flag ? flaggedH : h);
            g2d.setColor(color);
            g2d.drawRect(x, y, flag ? flaggedW : w, flag ? flaggedH : h);
        } else {
            g2d.fillOval(x, y, flag ? flaggedW : w, flag ? flaggedH : h);
            g2d.setColor(color);
            g2d.drawOval(x, y, flag ? flaggedW : w, flag ? flaggedH : h);
        }
        g2d.setColor(fgColor);
        g2d.drawString(flag ? flaggedTag : tag, x + SPACING + (flag ? flaggedDeltaX : deltaX),
                y + SPACING + (flag ? flaggedDeltaY : deltaY));
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    public SimpleIcon clone() {
        SimpleIcon icon = new SimpleIcon();
        icon.isModule = isModule;
        icon.bgColor = bgColor;
        icon.fgColor = fgColor;
        icon.tag = tag;
        icon.w = w;
        icon.h = h;
        icon.deltaY = deltaY;
        icon.deltaX = deltaX;
        icon.flag = flag;
        icon.flaggedTag = flaggedTag;
        icon.flaggedW = flaggedW;
        icon.flaggedH = flaggedH;
        icon.flaggedDeltaY = flaggedDeltaY;
        icon.flaggedDeltaX = flaggedDeltaX;
        return icon;
    }

    public void setIsModule(boolean b) {
        isModule = b;
    }

    public Color getColor() {
        return bgColor;
    }

    public boolean isFlagged() {
        return flag;
    }

    public void setColor(Color color) {
        bgColor = color;
        if ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3 < 128) {
            fgColor = Color.WHITE;
        } else {
            fgColor = Color.BLACK;
        }
    }

    public void setFlagged(boolean b) {
        flag = b;
    }

    public String getTag() {
        return tag;
    }
}

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

package templates.utils;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Interface for the icons used to represent components in a template design.
 * 
 * @author Lenko Grigorov
 */
public interface EntityIcon extends Icon {
    /**
     * The minimal width and height of the icon in pixels.
     */
    public int BOX_DISTANCE = 20;

    /**
     * Create a clone of this icon.
     * 
     * @return the clone of this icon
     */
    public EntityIcon clone();

    /**
     * Specify the type of icon to be painted.
     * 
     * @param b specify <code>true</code> for an icon for template components of
     *          type module, specify <code>false</code> for an icon for template
     *          components of type channel
     */
    public void setIsModule(boolean b);

    /**
     * Render the icon in the given graphics context according to the given
     * parameters.
     * 
     * @param g     the graphics context where the icon has to be rendered
     * @param x     the location of the icon on the horizontal axis
     * @param y     the location of the icon on the vertical axis
     * @param color the color to be used for the border of the icon
     */
    public void paintIcon(Graphics g, int x, int y, Color color);

    /**
     * Set the background color of the icon.
     * 
     * @param color the new background color of the icon
     */
    public void setColor(Color color);

    /**
     * Retrieve the background color of the icon.
     * 
     * @return the background color of the icon
     */
    public Color getColor();

    /**
     * Specify if the icon has to be "flagged".
     * 
     * @param b specify <code>true</code> if the icon has to be "flagged", specify
     *          <code>false</code> otherwise
     */
    public void setFlagged(boolean b);

    /**
     * Check if the icon is "flagged".
     * 
     * @return <code>true</code> if the icon is "flagged"; <code>false</code>
     *         otherwise
     */
    public boolean isFlagged();

    /**
     * Retrieve the text rendered inside the icon.
     * 
     * @return the text rendered inside the icon
     */
    public String getTag();
}

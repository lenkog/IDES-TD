/*
 * Copyright (c) 2010-2020, Lenko Grigorov
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
import java.awt.Point;

import templates.model.TemplateComponent;

/**
 * Encapsulation of the layout information for the graphical representation of
 * {@link TemplateComponent}s.
 * 
 * @author Lenko Grigorov
 */
public class EntityLayout {
    /**
     * The annotation key for the layout information.
     */
    public final static String KEY = "templates.diagram.EntityLayout";

    /**
     * The center point of the icon.
     */
    public Point location;

    /**
     * The label.
     */
    public String label;

    /**
     * The background color of the icon.
     */
    public Color color;

    /**
     * The text inside the icon.
     */
    public String tag;

    /**
     * Construct an entity layout. All attributes get default values.
     */
    public EntityLayout() {
        location = new Point();
        label = "";
        color = Color.WHITE;
        tag = "";
    }
}

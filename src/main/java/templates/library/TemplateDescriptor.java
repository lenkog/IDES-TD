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

package templates.library;

import java.awt.Color;

/**
 * Encapsulates the visual description of a {@link Template}.
 * 
 * @author Lenko Grigorov
 */
public class TemplateDescriptor {
    /**
     * The "ID" of the template. This is a short string which can be used to
     * identify the template, e.g., by displaying it inside the template icon.
     */
    public String tag = "TAG";

    /**
     * The color of the icon of the template.
     */
    public Color color = Color.WHITE;

    /**
     * The description of the template. This can be a longer piece of text including
     * any notes about the template.
     */
    public String description = "";

    /**
     * Produce a shortened version of the description of a template. The shortened
     * version consists of the first 47 characters of the first line of the
     * description appended with "..." (in case the first line contains more than 50
     * characters), or all characters of the first line of the description (in case
     * the first line contains not more than 50 characters).
     * 
     * @param d the original description of the template
     * @return the shortened version of the description of the template
     */
    public static String shortDescription(String d) {
        if (d == null) {
            return null;
        }
        String[] lines = d.split("\n");
        if (lines.length == 0) {
            return "";
        }
        return lines[0].length() > 50 ? lines[0].substring(0, 47) + "..." : lines[0];
    }
}

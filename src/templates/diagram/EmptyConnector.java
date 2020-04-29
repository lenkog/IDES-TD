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

import templates.model.TemplateComponent;
import templates.model.TemplateLink;

/**
 * Empty connector (with no associated {@link TemplateLink}s). This class is
 * used only to package empty connectors for IO purposes. The {@link Connector}
 * class maintains the graphical representations of empty connectors, similar to
 * non-empty connectors.
 * 
 * @author Lenko Grigorov
 */
public class EmptyConnector {
    /**
     * The id of the "left" {@link TemplateComponent} linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    public long leftComponent;

    /**
     * The id of the "right" {@link TemplateComponent} linked by the connector.
     * <p>
     * A connector is symmetric. "Left" and "right" are used only to enable
     * addressing the two linked components separately.
     */
    public long rightComponent;

    /**
     * Construct an empty connector linking the given components.
     * 
     * @param leftComponent  the id of the "left" {@link TemplateComponent} to be
     *                       linked by the connector
     * @param rightComponent the id of the "right" {@link TemplateComponent} to be
     *                       linked by the connector
     *                       <p>
     *                       A connector is symmetric. "Left" and "right" are used
     *                       only to enable addressing the two linked components
     *                       separately.
     */
    public EmptyConnector(long leftComponent, long rightComponent) {
        this.leftComponent = leftComponent;
        this.rightComponent = rightComponent;
    }
}

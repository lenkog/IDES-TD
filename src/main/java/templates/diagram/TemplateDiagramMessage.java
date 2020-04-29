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

import java.util.Collection;

/**
 * Describes messages sent by {@link TemplateDiagram} to listeners for changes
 * (i.e., {@link TemplateDiagramSubscriber}s).
 * 
 * @author Lenko Grigorov
 */
public class TemplateDiagramMessage {
    /**
     * Addition to the template diagram.
     */
    public static final int OP_ADD = 1;

    /**
     * Removal from the template diagram.
     */
    public static final int OP_REMOVE = 2;

    /**
     * Modification of the template diagram which is not addition or removal.
     */
    public static final int OP_MODIFY = 4;

    /**
     * The source of the message.
     */
    protected TemplateDiagram source;

    /**
     * The {@link DiagramElement}s affected by the change described by this message.
     */
    protected Collection<DiagramElement> elements;

    /**
     * The type of the operation ({@link #OP_ADD}, {@link #OP_REMOVE} or
     * {@link #OP_MODIFY})
     */
    protected int operationType;

    /**
     * Text message.
     */
    protected String message;

    /**
     * Create a message with the given parameters.
     * 
     * @param source        the source of the message
     * @param elements      the diagram elements affected by the change
     * @param operationType the type of the operation ({@link #OP_ADD},
     *                      {@link #OP_REMOVE} or {@link #OP_MODIFY})
     */
    public TemplateDiagramMessage(TemplateDiagram source, Collection<DiagramElement> elements, int operationType) {
        this(source, elements, operationType, "");
    }

    /**
     * Create a message with the given parameters.
     * 
     * @param source        the source of the message
     * @param elements      the diagram elements affected by the change
     * @param operationType the type of the operation ({@link #OP_ADD},
     *                      {@link #OP_REMOVE} or {@link #OP_MODIFY})
     * @param message       text message
     */
    public TemplateDiagramMessage(TemplateDiagram source, Collection<DiagramElement> elements, int operationType,
            String message) {
        this.source = source;
        this.elements = elements;
        this.operationType = operationType;
        this.message = message;
    }

    /**
     * Retrieve the source of the message.
     * 
     * @return the source of the message
     */
    public TemplateDiagram getSource() {
        return source;
    }

    /**
     * Retrieve the {@link DiagramElement}s affected by the change described by this
     * message.
     * 
     * @return the {@link DiagramElement}s affected by the change described by this
     *         message
     */
    public Collection<DiagramElement> getElements() {
        return elements;
    }

    /**
     * Retrieve the type of the operation.
     * 
     * @return the type of the operation ({@link #OP_ADD}, {@link #OP_REMOVE} or
     *         {@link #OP_MODIFY})
     */
    public int getOperationType() {
        return operationType;
    }

    /**
     * Retrieve the text message. May be <code>null</code>.
     * 
     * @return the text message
     */
    public String getMessage() {
        return message;
    }
}

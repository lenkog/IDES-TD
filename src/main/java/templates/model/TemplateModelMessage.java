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

package templates.model;

/**
 * Message sent by a {@link TemplateModel} to notify listeners of changes to the
 * model.
 * 
 * @author Lenko Grigorov
 */
public class TemplateModelMessage {
    /**
     * Addition to the model.
     */
    public static final int OP_ADD = 1;

    /**
     * Removal from the model.
     */
    public static final int OP_REMOVE = 2;

    /**
     * Modification of the model which is not addition or removal.
     */
    public static final int OP_MODIFY = 4;

    /**
     * Concerning a {@link TemplateComponent}.
     */
    public static final int ELEMENT_COMPONENT = 1;

    /**
     * Concerning a {@link TemplateLink}.
     */
    public static final int ELEMENT_LINK = 2;

    /**
     * The source of the message.
     */
    protected TemplateModel source;

    /**
     * The id of the element (component or link).
     */
    protected long elementId;

    /**
     * The type of the element ({@link #ELEMENT_COMPONENT} or
     * {@link #ELEMENT_LINK}).
     */
    protected int elementType;

    /**
     * The type of the operation ({@link #OP_ADD}, {@link #OP_REMOVE} or
     * {@link #OP_MODIFY}).
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
     * @param elementId     the id of the element
     * @param elementType   the type of the element ({@link #ELEMENT_COMPONENT} or
     *                      {@link #ELEMENT_LINK})
     * @param operationType the type of the operation ({@link #OP_ADD},
     *                      {@link #OP_REMOVE} or {@link #OP_MODIFY})
     * @param message       text message
     */
    public TemplateModelMessage(TemplateModel source, long elementId, int elementType, int operationType,
            String message) {
        this.source = source;
        this.elementId = elementId;
        this.elementType = elementType;
        this.operationType = operationType;
        this.message = message;
    }

    /**
     * Create a message with the given parameters and a <code>null</code> text
     * message.
     * 
     * @param source        the source of the message
     * @param elementId     the id of the element
     * @param elementType   the type of the element ({@link #ELEMENT_COMPONENT} or
     *                      {@link #ELEMENT_LINK})
     * @param operationType the type of the operation ({@link #OP_ADD},
     *                      {@link #OP_REMOVE} or {@link #OP_MODIFY})
     */
    public TemplateModelMessage(TemplateModel source, long elementId, int elementType, int operationType) {
        this(source, elementId, elementType, operationType, null);
    }

    /**
     * Retrieve the source of the message.
     * 
     * @return the source of the message
     */
    public TemplateModel getSource() {
        return source;
    }

    /**
     * Retrieve the id of the element.
     * 
     * @return the id of the element
     */
    public long getElementId() {
        return elementId;
    }

    /**
     * Retrieve the type of the element.
     * 
     * @return the type of the element ({@link #ELEMENT_COMPONENT} or
     *         {@link #ELEMENT_LINK})
     */
    public int getElementType() {
        return elementType;
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

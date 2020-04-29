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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import templates.diagram.DiagramElement;

/**
 * Description of a template design consistency issue.
 * 
 * @author Lenko Grigorov
 */
public class IssueDescriptor {
    /**
     * The indicator for an issue of type "error".
     */
    public static final int TYPE_ERROR = 1;

    /**
     * The indicator for an issue of type "warning".
     */
    public static final int TYPE_WARNING = 2;

    /**
     * The human-readable description of the issue.
     */
    public String message;

    /**
     * The type of issue (error or warning)
     */
    public int type;

    /**
     * A collection of actions which can fix the issue.
     */
    public List<Action> fixes;

    /**
     * The set of diagram elements contributing to the occurrence of the consistency
     * issue.
     */
    public Collection<DiagramElement> elements;

    /**
     * Construct a new consistency issue descriptor for the given arguments.
     * 
     * @param message  the human-readable description of the issue
     * @param type     the type of issue (error or warning)
     * @param elements collection of actions which can fix the issue
     * @param fixes    the set of diagram elements contributing to the occurrence of
     *                 the issue
     */
    public IssueDescriptor(String message, int type, Collection<DiagramElement> elements, List<Action> fixes) {
        this.message = message;
        this.elements = new HashSet<DiagramElement>(elements);
        this.type = type;
        this.fixes = new LinkedList<Action>(fixes);
    }
}

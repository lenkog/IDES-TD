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

import templates.model.TemplateComponent;

/**
 * To be thrown when an {@link Entity} cannot retrieve the layout information
 * for a {@link TemplateComponent} during instantiation.
 * 
 * @author Lenko Grigorov
 */
public class MissingLayoutException extends Exception {
    private static final long serialVersionUID = 5508872973612435905L;

    /**
     * Calls <code>super</code>.
     */
    public MissingLayoutException() {
    }

    /**
     * Calls <code>super</code> with the given arguments.
     * 
     * @param arg0
     */
    public MissingLayoutException(String arg0) {
        super(arg0);
    }

    /**
     * Calls <code>super</code> with the given arguments.
     * 
     * @param arg0
     */
    public MissingLayoutException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Calls <code>super</code> with the given arguments.
     * 
     * @param arg0
     * @param arg1
     */
    public MissingLayoutException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}

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

package templates.library;

import java.io.File;

/**
 * Singleton manager of the {@link TemplateLibrary}s available at runtime. As of
 * Template Design plugin version 3, a single template library is maintained, in
 * the "templates" sub-directory of the IDES installation.
 * 
 * @author Lenko Grigorov
 */
public class TemplateManager {
    /**
     * Singleton instance.
     */
    private static TemplateManager me = null;

    /**
     * The library of the local user.
     */
    protected TemplateLibrary localLib;

    /**
     * The library shared by all users.
     * <p>
     * Note: As of Template Design plugin version 3, shared libraries are not
     * supported. This functionality is reserved for future releases of the Template
     * Design plugin.
     */
    protected TemplateLibrary sharedLib;

    /**
     * Initialize the template manager. If the "templates" sub-directory does not
     * exist, it is created.
     */
    private TemplateManager() {
        File local = new File("templates");
        if (!local.exists()) {
            local.mkdir();
        }
        localLib = new TemplateLibrary(local);
    }

    /**
     * Access the singleton instance of the template manager.
     * 
     * @return the template manager
     */
    public static TemplateManager instance() {
        if (me == null) {
            me = new TemplateManager();
        }
        return me;
    }

    /**
     * @throws RuntimeException cloning is not allowed
     */
    @Override
    public Object clone() {
        throw new RuntimeException("Cloning of " + this.getClass().toString() + " not supported.");
    }

    /**
     * Retrieve the main template library.
     * <p>
     * Note: As of Template Design plugin version 3, only one library is supported
     * by the {@link TemplateManager}. This method returns this library.
     * 
     * @return the main template library
     */
    public TemplateLibrary getMainLibrary() {
        return localLib;
    }
}

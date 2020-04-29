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

package templates.plugin;

import java.util.ResourceBundle;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.plugin.Plugin;
import ides.api.plugin.PluginInitException;
import ides.api.plugin.io.IOPluginManager;
import ides.api.plugin.model.ModelManager;
import ides.api.plugin.operation.OperationManager;
import ides.api.plugin.presentation.ToolsetManager;
import templates.io.JPEGExporter;
import templates.io.PNGExporter;
import templates.io.TemplateFileIO;
import templates.library.TemplateMetaIO;
import templates.model.TemplateModel;
import templates.model.v3.TemplateDesign;
import templates.operations.CentralizedSupSolution;
import templates.operations.ChannelSup;
import templates.operations.ModularSupSolution;
import templates.presentation.TemplateToolset;

/**
 * The main class of the template design plugin. This is the class instantiated
 * by IDES when loading the plugin.
 * <p>
 * The name of the JAR file with the plugin has to give the path to this class,
 * i.e., <code>templates.plugin.TemplatesPlugin.jar</code>
 * 
 * @author Lenko Grigorov
 */
public class TemplatesPlugin implements Plugin {

    public String getCredits() {
        return Hub.string("TD_DEVELOPERS");
    }

    public String getDescription() {
        return Hub.string("TD_DESC");
    }

    public String getLicense() {
        return Hub.string("TD_LICENSE");
    }

    public String getName() {
        return Hub.string("TD_SHORT");
    }

    public String getVersion() {
        return Hub.string("TD_VER");
    }

    /**
     * Register all classes necessary to work with template designs:
     * {@link TemplateModel}, {@link TemplateToolset}, {@link TemplateFileIO},
     * {@link TemplateMetaIO}, template operations, etc.
     */
    public void initialize() throws PluginInitException {
        // Resources
        Hub.addResouceBundle(ResourceBundle.getBundle("templates"));

        // Models
        ModelManager.instance().registerModel(TemplateDesign.myDescriptor);

        // Toolsets
        ToolsetManager.instance().registerToolset(TemplateModel.class, new TemplateToolset());

        // IO
        TemplateFileIO ioPlugin = new TemplateFileIO();
        IOPluginManager.instance().registerDataSaver(ioPlugin, TemplateModel.class);
        IOPluginManager.instance().registerDataLoader(ioPlugin, ioPlugin.getIOTypeDescriptor());
        IOPluginManager.instance().registerMetaSaver(ioPlugin, TemplateModel.class);
        for (String tag : ioPlugin.getMetaTags()) {
            IOPluginManager.instance().registerMetaLoader(ioPlugin, ioPlugin.getIOTypeDescriptor(), tag);
        }
        // registers meta loader/saver used by Template Library
        // this will not impact loading/saving of FSA models not related to the
        // library
        TemplateMetaIO ioLibrary = new TemplateMetaIO();
        for (String tag : ioLibrary.getMetaTags()) {
            IOPluginManager.instance().registerMetaLoader(ioLibrary, "FSA", tag);
        }
        IOPluginManager.instance().registerMetaSaver(ioLibrary, FSAModel.class);
        // export
        IOPluginManager.instance().registerExport(new JPEGExporter(), TemplateModel.class);
        IOPluginManager.instance().registerExport(new PNGExporter(), TemplateModel.class);

        // Operations
        OperationManager.instance().register(new ChannelSup());
        OperationManager.instance().register(new ModularSupSolution());
        OperationManager.instance().register(new CentralizedSupSolution());
    }

    /**
     * Do nothing.
     */
    public void unload() {
    }

}

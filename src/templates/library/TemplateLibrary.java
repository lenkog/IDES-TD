/*
 * Copyright (c) 2010, Lenko Grigorov
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ides.api.core.Hub;
import ides.api.model.fsa.FSAModel;
import ides.api.notice.NoticeManager;
import ides.api.plugin.io.FileLoadException;
import ides.api.plugin.io.IOSubsytem;
import ides.api.plugin.model.DESModel;
import ides.api.utilities.GeneralUtils;

/**
 * Library of {@link Template}s.
 * 
 * @author Lenko Grigorov
 */
public class TemplateLibrary {
    /**
     * A map with the templates in the library. The key is the "ID" of the template.
     * 
     * @see Template#getName()
     */
    Map<String, Template> templates = new HashMap<String, Template>();

    /**
     * A map with the files where template models are saved.
     */
    Map<Template, File> files = new HashMap<Template, File>();

    /**
     * The directory which contains the files where the models of the templates in
     * the library are saved.
     */
    File dir;

    /**
     * Create a new template library using the files in the given directory.
     * Problems encountered when loading the templates are reported in the
     * {@link NoticeManager}.
     * 
     * @param dir the directory which contains the files where the models of the
     *            templates in the library are saved
     */
    public TemplateLibrary(File dir) {
        this.dir = dir;
        String errors = "";
        for (File file : dir.listFiles()) {
            FSAModel model = null;
            try {
                model = loadTemplateModel(file);
            } catch (FileLoadException e) {
                model = (FSAModel) e.getPartialModel();
                errors += e.getMessage();
            }
            if (model == null) {
                continue;
            }
            TemplateDescriptor td = (TemplateDescriptor) model.getAnnotation(Template.TEMPLATE_DESC);
            model.setName(td.tag);
            model.modelSaved();
            Template template = new FSATemplate(td, model);
            templates.put(template.getName(), template);
            files.put(template, file);
        }
        if (!"".equals(errors)) {
            Hub.getNoticeManager().postErrorTemporary(Hub.string("TD_problemLoadingTemplate"),
                    GeneralUtils.truncateMessage(errors));
        }
    }

    /**
     * Retrieve the set of templates in the library.
     * 
     * @return the set of templates in the library
     */
    public Collection<Template> getTemplates() {
        return new HashSet<Template>(templates.values());
    }

    /**
     * Retrieve the template with the given "ID".
     * 
     * @param name the "ID" of the template
     * @return the template with the given "ID" if the library contains it;
     *         <code>null</code> otherwise
     * @see Template#getName()
     */
    public Template getTemplate(String name) {
        return templates.get(name);
    }

    /**
     * Add a new template to the library, with the given properties and underlying
     * model. The template is saved into a file in the directory of templates.
     * 
     * @param td    the properties of the new template
     * @param model the underlying model for the new template
     * @throws IOException when there is an IO problem while saving the new template
     *                     into a file
     */
    public void addTemplate(TemplateDescriptor td, FSAModel model) throws IOException {
        model.setAnnotation(Template.TEMPLATE_DESC, td);
        StringBuffer fileName = new StringBuffer("");
        for (Character ch : td.tag.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                fileName.append(ch);
            }
        }
        Hub.getIOSubsystem();
        File file = new File(
                dir.getAbsolutePath() + File.separator + fileName.toString() + "." + IOSubsytem.MODEL_FILE_EXT);
        if (fileName.length() == 0 || file.exists()) {
            int idx = 0;
            do {
                Hub.getIOSubsystem();
                file = new File(dir.getAbsolutePath() + File.separator + fileName.toString() + idx + "."
                        + IOSubsytem.MODEL_FILE_EXT);
                idx++;
            } while (file.exists());
        }
        Hub.getIOSubsystem().save(model, file);
        Hub.getIOSubsystem().setFileOfModel(model, file);
        model.setName(td.tag);
        model.modelSaved();
        Template template = new FSATemplate(td, model);
        templates.put(template.getName(), template);
        files.put(template, file);
        fireCollectionChanged();
    }

    /**
     * Remove a template from the library and remove the file with the template from
     * the directory of templates.
     * 
     * @param name the "ID" of the template to be removed
     * @throws IOException when there is an IO problem while removing the file of
     *                     the template
     */
    public void removeTemplate(String name) throws IOException {
        Template template = getTemplate(name);
        if (template != null) {
            File file = files.get(template);
            if (file != null) {
                file.delete();
                files.remove(template);
            }
            templates.remove(name);
            fireCollectionChanged();
        }
    }

    /**
     * Reload the given template from the file where the template is saved.
     * 
     * @param name the "ID" of the template to be reloaded
     * @throws IOException when there is an IO problem while loading the template
     *                     from the file
     */
    public void reloadTemplate(String name) throws IOException {
        Template oldTemplate = getTemplate(name);
        if (oldTemplate == null) {
            return;
        }
        File file = files.get(oldTemplate);
        templates.remove(oldTemplate.getName());
        files.remove(oldTemplate);
        fireCollectionChanged();
        String errors = "";
        FSAModel model = null;
        try {
            model = loadTemplateModel(file);
        } catch (FileLoadException e) {
            if (e.getPartialModel() == null) {
                throw new IOException(e.getMessage());
            } else {
                model = (FSAModel) e.getPartialModel();
                errors = e.getMessage();
            }
        }
        Template newTemplate = new FSATemplate((TemplateDescriptor) model.getAnnotation(Template.TEMPLATE_DESC), model);
        templates.put(newTemplate.getName(), newTemplate);
        files.put(newTemplate, file);
        fireCollectionChanged();
        if (!"".equals(errors)) {
            throw new IOException(errors);
        }
    }

    /**
     * Load the model of a template from the given file.
     * 
     * @param file the file containing the template model
     * @return the underlying model of the template, annotated with the description
     *         of the template
     * @throws FileLoadException when the loading of the template model failed; the
     *                           exception may contain the partially-loaded model
     * @see TemplateDescriptor
     */
    protected FSAModel loadTemplateModel(File file) throws FileLoadException {
        String errors = "";
        DESModel model = null;
        try {
            model = Hub.getIOSubsystem().load(file);
        } catch (IOException e) {
            if (e instanceof FileLoadException) {
                model = ((FileLoadException) e).getPartialModel();
            }
            errors += Hub.string("TD_cantLoadTemplate") + " " + file.getAbsolutePath() + " [" + e.getMessage() + "]\n";
        }
        if (model == null) {
        } else if (!model.hasAnnotation(Template.TEMPLATE_DESC)) {
            errors += Hub.string("TD_cantLoadTemplate") + " " + file.getAbsolutePath() + " ["
                    + Hub.string("TD_missingTemplateInfo") + "]\n";
            model = null;
        } else if (!(model instanceof FSAModel)) {
            errors += Hub.string("TD_cantLoadTemplate") + " " + file.getAbsolutePath() + " ["
                    + Hub.string("TD_nonFSATemplate") + "]\n";
            model = null;
        }
        if (model == null || !"".equals(errors)) {
            throw new FileLoadException(errors, model);
        }
        return (FSAModel) model;
    }

    /**
     * Announce a change in the content of the template library to all subscribed
     * listeners.
     */
    protected void fireCollectionChanged() {
        for (TemplateLibraryListener listener : getTemplateLibraryListeners()) {
            listener.templateCollectionChanged(this);
        }
    }

    /**
     * The listeners subscribed to receive notifications from this template library.
     */
    private ArrayList<TemplateLibraryListener> listeners = new ArrayList<TemplateLibraryListener>();

    /**
     * Subscribe the given listener to receive notifications from this template
     * library.
     * 
     * @param listener the listener to be subscribed
     */
    public void addListener(TemplateLibraryListener listener) {
        listeners.add(listener);
    }

    /**
     * Cancel the subscription the given listener to receive notifications from this
     * template library.
     * 
     * @param listener the listener whose subscription is to be cancelled
     */
    public void removeListener(TemplateLibraryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns all current subscribers to this template library.
     * 
     * @return all current subscribers to this template library
     */
    public TemplateLibraryListener[] getTemplateLibraryListeners() {
        return listeners.toArray(new TemplateLibraryListener[] {});
    }

}

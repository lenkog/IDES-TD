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
 * Interface for publishers of notifications about changes in
 * {@link TemplateModel}s.
 * 
 * @author Lenko Grigorov
 */
public interface TemplateModelPublisher {
    /**
     * Attaches the given subscriber to this publisher. The given subscriber will
     * receive notifications of changes from this publisher.
     * 
     * @param subscriber the subscriber to be added
     */
    public void addSubscriber(TemplateModelSubscriber subscriber);

    /**
     * Removes the given subscriber from this publisher. The given subscriber will
     * no longer receive notifications of changes from this publisher.
     * 
     * @param subscriber the subscriber to be removed
     */
    public void removeSubscriber(TemplateModelSubscriber subscriber);

    /**
     * Returns all current subscribers to this publisher.
     * 
     * @return all current subscribers to this publisher
     */
    public TemplateModelSubscriber[] getTemplateModelSubscribers();

    /**
     * Triggers a notification to all subscribers that the structure of the
     * {@link TemplateModel} has changed.
     * 
     * @param message message with additional info about the change
     */
    public void fireTemplateModelStructureChanged(TemplateModelMessage message);
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;

import java.util.List;

/**
 * Interface for Weblog template operations.
 * Extracted from WeblogManager to address Insufficient Modularization.
 * 
 * <p>This interface provides CRUD and query operations for WeblogTemplate
 * and related template entities. Separating template operations from
 * core weblog operations reduces coupling and improves cohesion.</p>
 */
public interface WeblogTemplateOperations {

    /**
     * Store a custom weblog template.
     * 
     * @param data Template to save
     * @throws WebloggerException if error saving template
     */
    void saveTemplate(WeblogTemplate data) throws WebloggerException;

    /**
     * Remove a custom template.
     * 
     * @param template Template to remove
     * @throws WebloggerException if error removing template
     */
    void removeTemplate(WeblogTemplate template) throws WebloggerException;

    /**
     * Get a custom template by its id.
     * 
     * @param id Template id
     * @return WeblogTemplate or null if not found
     * @throws WebloggerException if error retrieving template
     */
    WeblogTemplate getTemplate(String id) throws WebloggerException;

    /**
     * Get a custom template by the action it supports.
     * 
     * @param w Weblog to get template for
     * @param a Component type/action
     * @return WeblogTemplate or null if not found
     * @throws WebloggerException if error retrieving template
     */
    WeblogTemplate getTemplateByAction(Weblog w, ComponentType a) throws WebloggerException;

    /**
     * Get a custom template by its name.
     * 
     * @param w    Weblog to get template for
     * @param name Template name
     * @return WeblogTemplate or null if not found
     * @throws WebloggerException if error retrieving template
     */
    WeblogTemplate getTemplateByName(Weblog w, String name) throws WebloggerException;

    /**
     * Get a custom template by its link.
     * 
     * @param w    Weblog to get template for
     * @param link Template link
     * @return WeblogTemplate or null if not found
     * @throws WebloggerException if error retrieving template
     */
    WeblogTemplate getTemplateByLink(Weblog w, String link) throws WebloggerException;

    /**
     * Save a custom template rendition.
     * 
     * @param templateCode Template rendition to save
     * @throws WebloggerException if error saving rendition
     */
    void saveTemplateRendition(CustomTemplateRendition templateCode) throws WebloggerException;

    /**
     * Get all custom templates for a weblog.
     * 
     * @param w Weblog to get templates for
     * @return List of WeblogTemplate objects
     * @throws WebloggerException if error retrieving templates
     */
    List<WeblogTemplate> getTemplates(Weblog w) throws WebloggerException;
}
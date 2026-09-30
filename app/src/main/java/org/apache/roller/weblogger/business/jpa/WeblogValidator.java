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

package org.apache.roller.weblogger.business.jpa;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Validates Weblog-related data.
 * Extracted from JPAWeblogManagerImpl to address Insufficient Modularization.
 * 
 * <p>Separating validation logic allows for:</p>
 * <ul>
 *   <li>Reuse across different managers</li>
 *   <li>Easy unit testing of validation rules</li>
 *   <li>Centralized validation logic</li>
 * </ul>
 */
public class WeblogValidator {

    /**
     * Validate a weblog handle.
     * 
     * @param handle The handle to validate
     * @throws WebloggerException if handle is invalid
     */
    public void validateHandle(String handle) throws WebloggerException {
        if (handle == null) {
            throw new WebloggerException("Handle cannot be null");
        }
        if (!isAlphanumeric(handle)) {
            throw new WebloggerException("Invalid handle: '" + handle + "'");
        }
    }

    /**
     * Validate that a weblog is not null.
     * 
     * @param weblog The weblog to validate
     * @param context Context for error message
     * @throws WebloggerException if weblog is null
     */
    public void validateWeblogNotNull(Weblog weblog, String context) throws WebloggerException {
        if (weblog == null) {
            throw new WebloggerException(context + ": weblog is null");
        }
    }

    /**
     * Validate that a template name is not null.
     * 
     * @param templateName The template name to validate
     * @throws WebloggerException if template name is null
     */
    public void validateTemplateNameNotNull(String templateName) throws WebloggerException {
        if (templateName == null) {
            throw new WebloggerException("Template name is null");
        }
    }

    /**
     * Validate that a template link is not null.
     * 
     * @param templateLink The template link to validate
     * @throws WebloggerException if template link is null
     */
    public void validateTemplateLinkNotNull(String templateLink) throws WebloggerException {
        if (templateLink == null) {
            throw new WebloggerException("templateLink is null");
        }
    }

    /**
     * Check if a string contains only alphanumeric characters or underscore.
     * 
     * @param str The string to check
     * @return true if alphanumeric or underscore only
     */
    public boolean isAlphanumeric(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }
}
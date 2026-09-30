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
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Interface for basic Weblog CRUD operations.
 * Extracted from WeblogManager to address Insufficient Modularization.
 * 
 * <p>This interface provides core create, read, update, delete operations
 * for Weblog entities.</p>
 */
public interface WeblogCrudOperations {

    /**
     * Add new website, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new website.
     * 
     * @param newWebsite New website to be created, must have creator.
     * @throws WebloggerException if error adding weblog
     */
    void addWeblog(Weblog newWebsite) throws WebloggerException;

    /**
     * Store a single weblog.
     * 
     * @param data Weblog to save
     * @throws WebloggerException if error saving weblog
     */
    void saveWeblog(Weblog data) throws WebloggerException;

    /**
     * Remove website object.
     * 
     * @param website Weblog to remove
     * @throws WebloggerException if error removing weblog
     */
    void removeWeblog(Weblog website) throws WebloggerException;

    /**
     * Get website object by id.
     * 
     * @param id Weblog id
     * @return Weblog or null if not found
     * @throws WebloggerException if error retrieving weblog
     */
    Weblog getWeblog(String id) throws WebloggerException;

    /**
     * Get website specified by handle (or null if enabled website not found).
     * 
     * @param handle Handle of website
     * @return Weblog or null if not found
     * @throws WebloggerException if error retrieving weblog
     */
    Weblog getWeblogByHandle(String handle) throws WebloggerException;

    /**
     * Get website specified by handle with option to return only enabled websites.
     * 
     * @param handle Handle of website
     * @param enabled If non-null, return only weblogs with this enabled state
     * @return Weblog or null if not found
     * @throws WebloggerException if error retrieving weblog
     */
    Weblog getWeblogByHandle(String handle, Boolean enabled) throws WebloggerException;
}
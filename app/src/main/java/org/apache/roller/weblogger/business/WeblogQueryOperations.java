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

import java.util.Date;
import java.util.List;

/**
 * Interface for Weblog query/search operations.
 * Extracted from WeblogManager to address Insufficient Modularization.
 * 
 * <p>This interface provides search and filter operations for Weblog entities.</p>
 */
public interface WeblogQueryOperations {

    /**
     * Get websites optionally restricted by enabled and active status.
     * 
     * @param enabled   Get all with this enabled state (or null for all)
     * @param active    Get all with this active state (or null for all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate   Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     * @return List of Weblog objects
     * @throws WebloggerException if error querying weblogs
     */
    List<Weblog> getWeblogs(
            Boolean enabled,
            Boolean active,
            Date startDate,
            Date endDate,
            int offset,
            int length) throws WebloggerException;

    /**
     * Get collection of weblogs whose handles begin with specified letter.
     * 
     * @param letter Starting letter
     * @param offset Offset into results (for paging)
     * @param length Maximum number of results to return (for paging)
     * @return List of Weblog objects
     * @throws WebloggerException if error querying weblogs
     */
    List<Weblog> getWeblogsByLetter(char letter, int offset, int length) throws WebloggerException;
}
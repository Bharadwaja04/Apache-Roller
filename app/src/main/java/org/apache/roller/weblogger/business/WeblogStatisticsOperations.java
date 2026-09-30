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
import org.apache.roller.weblogger.pojos.StatCount;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface for Weblog statistics operations.
 * Extracted from WeblogManager to address Insufficient Modularization.
 * 
 * <p>This interface provides statistics and aggregation operations
 * for Weblog entities.</p>
 */
public interface WeblogStatisticsOperations {

    /**
     * Get websites ordered by descending number of comments.
     * 
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate   Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     * @return List of StatCount objects
     * @throws WebloggerException if error querying statistics
     */
    List<StatCount> getMostCommentedWeblogs(
            Date startDate,
            Date endDate,
            int offset,
            int length) throws WebloggerException;

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     * 
     * @return Map of letter to count
     * @throws WebloggerException if error querying statistics
     */
    Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException;

    /**
     * Get count of active weblogs.
     * 
     * @return Count of weblogs
     * @throws WebloggerException if error counting weblogs
     */
    long getWeblogCount() throws WebloggerException;
}
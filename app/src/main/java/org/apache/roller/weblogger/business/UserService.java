/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Focused interface for user lifecycle and query operations.
 */
public interface UserService {

    // user CRUD
    void addUser(User newUser) throws WebloggerException;
    void saveUser(User user) throws WebloggerException;
    void removeUser(User user) throws WebloggerException;
    long getUserCount() throws WebloggerException;
    User getUserByActivationCode(String activationCode) throws WebloggerException;

    // user queries
    User getUser(String id) throws WebloggerException;
    User getUserByUserName(String userName) throws WebloggerException;
    User getUserByUserName(String userName, Boolean enabled) throws WebloggerException;
    User getUserByOpenIdUrl(String openIdUrl) throws WebloggerException;
    List<User> getUsers(Boolean enabled, Date startDate, Date endDate, int offset, int length)
            throws WebloggerException;
    List<User> getUsersStartingWith(String startsWith, Boolean enabled, int offset, int length)
            throws WebloggerException;
    Map<String, Long> getUserNameLetterMap() throws WebloggerException;
    List<User> getUsersByLetter(char letter, int offset, int length) throws WebloggerException;

    // Starring methods
    void toggleStarWeblog(User user, Weblog weblog) throws WebloggerException;
    void toggleStarEntry(User user, WeblogEntry entry) throws WebloggerException;
    List<Weblog> getStarredWeblogs(User user) throws WebloggerException;
    List<WeblogEntry> getStarredEntries(User user, int offset, int length) throws WebloggerException;
    long getStarredEntriesCount(User user) throws WebloggerException;
    boolean isStarredWeblog(User user, Weblog weblog) throws WebloggerException;
    boolean isStarredEntry(User user, WeblogEntry entry) throws WebloggerException;

    /**
     * Get total number of users who have liked/starred a given blog entry.
     * This count is public — not filtered by the current user.
     */
    long getLikeCount(WeblogEntry entry) throws WebloggerException;

    /**
     * Get total number of star/like events across the entire platform.
     */
    long getTotalStarCount() throws WebloggerException;

    /**
     * Get the ID of the most starred entry site-wide.
     * Returns null if no entries have been starred.
     */
    String getMostStarredEntryId() throws WebloggerException;

    /**
     * Get trending entries (top starred) up to the specified length.
     * Returns a list of Object array where Object[0] is the WeblogEntry and Object[1] is the like count (Long).
     */
    List<Object[]> getTrendingEntries(int length) throws WebloggerException;

    /**
     * Get the total number of likes/stars grouped by category name.
     */
    Map<String, Long> getCategoryLikeCounts() throws WebloggerException;
}

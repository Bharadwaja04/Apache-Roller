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
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

import java.util.List;

/**
 * Interface for Weblog-User relationship operations.
 * Extracted from WeblogManager to address Insufficient Modularization.
 * 
 * <p>This interface provides operations for managing relationships
 * between Weblog and User entities.</p>
 */
public interface WeblogUserOperations {

    /**
     * Get websites of a user.
     * 
     * @param user        Get all weblogs for this user
     * @param enabledOnly Include only enabled weblogs?
     * @return List of Weblog objects
     * @throws WebloggerException if error querying weblogs
     */
    List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException;

    /**
     * Get users of a weblog.
     * 
     * @param weblog      Weblog to retrieve users for
     * @param enabledOnly Include only enabled users?
     * @return List of User objects
     * @throws WebloggerException if error querying users
     */
    List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException;
}
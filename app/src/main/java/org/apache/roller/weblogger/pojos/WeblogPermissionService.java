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

package org.apache.roller.weblogger.pojos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Service class responsible for handling weblog permission checks.
 * Extracted from Weblog to reduce hub-like modularization and 
 * inappropriate intimacy with UserManager.
 */
public class WeblogPermissionService {

    private static final Log log = LogFactory.getLog(WeblogPermissionService.class);

    private final Weblog weblog;

    public WeblogPermissionService(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("Weblog cannot be null");
        }
        this.weblog = weblog;
    }

    /**
     * Returns true if user has the specified permission action.
     *
     * @param user   User to check permissions for
     * @param action Permission action to check
     * @return true if user has permission
     */
    public boolean hasUserPermission(User user, String action) {
        return hasUserPermissions(user, Collections.singletonList(action));
    }

    /**
     * Returns true if user has all specified permission actions in the weblog.
     *
     * @param user    User to check permissions for
     * @param actions List of permission actions to check
     * @return true if user has all permissions
     */
    public boolean hasUserPermissions(User user, List<String> actions) {
        if (user == null || actions == null || actions.isEmpty()) {
            return false;
        }

        try {
            UserManager umgr = getUserManager();
            WeblogPermission userPerms = new WeblogPermission(weblog, user, actions);
            return umgr.checkPermission(userPerms, user);
        } catch (WebloggerException ex) {
            log.error("ERROR checking user permission", ex);
        }
        return false;
    }

    /**
     * Get the creator of this weblog.
     *
     * @return User object of the creator, or null if not found
     */
    public User getCreator() {
        String creatorUserName = weblog.getCreatorUserName();
        if (creatorUserName == null) {
            return null;
        }

        try {
            return getUserManager().getUserByUserName(creatorUserName);
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: " + creatorUserName, e);
        }
        return null;
    }

    // -------------------- Helper Methods --------------------

    private UserManager getUserManager() {
        return WebloggerFactory.getWeblogger().getUserManager();
    }
}
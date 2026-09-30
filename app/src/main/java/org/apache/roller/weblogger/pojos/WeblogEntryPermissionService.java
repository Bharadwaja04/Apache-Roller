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

/**
 * Service class responsible for permission checking on weblog entries.
 * Extracted from WeblogEntry to address God Class and Hub-like Modularization.
 */
public class WeblogEntryPermissionService {

    private static final Log log = LogFactory.getLog(WeblogEntryPermissionService.class);

    private final WeblogEntry entry;

    public WeblogEntryPermissionService(WeblogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("WeblogEntry cannot be null");
        }
        this.entry = entry;
    }

    /**
     * Determine if the specified user has permissions to edit this entry.
     *
     * @param user User to check permissions for
     * @return true if user can edit this entry
     * @throws WebloggerException if error checking permissions
     */
    public boolean hasWritePermissions(User user) throws WebloggerException {
        if (user == null) {
            return false;
        }

        // Global admins can edit anything
        if (isGlobalAdmin(user)) {
            return true;
        }

        // Check weblog-level permissions
        return hasWeblogWritePermission(user);
    }

    /**
     * Get the creator of this entry.
     *
     * @return User object of the creator, or null if not found
     */
    public User getCreator() {
        String creatorUserName = entry.getCreatorUserName();
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

    // -------------------- Private Helper Methods --------------------

    private boolean isGlobalAdmin(User user) throws WebloggerException {
        GlobalPermission adminPerm = new GlobalPermission(
                Collections.singletonList(GlobalPermission.ADMIN));
        return getUserManager().checkPermission(adminPerm, user);
    }

    private boolean hasWeblogWritePermission(User user) {
        WeblogPermission perm;
        try {
            UserManager umgr = getUserManager();
            perm = umgr.getWeblogPermission(entry.getWebsite(), user);
        } catch (WebloggerException ex) {
            log.error("ERROR retrieving user's permission", ex);
            return false;
        }

        if (perm == null) {
            return false;
        }

        boolean isAuthor = perm.hasAction(WeblogPermission.POST) 
                || perm.hasAction(WeblogPermission.ADMIN);
        boolean isLimitedEditor = !isAuthor && perm.hasAction(WeblogPermission.EDIT_DRAFT);

        // Authors can edit any entry, limited editors only drafts/pending
        return isAuthor || (isLimitedEditor && isEditableDraft());
    }

    private boolean isEditableDraft() {
        WeblogEntry.PubStatus status = entry.getStatus();
        return status == WeblogEntry.PubStatus.DRAFT 
                || status == WeblogEntry.PubStatus.PENDING;
    }

    private UserManager getUserManager() {
        return WebloggerFactory.getWeblogger().getUserManager();
    }
}
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

package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Action that exposes the total like/star count for a blog entry and
 * whether the currently logged-in user has liked it.
 *
 * <p>Design pattern: Service Layer — delegates all business logic to
 * {@link org.apache.roller.weblogger.business.UserService}.
 *
 * <p>URL: {@code /roller-ui/likeCount?entryId={id}}
 */
public class LikeCountAction extends UIAction {

    private static final Log log = LogFactory.getLog(LikeCountAction.class);

    /** The ID of the blog entry to query. */
    private String entryId;

    /** Total number of users who have liked this entry. */
    private long likeCount = 0;

    /** Whether the currently logged-in user has liked this entry. */
    private boolean userLiked = false;

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {
        try {
            if (entryId != null) {
                WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                WeblogEntry entry = emgr.getWeblogEntry(entryId);
                if (entry != null) {
                    UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
                    likeCount = umgr.getLikeCount(entry);
                    userLiked = umgr.isStarredEntry(getAuthenticatedUser(), entry);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching like count for entryId=" + entryId, e);
            addError("generic.error.check.logs");
        }
        return SUCCESS;
    }

    // ---- getters and setters ----

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isUserLiked() {
        return userLiked;
    }
}

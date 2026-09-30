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
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Action for toggling "star" (favourite/like) status for blogs and entries.
 *
 * <p>Supports an optional {@code returnUrl} parameter. When provided the action
 * will redirect back to that URL after toggling so the user stays on the blog
 * page they came from. When absent the legacy SUCCESS (tiles) result is used.
 */
public class StarAction extends UIAction {
    
    private static Log log = LogFactory.getLog(StarAction.class);
    
    private String weblogId = null;
    private String entryId = null;

    /** Optional URL to redirect to after toggling. Set by the like button. */
    private String returnUrl = null;

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            if (weblogId != null) {
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                Weblog weblog = wmgr.getWeblog(weblogId);
                if (weblog != null) {
                    umgr.toggleStarWeblog(getAuthenticatedUser(), weblog);
                    WebloggerFactory.getWeblogger().flush();
                }
            } else if (entryId != null) {
                WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                WeblogEntry entry = emgr.getWeblogEntry(entryId);
                if (entry != null) {
                    umgr.toggleStarEntry(getAuthenticatedUser(), entry);
                    WebloggerFactory.getWeblogger().flush();
                }
            }
        } catch (Exception e) {
            log.error("Error toggling star status", e);
            addError("generic.error.check.logs");
        }
        // If a returnUrl was supplied, send user back there
        return (returnUrl != null && !returnUrl.isEmpty()) ? "redirect" : SUCCESS;
    }

    public String getWeblogId() {
        return weblogId;
    }

    public void setWeblogId(String weblogId) {
        this.weblogId = weblogId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}

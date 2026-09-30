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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Action for displaying a paginated list of starred entries,
 * including the total like count per entry.
 */
public class StarredEntriesAction extends UIAction {
    
    private static final Log log = LogFactory.getLog(StarredEntriesAction.class);
    
    private List<WeblogEntry> starredEntries = Collections.emptyList();
    private int page = 0;
    private int count = 0;
    private int pageSize = 10;

    /** Maps each entry's ID to its total like count (from all users). */
    private Map<String, Long> likeCounts = Collections.emptyMap();

    public StarredEntriesAction() {
        this.pageTitle = "starredEntries.title";
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            count = (int) umgr.getStarredEntriesCount(getAuthenticatedUser());
            starredEntries = umgr.getStarredEntries(getAuthenticatedUser(), page * pageSize, pageSize);

            // Build like count map for display
            Map<String, Long> counts = new HashMap<>();
            for (WeblogEntry entry : starredEntries) {
                counts.put(entry.getId(), umgr.getLikeCount(entry));
            }
            likeCounts = Collections.unmodifiableMap(counts);
        } catch (Exception e) {
            log.error("Error fetching starred entries", e);
            addError("generic.error.check.logs");
        }
        return SUCCESS;
    }

    public List<WeblogEntry> getStarredEntries() {
        return starredEntries;
    }

    public Map<String, Long> getLikeCounts() {
        return likeCounts;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    public int getPageSize() {
        return pageSize;
    }
}

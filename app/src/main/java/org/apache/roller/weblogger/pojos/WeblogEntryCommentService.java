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
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Service class responsible for comment-related operations on weblog entries.
 * Extracted from WeblogEntry to address God Class and Hub-like Modularization.
 */
public class WeblogEntryCommentService {

    private static final Log log = LogFactory.getLog(WeblogEntryCommentService.class);

    private final WeblogEntry entry;

    public WeblogEntryCommentService(WeblogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("WeblogEntry cannot be null");
        }
        this.entry = entry;
    }

    /**
     * Get all approved, non-spam comments for this entry.
     *
     * @return List of approved comments
     */
    public List<WeblogEntryComment> getComments() {
        return getComments(true, true);
    }

    /**
     * Get comments for this entry with filtering options.
     *
     * @param ignoreSpam   Whether to ignore spam comments
     * @param approvedOnly Whether to return only approved comments
     * @return List of comments
     */
    public List<WeblogEntryComment> getComments(boolean ignoreSpam, boolean approvedOnly) {
        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();

            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(entry.getWebsite());
            csc.setEntry(entry);
            csc.setStatus(approvedOnly ? WeblogEntryComment.ApprovalStatus.APPROVED : null);
            return wmgr.getComments(csc);
        } catch (WebloggerException e) {
            log.error("Error fetching comments for entry: " + entry.getId(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Get count of approved comments for this entry.
     *
     * @return Comment count
     */
    public int getCommentCount() {
        return getComments().size();
    }

    /**
     * Check if comments are still allowed on this entry considering the
     * allowComments and commentDays fields as well as the website and
     * site-wide configs.
     *
     * @return true if comments are allowed
     */
    public boolean getCommentsStillAllowed() {
        // Check site-wide setting
        if (!WebloggerRuntimeConfig.getBooleanProperty("users.comments.enabled")) {
            return false;
        }

        // Check weblog-level setting
        Weblog weblog = entry.getWebsite();
        if (weblog != null && weblog.getAllowComments() != null && !weblog.getAllowComments()) {
            return false;
        }

        // Check entry-level setting
        if (entry.getAllowComments() != null && !entry.getAllowComments()) {
            return false;
        }

        // Check comment days expiration
        return isWithinCommentPeriod();
    }

    // -------------------- Private Helper Methods --------------------

    private boolean isWithinCommentPeriod() {
        Integer commentDays = entry.getCommentDays();
        
        if (commentDays == null || commentDays == 0) {
            return true; // No expiration
        }

        Date pubTime = getEffectivePublishTime();
        if (pubTime == null) {
            return true;
        }

        Date expireDate = calculateExpireDate(pubTime, commentDays);
        return new Date().before(expireDate);
    }

    private Date getEffectivePublishTime() {
        // Use pubtime for calculating when comments expire, but
        // if pubtime isn't set (like for drafts) then use updatetime
        Date pubTime = entry.getPubTime();
        if (pubTime == null) {
            pubTime = entry.getUpdateTime();
        }
        return pubTime;
    }

    private Date calculateExpireDate(Date pubTime, int commentDays) {
        Weblog weblog = entry.getWebsite();
        Calendar expireCal = Calendar.getInstance(
                weblog != null ? weblog.getLocaleInstance() : java.util.Locale.getDefault());
        expireCal.setTime(pubTime);
        expireCal.add(Calendar.DATE, commentDays);
        return expireCal.getTime();
    }

    private WeblogEntryManager getWeblogEntryManager() {
        return WebloggerFactory.getWeblogger().getWeblogEntryManager();
    }
}
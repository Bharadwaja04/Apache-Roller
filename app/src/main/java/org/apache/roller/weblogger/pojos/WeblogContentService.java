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
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Service class responsible for retrieving weblog content such as entries,
 * comments, tags, and statistics. Extracted from Weblog to reduce hub-like
 * modularization and inappropriate intimacy with manager classes.
 */
public class WeblogContentService {

    private static final Log log = LogFactory.getLog(WeblogContentService.class);
    private static final int MAX_ENTRIES = 100;

    private final Weblog weblog;

    public WeblogContentService(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("Weblog cannot be null");
        }
        this.weblog = weblog;
    }

    /**
     * Get weblog entry specified by anchor or null if no such entry exists.
     *
     * @param anchor Weblog entry anchor
     * @return Weblog entry specified by anchor
     */
    public WeblogEntry getWeblogEntry(String anchor) {
        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            return wmgr.getWeblogEntryByAnchor(weblog, anchor);
        } catch (WebloggerException e) {
            log.error("ERROR: getting entry by anchor", e);
        }
        return null;
    }

    /**
     * Get weblog category by name.
     *
     * @param categoryName Category name or null for default
     * @return WeblogCategory or null if not found
     */
    public WeblogCategory getWeblogCategory(String categoryName) {
        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            if (categoryName != null && !categoryName.equals("nil")) {
                return wmgr.getWeblogCategoryByName(weblog, categoryName);
            } else {
                return weblog.getWeblogCategories().isEmpty() 
                    ? null 
                    : weblog.getWeblogCategories().iterator().next();
            }
        } catch (WebloggerException e) {
            log.error("ERROR: fetching category: " + categoryName, e);
        }
        return null;
    }

    /**
     * Get up to 100 most recent published entries in weblog.
     *
     * @param cat    Category name or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String cat, int length) {
        String category = normalizeNilValue(cat);
        int safeLength = clampLength(length);
        
        if (safeLength < 1) {
            return Collections.emptyList();
        }

        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setCatName(category);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setMaxResults(safeLength);
            return wmgr.getWeblogEntries(wesc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return Collections.emptyList();
    }

    /**
     * Get up to 100 most recent published entries in weblog by tag.
     *
     * @param tag    Blog entry tag to query by
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntriesByTag(String tag, int length) {
        String normalizedTag = normalizeNilValue(tag);
        int safeLength = clampLength(length);
        
        if (safeLength < 1) {
            return Collections.emptyList();
        }

        List<String> tags = (normalizedTag != null) 
            ? List.of(normalizedTag) 
            : Collections.emptyList();

        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setTags(tags);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setMaxResults(safeLength);
            return wmgr.getWeblogEntries(wesc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return Collections.emptyList();
    }

    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     *
     * @param length Max entries to return (1-100)
     * @return List of comment objects.
     */
    public List<WeblogEntryComment> getRecentComments(int length) {
        int safeLength = clampLength(length);
        
        if (safeLength < 1) {
            return Collections.emptyList();
        }

        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(weblog);
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
            csc.setReverseChrono(true);
            csc.setMaxResults(safeLength);
            return wmgr.getComments(csc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return Collections.emptyList();
    }

    /**
     * Get a list of TagStats objects for the most popular tags.
     *
     * @param sinceDays Number of days into past (or -1 for all days)
     * @param length    Max number of tags to return.
     * @return Collection of TagStat objects
     */
    public List<TagStat> getPopularTags(int sinceDays, int length) {
        Date startDate = calculateStartDate(sinceDays);

        try {
            WeblogEntryManager wmgr = getWeblogEntryManager();
            return wmgr.getPopularTags(weblog, startDate, 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching popular tags for weblog " + weblog.getName(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Get number of hits counted today.
     *
     * @return Today's hit count
     */
    public int getTodaysHits() {
        try {
            WeblogEntryManager mgr = getWeblogEntryManager();
            WeblogHitCount hitCount = mgr.getHitCountByWeblog(weblog);
            return (hitCount != null) ? hitCount.getDailyHits() : 0;
        } catch (WebloggerException e) {
            log.error("Error getting weblog hit count", e);
        }
        return 0;
    }

    /**
     * Get total comment count for this weblog.
     *
     * @return Comment count
     */
    public long getCommentCount() {
        try {
            WeblogEntryManager mgr = getWeblogEntryManager();
            return mgr.getCommentCount(weblog);
        } catch (WebloggerException e) {
            log.error("Error getting comment count for weblog " + weblog.getName(), e);
        }
        return 0;
    }

    /**
     * Get total entry count for this weblog.
     *
     * @return Entry count
     */
    public long getEntryCount() {
        try {
            WeblogEntryManager mgr = getWeblogEntryManager();
            return mgr.getEntryCount(weblog);
        } catch (WebloggerException e) {
            log.error("Error getting entry count for weblog " + weblog.getName(), e);
        }
        return 0;
    }

    // -------------------- Helper Methods --------------------

    private WeblogEntryManager getWeblogEntryManager() {
        return WebloggerFactory.getWeblogger().getWeblogEntryManager();
    }

    private String normalizeNilValue(String value) {
        return (value != null && "nil".equals(value)) ? null : value;
    }

    private int clampLength(int length) {
        return Math.min(length, MAX_ENTRIES);
    }

    private Date calculateStartDate(int sinceDays) {
        if (sinceDays > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);
            return cal.getTime();
        }
        return null;
    }
}
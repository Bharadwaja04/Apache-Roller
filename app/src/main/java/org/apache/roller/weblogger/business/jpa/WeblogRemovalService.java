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

package org.apache.roller.weblogger.business.jpa;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingQueueEntry;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;

import jakarta.persistence.NoResultException;
import java.util.List;

/**
 * Handles removal of weblog content.
 * Extracted from JPAWeblogManagerImpl to address Insufficient Modularization.
 * 
 * <p>This service handles the complex process of removing all weblog content:</p>
 * <ul>
 *   <li>Tags and tag aggregates</li>
 *   <li>Ping queue entries and auto-pings</li>
 *   <li>Templates</li>
 *   <li>Bookmarks and folders</li>
 *   <li>Media files</li>
 *   <li>Entries</li>
 *   <li>Categories</li>
 *   <li>Permissions</li>
 * </ul>
 */
public class WeblogRemovalService {

    private static final Log log = LogFactory.getLog(WeblogRemovalService.class);

    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;

    public WeblogRemovalService(Weblogger roller, JPAPersistenceStrategy strategy) {
        this.roller = roller;
        this.strategy = strategy;
    }

    /**
     * Remove all content from a weblog.
     * 
     * @param weblog The weblog to clean
     * @throws WebloggerException if removal fails
     */
    public void removeWeblogContents(Weblog weblog) throws WebloggerException {
        log.debug("Removing contents of weblog: " + weblog.getHandle());

        removeTags(weblog);
        removeTagAggregates(weblog);
        removePingQueueEntries(weblog);
        removeAutoPings(weblog);
        removeTemplates(weblog);
        removeBookmarksAndFolders(weblog);
        removeMediaFiles(weblog);
        removeEntries(weblog);
        removeCategories(weblog);
        removePermissions(weblog);
        removeStarredWeblogs(weblog);

        // flush to handle circular dependencies
        strategy.flush();

        log.debug("Weblog contents removed: " + weblog.getHandle());
    }

    /**
     * Remove all tags from weblog entries.
     */
    private void removeTags(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogEntryTag> tagQuery = strategy.getNamedQuery(
                "WeblogEntryTag.getByWeblog", WeblogEntryTag.class);
        tagQuery.setParameter(1, weblog);
        List<WeblogEntryTag> tags = tagQuery.getResultList();

        for (WeblogEntryTag tag : tags) {
            if (tag.getWeblogEntry() != null) {
                tag.getWeblogEntry().getTags().remove(tag);
            }
            strategy.remove(tag);
        }
        log.debug("Removed " + tags.size() + " tags");
    }

    /**
     * Remove tag aggregates for the weblog.
     */
    private void removeTagAggregates(Weblog weblog) throws WebloggerException {
        WeblogEntryManager emgr = roller.getWeblogEntryManager();

        // Update site-wide tag aggregates
        List<TagStat> tags = emgr.getTags(weblog, null, null, 0, -1);
        updateSiteTagAggregates(tags);

        // Delete weblog-specific aggregates
        Query removeAggs = strategy.getNamedUpdate("WeblogEntryTagAggregate.removeByWeblog");
        removeAggs.setParameter(1, weblog);
        int removed = removeAggs.executeUpdate();
        log.debug("Removed " + removed + " weblog tag aggregates");

        // Clean up zero-count aggregates
        Query removeCounts = strategy.getNamedUpdate("WeblogEntryTagAggregate.removeByTotalLessEqual");
        removeCounts.setParameter(1, 0);
        removeCounts.executeUpdate();
    }

    /**
     * Update site-wide tag aggregates when removing weblog tags.
     */
    private void updateSiteTagAggregates(List<TagStat> tags) throws WebloggerException {
        for (TagStat stat : tags) {
            TypedQuery<WeblogEntryTagAggregate> query = strategy.getNamedQueryCommitFirst(
                    "WeblogEntryTagAggregate.getByName&WebsiteNullOrderByLastUsedDesc",
                    WeblogEntryTagAggregate.class);
            query.setParameter(1, stat.getName());
            try {
                WeblogEntryTagAggregate agg = query.getSingleResult();
                agg.setTotal(agg.getTotal() - stat.getCount());
            } catch (NoResultException ignored) {
                // nothing to update
            }
        }
    }

    /**
     * Remove ping queue entries.
     */
    private void removePingQueueEntries(Weblog weblog) throws WebloggerException {
        TypedQuery<PingQueueEntry> query = strategy.getNamedQuery(
                "PingQueueEntry.getByWebsite", PingQueueEntry.class);
        query.setParameter(1, weblog);
        List<PingQueueEntry> entries = query.getResultList();

        for (PingQueueEntry entry : entries) {
            strategy.remove(entry);
        }
        log.debug("Removed " + entries.size() + " ping queue entries");
    }

    /**
     * Remove auto-ping configurations.
     */
    private void removeAutoPings(Weblog weblog) throws WebloggerException {
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        List<AutoPing> autopings = autoPingMgr.getAutoPingsByWebsite(weblog);

        for (AutoPing autoPing : autopings) {
            strategy.remove(autoPing);
        }
        log.debug("Removed " + autopings.size() + " auto-pings");
    }

    /**
     * Remove all templates.
     */
    private void removeTemplates(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblog", WeblogTemplate.class);
        query.setParameter(1, weblog);
        List<WeblogTemplate> templates = query.getResultList();

        for (WeblogTemplate template : templates) {
            strategy.remove(template);
        }
        log.debug("Removed " + templates.size() + " templates");
    }

    /**
     * Remove bookmarks and folders.
     */
    private void removeBookmarksAndFolders(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogBookmarkFolder> query = strategy.getNamedQuery(
                "WeblogBookmarkFolder.getByWebsite", WeblogBookmarkFolder.class);
        query.setParameter(1, weblog);
        List<WeblogBookmarkFolder> folders = query.getResultList();

        for (WeblogBookmarkFolder folder : folders) {
            strategy.remove(folder);
        }
        log.debug("Removed " + folders.size() + " bookmark folders");
    }

    /**
     * Remove media files.
     */
    private void removeMediaFiles(Weblog weblog) throws WebloggerException {
        MediaFileManager mfmgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        mfmgr.removeAllFiles(weblog);
        strategy.flush();
        log.debug("Removed media files");
    }

    /**
     * Remove all entries.
     */
    private void removeEntries(Weblog weblog) throws WebloggerException {
        WeblogEntryManager emgr = roller.getWeblogEntryManager();
        TypedQuery<WeblogEntry> query = strategy.getNamedQuery(
                "WeblogEntry.getByWebsite", WeblogEntry.class);
        query.setParameter(1, weblog);
        List<WeblogEntry> entries = query.getResultList();

        for (WeblogEntry entry : entries) {
            emgr.removeWeblogEntry(entry);
        }
        strategy.flush();
        log.debug("Removed " + entries.size() + " entries");
    }

    /**
     * Remove all categories.
     */
    private void removeCategories(Weblog weblog) throws WebloggerException {
        Query query = strategy.getNamedUpdate("WeblogCategory.removeByWeblog");
        query.setParameter(1, weblog);
        int removed = query.executeUpdate();
        log.debug("Removed " + removed + " categories");
    }

    /**
     * Remove all permissions.
     */
    private void removePermissions(Weblog weblog) throws WebloggerException {
        UserManager umgr = roller.getUserManager();
        List<WeblogPermission> perms = umgr.getWeblogPermissions(weblog);

        for (WeblogPermission perm : perms) {
            umgr.revokeWeblogPermission(perm.getWeblog(), perm.getUser(), WeblogPermission.ALL_ACTIONS);
        }
        log.debug("Removed " + perms.size() + " permissions");
    }

    /**
     * Remove all stars for the weblog.
     */
    private void removeStarredWeblogs(Weblog weblog) throws WebloggerException {
        strategy.getNamedUpdate("StarredWeblog.removeByWeblog").setParameter(1, weblog).executeUpdate();
        log.debug("Removed starred weblog records");
    }
}
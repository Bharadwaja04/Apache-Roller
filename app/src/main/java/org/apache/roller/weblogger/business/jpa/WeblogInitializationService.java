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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles initialization of new weblogs.
 * Extracted from JPAWeblogManagerImpl to address Insufficient Modularization.
 * 
 * <p>This service handles all the setup required for a new weblog:</p>
 * <ul>
 *   <li>Granting creator permissions</li>
 *   <li>Creating default categories</li>
 *   <li>Creating default bookmarks</li>
 *   <li>Setting up media directories</li>
 *   <li>Configuring auto-pings</li>
 * </ul>
 */
public class WeblogInitializationService {

    private static final Log log = LogFactory.getLog(WeblogInitializationService.class);

    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;

    public WeblogInitializationService(Weblogger roller, JPAPersistenceStrategy strategy) {
        this.roller = roller;
        this.strategy = strategy;
    }

    /**
     * Initialize a new weblog with default content.
     * 
     * @param newWeblog The weblog to initialize
     * @throws WebloggerException if initialization fails
     */
    public void initializeWeblog(Weblog newWeblog) throws WebloggerException {
        log.debug("Initializing new weblog: " + newWeblog.getHandle());

        grantCreatorPermissions(newWeblog);
        WeblogCategory firstCat = createDefaultCategories(newWeblog);
        setDefaultBloggerCategory(newWeblog, firstCat);
        createDefaultBookmarks(newWeblog);
        createDefaultMediaDirectory(newWeblog);

        // flush before setting up pings
        strategy.flush();

        setupAutoPings(newWeblog);

        log.debug("Weblog initialization complete: " + newWeblog.getHandle());
    }

    /**
     * Grant ADMIN permission to weblog creator.
     */
    private void grantCreatorPermissions(Weblog weblog) throws WebloggerException {
        List<String> actions = new ArrayList<>();
        actions.add(WeblogPermission.ADMIN);
        roller.getUserManager().grantWeblogPermission(
                weblog, weblog.getCreator(), actions);
        log.debug("Granted ADMIN permission to creator: " + weblog.getCreator().getUserName());
    }

    /**
     * Create default categories from configuration.
     * 
     * @return The first category created, or null if none
     */
    private WeblogCategory createDefaultCategories(Weblog weblog) throws WebloggerException {
        String cats = WebloggerConfig.getProperty("newuser.categories");
        WeblogCategory firstCat = null;

        if (cats != null) {
            String[] splitcats = cats.split(",");
            for (String split : splitcats) {
                if (split.isBlank()) {
                    continue;
                }
                WeblogCategory category = new WeblogCategory(
                        weblog,
                        split.trim(),
                        null,
                        null);
                if (firstCat == null) {
                    firstCat = category;
                }
                strategy.store(category);
                log.debug("Created category: " + split.trim());
            }
        }

        return firstCat;
    }

    /**
     * Set the default blogger category.
     */
    private void setDefaultBloggerCategory(Weblog weblog, WeblogCategory firstCat) 
            throws WebloggerException {
        if (firstCat != null) {
            weblog.setBloggerCategory(firstCat);
            strategy.store(weblog);
        }
    }

    /**
     * Create default bookmarks from configuration.
     */
    private void createDefaultBookmarks(Weblog weblog) throws WebloggerException {
        WeblogBookmarkFolder defaultFolder = new WeblogBookmarkFolder("default", weblog);
        strategy.store(defaultFolder);

        String blogroll = WebloggerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (String splitItem : splitroll) {
                String[] rollitems = splitItem.split("\\|");
                if (rollitems.length > 1) {
                    WeblogBookmark bookmark = new WeblogBookmark(
                            defaultFolder,
                            rollitems[0].trim(),
                            "",
                            rollitems[1].trim(),
                            null,
                            null);
                    strategy.store(bookmark);
                    log.debug("Created bookmark: " + rollitems[0].trim());
                }
            }
        }
    }

    /**
     * Create default media directory.
     */
    private void createDefaultMediaDirectory(Weblog weblog) throws WebloggerException {
        roller.getMediaFileManager().createDefaultMediaFileDirectory(weblog);
    }

    /**
     * Setup auto-pings for enabled ping targets.
     */
    private void setupAutoPings(Weblog weblog) throws WebloggerException {
        PingTargetManager pingTargetMgr = roller.getPingTargetManager();
        AutoPingManager autoPingMgr = roller.getAutopingManager();

        for (PingTarget pingTarget : pingTargetMgr.getCommonPingTargets()) {
            if (pingTarget.isAutoEnabled()) {
                AutoPing autoPing = new AutoPing(null, pingTarget, weblog);
                autoPingMgr.saveAutoPing(autoPing);
                log.debug("Created auto-ping for target: " + pingTarget.getName());
            }
        }
    }
}
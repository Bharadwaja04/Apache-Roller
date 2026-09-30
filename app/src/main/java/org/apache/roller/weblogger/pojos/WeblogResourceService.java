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
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.themes.ThemeManager;

import java.util.Map;

/**
 * Service class responsible for retrieving weblog resources such as
 * bookmarks, themes, and plugins. Extracted from Weblog to reduce 
 * hub-like modularization and inappropriate intimacy.
 */
public class WeblogResourceService {

    private static final Log log = LogFactory.getLog(WeblogResourceService.class);

    private final Weblog weblog;
    private Map<String, WeblogEntryPlugin> initializedPlugins = null;

    public WeblogResourceService(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("Weblog cannot be null");
        }
        this.weblog = weblog;
    }

    /**
     * Get the Theme object in use by this weblog, or null if no theme selected.
     *
     * @return WeblogTheme or null
     */
    public WeblogTheme getTheme() {
        try {
            ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
            return themeMgr.getTheme(weblog);
        } catch (WebloggerException ex) {
            log.error("Error getting theme for weblog - " + weblog.getHandle(), ex);
        }
        return null;
    }

    /**
     * Get bookmark folder by name.
     *
     * @param folderName Name or path of bookmark folder to be returned (null for root)
     * @return Folder object requested.
     */
    public WeblogBookmarkFolder getBookmarkFolder(String folderName) {
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            if (isDefaultFolderRequest(folderName)) {
                return bmgr.getDefaultFolder(weblog);
            } else {
                return bmgr.getFolder(weblog, folderName);
            }
        } catch (WebloggerException re) {
            log.error("ERROR: fetching folder for weblog", re);
        }
        return null;
    }

    /**
     * Get initialized plugins for use during rendering process.
     *
     * @return Map of plugin name to WeblogEntryPlugin
     */
    public Map<String, WeblogEntryPlugin> getInitializedPlugins() {
        if (initializedPlugins == null) {
            try {
                PluginManager ppmgr = WebloggerFactory.getWeblogger().getPluginManager();
                initializedPlugins = ppmgr.getWeblogEntryPlugins(weblog);
            } catch (Exception e) {
                log.error("ERROR: initializing plugins");
            }
        }
        return initializedPlugins;
    }

    /**
     * Get the URL for this weblog.
     *
     * @return Relative URL
     */
    public String getURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(weblog, null, false);
    }

    /**
     * Get the absolute URL for this weblog.
     *
     * @return Absolute URL
     */
    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(weblog, null, true);
    }

    // -------------------- Helper Methods --------------------

    private boolean isDefaultFolderRequest(String folderName) {
        return folderName == null 
            || folderName.equals("nil") 
            || folderName.trim().equals("/");
    }
}
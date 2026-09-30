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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business;

import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PlanetURLStrategy;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;


/**
 * The main entry point interface of the Weblogger business tier.
 * 
 * This interface now extends focused sub-interfaces to provide better
 * separation of concerns and reduce cyclic dependencies.
 * 
 * <p>Design Pattern: Interface Segregation Principle (ISP)</p>
 * 
 * <p>Clients that only need specific functionality can depend on the
 * appropriate sub-interface instead of the entire Weblogger interface:</p>
 * <ul>
 *   <li>{@link WebloggerCore} - Lifecycle operations</li>
 *   <li>{@link ContentServices} - Content management</li>
 *   <li>{@link UserServices} - User management</li>
 *   <li>{@link PingServices} - Ping operations</li>
 *   <li>{@link SystemServices} - System services</li>
 *   <li>{@link ThemeAndPluginServices} - Themes and plugins</li>
 *   <li>{@link PlanetServices} - Planet/feed aggregation</li>
 * </ul>
 * 
 * @see WebloggerCore
 * @see ContentServices
 * @see UserServices
 * @see PingServices
 * @see SystemServices
 * @see ThemeAndPluginServices
 * @see PlanetServices
 */
public interface Weblogger extends 
        WebloggerCore, 
        ContentServices, 
        UserServices, 
        PingServices, 
        SystemServices, 
        ThemeAndPluginServices, 
        PlanetServices {

    // ========================================================================
    // All methods are now inherited from sub-interfaces.
    // This interface serves as the composite for backward compatibility.
    // ========================================================================

    // -------------------- Lifecycle Methods (from WebloggerCore) --------------------
    // void initialize() throws InitializationException;
    // void shutdown();
    // void flush() throws WebloggerException;
    // void release();
    // String getVersion();
    // String getRevision();
    // String getBuildTime();
    // String getBuildUser();

    // -------------------- Content Services (from ContentServices) --------------------
    // WeblogManager getWeblogManager();
    // WeblogEntryManager getWeblogEntryManager();
    // MediaFileManager getMediaFileManager();
    // FileContentManager getFileContentManager();

    // -------------------- User Services (from UserServices) --------------------
    // UserManager getUserManager();
    // BookmarkManager getBookmarkManager();
    // OAuthManager getOAuthManager();

    // -------------------- Ping Services (from PingServices) --------------------
    // AutoPingManager getAutopingManager();
    // PingTargetManager getPingTargetManager();
    // PingQueueManager getPingQueueManager();

    // -------------------- System Services (from SystemServices) --------------------
    // PropertiesManager getPropertiesManager();
    // ThreadManager getThreadManager();
    // IndexManager getIndexManager();
    // URLStrategy getUrlStrategy();

    // -------------------- Theme & Plugin Services (from ThemeAndPluginServices) --------------------
    // ThemeManager getThemeManager();
    // PluginManager getPluginManager();

    // -------------------- Planet Services (from PlanetServices) --------------------
    // PlanetManager getPlanetManager();
    // FeedFetcher getFeedFetcher();
    // PlanetURLStrategy getPlanetURLStrategy();

	org.apache.roller.planet.business.PlanetURLStrategy getPlanetURLStrategy();

    /**
     * Get BugReportManager associated with this Weblogger instance.
     */
    BugReportManager getBugReportManager();
}

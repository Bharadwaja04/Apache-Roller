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

package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Improved service locator that provides type-safe access to services.
 * 
 * This class wraps WebloggerFactory to provide better service access patterns
 * while maintaining backward compatibility. It's a stepping stone toward
 * full dependency injection.
 * 
 * <p>Benefits over direct WebloggerFactory usage:</p>
 * <ul>
 *   <li>Type-safe service access</li>
 *   <li>Cleaner API with specific service accessors</li>
 *   <li>Easier to mock in tests via ServiceRegistry</li>
 *   <li>Service group facades for reduced coupling</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Instead of:
 * WebloggerFactory.getWeblogger().getUserManager().getUser(id);
 * 
 * // Use:
 * ServiceLocator.getUserManager().getUser(id);
 * 
 * // Or for service groups:
 * ServiceLocator.getContentServices().getWeblogEntryManager().getEntry(id);
 * </pre>
 */
public final class ServiceLocator {

    private static final Log LOG = LogFactory.getLog(ServiceLocator.class);

    // For testing - allows injection of mock registry
    private static ServiceRegistry testRegistry = null;

    // Private constructor - utility class
    private ServiceLocator() {
    }

    // -------------------- Test Support --------------------

    /**
     * Set a custom ServiceRegistry for testing purposes.
     * 
     * @param registry The mock registry, or null to use default
     */
    public static void setTestRegistry(ServiceRegistry registry) {
        testRegistry = registry;
        if (registry != null) {
            LOG.debug("Test registry set");
        } else {
            LOG.debug("Test registry cleared");
        }
    }

    /**
     * Get the current ServiceRegistry.
     * 
     * @return ServiceRegistry instance
     */
    public static ServiceRegistry getRegistry() {
        if (testRegistry != null) {
            return testRegistry;
        }
        return new ServiceRegistry(WebloggerFactory.getWeblogger());
    }

    // -------------------- Direct Manager Access --------------------

    /**
     * Get UserManager.
     * 
     * @return UserManager instance
     */
    public static UserManager getUserManager() {
        if (testRegistry != null) {
            return testRegistry.getService(UserManager.class);
        }
        return WebloggerFactory.getWeblogger().getUserManager();
    }

    /**
     * Get WeblogManager.
     * 
     * @return WeblogManager instance
     */
    public static WeblogManager getWeblogManager() {
        if (testRegistry != null) {
            return testRegistry.getService(WeblogManager.class);
        }
        return WebloggerFactory.getWeblogger().getWeblogManager();
    }

    /**
     * Get WeblogEntryManager.
     * 
     * @return WeblogEntryManager instance
     */
    public static WeblogEntryManager getWeblogEntryManager() {
        if (testRegistry != null) {
            return testRegistry.getService(WeblogEntryManager.class);
        }
        return WebloggerFactory.getWeblogger().getWeblogEntryManager();
    }

    /**
     * Get BookmarkManager.
     * 
     * @return BookmarkManager instance
     */
    public static BookmarkManager getBookmarkManager() {
        if (testRegistry != null) {
            return testRegistry.getService(BookmarkManager.class);
        }
        return WebloggerFactory.getWeblogger().getBookmarkManager();
    }

    /**
     * Get MediaFileManager.
     * 
     * @return MediaFileManager instance
     */
    public static MediaFileManager getMediaFileManager() {
        if (testRegistry != null) {
            return testRegistry.getService(MediaFileManager.class);
        }
        return WebloggerFactory.getWeblogger().getMediaFileManager();
    }

    /**
     * Get PropertiesManager.
     * 
     * @return PropertiesManager instance
     */
    public static PropertiesManager getPropertiesManager() {
        if (testRegistry != null) {
            return testRegistry.getService(PropertiesManager.class);
        }
        return WebloggerFactory.getWeblogger().getPropertiesManager();
    }

    /**
     * Get URLStrategy.
     * 
     * @return URLStrategy instance
     */
    public static URLStrategy getUrlStrategy() {
        if (testRegistry != null) {
            return testRegistry.getService(URLStrategy.class);
        }
        return WebloggerFactory.getWeblogger().getUrlStrategy();
    }

    // -------------------- Service Group Access --------------------

    /**
     * Get content-related services.
     * 
     * @return ContentServices facade
     */
    public static ContentServices getContentServices() {
        if (testRegistry != null) {
            return testRegistry.getContentServices();
        }
        return WebloggerFactory.getWeblogger();
    }

    /**
     * Get user-related services.
     * 
     * @return UserServices facade
     */
    public static UserServices getUserServices() {
        if (testRegistry != null) {
            return testRegistry.getUserServices();
        }
        return WebloggerFactory.getWeblogger();
    }

    /**
     * Get ping-related services.
     * 
     * @return PingServices facade
     */
    public static PingServices getPingServices() {
        if (testRegistry != null) {
            return testRegistry.getPingServices();
        }
        return WebloggerFactory.getWeblogger();
    }

    /**
     * Get system-related services.
     * 
     * @return SystemServices facade
     */
    public static SystemServices getSystemServices() {
        if (testRegistry != null) {
            return testRegistry.getSystemServices();
        }
        return WebloggerFactory.getWeblogger();
    }

    /**
     * Get theme and plugin services.
     * 
     * @return ThemeAndPluginServices facade
     */
    public static ThemeAndPluginServices getThemeAndPluginServices() {
        if (testRegistry != null) {
            return testRegistry.getThemeAndPluginServices();
        }
        return WebloggerFactory.getWeblogger();
    }

    /**
     * Get planet-related services.
     * 
     * @return PlanetServices facade
     */
    public static PlanetServices getPlanetServices() {
        if (testRegistry != null) {
            return testRegistry.getPlanetServices();
        }
        return WebloggerFactory.getWeblogger();
    }
}
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
import org.apache.roller.weblogger.pojos.Weblog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages caching of Weblog handle-to-ID mappings.
 * Extracted from JPAWeblogManagerImpl to address Insufficient Modularization.
 * 
 * <p>Separating cache management allows for:</p>
 * <ul>
 *   <li>Easy replacement with different cache implementations</li>
 *   <li>Centralized cache statistics and monitoring</li>
 *   <li>Testable cache behavior</li>
 * </ul>
 */
public class WeblogCacheManager {

    private static final Log log = LogFactory.getLog(WeblogCacheManager.class);

    private final Map<String, String> weblogHandleToIdMap;

    public WeblogCacheManager() {
        this.weblogHandleToIdMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Get cached weblog ID for a handle.
     * 
     * @param handle The weblog handle
     * @return The weblog ID, or null if not cached
     */
    public String getCachedId(String handle) {
        return weblogHandleToIdMap.get(handle);
    }

    /**
     * Cache a weblog handle-to-ID mapping.
     * 
     * @param weblog The weblog to cache
     */
    public void cacheWeblog(Weblog weblog) {
        if (weblog != null && weblog.getHandle() != null && weblog.getId() != null) {
            weblogHandleToIdMap.put(weblog.getHandle(), weblog.getId());
            log.debug("Cached weblog handle: " + weblog.getHandle());
        }
    }

    /**
     * Remove a weblog from the cache.
     * 
     * @param handle The weblog handle to remove
     */
    public void removeFromCache(String handle) {
        if (handle != null) {
            weblogHandleToIdMap.remove(handle);
            log.debug("Removed weblog from cache: " + handle);
        }
    }

    /**
     * Check if a handle is cached.
     * 
     * @param handle The weblog handle
     * @return true if cached
     */
    public boolean isCached(String handle) {
        return weblogHandleToIdMap.containsKey(handle);
    }

    /**
     * Clear the entire cache.
     */
    public void clearCache() {
        weblogHandleToIdMap.clear();
        log.debug("Weblog cache cleared");
    }

    /**
     * Get cache size.
     * 
     * @return Number of cached entries
     */
    public int getCacheSize() {
        return weblogHandleToIdMap.size();
    }
}
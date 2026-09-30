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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;

import java.util.Collections;
import java.util.List;

/**
 * Service for managing category-entry relationships.
 * Extracted from WeblogCategory to address Cyclic-Dependent Modularization.
 * 
 * <p>This service moves entry retrieval logic out of the POJO,
 * eliminating hidden dependencies on WebloggerFactory.</p>
 */
public class CategoryEntryService {

    /**
     * Retrieve all weblog entries in a category.
     * 
     * @param category The category
     * @param publishedOnly True if desired to return only published entries
     * @return List of WeblogEntry objects
     * @throws WebloggerException if retrieval fails
     */
    public List<WeblogEntry> getEntries(WeblogCategory category, boolean publishedOnly) 
            throws WebloggerException {
        
        if (category == null || category.getWeblog() == null) {
            return Collections.emptyList();
        }
        
        WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(category.getWeblog());
        wesc.setCatName(category.getName());
        
        if (publishedOnly) {
            wesc.setStatus(PubStatus.PUBLISHED);
        }
        
        return wmgr.getWeblogEntries(wesc);
    }

    /**
     * Retrieve all published entries in a category.
     * 
     * @param category The category
     * @return List of published WeblogEntry objects
     * @throws WebloggerException if retrieval fails
     */
    public List<WeblogEntry> getPublishedEntries(WeblogCategory category) 
            throws WebloggerException {
        return getEntries(category, true);
    }

    /**
     * Retrieve all entries in a category (regardless of status).
     * 
     * @param category The category
     * @return List of all WeblogEntry objects
     * @throws WebloggerException if retrieval fails
     */
    public List<WeblogEntry> getAllEntries(WeblogCategory category) 
            throws WebloggerException {
        return getEntries(category, false);
    }

    /**
     * Check if a category is in use (has any entries).
     * 
     * @param category The category
     * @return true if category has entries
     */
    public boolean isInUse(WeblogCategory category) {
        try {
            return WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager()
                    .isWeblogCategoryInUse(category);
        } catch (WebloggerException e) {
            throw new RuntimeException("Error checking category usage", e);
        }
    }

    /**
     * Get entry count for a category.
     * 
     * @param category The category
     * @param publishedOnly Count only published entries
     * @return Number of entries
     * @throws WebloggerException if count fails
     */
    public int getEntryCount(WeblogCategory category, boolean publishedOnly) 
            throws WebloggerException {
        List<WeblogEntry> entries = getEntries(category, publishedOnly);
        return entries.size();
    }
}
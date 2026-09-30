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

/**
 * Service for managing category-weblog relationships.
 * Extracted from WeblogCategory to address Cyclic-Dependent Modularization.
 * 
 * <p>This service provides ID-based navigation to break direct
 * bidirectional dependencies between WeblogCategory and Weblog.</p>
 * 
 * <p>Instead of:</p>
 * <pre>
 *   category.getWeblog().getHandle()
 * </pre>
 * 
 * <p>Use:</p>
 * <pre>
 *   referenceService.getWeblogHandle(category)
 * </pre>
 */
public class CategoryReferenceService {

    /**
     * Get the weblog ID for a category.
     * Uses ID-based reference to avoid triggering lazy loading.
     * 
     * @param category The category
     * @return Weblog ID or null
     */
    public String getWeblogId(WeblogCategory category) {
        Weblog weblog = category.getWeblog();
        return weblog != null ? weblog.getId() : null;
    }

    /**
     * Get the weblog handle for a category.
     * 
     * @param category The category
     * @return Weblog handle or null
     */
    public String getWeblogHandle(WeblogCategory category) {
        Weblog weblog = category.getWeblog();
        return weblog != null ? weblog.getHandle() : null;
    }

    /**
     * Get the weblog name for a category.
     * 
     * @param category The category
     * @return Weblog name or null
     */
    public String getWeblogName(WeblogCategory category) {
        Weblog weblog = category.getWeblog();
        return weblog != null ? weblog.getName() : null;
    }

    /**
     * Check if category belongs to a specific weblog.
     * 
     * @param category The category
     * @param weblogId The weblog ID to check
     * @return true if category belongs to the weblog
     */
    public boolean belongsToWeblog(WeblogCategory category, String weblogId) {
        String categoryWeblogId = getWeblogId(category);
        return weblogId != null && weblogId.equals(categoryWeblogId);
    }

    /**
     * Check if two categories belong to the same weblog.
     * 
     * @param category1 First category
     * @param category2 Second category
     * @return true if same weblog
     */
    public boolean sameWeblog(WeblogCategory category1, WeblogCategory category2) {
        String weblogId1 = getWeblogId(category1);
        String weblogId2 = getWeblogId(category2);
        return weblogId1 != null && weblogId1.equals(weblogId2);
    }

    /**
     * Compare categories for equality using ID-based comparison.
     * Avoids lazy loading issues from direct weblog comparison.
     * 
     * @param category1 First category
     * @param category2 Second category
     * @return true if equal (same name and weblog ID)
     */
    public boolean areEqual(WeblogCategory category1, WeblogCategory category2) {
        if (category1 == category2) {
            return true;
        }
        if (category1 == null || category2 == null) {
            return false;
        }
        
        String name1 = category1.getName();
        String name2 = category2.getName();
        String weblogId1 = getWeblogId(category1);
        String weblogId2 = getWeblogId(category2);
        
        boolean namesEqual = (name1 == null && name2 == null) || 
                             (name1 != null && name1.equals(name2));
        boolean weblogsEqual = (weblogId1 == null && weblogId2 == null) || 
                               (weblogId1 != null && weblogId1.equals(weblogId2));
        
        return namesEqual && weblogsEqual;
    }

    /**
     * Generate hashCode using ID-based comparison.
     * Avoids lazy loading issues from direct weblog comparison.
     * 
     * @param category The category
     * @return Hash code
     */
    public int generateHashCode(WeblogCategory category) {
        int result = 17;
        result = 31 * result + (category.getName() != null ? category.getName().hashCode() : 0);
        result = 31 * result + (getWeblogId(category) != null ? getWeblogId(category).hashCode() : 0);
        return result;
    }
}
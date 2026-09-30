/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Weblog Category.
 * 
 * <p>This class has been refactored to use service delegates for
 * specific operations to address Cyclic-Dependent Modularization:</p>
 * <ul>
 *   <li>{@link CategoryReferenceService} - ID-based weblog navigation</li>
 *   <li>{@link CategoryEntryService} - Entry retrieval operations</li>
 *   <li>{@link CategoryPositionService} - Position calculation logic</li>
 * </ul>
 * 
 * @see CategoryReferenceService
 * @see CategoryEntryService
 * @see CategoryPositionService
 */
public class WeblogCategory implements Serializable, Comparable<WeblogCategory> {
    
    public static final long serialVersionUID = 1435782148712018954L;
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private String description = null;
    private String image = null;
    private int position;

    // associations - kept for JPA mapping
    // For new code, prefer using CategoryReferenceService for ID-based access
    private Weblog weblog = null;
    
    // Service delegates (lazy initialized)
    private static CategoryReferenceService referenceService;
    private static CategoryEntryService entryService;
    private static CategoryPositionService positionService;


    public WeblogCategory() {
    }
    
    public WeblogCategory(
            Weblog weblog,
            String name,
            String description,
            String image) {
        
        this.name = name;
        this.description = description;
        this.image = image;
        
        this.weblog = weblog;
        weblog.getWeblogCategories().add(this);
        calculatePosition();
    }
    
    
    // -------------------- Service Accessors --------------------
    
    /**
     * Get the reference service for weblog navigation.
     * @return CategoryReferenceService instance
     */
    public static CategoryReferenceService getReferenceService() {
        if (referenceService == null) {
            referenceService = new CategoryReferenceService();
        }
        return referenceService;
    }
    
    /**
     * Get the entry service for entry operations.
     * @return CategoryEntryService instance
     */
    public static CategoryEntryService getEntryService() {
        if (entryService == null) {
            entryService = new CategoryEntryService();
        }
        return entryService;
    }
    
    /**
     * Get the position service for position operations.
     * @return CategoryPositionService instance
     */
    public static CategoryPositionService getPositionService() {
        if (positionService == null) {
            positionService = new CategoryPositionService();
        }
        return positionService;
    }


    // -------------------- Position Management --------------------

    /**
     * Calculate and set position for this category.
     * 
     * @deprecated Use {@link CategoryPositionService#assignPosition(WeblogCategory)} instead.
     */
    @Deprecated
    public void calculatePosition() {
        getPositionService().assignPosition(this);
    }


    // -------------------- Good Citizenship --------------------

    @Override
    public String toString() {
        return "{" + getId() + ", " + getName() + "}";
    }
    
    /**
     * Equals based on name and weblog ID.
     * 
     * <p>Note: This implementation uses ID-based comparison via
     * CategoryReferenceService to avoid lazy loading issues.</p>
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || !(other instanceof WeblogCategory)) {
            return false;
        }
        return getReferenceService().areEqual(this, (WeblogCategory) other);
    }

    /**
     * HashCode based on name and weblog ID.
     * 
     * <p>Note: This implementation uses ID-based comparison via
     * CategoryReferenceService to avoid lazy loading issues.</p>
     */
    @Override
    public int hashCode() {
        return getReferenceService().generateHashCode(this);
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(WeblogCategory other) {
        return getName().compareTo(other.getName());
    }


    // -------------------- ID and Basic Properties --------------------

    /**
     * Database surrogate key.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * The display name for this category.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = Utilities.removeHTML(name);
    }
    
    
    /**
     * A full description for this category.
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = Utilities.removeHTML(description);
    }

    /**
     * A 0-based position indicator for desired display order of that category.
     */
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * An image icon to represent this category.
     */
    public String getImage() {
        return this.image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    
    // -------------------- Weblog Association --------------------
    
    /**
     * Get the weblog which owns this category.
     * 
     * <p>Note: For ID-based access that avoids lazy loading issues,
     * use {@link CategoryReferenceService#getWeblogId(WeblogCategory)}.</p>
     */
    public Weblog getWeblog() {
        return weblog;
    }
    
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }
    
    /**
     * Get the weblog ID without triggering lazy loading.
     * Convenience method that delegates to CategoryReferenceService.
     * 
     * @return Weblog ID or null
     */
    public String getWeblogId() {
        return getReferenceService().getWeblogId(this);
    }


    // -------------------- Entry Operations --------------------

    /**
     * Retrieve all weblog entries in this category.
     *
     * @param publishedOnly True if desired to return only published entries
     * @return List of WeblogEntry objects.
     * @throws WebloggerException if retrieval fails
     * 
     * @deprecated Use {@link CategoryEntryService#getEntries(WeblogCategory, boolean)} instead.
     */
    @Deprecated
    public List<WeblogEntry> retrieveWeblogEntries(boolean publishedOnly) throws WebloggerException {
        return getEntryService().getEntries(this, publishedOnly);
    }
    
    /**
     * Returns true if category is in use.
     * 
     * @deprecated Use {@link CategoryEntryService#isInUse(WeblogCategory)} instead.
     */
    @Deprecated
    public boolean isInUse() {
        return getEntryService().isInUse(this);
    }
}
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
import java.util.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.util.I18nUtils;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Website has many-to-many association with users. Website has one-to-many and
 * one-direction associations with weblog entries, weblog categories, folders and
 * other objects. Use UserManager to create, fetch, update and retrieve websites.
 *
 * @author David M Johnson
 */
public class Weblog implements Serializable {
    
    public static final long serialVersionUID = 206437645033737127L;
    
    private static Log log = LogFactory.getLog(Weblog.class);
    
    // Simple properties
    private String  id               = UUIDGenerator.generateUUID();
    private String  handle           = null;
    private String  name             = null;
    private String  tagline          = null;
    private Boolean enableBloggerApi = Boolean.TRUE;
    private String  editorPage       = null;
    private String  bannedwordslist  = null;
    private Boolean allowComments    = Boolean.TRUE;
    private Boolean emailComments    = Boolean.FALSE;
    private String  emailAddress     = null;
    private String  editorTheme      = null;
    private String  locale           = null;
    private String  timeZone         = null;
    private String  defaultPlugins   = null;
    private Boolean visible          = Boolean.TRUE;
    private Boolean active           = Boolean.TRUE;
    private Date    dateCreated      = new java.util.Date();
    private Boolean defaultAllowComments = Boolean.TRUE;
    private int     defaultCommentDays = 0;
    private Boolean moderateComments = Boolean.FALSE;
    private int     entryDisplayCount = 15;
    private Date    lastModified     = new Date();
    private boolean enableMultiLang  = false;
    private boolean showAllLangs     = true;
    private String  iconPath         = null;
    private String  about            = null;
    private String  creator          = null;
    private String  analyticsCode    = null;

    // Associated objects
    private WeblogCategory bloggerCategory = null;

    private List<WeblogCategory> weblogCategories = new ArrayList<>();
    private List<WeblogBookmarkFolder> bookmarkFolders = new ArrayList<>();
    private List<MediaFileDirectory> mediaFileDirectories = new ArrayList<>();

    // Service delegates (lazy-initialized)
    private transient WeblogContentService contentService;
    private transient WeblogPermissionService permissionService;
    private transient WeblogResourceService resourceService;

    public Weblog() {
    }
    
    public Weblog(
            String handle,
            String creator,
            String name,
            String desc,
            String email,
            String editorTheme,
            String locale,
            String timeZone) {
        
        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.tagline = desc;
        this.emailAddress = email;
        this.editorTheme = editorTheme;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    // -------------------- Service Accessors (Lazy Initialization) --------------------

    /**
     * Get the content service for this weblog.
     * Handles retrieval of entries, comments, tags, and statistics.
     */
    public WeblogContentService getContentService() {
        if (contentService == null) {
            contentService = new WeblogContentService(this);
        }
        return contentService;
    }

    /**
     * Get the permission service for this weblog.
     * Handles user permission checks.
     */
    public WeblogPermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = new WeblogPermissionService(this);
        }
        return permissionService;
    }

    /**
     * Get the resource service for this weblog.
     * Handles themes, bookmarks, plugins, and URLs.
     */
    public WeblogResourceService getResourceService() {
        if (resourceService == null) {
            resourceService = new WeblogResourceService(this);
        }
        return resourceService;
    }
    
    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        return  "{" + getId() + ", " + getHandle()
        + ", " + getName() + ", " + getEmailAddress()
        + ", " + getLocale() + ", " + getTimeZone() + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Weblog)) {
            return false;
        }
        Weblog o = (Weblog)other;
        return new EqualsBuilder()
            .append(getHandle(), o.getHandle()) 
            .isEquals();
    }
    
    @Override
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getHandle())
            .toHashCode();
    } 
    
    // -------------------- Delegate Methods (Backward Compatibility) --------------------
    
    /**
     * Get the Theme object in use by this weblog, or null if no theme selected.
     * @deprecated Use getResourceService().getTheme() instead
     */
    public WeblogTheme getTheme() {
        return getResourceService().getTheme();
    }

    /**
     * Original creator of website.
     * @deprecated Use getPermissionService().getCreator() instead
     */
    public org.apache.roller.weblogger.pojos.User getCreator() {
        return getPermissionService().getCreator();
    }
    
    /**
     * Returns true if user has all permission action specified.
     * @deprecated Use getPermissionService().hasUserPermission() instead
     */
    public boolean hasUserPermission(User user, String action) {
        return getPermissionService().hasUserPermission(user, action);
    }
    
    /**
     * Returns true if user has all permissions actions specified in the weblog.
     * @deprecated Use getPermissionService().hasUserPermissions() instead
     */
    public boolean hasUserPermissions(User user, List<String> actions) {
        return getPermissionService().hasUserPermissions(user, actions);
    }
    
    /**
     * Get initialized plugins for use during rendering process.
     * @deprecated Use getResourceService().getInitializedPlugins() instead
     */
    public Map<String, WeblogEntryPlugin> getInitializedPlugins() {
        return getResourceService().getInitializedPlugins();
    }
    
    /** 
     * Get weblog entry specified by anchor or null if no such entry exists.
     * @deprecated Use getContentService().getWeblogEntry() instead
     */
    public WeblogEntry getWeblogEntry(String anchor) {
        return getContentService().getWeblogEntry(anchor);
    }

    /**
     * Get weblog category by name.
     * @deprecated Use getContentService().getWeblogCategory() instead
     */
    public WeblogCategory getWeblogCategory(String categoryName) {
        return getContentService().getWeblogCategory(categoryName);
    }

    /**
     * Get up to 100 most recent published entries in weblog.
     * @deprecated Use getContentService().getRecentWeblogEntries() instead
     */
    public List<WeblogEntry> getRecentWeblogEntries(String cat, int length) {
        return getContentService().getRecentWeblogEntries(cat, length);
    }
    
    /**
     * Get up to 100 most recent published entries in weblog by tag.
     * @deprecated Use getContentService().getRecentWeblogEntriesByTag() instead
     */
    public List<WeblogEntry> getRecentWeblogEntriesByTag(String tag, int length) {
        return getContentService().getRecentWeblogEntriesByTag(tag, length);
    }   
    
    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @deprecated Use getContentService().getRecentComments() instead
     */
    public List<WeblogEntryComment> getRecentComments(int length) {
        return getContentService().getRecentComments(length);
    }

    /**
     * Get bookmark folder by name.
     * @deprecated Use getResourceService().getBookmarkFolder() instead
     */
    public WeblogBookmarkFolder getBookmarkFolder(String folderName) {
        return getResourceService().getBookmarkFolder(folderName);
    }

    /**
     * Get number of hits counted today.
     * @deprecated Use getContentService().getTodaysHits() instead
     */
    public int getTodaysHits() {
        return getContentService().getTodaysHits();
    }

    /**
     * Get a list of TagStats objects for the most popular tags.
     * @deprecated Use getContentService().getPopularTags() instead
     */
    public List<TagStat> getPopularTags(int sinceDays, int length) {
        return getContentService().getPopularTags(sinceDays, length);
    }

    /**
     * Get comment count.
     * @deprecated Use getContentService().getCommentCount() instead
     */
    public long getCommentCount() {
        return getContentService().getCommentCount();
    }
    
    /**
     * Get entry count.
     * @deprecated Use getContentService().getEntryCount() instead
     */
    public long getEntryCount() {
        return getContentService().getEntryCount();
    }

    /**
     * Get weblog URL.
     * @deprecated Use getResourceService().getURL() instead
     */
    public String getURL() {
        return getResourceService().getURL();
    }

    /**
     * Get absolute weblog URL.
     * @deprecated Use getResourceService().getAbsoluteURL() instead
     */
    public String getAbsoluteURL() {
        return getResourceService().getAbsoluteURL();
    }

    // -------------------- Simple Property Accessors --------------------

    /**
     * Id of the Website.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Short URL safe string that uniquely identifies the website.
     */
    public String getHandle() {
        return this.handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Name of the Website.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = Utilities.removeHTML(name);
    }
    
    /**
     * Description
     */
    public String getTagline() {
        return this.tagline;
    }
    
    public void setTagline(String tagline) {
        this.tagline = Utilities.removeHTML(tagline);
    }
    
    /**
     * Username of original creator of website.
     */
    public String getCreatorUserName() {
        return creator;
    }
    
    public void setCreatorUserName(String creatorUserName) {
        creator = creatorUserName;
    }

    public Boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }
    
    public WeblogCategory getBloggerCategory() { 
        return bloggerCategory; 
    }
    
    public void setBloggerCategory(WeblogCategory bloggerCategory) {
        this.bloggerCategory = bloggerCategory;
    }
    
    public String getEditorPage() {
        return this.editorPage;
    }
    
    public void setEditorPage(String editorPage) {
        this.editorPage = editorPage;
    }
    
    public String getBannedwordslist() {
        return this.bannedwordslist;
    }
    
    public void setBannedwordslist(String bannedwordslist) {
        this.bannedwordslist = bannedwordslist;
    }
    
    public Boolean getAllowComments() {
        return this.allowComments;
    }
    
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }
    
    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }
    
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }
    
    public Boolean getModerateComments() {
        return moderateComments;
    }
    
    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }
    
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }
    
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * EditorTheme of the Website.
     */
    public String getEditorTheme() {
        return this.editorTheme;
    }
    
    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }
    
    /**
     * Locale of the Website.
     */
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Timezone of the Website.
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }

    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
            dateCreated = null;
        }
    }

    /**
     * Comma-delimited list of user's default Plugins.
     */
    public String getDefaultPlugins() {
        return defaultPlugins;
    }

    public void setDefaultPlugins(String string) {
        defaultPlugins = string;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(Weblog other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setHandle(other.getHandle());
        this.setTagline(other.getTagline());
        this.setCreatorUserName(other.getCreatorUserName());
        this.setEnableBloggerApi(other.getEnableBloggerApi());
        this.setBloggerCategory(other.getBloggerCategory());
        this.setEditorPage(other.getEditorPage());
        this.setBannedwordslist(other.getBannedwordslist());
        this.setAllowComments(other.getAllowComments());
        this.setEmailComments(other.getEmailComments());
        this.setEmailAddress(other.getEmailAddress());
        this.setEditorTheme(other.getEditorTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setVisible(other.getVisible());
        this.setDateCreated(other.getDateCreated());
        this.setEntryDisplayCount(other.getEntryDisplayCount());
        this.setActive(other.getActive());
        this.setLastModified(other.getLastModified());
        this.setWeblogCategories(other.getWeblogCategories());
    }
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     */
    public Locale getLocaleInstance() {
        return I18nUtils.toLocale(getLocale());
    }
    
    /**
     * Return TimeZone instance for value of timeZone,
     * otherwise return system default instance.
     */
    public TimeZone getTimeZoneInstance() {
        if (getTimeZone() == null) {
            this.setTimeZone(TimeZone.getDefault().getID());
        }
        return TimeZone.getTimeZone(getTimeZone());
    }
    
    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }
    
    public void setEntryDisplayCount(int entryDisplayCount) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    /**
     * Set to FALSE to disable and hide this weblog from public view.
     */
    public Boolean getVisible() {
        return this.visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Set to FALSE to exclude this weblog from community areas such as the
     * front page and the planet page.
     */
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    /**
     * Returns true if comment moderation is required by website or config.
     */ 
    public boolean getCommentModerationRequired() { 
        return (getModerateComments()
         || WebloggerRuntimeConfig.getBooleanProperty("users.moderation.required"));
    }
    
    /** No-op */
    public void setCommentModerationRequired(boolean modRequired) {}    

    /**
     * The last time any visible part of this weblog was modified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
  
    /**
     * Is multi-language blog support enabled for this weblog?
     */
    public boolean isEnableMultiLang() {
        return enableMultiLang;
    }

    public void setEnableMultiLang(boolean enableMultiLang) {
        this.enableMultiLang = enableMultiLang;
    }
    
    /**
     * Should the default weblog view show entries from all languages?
     */
    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }

    /**
     * The path under the weblog's resources to an icon image.
     */
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getAnalyticsCode() {
        return analyticsCode;
    }

    public void setAnalyticsCode(String analyticsCode) {
        this.analyticsCode = analyticsCode;
    }

    /**
     * A description for the weblog (its purpose, authors, etc.)
     */
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = Utilities.removeHTML(about);
    }

    // -------------------- Collection Management --------------------

    /**
     * Add a category as a child of this weblog.
     */
    public void addCategory(WeblogCategory category) {
        if (category == null || category.getName() == null) {
            throw new IllegalArgumentException("Category cannot be null and must have a valid name");
        }
        if (hasCategory(category.getName())) {
            throw new IllegalArgumentException("Duplicate category name '" + category.getName() + "'");
        }
        getWeblogCategories().add(category);
    }

    public List<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List<WeblogCategory> cats) {
        this.weblogCategories = cats;
    }

    public boolean hasCategory(String name) {
        for (WeblogCategory cat : getWeblogCategories()) {
            if (name.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<WeblogBookmarkFolder> getBookmarkFolders() {
        return bookmarkFolders;
    }

    public void setBookmarkFolders(List<WeblogBookmarkFolder> bookmarkFolders) {
        this.bookmarkFolders = bookmarkFolders;
    }

    public List<MediaFileDirectory> getMediaFileDirectories() {
        return mediaFileDirectories;
    }

    public void setMediaFileDirectories(List<MediaFileDirectory> mediaFileDirectories) {
        this.mediaFileDirectories = mediaFileDirectories;
    }

    /**
     * Add a bookmark folder to this weblog.
     */
    public void addBookmarkFolder(WeblogBookmarkFolder folder) {
        if (folder == null || folder.getName() == null) {
            throw new IllegalArgumentException("Folder cannot be null and must have a valid name");
        }
        if (this.hasBookmarkFolder(folder.getName())) {
            throw new IllegalArgumentException("Duplicate folder name '" + folder.getName() + "'");
        }
        getBookmarkFolders().add(folder);
    }

    /**
     * Does this Weblog have a bookmark folder with the specified name?
     */
    public boolean hasBookmarkFolder(String name) {
        for (WeblogBookmarkFolder folder : this.getBookmarkFolders()) {
            if (name.equalsIgnoreCase(folder.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether this weblog contains the specified media file directory.
     */
    public boolean hasMediaFileDirectory(String name) {
        for (MediaFileDirectory directory : this.getMediaFileDirectories()) {
            if (directory.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public MediaFileDirectory getMediaFileDirectory(String name) {
        for (MediaFileDirectory dir : this.getMediaFileDirectories()) {
            if (name.equals(dir.getName())) {
                return dir;
            }
        }
        return null;
    }
}
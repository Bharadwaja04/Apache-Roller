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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Represents a Weblog Entry.
 */
public class WeblogEntry implements Serializable {
    private static final Log mLogger = LogFactory.getFactory().getInstance(WeblogEntry.class);

    public static final long serialVersionUID = 2341505386843044125L;

    public enum PubStatus {DRAFT, PUBLISHED, PENDING, SCHEDULED}

    // Simple properties
    private String    id            = UUIDGenerator.generateUUID();
    private String    title         = null;
    private String    link          = null;
    private String    summary       = null;
    private String    text          = null;
    private String    contentType   = null;
    private String    contentSrc    = null;
    private String    anchor        = null;
    private Timestamp pubTime       = null;
    private Timestamp updateTime    = null;
    private String    plugins       = null;
    private Boolean   allowComments = Boolean.TRUE;
    private Integer   commentDays   = 7;
    private Boolean   rightToLeft   = Boolean.FALSE;
    private Boolean   pinnedToMain  = Boolean.FALSE;
    private PubStatus status        = PubStatus.DRAFT;
    private String    locale        = null;
    private String    creatorUserName = null;
    private String    searchDescription = null;

    // Set to true when switching between pending/draft/scheduled and published
    private Boolean   refreshAggregates = Boolean.FALSE;

    // Associated objects
    private Weblog        website  = null;
    private WeblogCategory category = null;

    // Collection of name/value entry attributes
    private Set<WeblogEntryAttribute> attSet = new TreeSet<>();

    private Set<WeblogEntryTag> tagSet = new HashSet<>();
    private Set<WeblogEntryTag> removedTags = new HashSet<>();
    private Set<WeblogEntryTag> addedTags = new HashSet<>();

    // Service delegates (lazy-initialized)
    private transient WeblogEntryContentRenderer contentRenderer;
    private transient WeblogEntryCommentService commentService;
    private transient WeblogEntryPermissionService permissionService;
    private transient WeblogEntryUrlService urlService;

    //----------------------------------------------------------- Construction

    public WeblogEntry() {
    }

    public WeblogEntry(
            String id,
            WeblogCategory category,
            Weblog website,
            User creator,
            String title,
            String link,
            String text,
            String anchor,
            Timestamp pubTime,
            Timestamp updateTime,
            PubStatus status) {
        this.category = category;
        this.website = website;
        this.creatorUserName = creator.getUserName();
        this.title = title;
        this.link = link;
        this.text = text;
        this.anchor = anchor;
        this.pubTime = pubTime;
        this.updateTime = updateTime;
        this.status = status;
    }

    public WeblogEntry(WeblogEntry otherData) {
        this.setData(otherData);
    }

    //---------------------------------------------------------- Service Accessors

    /**
     * Get the content renderer service for this entry.
     * Handles text transformation and plugin rendering.
     */
    public WeblogEntryContentRenderer getContentRenderer() {
        if (contentRenderer == null) {
            contentRenderer = new WeblogEntryContentRenderer(this);
        }
        return contentRenderer;
    }

    /**
     * Get the comment service for this entry.
     * Handles comment retrieval and permissions.
     */
    public WeblogEntryCommentService getCommentService() {
        if (commentService == null) {
            commentService = new WeblogEntryCommentService(this);
        }
        return commentService;
    }

    /**
     * Get the permission service for this entry.
     * Handles user permission checks.
     */
    public WeblogEntryPermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = new WeblogEntryPermissionService(this);
        }
        return permissionService;
    }

    /**
     * Get the URL service for this entry.
     * Handles permalink and anchor generation.
     */
    public WeblogEntryUrlService getUrlService() {
        if (urlService == null) {
            urlService = new WeblogEntryUrlService(this);
        }
        return urlService;
    }

    //---------------------------------------------------------- Initialization

    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntry other) {
        this.setId(other.getId());
        this.setCategory(other.getCategory());
        this.setWebsite(other.getWebsite());
        this.setCreatorUserName(other.getCreatorUserName());
        this.setTitle(other.getTitle());
        this.setLink(other.getLink());
        this.setText(other.getText());
        this.setSummary(other.getSummary());
        this.setSearchDescription(other.getSearchDescription());
        this.setAnchor(other.getAnchor());
        this.setPubTime(other.getPubTime());
        this.setUpdateTime(other.getUpdateTime());
        this.setStatus(other.getStatus());
        this.setPlugins(other.getPlugins());
        this.setAllowComments(other.getAllowComments());
        this.setCommentDays(other.getCommentDays());
        this.setRightToLeft(other.getRightToLeft());
        this.setPinnedToMain(other.getPinnedToMain());
        this.setLocale(other.getLocale());
    }

    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(this.getAnchor());
        buf.append(", ").append(this.getTitle());
        buf.append(", ").append(this.getPubTime());
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntry)) {
            return false;
        }
        WeblogEntry o = (WeblogEntry)other;
        return new EqualsBuilder()
            .append(getAnchor(), o.getAnchor())
            .append(getWebsite(), o.getWebsite())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getAnchor())
            .append(getWebsite())
            .toHashCode();
    }

    //------------------------------------------------------ Simple properties

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.isBlank()) {
            return;
        }
        this.id = id;
    }

    public WeblogCategory getCategory() {
        return this.category;
    }

    public void setCategory(WeblogCategory category) {
        this.category = category;
    }

    /**
     * Return collection of WeblogCategory objects of this entry.
     * Added for symmetry with PlanetEntryData object.
     */
    public List<WeblogCategory> getCategories() {
        return List.of(getCategory());
    }

    public Weblog getWebsite() {
        return this.website;
    }

    public void setWebsite(Weblog website) {
        this.website = website;
    }

    /**
     * Get the creator of this entry.
     * @deprecated Use getPermissionService().getCreator() instead
     */
    @Deprecated
    public User getCreator() {
        return getPermissionService().getCreator();
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get summary for weblog entry (maps to RSS description and Atom summary).
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Set summary for weblog entry (maps to RSS description and Atom summary).
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Get search description for weblog entry.
     */
    public String getSearchDescription() {
        return searchDescription;
    }

    /**
     * Set search description for weblog entry
     */
    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    /**
     * Get content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get content type (text, html, xhtml or a MIME content type)
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set content type (text, html, xhtml or a MIME content type)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get URL for out-of-line content.
     */
    public String getContentSrc() {
        return contentSrc;
    }

    /**
     * Set URL for out-of-line content.
     */
    public void setContentSrc(String contentSrc) {
        this.contentSrc = contentSrc;
    }

    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    //-------------------------------------------------------------------------

    public Set<WeblogEntryAttribute> getEntryAttributes() {
        return attSet;
    }

    public void setEntryAttributes(Set<WeblogEntryAttribute> atts) {
        this.attSet = atts;
    }

    public String findEntryAttribute(String name) {
        if (getEntryAttributes() != null) {
            for (WeblogEntryAttribute att : getEntryAttributes()) {
                if (name.equals(att.getName())) {
                    return att.getValue();
                }
            }
        }
        return null;
    }

    public void putEntryAttribute(String name, String value) throws Exception {
        WeblogEntryAttribute att = null;
        for (WeblogEntryAttribute o : getEntryAttributes()) {
            if (name.equals(o.getName())) {
                att = o;
                break;
            }
        }
        if (att == null) {
            att = new WeblogEntryAttribute();
            att.setEntry(this);
            att.setName(name);
            att.setValue(value);
            getEntryAttributes().add(att);
        } else {
            att.setValue(value);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Publish time is the time that an entry is to be (or was) made available
     * for viewing by newsfeed readers and visitors to the Roller site.
     */
    public Timestamp getPubTime() {
        return this.pubTime;
    }

    public void setPubTime(Timestamp pubTime) {
        this.pubTime = pubTime;
    }

    /**
     * Update time is the last time that an weblog entry was saved in the
     * Roller weblog editor or via web services API (XML-RPC or Atom).
     */
    public Timestamp getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public PubStatus getStatus() {
        return this.status;
    }

    public void setStatus(PubStatus status) {
        this.status = status;
    }

    /**
     * Some weblog entries are about one specific link.
     */
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Comma-delimited list of this entry's Plugins.
     */
    public String getPlugins() {
        return plugins;
    }

    public void setPlugins(String string) {
        plugins = string;
    }

    /**
     * True if comments are allowed on this weblog entry.
     */
    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    /**
     * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     */
    public Integer getCommentDays() {
        return commentDays;
    }

    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }

    /**
     * True if this entry should be rendered right to left.
     */
    public Boolean getRightToLeft() {
        return rightToLeft;
    }

    public void setRightToLeft(Boolean rightToLeft) {
        this.rightToLeft = rightToLeft;
    }

    /**
     * True if story should be pinned to the top of the Roller site main blog.
     */
    public Boolean getPinnedToMain() {
        return pinnedToMain;
    }

    public void setPinnedToMain(Boolean pinnedToMain) {
        this.pinnedToMain = pinnedToMain;
    }

    /**
     * The locale string that defines the i18n approach for this entry.
     */
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Set<WeblogEntryTag> getTags() {
        return tagSet;
    }

    @SuppressWarnings("unused")
    public void setTags(Set<WeblogEntryTag> tagSet) throws WebloggerException {
        this.tagSet = tagSet;
        this.removedTags = new HashSet<>();
        this.addedTags = new HashSet<>();
    }

    /**
     * Roller lowercases all tags based on locale.
     */
    public void addTag(String name) throws WebloggerException {
        Locale localeObject = getWebsite() != null ? getWebsite().getLocaleInstance() : Locale.getDefault();
        name = Utilities.normalizeTag(name, localeObject);
        if (name.length() == 0) {
            return;
        }

        for (WeblogEntryTag tag : getTags()) {
            if (tag.getName().equals(name)) {
                return;
            }
        }

        WeblogEntryTag tag = new WeblogEntryTag();
        tag.setName(name);
        tag.setCreatorUserName(getCreatorUserName());
        tag.setWeblog(getWebsite());
        tag.setWeblogEntry(this);
        tag.setTime(getUpdateTime());
        tagSet.add(tag);

        addedTags.add(tag);
    }

    public Set<WeblogEntryTag> getAddedTags() {
        return addedTags;
    }

    public Set<WeblogEntryTag> getRemovedTags() {
        return removedTags;
    }

    public String getTagsAsString() {
        StringBuilder sb = new StringBuilder();
        Set<WeblogEntryTag> tmp = new TreeSet<>(new WeblogEntryTagComparator());
        tmp.addAll(getTags());
        for (WeblogEntryTag entryTag : tmp) {
            sb.append(entryTag.getName()).append(" ");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public void setTagsAsString(String tags) throws WebloggerException {
        if (StringUtils.isEmpty(tags)) {
            removedTags.addAll(tagSet);
            tagSet.clear();
            return;
        }

        List<String> updatedTags = Utilities.splitStringAsTags(tags);
        Set<String> newTags = new HashSet<>(updatedTags.size());
        Locale localeObject = getWebsite() != null ? getWebsite().getLocaleInstance() : Locale.getDefault();

        for (String name : updatedTags) {
            newTags.add(Utilities.normalizeTag(name, localeObject));
        }

        for (Iterator<WeblogEntryTag> it = tagSet.iterator(); it.hasNext();) {
            WeblogEntryTag tag = it.next();
            if (!newTags.contains(tag.getName())) {
                removedTags.add(tag);
                it.remove();
            } else {
                newTags.remove(tag.getName());
            }
        }

        for (String newTag : newTags) {
            addTag(newTag);
        }
    }

    // ------------------------------------------------------------------------
    // Delegate Methods (Backward Compatibility)
    // ------------------------------------------------------------------------

    /**
     * True if comments are still allowed on this entry.
     * @deprecated Use getCommentService().getCommentsStillAllowed() instead
     */
    @Deprecated
    public boolean getCommentsStillAllowed() {
        return getCommentService().getCommentsStillAllowed();
    }

    public void setCommentsStillAllowed(boolean ignored) {
        // no-op
    }

    /**
     * Format the publish time using the specified pattern.
     */
    public String formatPubTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern,
                    this.getWebsite().getLocaleInstance());
            return format.format(getPubTime());
        } catch (RuntimeException e) {
            mLogger.error("Unexpected exception", e);
        }
        return "ERROR: formatting date";
    }

    /**
     * Format the update time using the specified pattern.
     */
    public String formatUpdateTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.format(getUpdateTime());
        } catch (RuntimeException e) {
            mLogger.error("Unexpected exception", e);
        }
        return "ERROR: formatting date";
    }

    /**
     * Get approved comments for this entry.
     * @deprecated Use getCommentService().getComments() instead
     */
    @Deprecated
    public List<WeblogEntryComment> getComments() {
        return getCommentService().getComments();
    }

    /**
     * Get comments with filtering options.
     * @deprecated Use getCommentService().getComments() instead
     */
    @Deprecated
    public List<WeblogEntryComment> getComments(boolean ignoreSpam, boolean approvedOnly) {
        return getCommentService().getComments(ignoreSpam, approvedOnly);
    }

    /**
     * Get comment count.
     * @deprecated Use getCommentService().getCommentCount() instead
     */
    @Deprecated
    public int getCommentCount() {
        return getCommentService().getCommentCount();
    }

    /**
     * Get total number of users who have liked/starred this entry.
     * Accessible from Velocity templates as $entry.likeCount.
     */
    
    public long getLikeCount() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getLikeCount(this);
        } catch (Exception e) {
            mLogger.warn("Error getting like count for entry: " + this.id, e);
            return 0;
        }
    }

    /**
     * Returns absolute entry permalink.
     * @deprecated Use getUrlService().getPermalink() instead
     */
    @Deprecated
    public String getPermalink() {
        return getUrlService().getPermalink();
    }

    /**
     * Returns entry permalink, relative to Roller context.
     * @deprecated Use getUrlService().getPermalink() instead
     */
    @Deprecated
    public String getPermaLink() {
        return getUrlService().getPermaLink();
    }

    /**
     * Get relative URL to comments page.
     * @deprecated Use getUrlService().getCommentsLink() instead
     */
    @Deprecated
    public String getCommentsLink() {
        return getUrlService().getCommentsLink();
    }

    /**
     * Return the Title of this post, or the first 255 characters of text.
     * @deprecated Use getContentRenderer().getDisplayTitle() instead
     */
    @Deprecated
    public String getDisplayTitle() {
        return getContentRenderer().getDisplayTitle();
    }

    /**
     * Return RSS 09x style description.
     * @deprecated Use getContentRenderer().getRss09xDescription() instead
     */
    @Deprecated
    public String getRss09xDescription() {
        return getContentRenderer().getRss09xDescription();
    }

    /**
     * Return RSS 09x style description with max length.
     * @deprecated Use getContentRenderer().getRss09xDescription(maxLength) instead
     */
    @Deprecated
    public String getRss09xDescription(int maxLength) {
        return getContentRenderer().getRss09xDescription(maxLength);
    }

    /**
     * Create anchor for weblog entry.
     * @deprecated Use getUrlService().createAnchor() instead
     */
    @Deprecated
    protected String createAnchor() throws WebloggerException {
        return getUrlService().createAnchor();
    }

    /**
     * Create anchor base for weblog entry.
     * @deprecated Use getUrlService().createAnchorBase() instead
     */
    @Deprecated
    public String createAnchorBase() {
        return getUrlService().createAnchorBase();
    }

    // No-op setters for form bean compatibility
    public void setPermalink(String string) {}
    public void setPermaLink(String string) {}
    public void setDisplayTitle(String string) {}
    public void setRss09xDescription(String string) {}

    /**
     * Convenience method to transform plugins to a List
     */
    public List<String> getPluginsList() {
        if (getPlugins() != null) {
            return Arrays.asList(StringUtils.split(getPlugins(), ","));
        }
        return Collections.emptyList();
    }

    /** Convenience method for checking status */
    public boolean isDraft() {
        return getStatus().equals(PubStatus.DRAFT);
    }

    /** Convenience method for checking status */
    public boolean isPending() {
        return getStatus().equals(PubStatus.PENDING);
    }

    /** Convenience method for checking status */
    public boolean isPublished() {
        return getStatus().equals(PubStatus.PUBLISHED);
    }

    /**
     * Get entry text, transformed by plugins.
     * @deprecated Use getContentRenderer().getTransformedText() instead
     */
    @Deprecated
    public String getTransformedText() {
        return getContentRenderer().getTransformedText();
    }

    /**
     * Get entry summary, transformed by plugins.
     * @deprecated Use getContentRenderer().getTransformedSummary() instead
     */
    @Deprecated
    public String getTransformedSummary() {
        return getContentRenderer().getTransformedSummary();
    }

    /**
     * Determine if user has permissions to edit this entry.
     * @deprecated Use getPermissionService().hasWritePermissions() instead
     */
    @Deprecated
    public boolean hasWritePermissions(User user) throws WebloggerException {
        return getPermissionService().hasWritePermissions(user);
    }

    /**
     * Get display content with optional read more link.
     * @deprecated Use getContentRenderer().displayContent() instead
     */
    @Deprecated
    public String displayContent(String readMoreLink) {
        return getContentRenderer().displayContent(readMoreLink);
    }

    /**
     * Get display content for permalink view.
     * @deprecated Use getContentRenderer().getDisplayContent() instead
     */
    @Deprecated
    public String getDisplayContent() {
        return getContentRenderer().getDisplayContent();
    }

    public Boolean getRefreshAggregates() {
        return refreshAggregates;
    }

    public void setRefreshAggregates(Boolean refreshAggregates) {
        this.refreshAggregates = refreshAggregates;
    }
}
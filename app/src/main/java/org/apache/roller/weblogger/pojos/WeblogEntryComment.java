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
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * WeblogEntry comment bean.
 * 
 * <p>This class has been refactored to use service delegates for
 * specific operations to address Cyclic-Dependent Modularization:</p>
 * <ul>
 *   <li>{@link CommentStatusService} - Status checking and transitions</li>
 *   <li>{@link CommentContentService} - Content formatting and escaping</li>
 *   <li>{@link CommentReferenceService} - ID-based entry/weblog navigation</li>
 * </ul>
 * 
 * @see CommentStatusService
 * @see CommentContentService
 * @see CommentReferenceService
 */
public class WeblogEntryComment implements Serializable {
    
    public static final long serialVersionUID = -6668122596726478462L;
    
    // approval status states
    public enum ApprovalStatus {APPROVED, DISAPPROVED, SPAM, PENDING}

    // attributes
    private String    id = UUIDGenerator.generateUUID();
    private String    name = null;
    private String    email = null;
    private String    url = null;
    private String    content = null;
    private Timestamp postTime = null;
    private ApprovalStatus status = ApprovalStatus.APPROVED;
    private Boolean   notify = Boolean.FALSE;
    private String    remoteHost = null;
    private String    referrer = null;
    private String    userAgent = null;
    private String    plugins = null;
    private String    contentType = "text/plain";

    // associations - kept for backward compatibility
    // For new code, prefer using CommentReferenceService for ID-based access
    private WeblogEntry weblogEntry = null;
    
    // Service delegates (lazy initialized)
    private static CommentStatusService statusService;
    private static CommentContentService contentService;
    private static CommentReferenceService referenceService;
    
    
    public WeblogEntryComment() {}
    
    
    // -------------------- Service Accessors --------------------
    
    /**
     * Get the status service for status operations.
     * @return CommentStatusService instance
     */
    public static CommentStatusService getStatusService() {
        if (statusService == null) {
            statusService = new CommentStatusService();
        }
        return statusService;
    }
    
    /**
     * Get the content service for content operations.
     * @return CommentContentService instance
     */
    public static CommentContentService getContentService() {
        if (contentService == null) {
            contentService = new CommentContentService();
        }
        return contentService;
    }
    
    /**
     * Get the reference service for entry/weblog navigation.
     * @return CommentReferenceService instance
     */
    public static CommentReferenceService getReferenceService() {
        if (referenceService == null) {
            referenceService = new CommentReferenceService();
        }
        return referenceService;
    }
    
    
    // -------------------- ID and Basic Properties --------------------
    
    /**
     * Database ID of comment 
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Database ID of comment
     */
    public void setId(String id) {
        this.id = id;
    }
    
    
    // -------------------- Entry Association --------------------
    
    /**
     * Weblog entry associated with comment.
     * 
     * <p>Note: For ID-based access that avoids lazy loading issues,
     * use {@link CommentReferenceService#getWeblogEntryId(WeblogEntryComment)}.</p>
     */
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    /**
     * Weblog entry associated with comment.
     */
    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
    }
    
    /**
     * Get the weblog entry ID without triggering lazy loading.
     * Convenience method that delegates to CommentReferenceService.
     * 
     * @return Entry ID or null
     */
    public String getWeblogEntryId() {
        return getReferenceService().getWeblogEntryId(this);
    }
    
    
    // -------------------- Author Information --------------------
    
    /**
     * Name of person who wrote comment.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Name of person who wrote comment.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * Email of person who wrote comment.
     */
    public String getEmail() {
        return this.email;
    }
    
    /**
     * Email of person who wrote comment.
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    
    /**
     * URL of person who wrote comment.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * URL of person who wrote comment.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    
    // -------------------- Content --------------------
    
    /**
     * Content of comment.
     */
    public String getContent() {
        return this.content;
    }
    
    /**
     * Content of comment.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    
    /**
     * The content-type of the comment.
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * The content-type of the comment.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    
    // -------------------- Timestamps --------------------
    
    /**
     * Time that comment was posted.
     */
    public Timestamp getPostTime() {
        return this.postTime;
    }
    
    /**
     * Time that comment was posted.
     */
    public void setPostTime(Timestamp postTime) {
        this.postTime = postTime;
    }
    
    
    /**
     * Timestamp to be used to formulate comment permlink.
     * 
     * @deprecated Use {@link CommentContentService#getTimestamp(WeblogEntryComment)} instead.
     */
    @Deprecated
    public String getTimestamp() {
        return getContentService().getTimestamp(this);
    }
    
    
    // -------------------- Status --------------------
    
    /**
     * Status of the comment, i.e. APPROVED, SPAM, PENDING, etc.
     */
    public ApprovalStatus getStatus() {
        return status;
    }

    /**
     * Status of the comment, i.e. APPROVED, SPAM, PENDING, etc.
     */
    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
    
    
    /**
     * Indicates that weblog owner considers this comment to be spam.
     * 
     * @deprecated Use {@link CommentStatusService#isSpam(WeblogEntryComment)} instead.
     */
    @Deprecated
    public Boolean getSpam() {
        return getStatusService().isSpam(this);
    }
    
    
    /**
     * True if comment is pending moderator approval.
     * 
     * @deprecated Use {@link CommentStatusService#isPending(WeblogEntryComment)} instead.
     */
    @Deprecated
    public Boolean getPending() {
        return getStatusService().isPending(this);
    }
    
    
    /**
     * Indicates that comment has been approved for display on weblog.
     * 
     * @deprecated Use {@link CommentStatusService#isApproved(WeblogEntryComment)} instead.
     */
    @Deprecated
    public Boolean getApproved() {
        return getStatusService().isApproved(this);
    }
    
    
    // -------------------- Notification --------------------
    
    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    public Boolean getNotify() {
        return this.notify;
    }
    
    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    public void setNotify(Boolean notify) {
        this.notify = notify;
    }
    
    
    // -------------------- Request Metadata --------------------
    
    /**
     * Host name or IP of person who wrote comment.
     */
    public String getRemoteHost() {
        return this.remoteHost;
    }
    
    /**
     * Host name or IP of person who wrote comment.
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    
    
    /**
     * HTTP referrer from comment post request.
     */
    public String getReferrer() {
        return referrer;
    }
    
    /**
     * HTTP referrer from comment post request.
     */
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    
    /**
     * HTTP user-agent from comment post request.
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * HTTP user-agent from comment post request.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    
    // -------------------- Plugins --------------------
    
    /**
     * Comma separated list of comment plugins to apply.
     */
    public String getPlugins() {
        return plugins;
    }
    
    /**
     * Comma separated list of comment plugins to apply.
     */
    public void setPlugins(String plugins) {
        this.plugins = plugins;
    }
    
    
    // -------------------- Good Citizenship --------------------

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
        buf.append(", ").append(getEmail());
        buf.append(", ").append(getPostTime());
        buf.append("}");
        return buf.toString();
    }

    /**
     * Equals based on ID, name, and postTime.
     * 
     * <p>Note: This implementation avoids using getWeblogEntry() directly
     * to prevent lazy loading issues. Uses entry ID for comparison instead.</p>
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntryComment)) {
            return false;
        }
        WeblogEntryComment o = (WeblogEntryComment)other;
        return new EqualsBuilder()
            .append(getId(), o.getId())
            .append(getName(), o.getName()) 
            .append(getPostTime(), o.getPostTime()) 
            .isEquals();
    }
    
    /**
     * HashCode based on ID, name, and postTime.
     * 
     * <p>Note: This implementation avoids using getWeblogEntry() directly
     * to prevent lazy loading issues.</p>
     */
    @Override
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getId())
            .append(getName())
            .append(getPostTime())
            .toHashCode();
    }
}
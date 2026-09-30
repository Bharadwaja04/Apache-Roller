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
 * Service for managing comment-entry relationships.
 * Extracted to address Cyclic-Dependent Modularization.
 * 
 * <p>This service provides ID-based navigation to break direct
 * bidirectional dependencies between WeblogEntryComment and WeblogEntry.</p>
 * 
 * <p>Instead of:</p>
 * <pre>
 *   comment.getWeblogEntry().getWeblog().getHandle()
 * </pre>
 * 
 * <p>Use:</p>
 * <pre>
 *   referenceService.getWeblogHandle(comment)
 * </pre>
 */
public class CommentReferenceService {

    /**
     * Get the weblog entry ID for a comment.
     * Uses ID-based reference to avoid triggering lazy loading.
     * 
     * @param comment The comment
     * @return Entry ID or null
     */
    public String getWeblogEntryId(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();
        return entry != null ? entry.getId() : null;
    }

    /**
     * Get the weblog ID for a comment.
     * 
     * @param comment The comment
     * @return Weblog ID or null
     */
    public String getWeblogId(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();
        if (entry != null && entry.getWebsite() != null) {
            return entry.getWebsite().getId();
        }
        return null;
    }

    /**
     * Get the weblog handle for a comment.
     * 
     * @param comment The comment
     * @return Weblog handle or null
     */
    public String getWeblogHandle(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();
        if (entry != null && entry.getWebsite() != null) {
            return entry.getWebsite().getHandle();
        }
        return null;
    }

    /**
     * Get the entry title for a comment.
     * 
     * @param comment The comment
     * @return Entry title or null
     */
    public String getEntryTitle(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();
        return entry != null ? entry.getTitle() : null;
    }

    /**
     * Get the entry anchor for a comment.
     * 
     * @param comment The comment
     * @return Entry anchor or null
     */
    public String getEntryAnchor(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();
        return entry != null ? entry.getAnchor() : null;
    }

    /**
     * Check if comment belongs to a specific weblog.
     * 
     * @param comment The comment
     * @param weblogId The weblog ID to check
     * @return true if comment belongs to the weblog
     */
    public boolean belongsToWeblog(WeblogEntryComment comment, String weblogId) {
        String commentWeblogId = getWeblogId(comment);
        return weblogId != null && weblogId.equals(commentWeblogId);
    }

    /**
     * Check if comment belongs to a specific entry.
     * 
     * @param comment The comment
     * @param entryId The entry ID to check
     * @return true if comment belongs to the entry
     */
    public boolean belongsToEntry(WeblogEntryComment comment, String entryId) {
        String commentEntryId = getWeblogEntryId(comment);
        return entryId != null && entryId.equals(commentEntryId);
    }
}
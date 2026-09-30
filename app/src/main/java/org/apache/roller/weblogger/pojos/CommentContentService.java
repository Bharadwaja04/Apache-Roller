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

import org.apache.commons.text.StringEscapeUtils;

/**
 * Service for handling comment content operations.
 * Extracted from WeblogEntryComment to address Cyclic-Dependent Modularization.
 * 
 * <p>This service provides:</p>
 * <ul>
 *   <li>Content escaping and sanitization</li>
 *   <li>Content type handling</li>
 *   <li>Timestamp formatting</li>
 * </ul>
 */
public class CommentContentService {

    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_PLAIN = "text/plain";

    /**
     * Get escaped content safe for HTML display.
     * 
     * @param comment The comment
     * @return Escaped content
     */
    public String getEscapedContent(WeblogEntryComment comment) {
        if (comment.getContent() == null) {
            return "";
        }
        return StringEscapeUtils.escapeHtml4(comment.getContent());
    }

    /**
     * Get content formatted based on content type.
     * 
     * @param comment The comment
     * @return Formatted content
     */
    public String getFormattedContent(WeblogEntryComment comment) {
        if (comment.getContent() == null) {
            return "";
        }
        
        if (isHtmlContent(comment)) {
            // HTML content - return as-is (assuming it's sanitized)
            return comment.getContent();
        } else {
            // Plain text - escape and convert newlines to <br>
            String escaped = StringEscapeUtils.escapeHtml4(comment.getContent());
            return escaped.replace("\n", "<br/>");
        }
    }

    /**
     * Check if comment content is HTML.
     * 
     * @param comment The comment
     * @return true if HTML content type
     */
    public boolean isHtmlContent(WeblogEntryComment comment) {
        return CONTENT_TYPE_HTML.equals(comment.getContentType());
    }

    /**
     * Check if comment content is plain text.
     * 
     * @param comment The comment
     * @return true if plain text content type
     */
    public boolean isPlainTextContent(WeblogEntryComment comment) {
        return CONTENT_TYPE_PLAIN.equals(comment.getContentType()) 
                || comment.getContentType() == null;
    }

    /**
     * Get timestamp string for permalink generation.
     * 
     * @param comment The comment
     * @return Timestamp string or null
     */
    public String getTimestamp(WeblogEntryComment comment) {
        if (comment.getPostTime() != null) {
            return Long.toString(comment.getPostTime().getTime());
        }
        return null;
    }

    /**
     * Get a truncated preview of the comment content.
     * 
     * @param comment The comment
     * @param maxLength Maximum length of preview
     * @return Truncated content with ellipsis if needed
     */
    public String getContentPreview(WeblogEntryComment comment, int maxLength) {
        if (comment.getContent() == null) {
            return "";
        }
        
        String content = comment.getContent();
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Get content length.
     * 
     * @param comment The comment
     * @return Content length or 0 if null
     */
    public int getContentLength(WeblogEntryComment comment) {
        if (comment.getContent() == null) {
            return 0;
        }
        return comment.getContent().length();
    }
}
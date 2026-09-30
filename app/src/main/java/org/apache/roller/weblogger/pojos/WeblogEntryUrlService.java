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

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.util.Utilities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

/**
 * Service class responsible for URL and anchor generation for weblog entries.
 * Extracted from WeblogEntry to address God Class and Hub-like Modularization.
 */
public class WeblogEntryUrlService {

    private static final char TITLE_SEPARATOR =
            WebloggerConfig.getBooleanProperty("weblogentry.title.useUnderscoreSeparator") ? '_' : '-';

    private final WeblogEntry entry;

    public WeblogEntryUrlService(WeblogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("WeblogEntry cannot be null");
        }
        this.entry = entry;
    }

    /**
     * Returns absolute entry permalink.
     *
     * @return Absolute permalink URL
     */
    public String getPermalink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy()
                .getWeblogEntryURL(entry.getWebsite(), null, entry.getAnchor(), true);
    }

    /**
     * Returns entry permalink, relative to Roller context.
     *
     * @return Relative permalink URL
     * @deprecated Use getPermalink() instead
     */
    @Deprecated
    public String getPermaLink() {
        String encodedAnchor = URLEncoder.encode(entry.getAnchor(), StandardCharsets.UTF_8);
        return "/" + entry.getWebsite().getHandle() + "/entry/" + encodedAnchor;
    }

    /**
     * Get relative URL to comments page.
     *
     * @return Comments page URL
     * @deprecated Use getPermalink() + "#comments" instead
     */
    @Deprecated
    public String getCommentsLink() {
        return getPermaLink() + "#comments";
    }

    /**
     * Create anchor for weblog entry, based on title or text.
     *
     * @return Generated anchor
     * @throws WebloggerException if error creating anchor
     */
    public String createAnchor() throws WebloggerException {
        return WebloggerFactory.getWeblogger().getWeblogEntryManager().createAnchor(entry);
    }

    /**
     * Create anchor base for weblog entry, based on title or text.
     * This is used as a starting point before uniqueness checks.
     *
     * @return Anchor base string
     */
    public String createAnchorBase() {
        String base = extractBaseFromTitle();

        if (StringUtils.isEmpty(base)) {
            base = extractBaseFromText();
        }

        if (!StringUtils.isEmpty(base)) {
            base = normalizeAnchorBase(base);
        } else {
            // No title or text, use date as fallback
            base = DateUtil.format8chars(entry.getPubTime());
        }

        return base;
    }

    // -------------------- Private Helper Methods --------------------

    private String extractBaseFromTitle() {
        if (!StringUtils.isEmpty(entry.getTitle())) {
            return Utilities.replaceNonAlphanumeric(entry.getTitle(), ' ').trim();
        }
        return null;
    }

    private String extractBaseFromText() {
        if (!StringUtils.isEmpty(entry.getText())) {
            return Utilities.replaceNonAlphanumeric(entry.getText(), ' ').trim();
        }
        return null;
    }

    private String normalizeAnchorBase(String base) {
        // Use only the first 4 words
        StringTokenizer toker = new StringTokenizer(base);
        StringBuilder result = new StringBuilder();
        int count = 0;

        while (toker.hasMoreTokens() && count < 5) {
            String word = toker.nextToken().toLowerCase();
            if (result.length() > 0) {
                result.append(TITLE_SEPARATOR);
            }
            result.append(word);
            count++;
        }

        return result.toString();
    }
}
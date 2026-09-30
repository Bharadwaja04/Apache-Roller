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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;

import java.util.List;
import java.util.Map;

/**
 * Service class responsible for rendering and transforming weblog entry content.
 * Extracted from WeblogEntry to address God Class and Feature Envy design smells.
 */
public class WeblogEntryContentRenderer {

    private static final Log log = LogFactory.getLog(WeblogEntryContentRenderer.class);

    private final WeblogEntry entry;

    public WeblogEntryContentRenderer(WeblogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("WeblogEntry cannot be null");
        }
        this.entry = entry;
    }

    /**
     * Get entry text, transformed by plugins enabled for entry.
     *
     * @return Transformed text content
     */
    public String getTransformedText() {
        return render(entry.getText());
    }

    /**
     * Get entry summary, transformed by plugins enabled for entry.
     *
     * @return Transformed summary content
     */
    public String getTransformedSummary() {
        return render(entry.getSummary());
    }

    /**
     * Get the right transformed display content depending on the situation.
     *
     * If the readMoreLink is specified then we assume the caller wants to
     * prefer summary over content and we include a "Read More" link at the
     * end of the summary if it exists. Otherwise, if the readMoreLink is
     * empty or null then we assume the caller prefers content over summary.
     *
     * @param readMoreLink Link for "Read More", or null for permalink view
     * @return Display content HTML
     */
    public String displayContent(String readMoreLink) {
        String displayContent;

        if (isPermalinkView(readMoreLink)) {
            // no readMore link means permalink, so prefer text over summary
            displayContent = getContentPreferringText();
        } else {
            // not a permalink, so prefer summary over text
            displayContent = getContentPreferringSummary(readMoreLink);
        }

        return HTMLSanitizer.conditionallySanitize(displayContent);
    }

    /**
     * Get the right transformed display content for permalink view.
     *
     * @return Display content HTML
     */
    public String getDisplayContent() {
        return displayContent(null);
    }

    /**
     * Return the Title of this post, or the first 255 characters of the
     * entry's text.
     *
     * @return Display title
     */
    public String getDisplayTitle() {
        if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            return StringUtils.left(Utilities.removeHTML(entry.getText()), RollerConstants.TEXTWIDTH_255);
        }
        return Utilities.removeHTML(entry.getTitle());
    }

    /**
     * Return RSS 09x style description (escaped HTML version of entry text)
     *
     * @return Escaped HTML description
     */
    public String getRss09xDescription() {
        return getRss09xDescription(-1);
    }

    /**
     * Return RSS 09x style description (escaped HTML version of entry text)
     *
     * @param maxLength Maximum length, or -1 for no limit
     * @return Escaped HTML description
     */
    public String getRss09xDescription(int maxLength) {
        String ret = StringEscapeUtils.escapeHtml3(entry.getText());
        if (maxLength != -1 && ret.length() > maxLength) {
            ret = ret.substring(0, maxLength - 3) + "...";
        }
        return ret;
    }

    // -------------------- Private Helper Methods --------------------

    private boolean isPermalinkView(String readMoreLink) {
        return readMoreLink == null || readMoreLink.isBlank() || "nil".equals(readMoreLink);
    }

    private String getContentPreferringText() {
        if (StringUtils.isNotEmpty(entry.getText())) {
            return getTransformedText();
        } else {
            return getTransformedSummary();
        }
    }

    private String getContentPreferringSummary(String readMoreLink) {
        if (StringUtils.isNotEmpty(entry.getSummary())) {
            String content = getTransformedSummary();
            if (StringUtils.isNotEmpty(entry.getText())) {
                content += buildReadMoreLink(readMoreLink);
            }
            return content;
        } else {
            return getTransformedText();
        }
    }

    private String buildReadMoreLink(String readMoreLink) {
        List<String> args = List.of(readMoreLink);
        Weblog weblog = entry.getWebsite();
        if (weblog != null) {
            return I18nMessages.getMessages(weblog.getLocaleInstance())
                    .getString("macro.weblog.readMoreLink", args);
        }
        return "";
    }

    /**
     * Transform string based on plugins enabled for this weblog entry.
     */
    private String render(String str) {
        String ret = str;
        log.debug("Applying page plugins to string");

        Weblog weblog = entry.getWebsite();
        if (weblog == null) {
            return HTMLSanitizer.conditionallySanitize(ret);
        }

        Map<String, WeblogEntryPlugin> plugins = weblog.getInitializedPlugins();
        if (str != null && plugins != null) {
            List<String> entryPlugins = entry.getPluginsList();

            if (entryPlugins != null && !entryPlugins.isEmpty()) {
                ret = applyPlugins(ret, plugins, entryPlugins);
            }
        }
        return HTMLSanitizer.conditionallySanitize(ret);
    }

    private String applyPlugins(String content, Map<String, WeblogEntryPlugin> plugins, 
                                List<String> entryPlugins) {
        String result = content;
        for (Map.Entry<String, WeblogEntryPlugin> pluginEntry : plugins.entrySet()) {
            if (entryPlugins.contains(pluginEntry.getKey())) {
                WeblogEntryPlugin plugin = pluginEntry.getValue();
                try {
                    result = plugin.render(entry, result);
                } catch (Exception e) {
                    log.error("ERROR from plugin: " + plugin.getName(), e);
                }
            }
        }
        return result;
    }
}
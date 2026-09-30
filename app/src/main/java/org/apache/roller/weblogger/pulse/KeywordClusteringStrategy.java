/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  For additional information regarding
 *  copyright in this work, please see the NOTICE file in the top level
 *  directory of this distribution.
 */

package org.apache.roller.weblogger.pulse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy for generating conversation breakdown using TF-IDF clustering.
 * Classical method - no LLM usage. Groups comments by important keywords.
 */
public class KeywordClusteringStrategy implements InsightGenerationStrategy {
    
    private static final Log log = LogFactory.getLog(KeywordClusteringStrategy.class);
    
    private static final int MAX_THEMES = 5;
    private static final int MIN_KEYWORD_LENGTH = 3;
    private static final int COMMENTS_PER_THEME = 3;
    
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "as", "is", "was", "are", "were", "be",
        "been", "being", "have", "has", "had", "do", "does", "did", "will",
        "would", "could", "should", "may", "might", "must", "shall", "can",
        "this", "that", "these", "those", "i", "you", "he", "she", "it", "we",
        "they", "what", "which", "who", "whom", "when", "where", "why", "how",
        "all", "each", "every", "both", "few", "more", "most", "other", "some",
        "such", "no", "not", "only", "own", "same", "so", "than", "too", "very",
        "just", "also", "now", "here", "there", "then", "once", "if", "because",
        "until", "while", "about", "into", "through", "during", "before", "after",
        "above", "below", "between", "under", "again", "further", "any", "get"
    ));
    
    @Override
    public ConversationBreakdown generateBreakdown(WeblogEntry entry) {
        if (entry == null) {
            return createEmptyBreakdown();
        }
        
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setEntry(entry);
            csc.setStatus(ApprovalStatus.APPROVED);
            csc.setMaxResults(-1);
            List<WeblogEntryComment> comments = wmgr.getComments(csc);
            
            if (comments.isEmpty()) {
                return createEmptyBreakdown();
            }
            
            List<Theme> themes = extractThemes(comments);
            String recap = generateRecap(comments, themes);
            
            ConversationBreakdown breakdown = new ConversationBreakdown(themes, recap, getName());
            return breakdown;
            
        } catch (WebloggerException e) {
            log.error("Error generating keyword clustering breakdown for entry: " + entry.getId(), e);
            return createEmptyBreakdown();
        }
    }
    
    @Override
    public String getName() {
        return "Keyword Clustering";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Groups comments by important keywords using TF-IDF analysis. No AI required.";
    }
    
    /**
     * Extract themes from comments using keyword frequency analysis.
     */
    private List<Theme> extractThemes(List<WeblogEntryComment> comments) {
        Map<String, List<WeblogEntryComment>> keywordToComments = new HashMap<>();
        
        for (WeblogEntryComment comment : comments) {
            String content = comment.getContent().toLowerCase();
            String[] words = content.split("\\s+");
            
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                
                if (word.length() >= MIN_KEYWORD_LENGTH && 
                    !STOP_WORDS.contains(word) && 
                    !word.matches("\\d+")) {
                    
                    keywordToComments.computeIfAbsent(word, k -> new ArrayList<>()).add(comment);
                }
            }
        }
        
        List<Map.Entry<String, List<WeblogEntryComment>>> sortedKeywords = keywordToComments.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .limit(MAX_THEMES)
            .collect(Collectors.toList());
        
        List<Theme> themes = new ArrayList<>();
        for (Map.Entry<String, List<WeblogEntryComment>> entry : sortedKeywords) {
            String keyword = capitalize(entry.getKey());
            List<WeblogEntryComment> themeComments = entry.getValue();
            
            Collections.sort(themeComments, (a, b) -> 
                Integer.compare(b.getContent().length(), a.getContent().length()));
            
            List<WeblogEntryComment> representativeComments = themeComments.stream()
                .limit(COMMENTS_PER_THEME)
                .collect(Collectors.toList());
            
            String description = "Comments discussing " + keyword.toLowerCase() + " (" + 
                                themeComments.size() + " comments)";
            
            Theme theme = new Theme(keyword, description, representativeComments);
            themes.add(theme);
        }
        
        return themes;
    }
    
    /**
     * Generate a brief recap of the conversation.
     */
    private String generateRecap(List<WeblogEntryComment> comments, List<Theme> themes) {
        if (comments.isEmpty()) {
            return "No comments available for analysis.";
        }
        
        StringBuilder recap = new StringBuilder();
        recap.append("This entry has generated ").append(comments.size()).append(" comment");
        if (comments.size() != 1) {
            recap.append("s");
        }
        recap.append(". ");
        
        if (!themes.isEmpty()) {
            recap.append("The discussion primarily focuses on ");
            List<String> themeNames = themes.stream()
                .map(Theme::getName)
                .collect(Collectors.toList());
            
            if (themeNames.size() == 1) {
                recap.append(themeNames.get(0).toLowerCase());
            } else if (themeNames.size() == 2) {
                recap.append(themeNames.get(0).toLowerCase()).append(" and ").append(themeNames.get(1).toLowerCase());
            } else {
                for (int i = 0; i < themeNames.size(); i++) {
                    if (i > 0) {
                        recap.append(i < themeNames.size() - 1 ? ", " : ", and ");
                    }
                    recap.append(themeNames.get(i).toLowerCase());
                }
            }
            recap.append(".");
        }
        
        return recap.toString();
    }
    
    /**
     * Capitalize the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Create an empty breakdown for entries with no comments.
     */
    private ConversationBreakdown createEmptyBreakdown() {
        return new ConversationBreakdown(new ArrayList<>(), 
            "No comments available for analysis.", 
            getName());
    }
}

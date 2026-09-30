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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Calculator for generating lightweight discussion overview indicators.
 * Uses only classical methods (keyword frequency, basic statistics) - no LLM/DL.
 */
public class DiscussionOverviewCalculator {
    
    private static final Log log = LogFactory.getLog(DiscussionOverviewCalculator.class);
    
    private static final int MAX_KEYWORDS = 10;
    private static final int MIN_KEYWORD_LENGTH = 3;
    
    private static final Pattern QUESTION_PATTERN = Pattern.compile(".*[?？].*");
    private static final Set<String> FEEDBACK_KEYWORDS = new HashSet<>(Arrays.asList(
        "good", "great", "excellent", "bad", "poor", "nice", "love", "hate", 
        "like", "dislike", "awesome", "terrible", "amazing", "helpful", "useful",
        "interesting", "boring", "thanks", "thank", "appreciate"
    ));
    private static final Set<String> DEBATE_KEYWORDS = new HashSet<>(Arrays.asList(
        "disagree", "agree", "however", "but", "actually", "wrong", "correct",
        "think", "believe", "opinion", "perspective", "view", "point", "argument",
        "counter", "response", "reply", "contrary", "instead", "although"
    ));
    
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
    
    /**
     * Calculate discussion overview for a weblog entry.
     */
    public DiscussionOverview calculate(WeblogEntry entry) {
        if (entry == null) {
            return createEmptyOverview();
        }
        
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setEntry(entry);
            csc.setStatus(ApprovalStatus.APPROVED);
            csc.setMaxResults(-1);
            List<WeblogEntryComment> comments = wmgr.getComments(csc);
            
            if (comments.isEmpty()) {
                return createEmptyOverview();
            }
            
            int activityLevel = calculateActivityLevel(comments);
            Map<String, Integer> responseTypes = categorizeResponseTypes(comments);
            List<KeywordFrequency> recurringPoints = extractRecurringPoints(comments);
            
            return new DiscussionOverview(activityLevel, responseTypes, recurringPoints);
            
        } catch (WebloggerException e) {
            log.error("Error calculating discussion overview for entry: " + entry.getId(), e);
            return createEmptyOverview();
        }
    }
    
    /**
     * Calculate activity level - total number of approved comments.
     */
    private int calculateActivityLevel(List<WeblogEntryComment> comments) {
        return comments.size();
    }
    
    /**
     * Categorize comments into response types: questions, feedback, debate, other.
     * Uses keyword matching and pattern detection.
     */
    private Map<String, Integer> categorizeResponseTypes(List<WeblogEntryComment> comments) {
        Map<String, Integer> types = new HashMap<>();
        types.put("questions", 0);
        types.put("feedback", 0);
        types.put("debate", 0);
        types.put("other", 0);
        
        for (WeblogEntryComment comment : comments) {
            String content = comment.getContent().toLowerCase();
            
            boolean isQuestion = QUESTION_PATTERN.matcher(content).matches();
            boolean isFeedback = containsAny(content, FEEDBACK_KEYWORDS);
            boolean isDebate = containsAny(content, DEBATE_KEYWORDS);
            
            if (isQuestion) {
                types.put("questions", types.get("questions") + 1);
            } else if (isFeedback && !isDebate) {
                types.put("feedback", types.get("feedback") + 1);
            } else if (isDebate) {
                types.put("debate", types.get("debate") + 1);
            } else {
                types.put("other", types.get("other") + 1);
            }
        }
        
        return types;
    }
    
    /**
     * Extract recurring points using keyword frequency analysis.
     * Returns top N most frequent meaningful keywords.
     */
    private List<KeywordFrequency> extractRecurringPoints(List<WeblogEntryComment> comments) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (WeblogEntryComment comment : comments) {
            String content = comment.getContent().toLowerCase();
            String[] words = content.split("\\s+");
            
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                
                if (word.length() >= MIN_KEYWORD_LENGTH && 
                    !STOP_WORDS.contains(word) && 
                    !word.matches("\\d+")) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        return wordFrequency.entrySet().stream()
            .map(entry -> new KeywordFrequency(entry.getKey(), entry.getValue()))
            .sorted()
            .limit(MAX_KEYWORDS)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if text contains any of the given keywords.
     */
    private boolean containsAny(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create an empty overview for entries with no comments.
     */
    private DiscussionOverview createEmptyOverview() {
        Map<String, Integer> emptyTypes = new HashMap<>();
        emptyTypes.put("questions", 0);
        emptyTypes.put("feedback", 0);
        emptyTypes.put("debate", 0);
        emptyTypes.put("other", 0);
        
        return new DiscussionOverview(0, emptyTypes, new ArrayList<>());
    }
}

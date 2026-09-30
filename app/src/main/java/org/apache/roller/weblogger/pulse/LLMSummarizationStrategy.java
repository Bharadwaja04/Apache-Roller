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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for generating conversation breakdown using LLM (Groq API).
 * Uses AI to identify themes, summarize conversation, and select representative comments.
 */
public class LLMSummarizationStrategy implements InsightGenerationStrategy {
    
    private static final Log log = LogFactory.getLog(LLMSummarizationStrategy.class);
    
    private GroqAPIClient apiClient;
    
    public LLMSummarizationStrategy() {
        this.apiClient = new GroqAPIClient();
    }
    
    public LLMSummarizationStrategy(GroqAPIClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @Override
    public ConversationBreakdown generateBreakdown(WeblogEntry entry) {
        if (entry == null) {
            return createEmptyBreakdown();
        }
        
        if (!isAvailable()) {
            System.out.println("=== LLM STRATEGY NOT AVAILABLE ===");
            System.out.println("API Client configured: " + (apiClient != null));
            System.out.println("API Key configured: " + (apiClient != null && apiClient.isConfigured()));
            log.warn("LLM strategy not available - falling back to keyword clustering");
            return new KeywordClusteringStrategy().generateBreakdown(entry);
        }
        
        System.out.println("=== LLM STRATEGY AVAILABLE - USING AI ===");
        log.info("LLM strategy is available - using AI for analysis");
        
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
            
            List<String> commentTexts = comments.stream()
                .map(WeblogEntryComment::getContent)
                .collect(Collectors.toList());
            
            GroqAPIClient.AnalysisResponse response = apiClient.analyzeComments(commentTexts);
            
            System.out.println("=== LLM API RESPONSE ===");
            System.out.println("Response success: " + response.isSuccess());
            System.out.println("Response summary: " + response.getSummary());
            System.out.println("Themes count: " + (response.getThemes() != null ? response.getThemes().size() : 0));
            
            if (!response.isSuccess()) {
                System.out.println("LLM analysis failed: " + response.getSummary());
                log.warn("LLM analysis failed: " + response.getSummary());
                return new KeywordClusteringStrategy().generateBreakdown(entry);
            }
            
            List<Theme> themes = convertToThemes(response.getThemes(), comments);
            String recap = response.getSummary();
            
            ConversationBreakdown breakdown = new ConversationBreakdown(themes, recap, getName());
            return breakdown;
            
        } catch (WebloggerException e) {
            log.error("Error generating LLM breakdown for entry: " + entry.getId(), e);
            return createEmptyBreakdown();
        }
    }
    
    @Override
    public String getName() {
        return "LLM Summarization";
    }
    
    @Override
    public boolean isAvailable() {
        return apiClient != null && apiClient.isConfigured();
    }
    
    @Override
    public String getDescription() {
        return "Uses AI (Groq API) to analyze comments, identify themes, and provide summaries.";
    }
    
    /**
     * Convert API response themes to Theme POJOs with representative comments.
     */
    private List<Theme> convertToThemes(List<GroqAPIClient.ThemeInfo> themeInfos, 
                                         List<WeblogEntryComment> comments) {
        List<Theme> themes = new ArrayList<>();
        
        if (themeInfos == null || themeInfos.isEmpty()) {
            return themes;
        }
        
        for (GroqAPIClient.ThemeInfo themeInfo : themeInfos) {
            List<WeblogEntryComment> representativeComments = new ArrayList<>();
            
            if (themeInfo.getCommentIndices() != null) {
                for (Integer index : themeInfo.getCommentIndices()) {
                    if (index >= 0 && index < comments.size()) {
                        representativeComments.add(comments.get(index));
                    }
                }
            }
            
            Theme theme = new Theme(
                themeInfo.getName(),
                themeInfo.getDescription(),
                representativeComments
            );
            themes.add(theme);
        }
        
        return themes;
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

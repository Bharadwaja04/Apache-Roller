/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.roller.weblogger.chatbot.strategies;

import org.apache.roller.weblogger.chatbot.ChatbotAnswerStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import static org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) strategy.
 * Searches weblog entries and uses them as context for AI responses.
 */
public class RAGStrategy implements ChatbotAnswerStrategy {
    
    private static final Log log = LogFactory.getLog(RAGStrategy.class);
    
    // TODO: Move this to configuration
    private static final String GROQ_API_KEY = resolveApiKey();
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    
    @Override
    public String answerQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Please ask me a question about the weblog content!";
        }
        
        try {
            log.info("RAG Strategy: Processing question: " + question);
            
            // Step 1: Search weblog entries for relevant content
            String context = searchWeblogEntries(question);
            
            // Step 2: Always generate answer using Groq API with context (even if empty)
            String answer = generateAnswerWithGroq(question, context);
            
            log.info("RAG Strategy: Generated answer successfully");
            return answer;
            
        } catch (Exception e) {
            log.error("Error in RAG strategy", e);
            return "Sorry, I encountered an error while processing your question: " + e.getMessage();
        }
    }
    
    /**
     * Search weblog entries for content relevant to the question.
     */
    private String searchWeblogEntries(String question) {
        try {
            log.info("Searching weblog entries for: " + question);
            
            WeblogEntryManager weblogManager = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            // Create search criteria for published entries
            WeblogEntrySearchCriteria criteria = new WeblogEntrySearchCriteria();
            criteria.setStatus(PubStatus.PUBLISHED);
            criteria.setMaxResults(10);
            
            // Simple keyword search - get recent published entries
            List<WeblogEntry> entries = weblogManager.getWeblogEntries(criteria);
            
            // If no entries found or very few entries, use recent entries as context
            List<String> relevantContent = new ArrayList<>();
            String[] keywords = question.toLowerCase().split("\\s+");
            
            for (WeblogEntry entry : entries) {
                if (entry.getText() != null) {
                    String entryText = entry.getText().toLowerCase();
                    boolean relevant = false;
                    
                    // Check if any keywords appear in the entry
                    for (String keyword : keywords) {
                        if (entryText.contains(keyword) && keyword.length() > 2) { // Skip very short keywords
                            relevant = true;
                            break;
                        }
                    }
                    
                    // If no relevant entries found, use recent entries anyway for general conversation
                    if (relevant || relevantContent.size() == 0) {
                        // Truncate very long entries
                        String content = entry.getText();
                        if (content.length() > 500) {
                            content = content.substring(0, 500) + "...";
                        }
                        relevantContent.add(content);
                        
                        // Limit context to prevent token overflow
                        if (relevantContent.size() >= 3) {
                            break;
                        }
                    }
                }
            }
            
            String context = relevantContent.stream().collect(Collectors.joining("\n\n"));
            log.info("Using " + relevantContent.size() + " entries as context");
            
            return context;
            
        } catch (Exception e) {
            log.error("Error searching weblog entries", e);
            return "";
        }
    }
    
    /**
     * Generate answer using Groq API with retrieved context.
     */
    private String generateAnswerWithGroq(String question, String context) {
        try {
            log.info("Generating answer with Groq API, context length: " + context.length());
            
            // Create prompt with weblog context
            String prompt;
            if (context.isEmpty()) {
                // No weblog content found - be honest about it
                prompt = "You are a helpful assistant for a weblog. " +
                         "The user asked: '" + question + "'. " +
                         "However, I couldn't find any published blog posts in this weblog to answer their question. " +
                         "Please explain that you don't have access to any weblog content and suggest they create some blog posts first.";
            } else {
                // Use weblog content as context
                prompt = "You are a helpful assistant for a weblog. " +
                         "Here are some relevant blog posts from this weblog:\n\n" + context + "\n\n" +
                         "Based on these blog posts, please answer the user's question: '" + question + "'. " +
                         "Provide a helpful answer based on the weblog content. If the content doesn't fully answer the question, say so and provide what help you can.";
            }
            
            // Create JSON request body with proper escaping
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            String jsonBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 300, \"temperature\": 0.7}",
                MODEL,
                escapedPrompt
            );
            
            log.info("Groq API request body length: " + jsonBody.length());
            log.info("Prompt preview: " + escapedPrompt.substring(0, Math.min(100, escapedPrompt.length())) + "...");
            log.info("API URL: " + GROQ_API_URL);
            log.info("Model: " + MODEL);
            
            // Create HTTP client with simpler setup
            HttpClient client = HttpClient.newHttpClient();
            
            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                log.info("Groq API response received successfully");
                
                // Parse JSON response to extract the answer
                // Simple parsing - in production, use a proper JSON library
                String answer = extractAnswerFromResponse(responseBody);
                return answer;
            } else {
                String errorBody = response.body();
                log.error("Groq API error: " + response.statusCode() + " " + errorBody);
                
                // Try to provide more helpful error message
                if (response.statusCode() == 400) {
                    return "Sorry, the AI service request was invalid. This might be due to a long prompt or special characters.";
                } else if (response.statusCode() == 401) {
                    return "Sorry, there's an issue with the AI service authentication.";
                } else if (response.statusCode() == 429) {
                    return "Sorry, the AI service is rate limited. Please try again later.";
                } else {
                    return "Sorry, I encountered an error with the AI service: " + response.statusCode();
                }
            }
            
        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            
            // Simple fallback so you can see what's happening
            return "I received your question: '" + question + "'. There was an API error: " + errorMessage;
        }
    }
    
    /**
     * Extract the answer from Groq API response.
     */
    private String extractAnswerFromResponse(String responseBody) {
        try {
            // Simple JSON parsing to extract content from Groq API
            int contentStart = responseBody.indexOf("\"content\":\"");
            if (contentStart == -1) {
                log.error("Could not find content in response: " + responseBody);
                return "Sorry, I couldn't parse the AI response. Content not found.";
            }
            
            int contentValueStart = contentStart + 11; // Length of "content":"
            
            // Find the end of content, handling escaped quotes properly
            int contentEnd = contentValueStart;
            while (contentEnd < responseBody.length()) {
                char c = responseBody.charAt(contentEnd);
                if (c == '"' && responseBody.charAt(contentEnd - 1) != '\\') {
                    break; // Found unescaped quote
                }
                contentEnd++;
            }
            
            if (contentEnd >= responseBody.length()) {
                log.error("Could not find end of content: " + responseBody);
                return "Sorry, I couldn't parse the AI response. Content end not found.";
            }
            
            String content = responseBody.substring(contentValueStart, contentEnd);
            
            // Handle escaped characters
            content = content.replace("\\n", "\n")
                           .replace("\\\"", "\"")
                           .replace("\\\\", "\\");
            
            log.info("Extracted content: " + content);
            return content;
            
        } catch (Exception e) {
            log.error("Error parsing response: " + responseBody, e);
            return "Sorry, I couldn't parse the AI response. Error: " + e.getMessage();
        }
    }
    
    @Override
    public String getName() {
        return "RAG Strategy";
    }
    
    @Override
    public String getDescription() {
        return "Searches weblog entries and uses AI to generate contextual answers based on the content.";
    }
    
    @Override
    public boolean isAvailable() {
        // Check if Groq API key is configured
        return GROQ_API_KEY != null && !GROQ_API_KEY.equals("gsk_...") && GROQ_API_KEY.startsWith("gsk_");
    }

    /** API key comes from the environment (GROQ_API_KEY) or the -Dgroq.api.key system property; never from source. */
    private static String resolveApiKey() {
        String key = System.getenv("GROQ_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = System.getProperty("groq.api.key", "");
        }
        return key.trim();
    }
}

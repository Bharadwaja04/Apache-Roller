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

package org.apache.roller.weblogger.chatbot;

import org.apache.roller.weblogger.chatbot.strategies.RAGStrategy;
import org.apache.roller.weblogger.chatbot.strategies.LongContextStrategy;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing chatbot answering strategies.
 */
public class ChatbotService {
    
    private Map<String, ChatbotAnswerStrategy> strategies;
    private String defaultStrategy;
    
    public ChatbotService() {
        initializeStrategies();
    }
    
    private void initializeStrategies() {
        strategies = new HashMap<>();
        
        // Initialize RAG strategy
        RAGStrategy ragStrategy = new RAGStrategy();
        strategies.put(ragStrategy.getName().toLowerCase(), ragStrategy);
        
        // Initialize Long Context strategy
        LongContextStrategy longContextStrategy = new LongContextStrategy();
        strategies.put(longContextStrategy.getName().toLowerCase(), longContextStrategy);
        
        // Set default strategy to rag
        defaultStrategy = "rag";
    }
    
    /**
     * Get answer using the specified strategy.
     * 
     * @param question The user's question
     * @param strategyKey The strategy to use
     * @return The answer
     */
    public String getAnswer(String question, String strategyKey) {
        if (question == null || question.trim().isEmpty()) {
            return "Please provide a valid question.";
        }
        
        ChatbotAnswerStrategy strategy = getStrategy(strategyKey);
        if (strategy == null || !strategy.isAvailable()) {
            strategy = getStrategy(defaultStrategy);
        }
        
        if (strategy != null) {
            return strategy.answerQuestion(question);
        }
        
        return "Sorry, no chatbot strategies are available.";
    }
    
    /**
     * Get strategy by key.
     * 
     * @param strategyKey The strategy key
     * @return The strategy or null if not found
     */
    private ChatbotAnswerStrategy getStrategy(String strategyKey) {
        if (strategyKey == null) {
            return null;
        }
        return strategies.get(strategyKey);
    }
    
    /**
     * Get available strategies for UI.
     * 
     * @return Map of strategy information
     */
    public Map<String, Object> getAvailableStrategies() {
        Map<String, Object> result = new HashMap<>();
        
        for (Map.Entry<String, ChatbotAnswerStrategy> entry : strategies.entrySet()) {
            ChatbotAnswerStrategy strategy = entry.getValue();
            Map<String, Object> info = new HashMap<>();
            info.put("name", strategy.getName());
            info.put("description", strategy.getDescription());
            info.put("available", strategy.isAvailable());
            result.put(entry.getKey(), info);
        }
        
        return result;
    }
    
    /**
     * Get the default strategy key.
     * 
     * @return Default strategy key
     */
    public String getDefaultStrategy() {
        return defaultStrategy;
    }
}

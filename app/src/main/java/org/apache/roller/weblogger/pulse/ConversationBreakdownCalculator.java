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
import org.apache.roller.weblogger.pojos.WeblogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for generating conversation breakdown using different strategies.
 * Supports switching between classical (keyword clustering) and LLM-based methods.
 */
public class ConversationBreakdownCalculator {
    
    private static Log log = org.apache.commons.logging.LogFactory.getLog(ConversationBreakdownCalculator.class);
    
    private List<InsightGenerationStrategy> strategies;
    
    public ConversationBreakdownCalculator() {
        this.strategies = new ArrayList<>();
        registerDefaultStrategies();
    }
    
    /**
     * Register default strategies.
     */
    private void registerDefaultStrategies() {
        strategies.add(new KeywordClusteringStrategy());
        strategies.add(new LLMSummarizationStrategy());
    }
    
    /**
     * Generate conversation breakdown using the specified strategy.
     * 
     * @param entry The weblog entry to analyze
     * @param strategyName The name of the strategy to use
     * @return ConversationBreakdown containing themes and recap
     */
    public ConversationBreakdown generateBreakdown(WeblogEntry entry, String strategyName) {
        if (entry == null) {
            return createEmptyBreakdown();
        }
        
        System.out.println("=== BREAKDOWN CALCULATOR ===");
        System.out.println("Requested strategy: " + strategyName);
        System.out.println("Available strategies:");
        for (InsightGenerationStrategy s : strategies) {
            System.out.println("  - " + s.getName() + " (available: " + s.isAvailable() + ")");
        }
        
        InsightGenerationStrategy strategy = findStrategy(strategyName);
        
        if (strategy == null) {
            log.warn("Strategy not found: " + strategyName + ", using default");
            strategy = getDefaultStrategy();
        }
        
        if (!strategy.isAvailable()) {
            log.warn("Strategy not available: " + strategyName + ", using fallback");
            strategy = getFallbackStrategy();
        }
        
        System.out.println("Selected strategy: " + strategy.getName());
        System.out.println("=== END BREAKDOWN CALCULATOR ===");
        
        return strategy.generateBreakdown(entry);
    }
    
    /**
     * Generate conversation breakdown using the default strategy.
     * 
     * @param entry The weblog entry to analyze
     * @return ConversationBreakdown containing themes and recap
     */
    public ConversationBreakdown generateBreakdown(WeblogEntry entry) {
        return generateBreakdown(entry, getDefaultStrategy().getName());
    }
    
    /**
     * Get all available strategies.
     * 
     * @return List of available strategies
     */
    public List<InsightGenerationStrategy> getAvailableStrategies() {
        List<InsightGenerationStrategy> available = new ArrayList<>();
        for (InsightGenerationStrategy strategy : strategies) {
            if (strategy.isAvailable()) {
                available.add(strategy);
            }
        }
        return available;
    }
    
    /**
     * Get all registered strategies (including unavailable ones).
     * 
     * @return List of all strategies
     */
    public List<InsightGenerationStrategy> getAllStrategies() {
        return new ArrayList<>(strategies);
    }
    
    /**
     * Get the default strategy (prefer LLM if available, otherwise keyword clustering).
     * 
     * @return The default strategy
     */
    public InsightGenerationStrategy getDefaultStrategy() {
        for (InsightGenerationStrategy strategy : strategies) {
            if (strategy.isAvailable() && strategy.getName().equals("LLM Summarization")) {
                return strategy;
            }
        }
        return getFallbackStrategy();
    }
    
    /**
     * Get the fallback strategy (always available).
     * 
     * @return The fallback strategy
     */
    public InsightGenerationStrategy getFallbackStrategy() {
        for (InsightGenerationStrategy strategy : strategies) {
            if (strategy.isAvailable()) {
                return strategy;
            }
        }
        return strategies.get(0);
    }
    
    /**
     * Find a strategy by name.
     * 
     * @param strategyName The name of the strategy
     * @return The strategy, or null if not found
     */
    public InsightGenerationStrategy findStrategy(String strategyName) {
        if (strategyName == null || strategyName.isEmpty()) {
            return null;
        }
        
        for (InsightGenerationStrategy strategy : strategies) {
            if (strategy.getName().equalsIgnoreCase(strategyName)) {
                return strategy;
            }
        }
        return null;
    }
    
    /**
     * Register a custom strategy.
     * 
     * @param strategy The strategy to register
     */
    public void registerStrategy(InsightGenerationStrategy strategy) {
        if (strategy != null && !strategies.contains(strategy)) {
            strategies.add(strategy);
        }
    }
    
    /**
     * Create an empty breakdown.
     */
    private ConversationBreakdown createEmptyBreakdown() {
        return new ConversationBreakdown(new ArrayList<>(), 
            "No comments available for analysis.", 
            "None");
    }
}

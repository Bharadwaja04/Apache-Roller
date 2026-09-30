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

import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Strategy interface for generating conversation breakdown insights.
 * Allows switching between different analysis methods (classical vs LLM-based).
 */
public interface InsightGenerationStrategy {
    
    /**
     * Generate a conversation breakdown for a weblog entry.
     * 
     * @param entry The weblog entry to analyze
     * @return ConversationBreakdown containing themes, representative comments, and recap
     */
    ConversationBreakdown generateBreakdown(WeblogEntry entry);
    
    /**
     * Get the name of this strategy for display purposes.
     * 
     * @return Strategy name (e.g., "Keyword Clustering", "LLM Summarization")
     */
    String getName();
    
    /**
     * Check if this strategy is available for use.
     * For example, LLM-based strategies may require API keys.
     * 
     * @return true if the strategy can be used, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Get a description of this strategy for UI display.
     * 
     * @return Description of how this strategy works
     */
    String getDescription();
}

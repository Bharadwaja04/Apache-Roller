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

/**
 * Strategy interface for different chatbot answering approaches.
 */
public interface ChatbotAnswerStrategy {
    
    /**
     * Generate a response to the given question.
     * 
     * @param question The user's question
     * @return The chatbot's response
     */
    String answerQuestion(String question);
    
    /**
     * Get the name of this strategy.
     * 
     * @return Strategy name
     */
    String getName();
    
    /**
     * Get description of this strategy.
     * 
     * @return Strategy description
     */
    String getDescription();
    
    /**
     * Check if this strategy is available.
     * 
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}

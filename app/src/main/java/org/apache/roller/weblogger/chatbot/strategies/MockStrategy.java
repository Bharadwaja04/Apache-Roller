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

/**
 * Mock strategy for testing and development.
 * Returns predefined responses.
 */
public class MockStrategy implements ChatbotAnswerStrategy {
    
    @Override
    public String answerQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Please ask me a question!";
        }
        
        // Simple mock responses based on keywords
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("hello") || lowerQuestion.contains("hi")) {
            return "Hello! I'm a mock chatbot. How can I help you today?";
        } else if (lowerQuestion.contains("how are you")) {
            return "I'm doing great, thanks for asking! I'm a mock strategy for testing.";
        } else if (lowerQuestion.contains("what is your name")) {
            return "I'm MockStrategy, a simple chatbot for testing the Roller Weblogger system.";
        } else if (lowerQuestion.contains("bye")) {
            return "Goodbye! Thanks for chatting with me!";
        } else {
            return "This is a mock response to: '" + question + "'. I'm a simple test strategy.";
        }
    }
    
    @Override
    public String getName() {
        return "Mock Strategy";
    }
    
    @Override
    public String getDescription() {
        return "Mock strategy for testing and development. Returns predefined responses.";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}

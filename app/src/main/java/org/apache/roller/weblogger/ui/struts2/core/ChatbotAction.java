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

package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.chatbot.ChatbotService;
import java.util.Map;

/**
 * Simple ChatbotAction for Step 1 testing.
 * Returns basic responses to verify Struts2 setup.
 */
public class ChatbotAction extends UIAction {
    
    private static final Log log = LogFactory.getLog(ChatbotAction.class);
    
    private String question;
    private String strategy;
    private String response;
    private java.io.InputStream inputStream;
    private ChatbotService chatbotService;
    
    public ChatbotAction() {
        log.info("ChatbotAction constructor called");
        this.actionName = "chatbot";
        this.desiredMenu = null;
        this.pageTitle = "Chatbot API";
    }
    
    @Override
    public void myPrepare() {
        log.info("myPrepare() called for ChatbotAction");
        try {
            this.chatbotService = new ChatbotService();
            log.info("ChatbotService initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize ChatbotService", e);
        }
    }
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    @Override
    public String execute() {
        log.info("execute() called with question: " + question);
        
        try {
            // Use ChatbotService for Step 2
            String answer = chatbotService.getAnswer(question, strategy);
            this.response = answer;
            
            log.info("Answer generated: " + answer);
        } catch (Exception e) {
            log.error("Error generating answer", e);
            this.response = "Sorry, I encountered an error: " + e.getMessage();
        }
        
        // Set inputStream for stream result
        this.inputStream = new java.io.ByteArrayInputStream(response.getBytes());
        
        log.info("Response set: " + response);
        log.info("InputStream set successfully");
        return SUCCESS;
    }
    
    /**
     * Get available strategies for the frontend.
     * Returns JSON with strategy information.
     */
    public String getStrategies() {
        log.info("getStrategies() called");
        
        try {
            // Get strategies from service
            Map<String, Object> availableStrategies = chatbotService.getAvailableStrategies();
            String defaultStrategy = chatbotService.getDefaultStrategy();
            
            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("{\"default\":\"").append(defaultStrategy).append("\",");
            json.append("\"strategies\":[");
            
            boolean first = true;
            for (Map.Entry<String, Object> entry : availableStrategies.entrySet()) {
                if (!first) json.append(",");
                first = false;
                
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) entry.getValue();
                
                json.append("{");
                json.append("\"key\":\"").append(entry.getKey()).append("\",");
                json.append("\"name\":\"").append(info.get("name")).append("\",");
                json.append("\"description\":\"").append(info.get("description")).append("\",");
                json.append("\"available\":").append(info.get("available"));
                json.append("}");
            }
            
            json.append("],\"success\":true}");
            
            this.response = json.toString();
            log.info("Strategies JSON generated: " + response);
            
        } catch (Exception e) {
            log.error("Error getting strategies", e);
            this.response = "{\"success\":false,\"error\":\"Failed to get strategies: " + e.getMessage() + "\"}";
        }
        
        // Set inputStream for stream result
        this.inputStream = new java.io.ByteArrayInputStream(response.getBytes());
        
        return SUCCESS;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public java.io.InputStream getInputStream() {
        return this.inputStream;
    }
}

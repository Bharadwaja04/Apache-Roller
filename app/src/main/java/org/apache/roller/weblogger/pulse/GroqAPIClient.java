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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with Groq API for LLM-powered comment analysis.
 * Reads API key from GROQ_API_KEY environment variable.
 */
public class GroqAPIClient {
    
    private static final Log log = LogFactory.getLog(GroqAPIClient.class);
    
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final int MAX_TOKENS = 2048;
    private static final int TEMPERATURE = 0;
    
    private String apiKey;
    
    public GroqAPIClient() {
        this.apiKey = System.getenv("GROQ_API_KEY");
        System.out.println("=== GROQ API CLIENT INIT ===");
        System.out.println("API Key present: " + (apiKey != null && !apiKey.isEmpty()));
        log.info("=== GROQ API CLIENT INIT ===");
        log.info("API Key present: " + (apiKey != null && !apiKey.isEmpty()));
    }
    
    public GroqAPIClient(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Analyze comments using Groq API to extract themes and summary.
     * 
     * @param commentTexts List of comment texts to analyze
     * @return AnalysisResponse containing themes and recap
     */
    public AnalysisResponse analyzeComments(List<String> commentTexts) {
        System.out.println("=== GROQ API ANALYZE COMMENTS ===");
        System.out.println("Comments count: " + (commentTexts != null ? commentTexts.size() : 0));
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("GROQ_API_KEY environment variable not set");
            return createErrorResponse("API key not configured");
        }
        
        if (commentTexts == null || commentTexts.isEmpty()) {
            return createErrorResponse("No comments to analyze");
        }
        
        try {
            String prompt = buildPrompt(commentTexts);
            String requestBody = buildRequestBody(prompt);
            
            System.out.println("=== GROQ API ANALYZE COMMENTS ===");
            System.out.println("Comments count: " + (commentTexts != null ? commentTexts.size() : 0));
            System.out.println("Calling Groq API...");
            System.out.println("URL: " + GROQ_API_URL);
            System.out.println("Model: " + MODEL);
            System.out.println("Request JSON:");
            System.out.println(requestBody);
            System.out.println("--- END JSON ---");
            
            URL url = new URL(GROQ_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                System.out.println("API call successful!");
                return parseResponse(response);
            } else {
                String errorResponse = readErrorResponse(connection);
                System.out.println("API error response: " + errorResponse);
                log.error("Groq API error: " + responseCode + " - " + errorResponse);
                return createErrorResponse("API request failed: " + responseCode);
            }
            
        } catch (Exception e) {
            System.out.println("Exception during API call: " + e.getMessage());
            e.printStackTrace();
            log.error("Error calling Groq API", e);
            return createErrorResponse("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Build the prompt for the LLM.
     */
    private String buildPrompt(List<String> commentTexts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following blog comments and provide:\n");
        prompt.append("1. A short summary (2-3 sentences) of the overall conversation\n");
        prompt.append("2. 3-5 major themes being discussed, each with a brief description\n");
        prompt.append("3. For each theme, identify 2-3 representative comment indices\n\n");
        prompt.append("Comments:\n");
        
        for (int i = 0; i < commentTexts.size(); i++) {
            prompt.append("[").append(i).append("] ").append(commentTexts.get(i)).append("\n");
        }
        
        prompt.append("\n\nProvide your response in the following JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"overall summary\",\n");
        prompt.append("  \"themes\": [\n");
        prompt.append("    {\"name\": \"theme name\", \"description\": \"brief description\", \"commentIndices\": [0, 1]}\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Build the request body for Groq API.
     */
    private String buildRequestBody(String prompt) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"model\": \"").append(MODEL).append("\",\n");
        json.append("  \"messages\": [\n");
        json.append("    {\"role\": \"user\", \"content\": \"").append(escapeJson(prompt)).append("\"}\n");
        json.append("  ],\n");
        json.append("  \"max_tokens\": ").append(MAX_TOKENS).append(",\n");
        json.append("  \"temperature\": ").append(TEMPERATURE).append("\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Read the response from the connection.
     */
    private String readResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * Read the error response from the connection.
     */
    private String readErrorResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * Parse the JSON response from Groq API.
     */
    private AnalysisResponse parseResponse(String jsonResponse) {
        try {
            Map<String, Object> responseMap = parseJson(jsonResponse);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                return createErrorResponse("Invalid response format");
            }
            
            Map<String, Object> choice = choices.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");
            
            return parseAnalysisContent(content);
            
        } catch (Exception e) {
            log.error("Error parsing Groq API response", e);
            return createErrorResponse("Error parsing response");
        }
    }
    
    /**
     * Parse the analysis content from LLM response.
     */
    private AnalysisResponse parseAnalysisContent(String content) {
        try {
            content = content.trim();
            
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();
            
            Map<String, Object> analysisMap = parseJson(content);
            
            String summary = (String) analysisMap.get("summary");
            
            List<ThemeInfo> themes = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> themesData = (List<Map<String, Object>>) analysisMap.get("themes");
            
            if (themesData != null) {
                for (Map<String, Object> themeData : themesData) {
                    String name = (String) themeData.get("name");
                    String description = (String) themeData.get("description");
                    
                    @SuppressWarnings("unchecked")
                    List<Object> commentIndicesRaw = (List<Object>) themeData.get("commentIndices");
                    List<Integer> commentIndices = new ArrayList<>();
                    
                    if (commentIndicesRaw != null) {
                        for (Object index : commentIndicesRaw) {
                            if (index instanceof Number) {
                                commentIndices.add(((Number) index).intValue());
                            } else if (index instanceof String) {
                                try {
                                    commentIndices.add(Integer.parseInt((String) index));
                                } catch (NumberFormatException e) {
                                    // Skip invalid indices
                                }
                            }
                        }
                    }
                    
                    themes.add(new ThemeInfo(name, description, commentIndices));
                }
            }
            
            return new AnalysisResponse(summary, themes, true);
            
        } catch (Exception e) {
            log.error("Error parsing analysis content", e);
            return createErrorResponse("Error parsing analysis content");
        }
    }
    
    /**
     * Simple JSON parser for basic parsing needs.
     * This is a simplified implementation - for production, consider using a proper JSON library.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        return new SimpleJsonParser().parse(json);
    }
    
    /**
     * Escape string for JSON.
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Create an error response.
     */
    private AnalysisResponse createErrorResponse(String error) {
        return new AnalysisResponse("Error: " + error, new ArrayList<>(), false);
    }
    
    /**
     * Check if the client is configured with an API key.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Inner class representing analysis response.
     */
    public static class AnalysisResponse {
        private String summary;
        private List<ThemeInfo> themes;
        private boolean success;
        
        public AnalysisResponse(String summary, List<ThemeInfo> themes, boolean success) {
            this.summary = summary;
            this.themes = themes;
            this.success = success;
        }
        
        public String getSummary() {
            return summary;
        }
        
        public List<ThemeInfo> getThemes() {
            return themes;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
    
    /**
     * Inner class representing theme information.
     */
    public static class ThemeInfo {
        private String name;
        private String description;
        private List<Integer> commentIndices;
        
        public ThemeInfo(String name, String description, List<Integer> commentIndices) {
            this.name = name;
            this.description = description;
            this.commentIndices = commentIndices != null ? commentIndices : new ArrayList<>();
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<Integer> getCommentIndices() {
            return commentIndices;
        }
    }
    
    /**
     * Simple JSON parser implementation.
     */
    private static class SimpleJsonParser {
        private int pos = 0;
        private String json;
        
        public Map<String, Object> parse(String json) {
            this.json = json.trim();
            this.pos = 0;
            return parseObject();
        }
        
        private Map<String, Object> parseObject() {
            Map<String, Object> map = new java.util.HashMap<>();
            skipWhitespace();
            if (json.charAt(pos) != '{') {
                throw new RuntimeException("Expected '{'");
            }
            pos++;
            skipWhitespace();
            
            while (pos < json.length() && json.charAt(pos) != '}') {
                String key = parseString();
                skipWhitespace();
                if (json.charAt(pos) != ':') {
                    throw new RuntimeException("Expected ':'");
                }
                pos++;
                skipWhitespace();
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (json.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                }
            }
            pos++;
            return map;
        }
        
        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (json.charAt(pos) != '[') {
                throw new RuntimeException("Expected '['");
            }
            pos++;
            skipWhitespace();
            
            while (pos < json.length() && json.charAt(pos) != ']') {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (json.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                }
            }
            pos++;
            return list;
        }
        
        private Object parseValue() {
            skipWhitespace();
            char c = json.charAt(pos);
            
            if (c == '"') {
                return parseString();
            } else if (c == '{') {
                return parseObject();
            } else if (c == '[') {
                return parseArray();
            } else if (c == 't' || c == 'f') {
                return parseBoolean();
            } else if (c == 'n') {
                return parseNull();
            } else if (Character.isDigit(c) || c == '-') {
                return parseNumber();
            }
            
            throw new RuntimeException("Unexpected character: " + c);
        }
        
        private String parseString() {
            if (json.charAt(pos) != '"') {
                throw new RuntimeException("Expected '\"'");
            }
            pos++;
            StringBuilder sb = new StringBuilder();
            
            while (pos < json.length() && json.charAt(pos) != '"') {
                char c = json.charAt(pos);
                if (c == '\\') {
                    pos++;
                    if (pos < json.length()) {
                        char escaped = json.charAt(pos);
                        switch (escaped) {
                            case 'n': sb.append('\n'); break;
                            case 't': sb.append('\t'); break;
                            case 'r': sb.append('\r'); break;
                            case '"': sb.append('"'); break;
                            case '\\': sb.append('\\'); break;
                            default: sb.append(escaped);
                        }
                    }
                } else {
                    sb.append(c);
                }
                pos++;
            }
            pos++;
            return sb.toString();
        }
        
        private Boolean parseBoolean() {
            if (json.startsWith("true", pos)) {
                pos += 4;
                return true;
            } else if (json.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new RuntimeException("Expected boolean");
        }
        
        private Object parseNull() {
            if (json.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new RuntimeException("Expected null");
        }
        
        private Number parseNumber() {
            int start = pos;
            if (json.charAt(pos) == '-') {
                pos++;
            }
            while (pos < json.length() && (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '.')) {
                pos++;
            }
            String numStr = json.substring(start, pos);
            try {
                if (numStr.contains(".")) {
                    return Double.parseDouble(numStr);
                } else {
                    return Long.parseLong(numStr);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number: " + numStr);
            }
        }
        
        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
    }
}

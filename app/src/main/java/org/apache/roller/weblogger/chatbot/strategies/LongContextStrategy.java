package org.apache.roller.weblogger.chatbot.strategies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.chatbot.ChatbotAnswerStrategy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;

/**
 * Long Context strategy.
 * Maintains conversation history and provides context-aware responses.
 */
public class LongContextStrategy implements ChatbotAnswerStrategy {
    
    private static final Log log = LogFactory.getLog(LongContextStrategy.class);
    
    // TODO: Move this to configuration
    private static final String GROQ_API_KEY = resolveApiKey();
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    
    // Simple conversation history (in production, use a proper session management)
    private static List<String> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 10; // Keep last 10 exchanges
    
    @Override
    public String answerQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Please ask me a question!";
        }
        
        try {
            log.info("Long Context Strategy: Processing question: " + question);
            
            // Add user question to history
            conversationHistory.add("User: " + question);
            
            // Generate answer using Groq API with conversation context
            String answer = generateAnswerWithContext(question);
            
            // Add bot answer to history
            conversationHistory.add("Assistant: " + answer);
            
            // Trim history if too long
            while (conversationHistory.size() > MAX_HISTORY_SIZE * 2) {
                conversationHistory.remove(0);
            }
            
            log.info("Long Context Strategy: Generated answer successfully");
            return answer;
            
        } catch (Exception e) {
            log.error("Error in Long Context strategy", e);
            return "Sorry, I encountered an error while processing your question: " + e.getMessage();
        }
    }
    
    /**
     * Generate answer using Groq API with conversation context and weblog content.
     */
    private String generateAnswerWithContext(String question) {
        try {
            log.info("Generating answer with conversation context and weblog content");
            
            // Get weblog content
            String weblogContext = searchWeblogEntries();
            
            // Create conversation context
            String conversationContext = String.join("\n", conversationHistory);
            
            // Create prompt with both conversation history and weblog content
            String prompt;
            if (weblogContext.isEmpty()) {
                // No weblog content - use conversation only
                prompt = "You are a helpful assistant having a conversation about a weblog. " +
                         "Here's the conversation history:\n" + conversationContext + "\n\n" +
                         "Current question: " + question + "\n\n" +
                         "Note: There are no published blog posts in this weblog yet. " +
                         "Please provide a helpful, context-aware response:";
            } else {
                // Include weblog content
                prompt = "You are a helpful assistant having a conversation about a weblog. " +
                         "Here's the conversation history:\n" + conversationContext + "\n\n" +
                         "Here are relevant blog posts from this weblog:\n" + weblogContext + "\n\n" +
                         "Current question: " + question + "\n\n" +
                         "Please provide a helpful response that considers both the conversation and the weblog content:";
            }
            
            // Create JSON request body
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            String jsonBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 400, \"temperature\": 0.7}",
                MODEL,
                escapedPrompt
            );
            
            // Create HTTP client
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
                String answer = extractAnswerFromResponse(responseBody);
                return answer;
            } else {
                String errorBody = response.body();
                log.error("Groq API error: " + response.statusCode() + " " + errorBody);
                
                if (response.statusCode() == 400) {
                    return "Sorry, the AI service request was invalid.";
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
            return "Sorry, I encountered an error while generating the answer: " + errorMessage;
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
                return "Sorry, I couldn't parse the AI response.";
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
                return "Sorry, I couldn't parse the AI response.";
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
            return "Sorry, I couldn't parse the AI response.";
        }
    }
    
    @Override
    public String getName() {
        return "Long Context";
    }
    
    @Override
    public String getDescription() {
        return "Maintains conversation history for context-aware responses";
    }
    
    @Override
    public boolean isAvailable() {
        return GROQ_API_KEY != null && !GROQ_API_KEY.trim().isEmpty();
    }
    
    /**
     * Clear conversation history (useful for testing or starting fresh).
     */
    public static void clearHistory() {
        conversationHistory.clear();
    }
    
    /**
     * Get current conversation history size.
     */
    public static int getHistorySize() {
        return conversationHistory.size();
    }
    
    /**
     * Search weblog entries for relevant content.
     */
    private String searchWeblogEntries() {
        try {
            log.info("Searching weblog entries for Long Context strategy");
            
            WeblogEntryManager weblogManager = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            // Create search criteria for published entries
            WeblogEntrySearchCriteria criteria = new WeblogEntrySearchCriteria();
            criteria.setStatus(PubStatus.PUBLISHED);
            criteria.setMaxResults(5); // Limit for Long Context strategy
            
            // Get recent published entries
            List<WeblogEntry> entries = weblogManager.getWeblogEntries(criteria);
            
            List<String> relevantContent = new ArrayList<>();
            
            for (WeblogEntry entry : entries) {
                if (entry.getText() != null) {
                    // Include entry title and content
                    String content = "Title: " + entry.getTitle() + "\n" + entry.getText();
                    
                    // Truncate very long entries
                    if (content.length() > 800) {
                        content = content.substring(0, 800) + "...";
                    }
                    relevantContent.add(content);
                }
            }
            
            String context = relevantContent.stream().collect(Collectors.joining("\n\n---\n\n"));
            
            return context;
            
        } catch (Exception e) {
            log.error("Error searching weblog entries", e);
            return "";
        }
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

package org.apache.roller.weblogger.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * OpenAI/Groq-compatible LLM client implementation.
 */
public class LLMClient implements LLMService {

    private static final Log LOG = LogFactory.getLog(LLMClient.class);

    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public LLMClient(String apiUrl, String apiKey, String model) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String sendPrompt(String prompt) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            String jsonBody = buildRequestBody(prompt);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                LOG.error("LLM API returned status " + responseCode + ": " + errorResponse);
                return "";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return parseResponse(response.toString());

        } catch (Exception e) {
            LOG.error("Failed to communicate with LLM API.", e);
            return "";
        }
    }

    private String buildRequestBody(String prompt) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(escapeJson(model)).append("\",");
        json.append("\"messages\":[{");
        json.append("\"role\":\"user\",");
        json.append("\"content\":\"").append(escapeJson(prompt)).append("\"");
        json.append("}],");
        json.append("\"temperature\":0.7,");
        json.append("\"max_tokens\":2048");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 64);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private String parseResponse(String jsonResponse) {
        try {
            String messageMarker = "\"message\"";
            int messageStart = jsonResponse.indexOf(messageMarker);
            if (messageStart == -1) {
                LOG.warn("No 'message' field in LLM response.");
                return "";
            }

            String contentMarker = "\"content\"";
            int contentKeyStart = jsonResponse.indexOf(contentMarker, messageStart);
            if (contentKeyStart == -1) {
                LOG.warn("No 'content' field in LLM response.");
                return "";
            }

            int colonPos = jsonResponse.indexOf(':', contentKeyStart + contentMarker.length());
            if (colonPos == -1) {
                return "";
            }

            int pos = colonPos + 1;
            while (pos < jsonResponse.length() && Character.isWhitespace(jsonResponse.charAt(pos))) {
                pos++;
            }

            if (pos < jsonResponse.length() && jsonResponse.charAt(pos) == 'n') {
                return "";
            }

            if (pos >= jsonResponse.length() || jsonResponse.charAt(pos) != '"') {
                return "";
            }

            pos++; // skip opening quote
            StringBuilder result = new StringBuilder(1024);
            boolean escaped = false;

            while (pos < jsonResponse.length()) {
                char c = jsonResponse.charAt(pos);

                if (escaped) {
                    switch (c) {
                        case '"':  result.append('"'); break;
                        case '\\': result.append('\\'); break;
                        case 'n':  result.append('\n'); break;
                        case 'r':  result.append('\r'); break;
                        case 't':  result.append('\t'); break;
                        case '/':  result.append('/'); break;
                        case 'b':  result.append('\b'); break;
                        case 'f':  result.append('\f'); break;
                        case 'u':
                            if (pos + 4 < jsonResponse.length()) {
                                try {
                                    String hex = jsonResponse.substring(pos + 1, pos + 5);
                                    result.append((char) Integer.parseInt(hex, 16));
                                    pos += 4;
                                } catch (NumberFormatException e) {
                                    result.append("\\u");
                                }
                            }
                            break;
                        default:
                            result.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                }
                pos++;
            }

            return result.toString().trim();

        } catch (Exception e) {
            LOG.error("Failed to parse LLM response.", e);
            return "";
        }
    }
}
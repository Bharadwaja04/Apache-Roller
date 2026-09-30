package org.apache.roller.weblogger.business.translation;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SarvamTranslationProvider implements TranslationProvider {
    private static final Log log = LogFactory.getLog(SarvamTranslationProvider.class);
    
    @Override
    public String getName() {
        return "sarvam";
    }

    @Override
    public String translate(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) return text;
        
        URL url = new URL("http://localhost:3001/api/translate/sarvam");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        
        String escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        String jsonInputString = "{\"text\": \"" + escapedText + "\", \"sourceLang\": \"" + sourceLang + "\", \"targetLang\": \"" + targetLang + "\"}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            log.error("Sarvam local proxy failed with code: " + responseCode);
            return text; 
        }
        
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            String responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            String key = "\"translatedText\":\"";
            int start = responseBody.indexOf(key);
            if (start != -1) {
                start += key.length();
                StringBuilder result = new StringBuilder();
                boolean escaped = false;
                for (int i = start; i < responseBody.length(); i++) {
                    char c = responseBody.charAt(i);
                    if (escaped) {
                        result.append(c);
                        escaped = false;
                    } else if (c == '\\') {
                        result.append(c);
                        escaped = true;
                    } else if (c == '"') {
                        break;
                    } else {
                        result.append(c);
                    }
                }
                return unescapeSimple(result.toString());
            }
        }
        return text;
    }
    
    private String unescapeSimple(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}

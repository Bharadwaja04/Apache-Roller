package org.apache.roller.weblogger.business.translation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyMemoryTranslationProvider implements TranslationProvider {
    private static final Log log = LogFactory.getLog(MyMemoryTranslationProvider.class);

    @Override
    public String getName() {
        return "mymemory";
    }

    @Override
    public String translate(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) return text;
        
        String src = toIsoCode(sourceLang);
        String tgt = toIsoCode(targetLang);
        
        String urlStr = "https://api.mymemory.translated.net/get?q=" 
                + URLEncoder.encode(text, StandardCharsets.UTF_8.name())
                + "&langpair=" + src + "|" + tgt;
                
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            log.error("MyMemory API failed with code: " + responseCode);
            return text; 
        }
        
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            String responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            String key = "\"translatedText\":\"";
            int start = responseBody.indexOf(key);
            if (start != -1) {
                start += key.length();
                int end = responseBody.indexOf("\"", start);
                if (end != -1) {
                    String translated = responseBody.substring(start, end);
                    // MyMemory free tier warning
                    if (translated.contains("MYMEMORY WARNING")) {
                        return text;
                    }
                    return unescapeJson(translated);
                }
            }
        }
        return text;
    }
    
    private String toIsoCode(String bcp47) {
        if (bcp47 == null) return "en";
        if (bcp47.contains("-")) {
            return bcp47.split("-")[0];
        }
        if (bcp47.contains("_")) {
           return bcp47.split("_")[0];
        }
        return bcp47;
    }
    
    private String unescapeJson(String text) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == 'u' && i + 5 < text.length()) {
                    String hex = text.substring(i + 2, i + 6);
                    try {
                        builder.append((char) Integer.parseInt(hex, 16));
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        // loop natively falls through
                    }
                } else if (next == 'n') {
                    builder.append('\n');
                    i += 2;
                    continue;
                } else {
                    builder.append(next);
                    i += 2;
                    continue;
                }
            }
            builder.append(c);
            i++;
        }
        return builder.toString();
    }
}

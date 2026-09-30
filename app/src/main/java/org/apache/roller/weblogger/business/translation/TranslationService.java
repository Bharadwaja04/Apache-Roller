package org.apache.roller.weblogger.business.translation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TranslationService {
    private static final Log log = LogFactory.getLog(TranslationService.class);
    
    private static final TranslationService instance = new TranslationService();
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    private final Map<String, TranslationProvider> providers = new HashMap<>();
    
    private TranslationService() {
        TranslationProvider myMemory = new MyMemoryTranslationProvider();
        TranslationProvider sarvam = new SarvamTranslationProvider();
        providers.put(myMemory.getName(), myMemory);
        providers.put(sarvam.getName(), sarvam);
    }
    
    public static TranslationService getInstance() {
        return instance;
    }
    
    public String translate(String identifier, String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        if (targetLang == null || targetLang.trim().isEmpty()) {
            return text;
        }
        
        if (isSameLanguage(sourceLang, targetLang)) {
            return text;
        }
        
        // Don't translate HTML entities or very short strings blindly if needed, but our DOM walker prevented tags, now backend intercepts raw text
        
        String providerName = TranslationContext.getProviderName();
        if (providerName == null || providerName.trim().isEmpty() || !providers.containsKey(providerName)) {
            providerName = "mymemory"; 
        }
        
        String cacheKey = providerName + "_" + identifier + "_" + targetLang;
        
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        try {
            TranslationProvider provider = providers.get(providerName);
            String translatedText = provider.translate(text, sourceLang, targetLang);
            
            if (translatedText != null && !translatedText.isEmpty() && !translatedText.equals(text)) {
                 cache.put(cacheKey, translatedText);
                 return translatedText;
            }
        } catch (Exception e) {
            log.error("Translation failed for identifier: " + identifier, e);
        }
        
        return text; 
    }
    
    private boolean isSameLanguage(String lang1, String lang2) {
        if (lang1 == null || lang2 == null) return false;
        String code1 = lang1.contains("-") ? lang1.split("-")[0] : 
                       (lang1.contains("_") ? lang1.split("_")[0] : lang1);
        String code2 = lang2.contains("-") ? lang2.split("-")[0] : 
                       (lang2.contains("_") ? lang2.split("_")[0] : lang2);
        return code1.equalsIgnoreCase(code2);
    }
}

package org.apache.roller.weblogger.business.translation;

public class TranslationContext {
    private static final ThreadLocal<String> targetLanguage = new ThreadLocal<>();
    private static final ThreadLocal<String> providerName = new ThreadLocal<>();

    public static void setTargetLanguage(String lang) {
        targetLanguage.set(lang);
    }

    public static String getTargetLanguage() {
        return targetLanguage.get();
    }

    public static void setProviderName(String provider) {
        providerName.set(provider);
    }

    public static String getProviderName() {
        return providerName.get();
    }

    public static void clear() {
        targetLanguage.remove();
        providerName.remove();
    }
}

package org.apache.roller.weblogger.business.translation;

public interface TranslationProvider {
    String getName();
    String translate(String text, String sourceLang, String targetLang) throws Exception;
}

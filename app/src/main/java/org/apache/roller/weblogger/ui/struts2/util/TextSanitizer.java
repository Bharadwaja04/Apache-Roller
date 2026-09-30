package org.apache.roller.weblogger.ui.struts2.util;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

public class TextSanitizer {
    
    public static String cleanExpressions(String s) {
        return (s == null || s.contains("${") || s.contains("%{")) ? "" : s;
    }
    
    public static String cleanTextKey(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return StringEscapeUtils.escapeHtml4(cleanExpressions(s));
    }
    
    public static String cleanTextArg(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return StringEscapeUtils.escapeHtml4(s);
    }
    
    public static List<?> cleanArgs(List<?> args) {
        List<Object> cleanedArgs = new ArrayList<>(args.size());
        for (Object arg : args) {
            cleanedArgs.add(arg instanceof String ? cleanTextArg((String) arg) : arg);
        }
        return cleanedArgs;
    }
}

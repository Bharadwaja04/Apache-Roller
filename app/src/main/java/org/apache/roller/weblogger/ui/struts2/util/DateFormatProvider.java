package org.apache.roller.weblogger.ui.struts2.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatProvider {
    
    private Locale locale;
    
    public DateFormatProvider(Locale locale) {
        this.locale = locale;
    }
    
    public String getShortDateFormat() {
        DateFormat sdf = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        if (sdf instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) sdf).toPattern();
        }
        return "yyyy/MM/dd";
    }
    
    public String getMediumDateFormat() {
        DateFormat sdf = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        if (sdf instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) sdf).toPattern();
        }
        return "MMM dd, yyyy";
    }
}

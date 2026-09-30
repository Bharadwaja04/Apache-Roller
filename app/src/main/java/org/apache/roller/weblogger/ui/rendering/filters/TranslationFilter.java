package org.apache.roller.weblogger.ui.rendering.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.weblogger.business.translation.TranslationContext;

public class TranslationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            
            String lang = req.getParameter("lang");
            String provider = req.getParameter("provider");
            
            if ((lang == null || lang.isEmpty()) && req.getCookies() != null) {
                for (Cookie c : req.getCookies()) {
                    if ("roller_translate_lang".equals(c.getName())) {
                        lang = c.getValue();
                    }
                    if ("roller_translate_provider".equals(c.getName())) {
                        provider = c.getValue();
                    }
                }
            }
            
            if (lang != null && !lang.trim().isEmpty()) {
                TranslationContext.setTargetLanguage(lang);
            }
            if (provider != null && !provider.trim().isEmpty()) {
                TranslationContext.setProviderName(provider);
            }
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            TranslationContext.clear();
        }
    }

    @Override
    public void destroy() {
    }
}

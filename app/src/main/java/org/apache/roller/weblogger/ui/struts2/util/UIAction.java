/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.core.util.menu.Menu;
import org.apache.struts2.interceptor.RequestAware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// REFACTORED: Extracted responsibilities to modular components to address insufficient modularization
// Now uses AuthenticationContext, TextSanitizer, DateFormatProvider, and existing utilities
public abstract class UIAction extends ActionSupport
        implements UIActionPreparable, UISecurityEnforced, RequestAware {
    
    public static final String DENIED = "access-denied";
    
    public static final String LIST = "list";
    
    public static final String CANCEL = "cancel";
    
    // REFACTORED: Delegated user/weblog state management to AuthenticationContext
    private AuthenticationContext authContext = new AuthenticationContext();
    
    // Backward compatibility: allow child classes to set these directly
    protected String actionName;
    protected String desiredMenu;
    
    protected String pageTitle = null;

    protected String salt = null;
    
    // REFACTORED: Delegated date formatting to DateFormatProvider
    private DateFormatProvider dateFormatProvider;
    
    @Override
    public void myPrepare() {
    }
	
    @Override
	public void setRequest(Map<String, Object> map) {
		this.salt = (String) map.get("salt");
	}

	public String getSalt() {
		return salt;
	}
	
    public void setSalt(String salt) {
    }

    @Override
    public boolean isUserRequired() {
        return true;
    }
    
    
    @Override
    public boolean isWeblogRequired() {
        return true;
    }
    
    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.LOGIN);
    }
    
    
    // REFACTORED: Delegated admin permission check to AuthenticationContext
    public boolean isUserIsAdmin() {
        return authContext.isUserIsAdmin();
    }

    public String cancel() {
        return CANCEL;
    }

    public String getSiteURL() {
        return WebloggerRuntimeConfig.getRelativeContextURL();
    }
    
    public String getAbsoluteSiteURL() {
        return WebloggerRuntimeConfig.getAbsoluteContextURL();
    }
    
    public String getProp(String key) {
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? key : value;
    }
    
    public boolean getBooleanProp(String key) {
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? false : Boolean.valueOf(value);
    }
    
    public int getIntProp(String key) {
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? 0 : Integer.valueOf(value);
    }

    // REFACTORED: Delegated text sanitization to TextSanitizer
    @Override
    public String getText(String aTextName) {
        return super.getText(TextSanitizer.cleanTextKey(aTextName));
    }

    @Override
    public String getText(String aTextName, String defaultValue) {
        return super.getText(TextSanitizer.cleanTextKey(aTextName), TextSanitizer.cleanTextKey(defaultValue));
    }

    @Override
    public String getText(String aTextName, String defaultValue, String obj) {
        return super.getText(TextSanitizer.cleanTextKey(aTextName), TextSanitizer.cleanTextKey(defaultValue), TextSanitizer.cleanTextArg(obj));
    }

    @Override
    public String getText(String aTextName, List<?> args) {
        return super.getText(TextSanitizer.cleanTextKey(aTextName), TextSanitizer.cleanArgs(args));
    }

    @Override
    public String getText(String key, String[] args) {
        return super.getText(TextSanitizer.cleanTextKey(key), TextSanitizer.cleanArgs(Arrays.asList(args)));
    }

    @Override
    public String getText(String aTextName, String defaultValue, List<?> args) {
        List<Object> cleanedArgs = new ArrayList<>(args.size());
        for (Object el : args) {
            cleanedArgs.add(el instanceof String ? TextSanitizer.cleanTextArg((String) el) : el);
        }
        return super.getText(TextSanitizer.cleanTextKey(aTextName), TextSanitizer.cleanTextKey(defaultValue), cleanedArgs);
    }

    @Override
    public String getText(String key, String defaultValue, String[] args) {
        String[] cleanedArgs = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            cleanedArgs[i] = TextSanitizer.cleanTextArg(args[i]);
        }
        return super.getText(TextSanitizer.cleanTextKey(key), TextSanitizer.cleanTextKey(defaultValue), cleanedArgs);
    }

    public void addError(String errorKey) {
        addActionError(getText(errorKey));
    }
    
    public void addError(String errorKey, String param) {
        addActionError(getText(errorKey, errorKey, param));
    }
    
    public void addError(String errorKey, List<?> args) {
        addActionError(getText(errorKey, args));
    }
    
    public boolean errorsExist() {
        return hasActionErrors();
    }
    
    
    public void addMessage(String msgKey) {
        addActionMessage(getText(msgKey));
    }
    
    public void addMessage(String msgKey, String param) {
        addActionMessage(getText(msgKey, msgKey, param));
    }
    
    public void addMessage(String msgKey, List<?> args) {
        addActionMessage(getText(msgKey, args));
    }
    
    public boolean messagesExist() {
        return hasActionMessages();
    }
    

    // REFACTORED: Delegated to AuthenticationContext
    public User getAuthenticatedUser() {
        return authContext.getAuthenticatedUser();
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        authContext.setAuthenticatedUser(authenticatedUser);
    }

    // REFACTORED: Delegated to AuthenticationContext
    public Weblog getActionWeblog() {
        return authContext.getActionWeblog();
    }

    public void setActionWeblog(Weblog workingWeblog) {
        authContext.setActionWeblog(workingWeblog);
    }

    // REFACTORED: Delegated to AuthenticationContext
    public String getWeblog() {
        return authContext.getWeblog();
    }

    public void setWeblog(String weblog) {
        authContext.setWeblog(weblog);
    }
    
    public String getPageTitle() {
        return getText(pageTitle);
    }

    public void setPageTitle(String pageTitle) {
    }

    // REFACTORED: Delegated to AuthenticationContext
    public String getActionName() {
        return authContext.getActionName();
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
        authContext.setActionName(actionName);
    }

    // REFACTORED: Delegated to AuthenticationContext
    public String getDesiredMenu() {
        return authContext.getDesiredMenu();
    }

    public void setDesiredMenu(String desiredMenu) {
        this.desiredMenu = desiredMenu;
        authContext.setDesiredMenu(desiredMenu);
    }
    
    // REFACTORED: Delegated menu retrieval to AuthenticationContext
    public Menu getMenu() {
        return authContext.getMenu();
    }
    
    
    // REFACTORED: Delegated to DateFormatProvider
    public String getShortDateFormat() {
        return getDateFormatProvider().getShortDateFormat();
    }
    
    // REFACTORED: Delegated to DateFormatProvider
    public String getMediumDateFormat() {
        return getDateFormatProvider().getMediumDateFormat();
    }
    
    // REFACTORED: Delegated to existing UIUtils
    public List<Locale> getLocalesList() {
        return UIUtils.getLocales();
    }
    
    // REFACTORED: Delegated to existing UIUtils
    public List<String> getTimeZonesList() {
        return UIUtils.getTimeZones();
    }
    
    public List<Integer> getHoursList() {
        return IntStream.range(0, 24).boxed().collect(Collectors.toList());
    }
    
    public List<Integer> getMinutesList() {
        return IntStream.range(0, 60).boxed().collect(Collectors.toList());
    }
    
    public List<Integer> getSecondsList() {
        return getMinutesList();
    }
    
    public List<KeyValueObject> getCommentDaysList() {
        
        List<KeyValueObject> opts = new ArrayList<>();

        opts.add(new KeyValueObject(-1, getText("weblogEdit.commentsNotAllowed")));
        opts.add(new KeyValueObject(0, getText("weblogEdit.unlimitedCommentDays")));
        opts.add(new KeyValueObject(3, getText("weblogEdit.days3")));
        opts.add(new KeyValueObject(7, getText("weblogEdit.days7")));
        opts.add(new KeyValueObject(14, getText("weblogEdit.days14")));
        opts.add(new KeyValueObject(30, getText("weblogEdit.days30")));
        opts.add(new KeyValueObject(60, getText("weblogEdit.days60")));
        opts.add(new KeyValueObject(90, getText("weblogEdit.days90")));
        
        return opts;
    }
    
    private DateFormatProvider getDateFormatProvider() {
        if (dateFormatProvider == null) {
            dateFormatProvider = new DateFormatProvider(getLocale());
        }
        return dateFormatProvider;
    }
}

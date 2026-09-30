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

package org.apache.roller.weblogger.ui.struts2.core;

import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.ActivationResult;
import org.apache.roller.weblogger.business.RegistrationDTO;
import org.apache.roller.weblogger.business.RegistrationResult;
import org.apache.roller.weblogger.business.UserActivationService;
import org.apache.roller.weblogger.business.UserRegistrationService;
import org.apache.roller.weblogger.business.UserValidationService;
import org.apache.roller.weblogger.business.ValidationResult;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.core.security.CustomUserRegistry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;


// REFACTORED: Extracted business logic to service layer to address deficient encapsulation
// Now uses UserRegistrationService, UserValidationService, and UserActivationService
public class Register extends UIAction implements ServletRequestAware {
    
    private static Log log = LogFactory.getLog(Register.class);
    private static final String DISABLED_RETURN_CODE = "disabled";

    private HttpServletRequest servletRequest = null;

    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    private String activationStatus = null;
    
    private String activationCode = null;
    private ProfileBean bean = new ProfileBean();
    
    // REFACTORED: Service layer dependencies injected to separate business logic from UI
    private UserRegistrationService registrationService;
    private UserValidationService validationService;
    private UserActivationService activationService;

    public Register() {
        this.pageTitle = "newUser.addNewUser";
    }
    
    @Override
    public boolean isUserRequired() {
        return false;
    }
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    public String getAuthMethod() {
        return authMethod.name();
    }

    @SkipValidation
    @Override
    public String execute() {
        
        // REFACTORED: Delegated registration eligibility check to UserRegistrationService
        try {
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            initializeServices(mgr);
            
            if (!registrationService.isRegistrationEnabled()) {
                addError("Register.disabled");
                return DISABLED_RETURN_CODE;
            }
        } catch (Exception e) {
            log.error("Error checking user count", e);
            addError("generic.error.check.logs");
            return DISABLED_RETURN_CODE;
        }
                
        bean.setLocale(getServletRequest().getLocale().toString());
        
        bean.setTimeZone(TimeZone.getDefault().getID());
        
        try {

            if (WebloggerConfig.getAuthMethod() == AuthMethod.LDAP) {
                User fromSSOUser = CustomUserRegistry.getUserDetailsFromAuthentication(getServletRequest());
                if (fromSSOUser != null) {
                    getBean().copyFrom(fromSSOUser);
                }
            } else if (WebloggerConfig.getAuthMethod() == AuthMethod.CMA) {
                if (getServletRequest().getUserPrincipal() != null) {
                    getBean().setUserName(getServletRequest().getUserPrincipal().getName());
                    getBean().setScreenName(getServletRequest().getUserPrincipal().getName());
                }
            }
            
        } catch (Exception ex) {
            log.error("Error reading SSO user data", ex);
            addError("error.editing.user", ex.toString());
        }
        
        return INPUT;
    }
    
    
    public String save() {
        
        // REFACTORED: Replaced embedded business logic with service layer delegation
        try {
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            initializeServices(mgr);
            
            if (!registrationService.isRegistrationEnabled()) {
                return DISABLED_RETURN_CODE;
            }
        } catch (Exception e) {
            log.error("Error checking user count", e);
            return DISABLED_RETURN_CODE;
        }
        
        // REFACTORED: Convert ProfileBean to DTO for service layer communication
        RegistrationDTO dto = convertBeanToDTO();
        
        // REFACTORED: Delegated validation logic to UserValidationService (previously in myValidate())
        ValidationResult validationResult = validationService.validate(dto, getServletRequest());
        
        if (validationResult.hasErrors()) {
            for (String error : validationResult.getErrors()) {
                addError(error);
            }
            convertDTOToBean(dto);
        }
        
        if (!hasActionErrors()) {
            try {
                // REFACTORED: Delegated user creation and activation to UserRegistrationService
                RegistrationResult registrationResult = registrationService.registerUser(dto);
                
                if (registrationResult.isSuccess()) {
                    WebloggerFactory.getWeblogger().flush();
                    
                    if (registrationResult.getActivationStatus() != null) {
                        setActivationStatus(registrationResult.getActivationStatus());
                    }
                    
                    getServletRequest().getSession().removeAttribute(RollerSession.ROLLER_SESSION);
                    getServletRequest().getSession().invalidate();
                    
                    setPageTitle("welcome.title");
                    
                    return SUCCESS;
                } else {
                    for (String error : registrationResult.getErrors()) {
                        addError(error);
                    }
                }

            } catch (WebloggerException ex) {
                log.error("Error during registration", ex);
                addError("generic.error.check.logs");
            }
        }
        
        return INPUT;
    }
    
    @SkipValidation
    public String activate() {
        
        // REFACTORED: Delegated activation logic to UserActivationService
        try {
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            initializeServices(mgr);
            
            ActivationResult result = activationService.activateUser(getActivationCode());
            
            if (result.hasErrors()) {
                for (String error : result.getErrors()) {
                    addError(error);
                }
            }
            
            setActivationStatus(result.getStatus());
            
            WebloggerFactory.getWeblogger().flush();
            
        } catch (WebloggerException e) {
            addError(e.getMessage());
            log.error("ERROR in activateUser", e);
            setActivationStatus("error");
        }
        
        setPageTitle("welcome.title");
            
        return SUCCESS;
    }
    
    // REFACTORED: Initialize service layer dependencies
    private void initializeServices(UserManager mgr) {
        if (activationService == null) {
            activationService = new UserActivationService(mgr);
        }
        if (registrationService == null) {
            registrationService = new UserRegistrationService(mgr, activationService);
        }
        if (validationService == null) {
            validationService = new UserValidationService(mgr, authMethod);
        }
    }
    
    // REFACTORED: Convert ProfileBean to DTO for clean separation between UI and business layers
    private RegistrationDTO convertBeanToDTO() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUserName(bean.getUserName());
        dto.setScreenName(bean.getScreenName());
        dto.setFullName(bean.getFullName());
        dto.setEmailAddress(bean.getEmailAddress());
        dto.setLocale(bean.getLocale());
        dto.setTimeZone(bean.getTimeZone());
        dto.setOpenIdUrl(bean.getOpenIdUrl());
        dto.setPasswordText(bean.getPasswordText());
        dto.setPasswordConfirm(bean.getPasswordConfirm());
        return dto;
    }
    
    // REFACTORED: Convert DTO back to ProfileBean after validation (preserves modified values)
    private void convertDTOToBean(RegistrationDTO dto) {
        bean.setUserName(dto.getUserName());
        bean.setScreenName(dto.getScreenName());
        bean.setFullName(dto.getFullName());
        bean.setEmailAddress(dto.getEmailAddress());
        bean.setLocale(dto.getLocale());
        bean.setTimeZone(dto.getTimeZone());
        bean.setOpenIdUrl(dto.getOpenIdUrl());
        bean.setPasswordText(dto.getPasswordText());
        bean.setPasswordConfirm(dto.getPasswordConfirm());
    }
    
    
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
    
    public ProfileBean getBean() {
        return bean;
    }

    public void setBean(ProfileBean bean) {
        this.bean = bean;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    
}

package org.apache.roller.weblogger.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;

public class UserRegistrationService {
    
    private static Log log = LogFactory.getLog(UserRegistrationService.class);
    
    private UserManager userManager;
    private UserActivationService activationService;
    private UserPasswordService passwordService = new UserPasswordService();
    
    public UserRegistrationService(UserManager userManager, UserActivationService activationService) {
        this.userManager = userManager;
        this.activationService = activationService;
    }
    
    public boolean isRegistrationEnabled() {
        try {
            return WebloggerRuntimeConfig.getBooleanProperty("users.registration.enabled")
                || userManager.getUserCount() == 0;
        } catch (Exception e) {
            log.error("Error checking user count", e);
            return false;
        }
    }
    
    public RegistrationResult registerUser(RegistrationDTO dto) {
        RegistrationResult result = new RegistrationResult();
        
        try {
            User user = new User();
            copyDtoToUser(dto, user);
            user.setUserName(dto.getUserName());
            user.setDateCreated(new java.util.Date());
            user.setEnabled(Boolean.TRUE);
            
            if (!StringUtils.isEmpty(dto.getPasswordText()) &&
                    !StringUtils.isEmpty(dto.getPasswordConfirm())) {
                passwordService.resetPassword(user, dto.getPasswordText());
            }
            
            boolean activationEnabled = WebloggerRuntimeConfig.getBooleanProperty(
                    "user.account.email.activation");
            if (activationEnabled) {
                user.setEnabled(Boolean.FALSE);
                String activationCode = activationService.generateActivationCode();
                user.setActivationCode(activationCode);
            }
            
            String openidurl = dto.getOpenIdUrl();
            if (openidurl != null) {
                if (openidurl.endsWith("/")) {
                    openidurl = openidurl.substring(0, openidurl.length() - 1);
                }
                user.setOpenIdUrl(openidurl);
            }
            
            userManager.addUser(user);
            
            String activationStatus = activationService.sendActivationEmail(user);
            result.setActivationStatus(activationStatus);
            result.setSuccess(true);
            
        } catch (WebloggerException ex) {
            log.error("Error adding new user", ex);
            result.addError("generic.error.check.logs");
            result.setSuccess(false);
        }
        
        return result;
    }
    
    private void copyDtoToUser(RegistrationDTO dto, User user) {
        user.setScreenName(dto.getScreenName());
        user.setFullName(dto.getFullName());
        user.setEmailAddress(dto.getEmailAddress());
        user.setLocale(dto.getLocale());
        user.setTimeZone(dto.getTimeZone());
        user.setOpenIdUrl(dto.getOpenIdUrl());
    }
}

package org.apache.roller.weblogger.business;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.CustomUserRegistry;

public class UserValidationService {
    
    private static Log log = LogFactory.getLog(UserValidationService.class);
    public static final String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";
    
    private UserManager userManager;
    private AuthMethod authMethod;
    
    public UserValidationService(UserManager userManager, AuthMethod authMethod) {
        this.userManager = userManager;
        this.authMethod = authMethod;
    }
    
    public ValidationResult validate(RegistrationDTO dto, HttpServletRequest request) {
        ValidationResult result = new ValidationResult();
        
        boolean usingSSO = authMethod == AuthMethod.LDAP || authMethod == AuthMethod.CMA;
        if (usingSSO) {
            String unusedPassword = WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>");
            preserveUsernameAndPassword(dto, request, unusedPassword);
        }
        
        validateUsername(dto, result);
        validatePassword(dto, result);
        validatePasswordMatch(dto, result);
        validateUsernameUniqueness(dto, result);
        validateOpenIdUniqueness(dto, result);
        
        return result;
    }
    
    private void preserveUsernameAndPassword(RegistrationDTO dto, HttpServletRequest request, String unusedPassword) {
        User fromSSOUser = CustomUserRegistry.getUserDetailsFromAuthentication(request);
        if (fromSSOUser != null) {
            dto.setPasswordText(unusedPassword);
            dto.setPasswordConfirm(unusedPassword);
            dto.setUserName(fromSSOUser.getUserName());
        } else if (request.getUserPrincipal() != null) {
            dto.setUserName(request.getUserPrincipal().getName());
            dto.setPasswordText(unusedPassword);
            dto.setPasswordConfirm(unusedPassword);
        }
    }
    
    private void validateUsername(RegistrationDTO dto, ValidationResult result) {
        String allowed = WebloggerConfig.getProperty("username.allowedChars");
        if (allowed == null || allowed.isBlank()) {
            allowed = DEFAULT_ALLOWED_CHARS;
        }
        
        String safe = CharSetUtils.keep(dto.getUserName(), allowed);
        if (!safe.equals(dto.getUserName())) {
            result.addError("error.add.user.badUserName");
        }
    }
    
    private void validatePassword(RegistrationDTO dto, ValidationResult result) {
        if (AuthMethod.ROLLERDB.name().equals(authMethod.name())
                && StringUtils.isEmpty(dto.getPasswordText())) {
            result.addError("error.add.user.passwordEmpty");
            return;
        }
        
        if (authMethod.equals(AuthMethod.OPENID) ||
                (authMethod.equals(AuthMethod.DB_OPENID) && !StringUtils.isEmpty(dto.getOpenIdUrl()))) {
            String randomString = RandomStringUtils.randomAlphanumeric(255);
            dto.setPasswordText(randomString);
            dto.setPasswordConfirm(randomString);
        }
    }
    
    private void validatePasswordMatch(RegistrationDTO dto, ValidationResult result) {
        if (!StringUtils.equals(dto.getPasswordText(), dto.getPasswordConfirm())) {
            result.addError("userRegister.error.mismatchedPasswords");
        }
    }
    
    private void validateUsernameUniqueness(RegistrationDTO dto, ValidationResult result) {
        if (!StringUtils.isEmpty(dto.getUserName())) {
            try {
                if (userManager.getUserByUserName(dto.getUserName(), null) != null) {
                    result.addError("error.add.user.userNameInUse");
                    dto.setUserName(null);
                }
            } catch (WebloggerException ex) {
                log.error("error checking for user", ex);
                result.addError("generic.error.check.logs");
            }
        }
    }
    
    private void validateOpenIdUniqueness(RegistrationDTO dto, ValidationResult result) {
        if (!StringUtils.isEmpty(dto.getOpenIdUrl())) {
            try {
                if (userManager.getUserByOpenIdUrl(dto.getOpenIdUrl()) != null) {
                    result.addError("error.add.user.openIdInUse");
                    dto.setOpenIdUrl(null);
                }
            } catch (WebloggerException ex) {
                log.error("error checking OpenID URL", ex);
                result.addError("generic.error.check.logs");
            }
        }
    }
}

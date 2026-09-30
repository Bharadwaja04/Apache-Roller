package org.apache.roller.weblogger.business;

import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.MailUtil;

public class UserActivationService {
    
    private static Log log = LogFactory.getLog(UserActivationService.class);
    
    private UserManager userManager;
    
    public UserActivationService(UserManager userManager) {
        this.userManager = userManager;
    }
    
    public String generateActivationCode() throws WebloggerException {
        String activationCode = UUID.randomUUID().toString();
        return retryActivationCode(activationCode);
    }
    
    private String retryActivationCode(String inActivationCode) throws WebloggerException {
        if (userManager.getUserByActivationCode(inActivationCode) != null) {
            int numOfRetries = 3;
            if (numOfRetries < 1) {
                numOfRetries = 1;
            }
            for (int i = 0; i < numOfRetries; i++) {
                inActivationCode = UUID.randomUUID().toString();
                if (userManager.getUserByActivationCode(inActivationCode) == null) {
                    break;
                } else {
                    inActivationCode = null;
                }
            }
            if (inActivationCode == null) {
                throw new WebloggerException("error.add.user.activationCodeInUse");
            }
        }
        return inActivationCode;
    }
    
    public String sendActivationEmail(User user) {
        if (user.getActivationCode() != null) {
            try {
                MailUtil.sendUserActivationEmail(user);
                return "pending";
            } catch (WebloggerException ex) {
                log.error("Error sending activation email to - " + user.getEmailAddress(), ex);
            }
        }
        return null;
    }
    
    public ActivationResult activateUser(String activationCode) {
        ActivationResult result = new ActivationResult();
        
        try {
            if (activationCode == null) {
                result.addError("error.activate.user.missingActivationCode");
                result.setStatus("error");
            } else {
                User user = userManager.getUserByActivationCode(activationCode);
                
                if (user != null) {
                    user.setEnabled(Boolean.TRUE);
                    user.setActivationCode(null);
                    userManager.saveUser(user);
                    result.setStatus("active");
                } else {
                    result.addError("error.activate.user.invalidActivationCode");
                    result.setStatus("error");
                }
            }
        } catch (WebloggerException e) {
            result.addError(e.getMessage());
            result.setStatus("error");
            log.error("ERROR in activateUser", e);
        }
        
        return result;
    }
}

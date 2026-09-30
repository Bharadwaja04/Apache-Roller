package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserPasswordService {
    
    public void resetPassword(User user, String newPassword) {
        PasswordEncoder encoder = RollerContext.getPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
    }
}

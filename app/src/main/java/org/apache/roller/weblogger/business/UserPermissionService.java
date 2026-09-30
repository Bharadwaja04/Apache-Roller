package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;

import java.util.Collections;
import java.util.List;

public class UserPermissionService {
    
    private UserManager userManager;
    
    public UserPermissionService(UserManager userManager) {
        this.userManager = userManager;
    }
    
    public boolean hasGlobalPermission(User user, String action) {
        return hasGlobalPermissions(user, Collections.singletonList(action));
    }
    
    public boolean hasGlobalPermissions(User user, List<String> actions) {
        try {
            GlobalPermission perm = new GlobalPermission(actions);
            return userManager.checkPermission(perm, user);
        } catch (WebloggerException ex) {
            return false;
        }
    }
}

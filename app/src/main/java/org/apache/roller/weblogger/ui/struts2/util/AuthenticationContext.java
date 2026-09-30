package org.apache.roller.weblogger.ui.struts2.util;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.core.util.menu.Menu;
import org.apache.roller.weblogger.ui.core.util.menu.MenuHelper;

import java.util.Collections;

public class AuthenticationContext {
    
    private User authenticatedUser;
    private Weblog actionWeblog;
    private String weblog;
    private String actionName;
    private String desiredMenu;
    
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
    
    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }
    
    public Weblog getActionWeblog() {
        return actionWeblog;
    }
    
    public void setActionWeblog(Weblog actionWeblog) {
        this.actionWeblog = actionWeblog;
    }
    
    public String getWeblog() {
        return weblog;
    }
    
    public void setWeblog(String weblog) {
        this.weblog = weblog;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public String getDesiredMenu() {
        return desiredMenu;
    }
    
    public void setDesiredMenu(String desiredMenu) {
        this.desiredMenu = desiredMenu;
    }
    
    public boolean isUserIsAdmin() {
        try {
            GlobalPermission adminPerm = new GlobalPermission(
                Collections.singletonList(GlobalPermission.ADMIN));
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            return umgr.checkPermission(adminPerm, getAuthenticatedUser());
        } catch (Exception e) {
            return false;
        }
    }
    
    public Menu getMenu() {
        return MenuHelper.getMenu(getDesiredMenu(), getActionName(), getAuthenticatedUser(), getActionWeblog());
    }
}

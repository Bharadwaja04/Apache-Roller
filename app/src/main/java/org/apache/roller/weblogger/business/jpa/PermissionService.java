package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.RollerPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

@com.google.inject.Singleton
public class PermissionService {
    private static final Log log = LogFactory.getLog(PermissionService.class);

    private final JPAUserDao dao;

    @com.google.inject.Inject
    public PermissionService(JPAUserDao dao) {
        this.dao = dao;
    }

    public boolean checkPermission(RollerPermission perm, User user) throws WebloggerException {

        if (perm instanceof WeblogPermission) {
            WeblogPermission permToCheck = (WeblogPermission)perm;
            try {
                RollerPermission existingPerm = dao.findWeblogPermission(permToCheck.getWeblog(), user);
                if (existingPerm != null && existingPerm.implies(perm)) {
                    return true;
                }
            } catch (WebloggerException ignored) {
            }
        }

        GlobalPermission globalPerm = new GlobalPermission(user);
        if (globalPerm.implies(perm)) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("PERM CHECK FAILED: user " + user.getUserName() + " does not have " + perm.toString());
        }
        return false;
    }

    public WeblogPermission getWeblogPermission(Weblog weblog, User user) throws WebloggerException {
        return dao.findWeblogPermission(weblog, user);
    }

    public WeblogPermission getWeblogPermissionIncludingPending(Weblog weblog, User user) throws WebloggerException {
        return dao.findWeblogPermissionIncludingPending(weblog, user);
    }

    public void grantWeblogPermission(Weblog weblog, User user, java.util.List<String> actions) throws WebloggerException {
        // reuse existing DAO operations
        WeblogPermission existingPerm = dao.findWeblogPermissionIncludingPending(weblog, user);
        if (existingPerm != null) {
            existingPerm.addActions(actions);
            dao.store(existingPerm);
        } else {
            WeblogPermission perm = new WeblogPermission(weblog, user, actions);
            dao.store(perm);
        }
    }

    public void grantWeblogPermissionPending(Weblog weblog, User user, java.util.List<String> actions) throws WebloggerException {
        WeblogPermission existingPerm = dao.findWeblogPermissionIncludingPending(weblog, user);
        if (existingPerm != null) {
            throw new WebloggerException("Cannot make existing permission into pending permission");
        } else {
            WeblogPermission perm = new WeblogPermission(weblog, user, actions);
            perm.setPending(true);
            dao.store(perm);
        }
    }

    public void confirmWeblogPermission(Weblog weblog, User user) throws WebloggerException {
        WeblogPermission existingPerm = dao.findWeblogPermissionIncludingPending(weblog, user);
        if (existingPerm == null) {
            throw new WebloggerException("ERROR: permission not found");
        }
        existingPerm.setPending(false);
        dao.store(existingPerm);
    }

    public void declineWeblogPermission(Weblog weblog, User user) throws WebloggerException {
        WeblogPermission existingPerm = dao.findWeblogPermissionIncludingPending(weblog, user);
        if (existingPerm == null) {
            throw new WebloggerException("ERROR: permission not found");
        }
        dao.remove(existingPerm);
    }

    public void revokeWeblogPermission(Weblog weblog, User user, java.util.List<String> actions) throws WebloggerException {
        WeblogPermission oldperm = dao.findWeblogPermissionIncludingPending(weblog, user);
        if (oldperm == null) {
            throw new WebloggerException("ERROR: permission not found");
        }
        oldperm.removeActions(actions);
        if (oldperm.isEmpty()) {
            dao.remove(oldperm);
        } else {
            dao.store(oldperm);
        }
    }

    public java.util.List<WeblogPermission> getWeblogPermissions(User user) throws WebloggerException {
        return dao.findWeblogPermissionsByUser(user);
    }

    public java.util.List<WeblogPermission> getWeblogPermissions(Weblog weblog) throws WebloggerException {
        return dao.findWeblogPermissionsByWeblog(weblog);
    }

    public java.util.List<WeblogPermission> getWeblogPermissionsIncludingPending(Weblog weblog) throws WebloggerException {
        return dao.findWeblogPermissionsByWeblogIncludingPending(weblog);
    }

    public java.util.List<WeblogPermission> getPendingWeblogPermissions(User user) throws WebloggerException {
        return dao.findPendingWeblogPermissionsByUser(user);
    }

    public java.util.List<WeblogPermission> getPendingWeblogPermissions(Weblog weblog) throws WebloggerException {
        return dao.findPendingWeblogPermissionsByWeblog(weblog);
    }
}

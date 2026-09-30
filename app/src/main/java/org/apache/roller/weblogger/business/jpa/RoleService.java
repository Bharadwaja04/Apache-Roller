package org.apache.roller.weblogger.business.jpa;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserRole;

@com.google.inject.Singleton
public class RoleService {

    private final JPAPersistenceStrategy strategy;

    @com.google.inject.Inject
    public RoleService(JPAPersistenceStrategy strat) {
        this.strategy = strat;
    }

    public boolean hasRole(String roleName, User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserNameAndRole", UserRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, roleName);
        try {
            q.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public java.util.List<String> getRoles(User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserName", UserRole.class);
        q.setParameter(1, user.getUserName());
        java.util.List<UserRole> roles = q.getResultList();
        java.util.List<String> roleNames = new java.util.ArrayList<>();
        if (roles != null) {
            for (UserRole userRole : roles) {
                roleNames.add(userRole.getRole());
            }
        }
        return roleNames;
    }

    public void grantRole(String roleName, User user) throws WebloggerException {
        if (!hasRole(roleName, user)) {
            UserRole role = new UserRole(user.getUserName(), roleName);
            this.strategy.store(role);
        }
    }

    public void revokeRole(String roleName, User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserNameAndRole", UserRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, roleName);
        try {
            UserRole role = q.getSingleResult();
            this.strategy.remove(role);
        } catch (NoResultException e) {
            throw new WebloggerException("ERROR: removing role", e);
        }
    }
}

package org.apache.roller.weblogger.business.jpa;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.Weblog;

@com.google.inject.Singleton
public class JPAUserDao {

    private final JPAPersistenceStrategy strategy;

    @com.google.inject.Inject
    public JPAUserDao(JPAPersistenceStrategy strat) {
        this.strategy = strat;
    }

    public void store(Object o) throws WebloggerException {
        this.strategy.store(o);
    }

    public void remove(Object o) throws WebloggerException {
        this.strategy.remove(o);
    }

    public User loadUser(String id) throws WebloggerException {
        return (User) this.strategy.load(User.class, id);
    }

    public User findUserByUserName(String userName, Boolean enabled) throws WebloggerException {
        TypedQuery<User> query;
        Object[] params;
        if (enabled != null) {
            query = strategy.getNamedQuery(
                    "User.getByUserName&Enabled", User.class);
            params = new Object[] {userName, enabled};
        } else {
            query = strategy.getNamedQuery(
                    "User.getByUserName", User.class);
            params = new Object[] {userName};
        }
        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<WeblogPermission> findWeblogPermissionsByUser(User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public WeblogPermission findWeblogPermission(Weblog weblog, User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogId",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public WeblogPermission findWeblogPermissionIncludingPending(Weblog weblog, User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public List<WeblogPermission> findWeblogPermissionsByWeblog(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogId",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }

    public List<WeblogPermission> findWeblogPermissionsByWeblogIncludingPending(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }

    public List<WeblogPermission> findPendingWeblogPermissionsByUser(User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&Pending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<WeblogPermission> findPendingWeblogPermissionsByWeblog(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogId&Pending",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }
}

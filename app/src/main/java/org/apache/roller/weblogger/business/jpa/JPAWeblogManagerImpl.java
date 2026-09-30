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

package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.StatCountCountComparator;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * JPA implementation of WeblogManager.
 * 
 * <p>This class has been refactored to use dedicated service classes for
 * specific responsibilities:</p>
 * <ul>
 *   <li>{@link WeblogValidator} - Validation logic</li>
 *   <li>{@link WeblogCacheManager} - Handle-to-ID caching</li>
 *   <li>{@link WeblogInitializationService} - New weblog setup</li>
 *   <li>{@link WeblogRemovalService} - Weblog content removal</li>
 *   <li>{@link WeblogQueryBuilder} - Dynamic query construction</li>
 * </ul>
 * 
 * @see WeblogValidator
 * @see WeblogCacheManager
 * @see WeblogInitializationService
 * @see WeblogRemovalService
 * @see WeblogQueryBuilder
 */
@com.google.inject.Singleton
public class JPAWeblogManagerImpl implements WeblogManager {
    
    private static final Log log = LogFactory.getLog(JPAWeblogManagerImpl.class);
    
    private static final Comparator<StatCount> STAT_COUNT_COUNT_REVERSE_COMPARATOR =
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    
    // Extracted service delegates
    private final WeblogValidator validator;
    private final WeblogCacheManager cacheManager;
    private final WeblogQueryBuilder queryBuilder;
    
    // Lazy-initialized services (need 'this' reference)
    private WeblogInitializationService initService;
    private WeblogRemovalService removalService;

    @com.google.inject.Inject
    protected JPAWeblogManagerImpl(Weblogger roller, JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA Weblog Manager");
        this.roller = roller;
        this.strategy = strat;
        
        // Initialize extracted services
        this.validator = new WeblogValidator();
        this.cacheManager = new WeblogCacheManager();
        this.queryBuilder = new WeblogQueryBuilder(strat);
    }
    
    // -------------------- Service Accessors --------------------
    
    private WeblogInitializationService getInitService() {
        if (initService == null) {
            initService = new WeblogInitializationService(roller, strategy);
        }
        return initService;
    }
    
    private WeblogRemovalService getRemovalService() {
        if (removalService == null) {
            removalService = new WeblogRemovalService(roller, strategy);
        }
        return removalService;
    }
    
    // -------------------- Lifecycle --------------------
    
    @Override
    public void release() {}
    
    // -------------------- CRUD Operations --------------------
    
    @Override
    public void addWeblog(Weblog newWeblog) throws WebloggerException {
        this.strategy.store(newWeblog);
        this.strategy.flush();
        getInitService().initializeWeblog(newWeblog);
    }
    
    @Override
    public void saveWeblog(Weblog weblog) throws WebloggerException {
        weblog.setLastModified(new java.util.Date());
        strategy.store(weblog);
    }
    
    @Override
    public void removeWeblog(Weblog weblog) throws WebloggerException {
        getRemovalService().removeWeblogContents(weblog);
        this.strategy.remove(weblog);
        cacheManager.removeFromCache(weblog.getHandle());
    }
    
    @Override
    public Weblog getWeblog(String id) throws WebloggerException {
        return (Weblog) this.strategy.load(Weblog.class, id);
    }
    
    @Override
    public Weblog getWeblogByHandle(String handle) throws WebloggerException {
        return getWeblogByHandle(handle, Boolean.TRUE);
    }
    
    @Override
    public Weblog getWeblogByHandle(String handle, Boolean visible) throws WebloggerException {
        validator.validateHandle(handle);
        
        // Check cache first
        String blogID = cacheManager.getCachedId(handle);
        if (blogID != null) {
            Weblog weblog = this.getWeblog(blogID);
            if (weblog != null) {
                if (visible == null || visible.equals(weblog.getVisible())) {
                    log.debug("weblogHandleToId CACHE HIT - " + handle);
                    return weblog;
                }
            } else {
                cacheManager.removeFromCache(handle);
            }
        }
        
        // Query database
        TypedQuery<Weblog> query = strategy.getNamedQuery("Weblog.getByHandle", Weblog.class);
        query.setParameter(1, handle);
        Weblog weblog;
        try {
            weblog = query.getSingleResult();
        } catch (NoResultException e) {
            weblog = null;
        }
        
        // Update cache
        if (weblog != null) {
            log.debug("weblogHandleToId CACHE MISS - " + handle);
            cacheManager.cacheWeblog(weblog);
        }
        
        if (weblog != null && (visible == null || visible.equals(weblog.getVisible()))) {
            return weblog;
        }
        return null;
    }
    
    // -------------------- Query Operations --------------------
    
    @Override
    public List<Weblog> getWeblogs(
            Boolean enabled, Boolean active,
            Date startDate, Date endDate, 
            int offset, int length) throws WebloggerException {
        return queryBuilder.getWeblogs(enabled, active, startDate, endDate, offset, length);
    }
    
    @Override
    public List<Weblog> getWeblogsByLetter(char letter, int offset, int length)
            throws WebloggerException {
        TypedQuery<Weblog> query = strategy.getNamedQuery(
                "Weblog.getByLetterOrderByHandle", Weblog.class);
        query.setParameter(1, letter + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    // -------------------- User Operations --------------------
    
    @Override
    public List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException {
        List<Weblog> weblogs = new ArrayList<>();
        if (user == null) {
            return weblogs;
        }
        List<WeblogPermission> perms = roller.getUserManager().getWeblogPermissions(user);
        for (WeblogPermission perm : perms) {
            Weblog weblog = perm.getWeblog();
            if ((!enabledOnly || weblog.getVisible()) && BooleanUtils.isTrue(weblog.getActive())) {
                weblogs.add(weblog);
            }
        }
        return weblogs;
    }
    
    @Override
    public List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException {
        List<User> users = new ArrayList<>();
        List<WeblogPermission> perms = roller.getUserManager().getWeblogPermissions(weblog);
        for (WeblogPermission perm : perms) {
            User user = perm.getUser();
            if (user == null) {
                log.error("ERROR user is null, userName:" + perm.getUserName());
                continue;
            }
            if (!enabledOnly || user.getEnabled()) {
                users.add(user);
            }
        }
        return users;
    }
    
    // -------------------- Statistics Operations --------------------
    
    @Override
    public Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Long> results = new TreeMap<>();
        TypedQuery<Long> query = strategy.getNamedQuery("Weblog.getCountByHandleLike", Long.class);
        
        for (int i = 0; i < 26; i++) {
            char currentChar = letters.charAt(i);
            query.setParameter(1, currentChar + "%");
            List<Long> row = query.getResultList();
            results.put(String.valueOf(currentChar), row.get(0));
        }
        return results;
    }
    
    @Override
    public List<StatCount> getMostCommentedWeblogs(Date startDate, Date endDate,
            int offset, int length) throws WebloggerException {
        
        Query query;
        if (endDate == null) {
            endDate = new Date();
        }
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate&StartDate");
            query.setParameter(1, end);
            query.setParameter(2, start);
        } else {
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate");
            query.setParameter(1, end);
        }
        
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        
        List<?> queryResults = query.getResultList();
        List<StatCount> results = new ArrayList<>();
        
        if (queryResults != null) {
            for (Object obj : queryResults) {
                Object[] row = (Object[]) obj;
                StatCount sc = new StatCount(
                        (String) row[1],                     // weblog id
                        (String) row[2],                     // weblog handle
                        (String) row[3],                     // weblog name
                        "statCount.weblogCommentCountType",  // stat type
                        ((Long) row[0]));                    // # comments
                sc.setWeblogHandle((String) row[2]);
                results.add(sc);
            }
        }
        
        results.sort(STAT_COUNT_COUNT_REVERSE_COMPARATOR);
        return results;
    }
    
    @Override
    public long getWeblogCount() throws WebloggerException {
        List<Long> results = strategy.getNamedQuery(
                "Weblog.getCountAllDistinct", Long.class).getResultList();
        return results.get(0);
    }
    
    // -------------------- Template Operations --------------------
    
    @Override
    public void saveTemplate(WeblogTemplate template) throws WebloggerException {
        this.strategy.store(template);
        roller.getWeblogManager().saveWeblog(template.getWeblog());
    }

    @Override
    public void saveTemplateRendition(CustomTemplateRendition rendition) throws WebloggerException {
        this.strategy.store(rendition);
        roller.getWeblogManager().saveWeblog(rendition.getWeblogTemplate().getWeblog());
    }
    
    @Override
    public void removeTemplate(WeblogTemplate template) throws WebloggerException {
        this.strategy.remove(template);
        roller.getWeblogManager().saveWeblog(template.getWeblog());
    }

    @Override
    public WeblogTemplate getTemplate(String id) throws WebloggerException {
        if (id != null && id.endsWith(".vm")) {
            return null;
        }
        return (WeblogTemplate) this.strategy.load(WeblogTemplate.class, id);
    }
    
    @Override
    public WeblogTemplate getTemplateByLink(Weblog weblog, String templateLink)
            throws WebloggerException {
        validator.validateWeblogNotNull(weblog, "getTemplateByLink");
        validator.validateTemplateLinkNotNull(templateLink);

        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblog&Link", WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, templateLink);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    @Override
    public WeblogTemplate getTemplateByAction(Weblog weblog, ComponentType action)
            throws WebloggerException {
        validator.validateWeblogNotNull(weblog, "getTemplateByAction");
        if (action == null) {
            throw new WebloggerException("Action name is null");
        }
        
        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery(
                "WeblogTemplate.getByAction", WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, action);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    @Override
    public WeblogTemplate getTemplateByName(Weblog weblog, String templateName)
            throws WebloggerException {
        validator.validateWeblogNotNull(weblog, "getTemplateByName");
        validator.validateTemplateNameNotNull(templateName);
        
        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblog&Name", WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, templateName);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<WeblogTemplate> getTemplates(Weblog weblog) throws WebloggerException {
        validator.validateWeblogNotNull(weblog, "getTemplates");
        TypedQuery<WeblogTemplate> q = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblogOrderByName", WeblogTemplate.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }
}
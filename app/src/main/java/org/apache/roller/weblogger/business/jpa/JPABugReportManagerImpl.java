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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.notifications.NotificationService;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.util.UUIDGenerator;

import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@com.google.inject.Singleton
public class JPABugReportManagerImpl implements BugReportManager {

    private static final Log log = LogFactory.getLog(JPABugReportManagerImpl.class);

    private final JPAPersistenceStrategy strategy;
    private final NotificationService    notificationService;

    @com.google.inject.Inject
    protected JPABugReportManagerImpl(JPAPersistenceStrategy strategy,
                                      NotificationService notificationService) {
        log.debug("Instantiating JPA Bug Report Manager");
        this.strategy            = strategy;
        this.notificationService = notificationService;
    }

    @Override
    public void saveBugReport(BugReport report) throws WebloggerException {
        if (report.getId() == null) {
            report.setId(UUIDGenerator.generateUUID());
            report.setCreatedTime(new Date());
        }
        report.setUpdatedTime(new Date());
        this.strategy.store(report);
        notificationService.notifyBugCreated(report);
    }

    @Override
    public void updateStatus(BugReport report, BugReport.Status newStatus,
                             String adminNotes) throws WebloggerException {
        BugReport.Status oldStatus = report.getStatus();
        report.setStatus(newStatus);
        report.setAdminNotes(adminNotes);
        report.setUpdatedTime(new Date());
        if (newStatus == BugReport.Status.RESOLVED) {
            report.setResolvedTime(new Date());
        }
        this.strategy.store(report);
        notificationService.notifyStatusChanged(report, oldStatus, newStatus);
    }

    @Override
    public void deleteBugReport(BugReport report) throws WebloggerException {
        BugReport.Status oldStatus = report.getStatus();
        report.setStatus(BugReport.Status.DELETED);
        report.setUpdatedTime(new Date());
        this.strategy.store(report);
        notificationService.notifyBugDeleted(report);
    }

    @Override
    public BugReport getBugReport(String id) throws WebloggerException {
        return (BugReport) this.strategy.load(BugReport.class, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BugReport> getAll() throws WebloggerException {
        TypedQuery<BugReport> query = this.strategy.getNamedQuery(
                "BugReport.getAll", BugReport.class);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BugReport> getByStatus(BugReport.Status status) throws WebloggerException {
        TypedQuery<BugReport> query = this.strategy.getNamedQuery(
                "BugReport.getByStatus", BugReport.class);
        query.setParameter(1, status);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BugReport> getByReporter(User reporter) throws WebloggerException {
        TypedQuery<BugReport> query = this.strategy.getNamedQuery(
                "BugReport.getByReporter", BugReport.class);
        query.setParameter(1, reporter);
        return query.getResultList();
    }

    @Override
    public long countByStatus(BugReport.Status status) throws WebloggerException {
        TypedQuery<Long> query = this.strategy.getNamedQuery(
                "BugReport.countByStatus", Long.class);
        query.setParameter(1, status);
        return query.getSingleResult();
    }
}

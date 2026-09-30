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
package org.apache.roller.weblogger.ui.struts2.admin;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Admin action for viewing and managing bug reports.
 */
public class AdminBugAction extends UIAction {

    private List<BugReport> bugReports = Collections.emptyList();
    private String reportId;
    private String adminNotes;
    private String statusFilter;

    public AdminBugAction() {
        this.actionName  = "adminBugs";
        this.desiredMenu = "admin";
        this.pageTitle   = "bugReport.admin.dashboard";
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    /** Lists all bug reports, optionally filtered by status. */
    @Override
    public String execute() {
        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            if (StringUtils.isNotBlank(statusFilter)) {
                try {
                    BugReport.Status filter = BugReport.Status.valueOf(statusFilter);
                    bugReports = mgr.getByStatus(filter);
                } catch (IllegalArgumentException e) {
                    bugReports = mgr.getAll();
                }
            } else {
                bugReports = mgr.getAll();
            }
        } catch (WebloggerException e) {
            addActionError(e.getMessage());
        }
        return LIST;
    }

    /** Marks the selected report as RESOLVED. */
    public String resolve() {
        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            BugReport report = mgr.getBugReport(reportId);
            if (report == null) {
                addActionError(getText("bugReport.error.notFound"));
                return LIST;
            }
            mgr.updateStatus(report, BugReport.Status.RESOLVED, adminNotes);
            WebloggerFactory.getWeblogger().flush();
        } catch (WebloggerException e) {
            addActionError(e.getMessage());
        }
        return "redirect";
    }

    /** Soft-deletes the selected report (sets status=DELETED). */
    public String delete() {
        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            BugReport report = mgr.getBugReport(reportId);
            if (report == null) {
                addActionError(getText("bugReport.error.notFound"));
                return LIST;
            }
            mgr.deleteBugReport(report);
            WebloggerFactory.getWeblogger().flush();
        } catch (WebloggerException e) {
            addActionError(e.getMessage());
        }
        return "redirect";
    }

    public List<BugReport> getBugReports() { return bugReports; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
}

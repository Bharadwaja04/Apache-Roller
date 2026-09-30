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
package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.commons.lang3.StringUtils;

/**
 * Allows authenticated users to submit a bug report.
 */
public class BugReportAction extends UIAction {

    private String title;
    private String description;
    private String severity = "NORMAL";

    public BugReportAction() {
        this.actionName  = "bugReport";
        this.desiredMenu = null;
        this.pageTitle   = "bugReport.title";
    }

    // no weblog context needed
    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    /** Renders the submission form. */
    @Override
    public String execute() {
        return INPUT;
    }

    /** Processes the form submission. */
    public String save() {
        if (StringUtils.isBlank(title)) {
            addActionError(getText("bugReport.error.titleRequired"));
            return INPUT;
        }
        if (StringUtils.isBlank(description)) {
            addActionError(getText("bugReport.error.descriptionRequired"));
            return INPUT;
        }

        try {
            BugReport report = new BugReport();
            report.setTitle(title.trim());
            report.setDescription(description.trim());
            report.setSeverity(BugReport.Severity.valueOf(severity));
            report.setReporter(getAuthenticatedUser());
            report.setStatus(BugReport.Status.OPEN);

            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            mgr.saveBugReport(report);
            WebloggerFactory.getWeblogger().flush();

            addActionMessage(getText("bugReport.submitted"));
            return SUCCESS;
        } catch (IllegalArgumentException e) {
            addActionError(getText("bugReport.error.invalidSeverity"));
            return INPUT;
        } catch (WebloggerException e) {
            addActionError(e.getMessage());
            return INPUT;
        }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}

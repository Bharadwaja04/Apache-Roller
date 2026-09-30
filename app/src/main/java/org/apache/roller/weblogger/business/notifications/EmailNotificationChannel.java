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
package org.apache.roller.weblogger.business.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.util.MailUtil;

/**
 * Email notification channel using Roller's existing MailProvider/MailUtil.
 * Reads configuration from roller-custom.properties:
 *   bugreport.email.admin   — admin recipient
 *   bugreport.notifications.email.enabled — set false to disable (default true)
 */
@com.google.inject.Singleton
public class EmailNotificationChannel implements NotificationChannel {

    private static final Log log = LogFactory.getLog(EmailNotificationChannel.class);

    @Override
    public String getChannelName() {
        return "EmailChannel";
    }

    private boolean isEnabled() {
        String enabled = WebloggerConfig.getProperty("bugreport.notifications.email.enabled");
        return enabled == null || "true".equalsIgnoreCase(enabled.trim());
    }

    private String getAdminEmail() {
        return WebloggerConfig.getProperty("bugreport.email.admin");
    }

    @Override
    public void onBugCreated(BugReport report) {
        if (!isEnabled()) return;
        String adminEmail = getAdminEmail();
        String reporterEmail = report.getReporter() != null
                ? report.getReporter().getEmailAddress() : null;

        String subject = "[Roller] New Bug Report: " + report.getTitle();
        String body = buildCreatedBody(report);

        sendSafely(adminEmail, subject, body);
        sendSafely(reporterEmail, "[Roller] Your bug report has been received", buildReporterAckBody(report));
    }

    @Override
    public void onStatusChanged(BugReport report, BugReport.Status oldStatus,
                                BugReport.Status newStatus) {
        if (!isEnabled()) return;
        String subject = "[Roller] Bug Report Status Updated: " + newStatus;
        String body = buildStatusBody(report, oldStatus, newStatus);

        sendSafely(getAdminEmail(), subject, body);
        if (report.getReporter() != null) {
            sendSafely(report.getReporter().getEmailAddress(), subject, body);
        }
    }

    @Override
    public void onBugDeleted(BugReport report) {
        if (!isEnabled()) return;
        String subject = "[Roller] Bug Report Deleted: " + report.getTitle();
        String body = buildDeletedBody(report);

        sendSafely(getAdminEmail(), subject, body);
        if (report.getReporter() != null) {
            sendSafely(report.getReporter().getEmailAddress(),
                    "[Roller] Your bug report has been removed", body);
        }
    }

    private void sendSafely(String to, String subject, String body) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("EmailNotificationChannel: skipping send, recipient is null/empty for subject: " + subject);
            return;
        }
        try {
            String from = WebloggerRuntimeConfig.getProperty("site.emailAddress");
            MailUtil.sendTextMessage(from, new String[]{to}, new String[0], new String[0], subject, body);
        } catch (Exception e) {
            log.error("EmailNotificationChannel: failed to send email to " + to, e);
        }
    }

    private String buildCreatedBody(BugReport report) {
        return "A new bug report has been submitted.\n\n"
                + "Title: " + report.getTitle() + "\n"
                + "Severity: " + report.getSeverity() + "\n"
                + "Reporter: " + (report.getReporter() != null ? report.getReporter().getUserName() : "unknown") + "\n"
                + "Description:\n" + report.getDescription();
    }

    private String buildReporterAckBody(BugReport report) {
        return "Thank you for submitting a bug report.\n\n"
                + "Title: " + report.getTitle() + "\n"
                + "Severity: " + report.getSeverity() + "\n"
                + "Status: " + report.getStatus() + "\n\n"
                + "We will review your report and update you on any status changes.";
    }

    private String buildStatusBody(BugReport report, BugReport.Status oldStatus,
                                   BugReport.Status newStatus) {
        return "The status of a bug report has changed.\n\n"
                + "Title: " + report.getTitle() + "\n"
                + "Old Status: " + oldStatus + "\n"
                + "New Status: " + newStatus + "\n"
                + (report.getAdminNotes() != null ? "Admin Notes: " + report.getAdminNotes() + "\n" : "");
    }

    private String buildDeletedBody(BugReport report) {
        return "A bug report has been removed.\n\n"
                + "Title: " + report.getTitle() + "\n"
                + "Severity: " + report.getSeverity();
    }
}

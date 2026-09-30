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

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.BugReport;

/**
 * MailerSend notification channel for bug report lifecycle events.
 *
 * Required roller-custom.properties keys:
 *   bugreport.mailersend.enabled        — set "false" to disable (default: true)
 *   bugreport.mailersend.token          — MailerSend API token with Send permission
 *   bugreport.mailersend.from.email     — verified sender address in your MailerSend account
 *   bugreport.mailersend.from.name      — display name for the from address
 *   bugreport.email.admin               — admin recipient email address
 */
@com.google.inject.Singleton
public class MailerSendNotificationChannel implements NotificationChannel {

    private static final Log log = LogFactory.getLog(MailerSendNotificationChannel.class);

    @Override
    public String getChannelName() {
        return "MailerSendChannel";
    }

    private boolean isEnabled() {
        String val = WebloggerConfig.getProperty("bugreport.mailersend.enabled");
        return val == null || "true".equalsIgnoreCase(val.trim());
    }

    private String getToken() {
        return WebloggerConfig.getProperty("bugreport.mailersend.token");
    }

    private String getFromEmail() {
        return WebloggerConfig.getProperty("bugreport.mailersend.from.email");
    }

    private String getFromName() {
        String name = WebloggerConfig.getProperty("bugreport.mailersend.from.name");
        return name != null ? name : "Roller Bug Reports";
    }

    private String getAdminEmail() {
        return WebloggerConfig.getProperty("bugreport.email.admin");
    }

    // -------------------------------------------------------------------------
    // NotificationChannel events
    // -------------------------------------------------------------------------

    @Override
    public void onBugCreated(BugReport report) {
        if (!isEnabled()) return;

        String adminEmail = getAdminEmail();
        String reporterEmail = report.getReporter() != null
                ? report.getReporter().getEmailAddress() : null;

        sendSafely(adminEmail, null,
                "[Roller] New Bug Report: " + report.getTitle(),
                buildCreatedBody(report));

        if (reporterEmail != null && !reporterEmail.trim().isEmpty()) {
            String reporterName = report.getReporter().getUserName();
            sendSafely(reporterEmail, reporterName,
                    "[Roller] Your bug report has been received",
                    buildReporterAckBody(report));
        }
    }

    @Override
    public void onStatusChanged(BugReport report, BugReport.Status oldStatus,
                                BugReport.Status newStatus) {
        if (!isEnabled()) return;

        String subject = "[Roller] Bug Report Status Updated: " + newStatus;
        String body = buildStatusBody(report, oldStatus, newStatus);

        sendSafely(getAdminEmail(), null, subject, body);

        if (report.getReporter() != null) {
            sendSafely(report.getReporter().getEmailAddress(),
                    report.getReporter().getUserName(), subject, body);
        }
    }

    @Override
    public void onBugDeleted(BugReport report) {
        if (!isEnabled()) return;

        String subject = "[Roller] Bug Report Deleted: " + report.getTitle();
        String body = buildDeletedBody(report);

        sendSafely(getAdminEmail(), null, subject, body);

        if (report.getReporter() != null) {
            sendSafely(report.getReporter().getEmailAddress(),
                    report.getReporter().getUserName(),
                    "[Roller] Your bug report has been removed", body);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Sends a plain-text email via the MailerSend API.
     * Logs a warning and returns silently on any error so one failed send
     * does not abort the notification fan-out.
     *
     * @param toAddress   recipient email address
     * @param toName      recipient display name (may be null)
     * @param subject     email subject line
     * @param body        plain-text email body
     */
    private void sendSafely(String toAddress, String toName,
                             String subject, String body) {
        if (toAddress == null || toAddress.trim().isEmpty()) {
            log.warn("MailerSendChannel: skipping send — recipient is null/empty for subject: " + subject);
            return;
        }
        String token = getToken();
        if (token == null || token.trim().isEmpty()) {
            log.warn("MailerSendChannel: skipping send — bugreport.mailersend.token is not configured");
            return;
        }
        String fromEmail = getFromEmail();
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.warn("MailerSendChannel: skipping send — bugreport.mailersend.from.email is not configured");
            return;
        }

        try {
            Email email = new Email();
            email.setFrom(getFromName(), fromEmail);
            email.addRecipient(toName != null ? toName : toAddress, toAddress);
            email.setSubject(subject);
            email.setPlain(body);

            MailerSend ms = new MailerSend();
            ms.setToken(token);

            MailerSendResponse response = ms.emails().send(email);
            log.debug("MailerSendChannel: sent to " + toAddress
                    + " — message id: " + response.messageId);
        } catch (MailerSendException e) {
            log.error("MailerSendChannel: API error sending to " + toAddress
                    + " (HTTP " + e.code + "): " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("MailerSendChannel: unexpected error sending to " + toAddress, e);
        }
    }

    // -------------------------------------------------------------------------
    // Email body builders
    // -------------------------------------------------------------------------

    private String buildCreatedBody(BugReport report) {
        return "A new bug report has been submitted.\n\n"
                + "Title:       " + report.getTitle() + "\n"
                + "Severity:    " + report.getSeverity() + "\n"
                + "Reporter:    " + (report.getReporter() != null
                        ? report.getReporter().getUserName() : "unknown") + "\n"
                + "Description:\n" + report.getDescription();
    }

    private String buildReporterAckBody(BugReport report) {
        return "Thank you for submitting a bug report.\n\n"
                + "Title:    " + report.getTitle() + "\n"
                + "Severity: " + report.getSeverity() + "\n"
                + "Status:   " + report.getStatus() + "\n\n"
                + "We will review your report and update you on any status changes.";
    }

    private String buildStatusBody(BugReport report, BugReport.Status oldStatus,
                                   BugReport.Status newStatus) {
        return "The status of a bug report has changed.\n\n"
                + "Title:      " + report.getTitle() + "\n"
                + "Old Status: " + oldStatus + "\n"
                + "New Status: " + newStatus + "\n"
                + (report.getAdminNotes() != null
                        ? "Admin Notes: " + report.getAdminNotes() + "\n" : "");
    }

    private String buildDeletedBody(BugReport report) {
        return "A bug report has been deleted.\n\n"
                + "Title:    " + report.getTitle() + "\n"
                + "Severity: " + report.getSeverity() + "\n"
                + "Reporter: " + (report.getReporter() != null
                        ? report.getReporter().getUserName() : "unknown");
    }
}

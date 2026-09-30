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
 * limitations under the License.
 */
package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.notifications.MailerSendNotificationChannel;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Live-fire test for MailerSendNotificationChannel.
 *
 * REQUIRES the following system properties (or roller-custom.properties entries) to be set:
 *   bugreport.mailersend.token        — your MailerSend API token
 *   bugreport.mailersend.from.email   — verified sender in your MailerSend account
 *   bugreport.email.admin             — where the admin notification should land
 *
 * Run with:
 *   mvn -pl app test -Dtest=MailerSendNotificationChannelTest
 */
public class MailerSendNotificationChannelTest {

    @BeforeAll
    public static void bootstrapRoller() throws Exception {
        TestUtils.setupWeblogger();
    }

    @Test
    public void testOnBugCreated() throws Exception {
        // Skip if the token is not configured (CI environment)
        String token = org.apache.roller.weblogger.config.WebloggerConfig
                .getProperty("bugreport.mailersend.token");
        Assumptions.assumeTrue(token != null && !token.trim().isEmpty(),
                "Skipping MailerSend live test — bugreport.mailersend.token not configured");

        User reporter = makeUser("reporter@test-dnvo4d9kn0rg5r86.mlsender.net", "testreporter");
        BugReport report = makeReport(reporter);

        MailerSendNotificationChannel channel = new MailerSendNotificationChannel();

        // Should complete without throwing — check your MailerSend dashboard for delivery
        channel.onBugCreated(report);
    }

    @Test
    public void testOnStatusChanged() throws Exception {
        String token = org.apache.roller.weblogger.config.WebloggerConfig
                .getProperty("bugreport.mailersend.token");
        Assumptions.assumeTrue(token != null && !token.trim().isEmpty(),
                "Skipping MailerSend live test — bugreport.mailersend.token not configured");

        User reporter = makeUser("reporter@test-dnvo4d9kn0rg5r86.mlsender.net", "testreporter");
        BugReport report = makeReport(reporter);
        report.setStatus(BugReport.Status.RESOLVED);
        report.setAdminNotes("Confirmed and patched in v1.2.");

        MailerSendNotificationChannel channel = new MailerSendNotificationChannel();
        channel.onStatusChanged(report, BugReport.Status.OPEN, BugReport.Status.RESOLVED);
    }

    @Test
    public void testOnBugDeleted() throws Exception {
        String token = org.apache.roller.weblogger.config.WebloggerConfig
                .getProperty("bugreport.mailersend.token");
        Assumptions.assumeTrue(token != null && !token.trim().isEmpty(),
                "Skipping MailerSend live test — bugreport.mailersend.token not configured");

        User reporter = makeUser("reporter@test-dnvo4d9kn0rg5r86.mlsender.net", "testreporter");
        BugReport report = makeReport(reporter);

        MailerSendNotificationChannel channel = new MailerSendNotificationChannel();
        channel.onBugDeleted(report);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User makeUser(String email, String username) {
        User u = new User();
        u.setUserName(username);
        u.setEmailAddress(email);
        return u;
    }

    private BugReport makeReport(User reporter) {
        BugReport r = new BugReport();
        r.setId("test-id-001");
        r.setTitle("Login button unresponsive on mobile");
        r.setDescription("Tapping the login button on iOS Safari does nothing. "
                + "No error appears. Tested on iPhone 14, iOS 17.2.");
        r.setSeverity(BugReport.Severity.HIGH);
        r.setStatus(BugReport.Status.OPEN);
        r.setReporter(reporter);
        r.setCreatedTime(new java.util.Date());
        return r;
    }
}

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

import org.apache.roller.weblogger.pojos.BugReport;

/**
 * A pluggable notification channel.
 * Implement this interface to add a new channel (e.g. Slack, MS Teams, SMS).
 * Each channel must be completely independent — a failure in one must never
 * affect the others.
 */
public interface NotificationChannel {

    /** Human-readable name used in logs, e.g. "EmailChannel". */
    String getChannelName();

    /** Called when a new bug report is submitted. */
    void onBugCreated(BugReport report);

    /** Called when an admin changes the status of a bug report. */
    void onStatusChanged(BugReport report, BugReport.Status oldStatus,
                         BugReport.Status newStatus);

    /** Called when an admin soft-deletes a bug report. */
    void onBugDeleted(BugReport report);
}

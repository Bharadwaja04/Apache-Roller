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
import org.apache.roller.weblogger.pojos.BugReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Fans out lifecycle events to every registered NotificationChannel.
 * Each channel is invoked in a try/catch so one broken channel cannot
 * prevent the others from firing.
 */
@com.google.inject.Singleton
public class NotificationServiceImpl implements NotificationService {

    private static final Log log = LogFactory.getLog(NotificationServiceImpl.class);

    private final List<NotificationChannel> channels;

    @com.google.inject.Inject
    public NotificationServiceImpl(Set<NotificationChannel> channels) {
        this.channels = new ArrayList<>(channels);
    }

    @Override
    public void notifyBugCreated(BugReport report) {
        for (NotificationChannel channel : channels) {
            try {
                channel.onBugCreated(report);
            } catch (Exception e) {
                log.error("[" + channel.getChannelName() + "] Failed on notifyBugCreated for report "
                        + report.getId(), e);
            }
        }
    }

    @Override
    public void notifyStatusChanged(BugReport report, BugReport.Status oldStatus,
                                    BugReport.Status newStatus) {
        for (NotificationChannel channel : channels) {
            try {
                channel.onStatusChanged(report, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("[" + channel.getChannelName() + "] Failed on notifyStatusChanged for report "
                        + report.getId(), e);
            }
        }
    }

    @Override
    public void notifyBugDeleted(BugReport report) {
        for (NotificationChannel channel : channels) {
            try {
                channel.onBugDeleted(report);
            } catch (Exception e) {
                log.error("[" + channel.getChannelName() + "] Failed on notifyBugDeleted for report "
                        + report.getId(), e);
            }
        }
    }
}

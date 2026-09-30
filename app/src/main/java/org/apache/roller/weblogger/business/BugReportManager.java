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
package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.User;

import java.util.List;

/**
 * Manages bug report lifecycle: creation, status changes, deletion.
 */
public interface BugReportManager {

    /** Persist a new bug report and fire create notification. */
    void saveBugReport(BugReport report) throws WebloggerException;

    /** Update status and fire status-change notification. */
    void updateStatus(BugReport report, BugReport.Status newStatus,
                      String adminNotes) throws WebloggerException;

    /** Soft-delete (sets status=DELETED) and fires delete notification. */
    void deleteBugReport(BugReport report) throws WebloggerException;

    BugReport getBugReport(String id) throws WebloggerException;

    List<BugReport> getAll() throws WebloggerException;

    List<BugReport> getByStatus(BugReport.Status status) throws WebloggerException;

    List<BugReport> getByReporter(User reporter) throws WebloggerException;

    long countByStatus(BugReport.Status status) throws WebloggerException;
}

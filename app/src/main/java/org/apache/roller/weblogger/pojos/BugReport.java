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
package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a user-submitted bug report.
 * JPA mapping is defined in BugReport.orm.xml.
 */
public class BugReport implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status   { OPEN, RESOLVED, DELETED }
    public enum Severity { LOW, NORMAL, HIGH, CRITICAL }

    private String   id;
    private String   title;
    private String   description;
    private User     reporter;
    private Status   status   = Status.OPEN;
    private Severity severity = Severity.NORMAL;
    private Date     createdTime;
    private Date     updatedTime;
    private Date     resolvedTime;
    private String   adminNotes;

    public BugReport() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Date getCreatedTime() { return createdTime != null ? (Date) createdTime.clone() : null; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime != null ? (Date) createdTime.clone() : null; }

    public Date getUpdatedTime() { return updatedTime != null ? (Date) updatedTime.clone() : null; }
    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime != null ? (Date) updatedTime.clone() : null; }

    public Date getResolvedTime() { return resolvedTime != null ? (Date) resolvedTime.clone() : null; }
    public void setResolvedTime(Date resolvedTime) { this.resolvedTime = resolvedTime != null ? (Date) resolvedTime.clone() : null; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
}

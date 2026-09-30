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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * POJO representing a user's starred blog.
 */
public class StarredWeblog implements Serializable {

    private static final long serialVersionUID = -1234567890123456789L;

    private String id = UUIDGenerator.generateUUID();
    private User user;
    private Weblog weblog;
    private Date dateStarred = new Date();

    public StarredWeblog() {
    }

    public StarredWeblog(User user, Weblog weblog) {
        this.user = user;
        this.weblog = weblog;
        this.dateStarred = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public Date getDateStarred() {
        return dateStarred;
    }

    public void setDateStarred(Date dateStarred) {
        this.dateStarred = dateStarred;
    }

    @Override
    public String toString() {
        return "StarredWeblog{" +
                "id='" + id + '\'' +
                ", user=" + user.getUserName() +
                ", weblog=" + weblog.getHandle() +
                ", dateStarred=" + dateStarred +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StarredWeblog)) {
            return false;
        }
        StarredWeblog o = (StarredWeblog) other;
        return new EqualsBuilder()
                .append(getUser(), o.getUser())
                .append(getWeblog(), o.getWeblog())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUser())
                .append(getWeblog())
                .toHashCode();
    }
}

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

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.RollerPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

/**
 * Focused interface for permission-related operations.
 */
public interface PermissionManager {

    boolean checkPermission(RollerPermission perm, User user) throws WebloggerException;

    void grantWeblogPermission(Weblog weblog, User user, List<String> actions) throws WebloggerException;
    void grantWeblogPermissionPending(Weblog weblog, User user, List<String> actions) throws WebloggerException;
    void confirmWeblogPermission(Weblog weblog, User user) throws WebloggerException;
    void declineWeblogPermission(Weblog weblog, User user) throws WebloggerException;
    void revokeWeblogPermission(Weblog weblog, User user, List<String> actions) throws WebloggerException;

    List<WeblogPermission> getWeblogPermissions(User user) throws WebloggerException;
    List<WeblogPermission> getPendingWeblogPermissions(User user) throws WebloggerException;
    List<WeblogPermission> getWeblogPermissions(Weblog weblog) throws WebloggerException;
    List<WeblogPermission> getPendingWeblogPermissions(Weblog weblog) throws WebloggerException;
    List<WeblogPermission> getWeblogPermissionsIncludingPending(Weblog weblog) throws WebloggerException;

    WeblogPermission getWeblogPermission(Weblog weblog, User user) throws WebloggerException;
    WeblogPermission getWeblogPermissionIncludingPending(Weblog weblog, User user) throws WebloggerException;
}

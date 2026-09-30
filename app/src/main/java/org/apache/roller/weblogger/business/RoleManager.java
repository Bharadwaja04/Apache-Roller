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
import org.apache.roller.weblogger.pojos.User;

/**
 * Focused interface for role management.
 */
public interface RoleManager {

    void grantRole(String roleName, User user) throws WebloggerException;
    void revokeRole(String roleName, User user) throws WebloggerException;

    /**
     * Returns true if user has role specified, should be used only for testing.
     * @deprecated Use checkPermission() on PermissionManager instead.
     */
    @Deprecated
    boolean hasRole(String roleName, User user) throws WebloggerException;

    /**
     * Get roles associated with user, should be used only for testing.
     * @deprecated Use checkPermission() on PermissionManager instead.
     */
    @Deprecated
    List<String> getRoles(User user) throws WebloggerException;
}

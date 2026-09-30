/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

/**
 * Interface for user-related services.
 * Extracted from Weblogger to address Cyclic-Dependent Modularization.
 * 
 * Groups user management services together to reduce coupling.
 */
public interface UserServices {

    /**
     * Get UserManager for user operations.
     * 
     * @return UserManager instance
     */
    UserManager getUserManager();

    /**
     * Get BookmarkManager for bookmark operations.
     * 
     * @return BookmarkManager instance
     */
    BookmarkManager getBookmarkManager();

    /**
     * Get OAuthManager for OAuth operations.
     * 
     * @return OAuthManager instance
     */
    OAuthManager getOAuthManager();
}
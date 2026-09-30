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

import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.search.IndexManager;

/**
 * Interface for system-level services.
 * Extracted from Weblogger to address Cyclic-Dependent Modularization.
 * 
 * Groups system management services together to reduce coupling.
 */
public interface SystemServices {

    /**
     * Get PropertiesManager for configuration operations.
     * 
     * @return PropertiesManager instance
     */
    PropertiesManager getPropertiesManager();

    /**
     * Get ThreadManager for thread management operations.
     * 
     * @return ThreadManager instance
     */
    ThreadManager getThreadManager();

    /**
     * Get IndexManager for search index operations.
     * 
     * @return IndexManager instance
     */
    IndexManager getIndexManager();

    /**
     * Get URLStrategy for URL building.
     * 
     * @return URLStrategy instance
     */
    URLStrategy getUrlStrategy();
}
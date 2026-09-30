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

import org.apache.roller.weblogger.WebloggerException;

/**
 * Core lifecycle interface for Weblogger.
 * Extracted from Weblogger to address Cyclic-Dependent Modularization.
 * 
 * This interface handles application lifecycle operations separate from
 * manager access to break circular dependencies.
 */
public interface WebloggerCore {

    /**
     * Initialize any resources necessary for this instance of Weblogger.
     * 
     * @throws InitializationException if initialization fails
     */
    void initialize() throws InitializationException;

    /**
     * Release all resources necessary for this instance of Weblogger.
     */
    void shutdown();

    /**
     * Flush object states to persistent storage.
     * 
     * @throws WebloggerException if flush fails
     */
    void flush() throws WebloggerException;

    /**
     * Release all resources associated with Weblogger session.
     */
    void release();

    /**
     * Get Weblogger version string.
     * 
     * @return version string
     */
    String getVersion();

    /**
     * Get Weblogger source code management revision.
     * 
     * @return revision string
     */
    String getRevision();

    /**
     * Get Weblogger build time.
     * 
     * @return build time string
     */
    String getBuildTime();

    /**
     * Get username that built Weblogger.
     * 
     * @return build user string
     */
    String getBuildUser();
}
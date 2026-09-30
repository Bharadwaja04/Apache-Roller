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
 * Interface for content-related services.
 * Extracted from Weblogger to address Cyclic-Dependent Modularization.
 * 
 * Groups content management services together to reduce coupling.
 */
public interface ContentServices {

    /**
     * Get WeblogManager for weblog operations.
     * 
     * @return WeblogManager instance
     */
    WeblogManager getWeblogManager();

    /**
     * Get WeblogEntryManager for entry operations.
     * 
     * @return WeblogEntryManager instance
     */
    WeblogEntryManager getWeblogEntryManager();

    /**
     * Get MediaFileManager for media file operations.
     * 
     * @return MediaFileManager instance
     */
    MediaFileManager getMediaFileManager();

    /**
     * Get FileContentManager for file content operations.
     * 
     * @return FileContentManager instance
     */
    FileContentManager getFileContentManager();
}
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * Interface to weblog and weblog custom template management.
 * 
 * <p>This interface now extends focused sub-interfaces to provide better
 * separation of concerns and reduce coupling. Clients that only need
 * specific functionality can depend on the appropriate sub-interface:</p>
 * 
 * <ul>
 *   <li>{@link WeblogCrudOperations} - Basic CRUD operations</li>
 *   <li>{@link WeblogQueryOperations} - Search and filter operations</li>
 *   <li>{@link WeblogUserOperations} - User-Weblog relationships</li>
 *   <li>{@link WeblogStatisticsOperations} - Statistics and counts</li>
 *   <li>{@link WeblogTemplateOperations} - Template management</li>
 * </ul>
 * 
 * <p>Design Pattern: Interface Segregation Principle (ISP)</p>
 * 
 * @see WeblogCrudOperations
 * @see WeblogQueryOperations
 * @see WeblogUserOperations
 * @see WeblogStatisticsOperations
 * @see WeblogTemplateOperations
 */
public interface WeblogManager extends 
        WeblogCrudOperations,
        WeblogQueryOperations,
        WeblogUserOperations,
        WeblogStatisticsOperations,
        WeblogTemplateOperations {

    // ========================================================================
    // All methods are now inherited from sub-interfaces.
    // This interface serves as the composite for backward compatibility.
    // ========================================================================

    // -------------------- CRUD Operations (from WeblogCrudOperations) --------------------
    // void addWeblog(Weblog newWebsite) throws WebloggerException;
    // void saveWeblog(Weblog data) throws WebloggerException;
    // void removeWeblog(Weblog website) throws WebloggerException;
    // Weblog getWeblog(String id) throws WebloggerException;
    // Weblog getWeblogByHandle(String handle) throws WebloggerException;
    // Weblog getWeblogByHandle(String handle, Boolean enabled) throws WebloggerException;

    // -------------------- Query Operations (from WeblogQueryOperations) --------------------
    // List<Weblog> getWeblogs(Boolean enabled, Boolean active, Date startDate, 
    //                         Date endDate, int offset, int length) throws WebloggerException;
    // List<Weblog> getWeblogsByLetter(char letter, int offset, int length) throws WebloggerException;

    // -------------------- User Operations (from WeblogUserOperations) --------------------
    // List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException;
    // List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException;

    // -------------------- Statistics Operations (from WeblogStatisticsOperations) --------------------
    // List<StatCount> getMostCommentedWeblogs(Date startDate, Date endDate, 
    //                                         int offset, int length) throws WebloggerException;
    // Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException;
    // long getWeblogCount() throws WebloggerException;

    // -------------------- Template Operations (from WeblogTemplateOperations) --------------------
    // void saveTemplate(WeblogTemplate data) throws WebloggerException;
    // void removeTemplate(WeblogTemplate template) throws WebloggerException;
    // WeblogTemplate getTemplate(String id) throws WebloggerException;
    // WeblogTemplate getTemplateByAction(Weblog w, ComponentType a) throws WebloggerException;
    // WeblogTemplate getTemplateByName(Weblog w, String p) throws WebloggerException;
    // WeblogTemplate getTemplateByLink(Weblog w, String p) throws WebloggerException;
    // void saveTemplateRendition(CustomTemplateRendition templateCode) throws WebloggerException;
    // List<WeblogTemplate> getTemplates(Weblog w) throws WebloggerException;

    /**
     * Release any resources held by manager.
     */
    void release();
}
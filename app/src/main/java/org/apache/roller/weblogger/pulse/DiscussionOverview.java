/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  For additional information regarding
 *  copyright in this work, please see the NOTICE file in the top level
 *  directory of this distribution.
 */

package org.apache.roller.weblogger.pulse;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * POJO representing a lightweight overview of comment activity for a weblog entry.
 * Contains three indicators: activity level, response types, and recurring points.
 */
public class DiscussionOverview implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int activityLevel;
    private Map<String, Integer> responseTypes;
    private List<KeywordFrequency> recurringPoints;
    private Timestamp lastUpdated;
    
    public DiscussionOverview() {
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }
    
    public DiscussionOverview(int activityLevel, Map<String, Integer> responseTypes, 
                              List<KeywordFrequency> recurringPoints) {
        this.activityLevel = activityLevel;
        this.responseTypes = responseTypes;
        this.recurringPoints = recurringPoints;
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }
    
    public int getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(int activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public Map<String, Integer> getResponseTypes() {
        return responseTypes;
    }
    
    public void setResponseTypes(Map<String, Integer> responseTypes) {
        this.responseTypes = responseTypes;
    }
    
    public List<KeywordFrequency> getRecurringPoints() {
        return recurringPoints;
    }
    
    public void setRecurringPoints(List<KeywordFrequency> recurringPoints) {
        this.recurringPoints = recurringPoints;
    }
    
    public Timestamp getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public void updateTimestamp() {
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }
}

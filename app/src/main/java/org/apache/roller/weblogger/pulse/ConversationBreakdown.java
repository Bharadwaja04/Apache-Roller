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
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing an AI-powered conversation breakdown for a weblog entry.
 * Contains major themes, representative comments, and overall recap.
 */
public class ConversationBreakdown implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<Theme> themes;
    private String overallRecap;
    private String methodUsed;
    private Timestamp generatedAt;
    
    public ConversationBreakdown() {
        this.themes = new ArrayList<>();
        this.generatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    public ConversationBreakdown(List<Theme> themes, String overallRecap, String methodUsed) {
        this.themes = themes != null ? themes : new ArrayList<>();
        this.overallRecap = overallRecap;
        this.methodUsed = methodUsed;
        this.generatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    public List<Theme> getThemes() {
        return themes;
    }
    
    public void setThemes(List<Theme> themes) {
        this.themes = themes != null ? themes : new ArrayList<>();
    }
    
    public String getOverallRecap() {
        return overallRecap;
    }
    
    public void setOverallRecap(String overallRecap) {
        this.overallRecap = overallRecap;
    }
    
    public String getMethodUsed() {
        return methodUsed;
    }
    
    public void setMethodUsed(String methodUsed) {
        this.methodUsed = methodUsed;
    }
    
    public Timestamp getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public void addTheme(Theme theme) {
        if (theme != null) {
            themes.add(theme);
        }
    }
    
    public boolean hasThemes() {
        return themes != null && !themes.isEmpty();
    }
}

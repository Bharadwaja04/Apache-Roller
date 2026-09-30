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

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing a theme identified in conversation breakdown.
 * Contains theme name, description, and representative comments.
 */
public class Theme implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String description;
    private List<WeblogEntryComment> representativeComments;
    
    public Theme() {
        this.representativeComments = new ArrayList<>();
    }
    
    public Theme(String name, String description) {
        this.name = name;
        this.description = description;
        this.representativeComments = new ArrayList<>();
    }
    
    public Theme(String name, String description, List<WeblogEntryComment> representativeComments) {
        this.name = name;
        this.description = description;
        this.representativeComments = representativeComments != null ? representativeComments : new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<WeblogEntryComment> getRepresentativeComments() {
        return representativeComments;
    }
    
    public void setRepresentativeComments(List<WeblogEntryComment> representativeComments) {
        this.representativeComments = representativeComments;
    }
    
    public void addRepresentativeComment(WeblogEntryComment comment) {
        if (comment != null && !representativeComments.contains(comment)) {
            representativeComments.add(comment);
        }
    }
}

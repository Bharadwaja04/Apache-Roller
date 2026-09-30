/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
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
 * copyright in this work, please see the NOTICE in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pulse.ConversationBreakdown;
import org.apache.roller.weblogger.pulse.ConversationBreakdownCalculator;

import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;

/**
 * Step 5: Implement conversation breakdown analysis.
 */
@AllowedMethods({"execute"})
public class PulseBreakdown extends UIAction {
    
    private static Log log = LogFactory.getLog(PulseBreakdown.class);
    
    private String entryId;
    private WeblogEntry entry;
    private List<WeblogEntryComment> comments;
    private ConversationBreakdown conversationBreakdown = null;
    
    @Override
    public String execute() {
        System.out.println("=== Step 5: PulseBreakdown execute called ===");
        System.out.println("entryId: " + entryId);
        
        log.info("=== Step 5: PulseBreakdown execute called ===");
        log.info("entryId: " + entryId);
        
        // Load the entry
        if (StringUtils.isNotEmpty(entryId)) {
            try {
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                entry = wmgr.getWeblogEntry(entryId);
                
                if (entry != null) {
                    System.out.println("Step 5: Entry loaded successfully!");
                    System.out.println("Entry title: " + entry.getTitle());
                    
                    log.info("Step 5: Entry loaded successfully!");
                    log.info("Entry title: " + entry.getTitle());
                    
                    // Load approved comments
                    CommentSearchCriteria csc = new CommentSearchCriteria();
                    csc.setEntry(entry);
                    csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
                    csc.setMaxResults(-1);
                    comments = wmgr.getComments(csc);
                    
                    System.out.println("Step 5: Found " + comments.size() + " approved comments");
                    log.info("Step 5: Found " + comments.size() + " approved comments");
                    
                    // Generate conversation breakdown
                    if (!comments.isEmpty()) {
                        System.out.println("Step 5: Generating conversation breakdown...");
                        ConversationBreakdownCalculator calculator = new ConversationBreakdownCalculator();
                        conversationBreakdown = calculator.generateBreakdown(entry, null);
                        
                        System.out.println("Step 5: Breakdown generated successfully!");
                        System.out.println("Method used: " + conversationBreakdown.getMethodUsed());
                        System.out.println("Themes count: " + conversationBreakdown.getThemes().size());
                        
                        log.info("Step 5: Breakdown generated successfully!");
                        log.info("Method used: " + conversationBreakdown.getMethodUsed());
                        log.info("Themes count: " + conversationBreakdown.getThemes().size());
                        
                        // Display theme previews
                        for (int i = 0; i < conversationBreakdown.getThemes().size(); i++) {
                            org.apache.roller.weblogger.pulse.Theme theme = conversationBreakdown.getThemes().get(i);
                            String preview = theme.getName().length() > 50 ? theme.getName().substring(0, 50) + "..." : theme.getName();
                            System.out.println("  Theme " + (i+1) + ": " + preview);
                        }
                        
                    } else {
                        System.out.println("Step 5: No comments available for breakdown");
                        log.warn("Step 5: No comments available for breakdown");
                        conversationBreakdown = null;
                    }
                    
                } else {
                    System.out.println("Step 5: Entry not found!");
                    log.error("Step 5: Entry not found for ID: " + entryId);
                    addActionError("Entry not found");
                    return ERROR;
                }
            } catch (WebloggerException e) {
                System.out.println("Step 5: Error loading entry: " + e.getMessage());
                log.error("Step 5: Error loading entry", e);
                addActionError("Error loading entry: " + e.getMessage());
                return ERROR;
            }
        } else {
            System.out.println("Step 5: Entry ID is empty!");
            log.error("Step 5: Entry ID is empty");
            addActionError("Entry ID is required");
            return ERROR;
        }
        
        return SUCCESS;
    }
    
    public String getEntryId() {
        return entryId;
    }
    
    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
    
    public WeblogEntry getEntry() {
        return entry;
    }
    
    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }
    
    public List<WeblogEntryComment> getComments() {
        return comments;
    }
    
    public void setComments(List<WeblogEntryComment> comments) {
        this.comments = comments;
    }
    
    public ConversationBreakdown getConversationBreakdown() {
        return conversationBreakdown;
    }
    
    public void setConversationBreakdown(ConversationBreakdown conversationBreakdown) {
        this.conversationBreakdown = conversationBreakdown;
    }
}

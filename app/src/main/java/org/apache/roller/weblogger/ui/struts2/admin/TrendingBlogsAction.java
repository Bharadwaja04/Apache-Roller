/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Action that exposes the Top 5 Trending Blogs based on like count.
 *
 * <p>Fetches at most 5 PUBLISHED entries ordered by number of likes (stars)
 * descending.  If fewer than 5 entries have likes, the remaining slots are
 * filled with the most-recently-published entries.
 *
 * <p>URL: {@code /roller-ui/trendingBlogs.rol}
 */
public class TrendingBlogsAction extends UIAction {

    private static final Log log = LogFactory.getLog(TrendingBlogsAction.class);
    private static final int MAX_TRENDING = 5;

    // ------ data exposed to the JSP ------
    private List<TrendingPost> trendingBlogs = Collections.emptyList();

    public TrendingBlogsAction() {
        this.actionName = "trendingBlogs";
        this.desiredMenu = "main";
        this.pageTitle = "tabbedmenu.admin.trendingBlogs";
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.LOGIN);
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {
        List<TrendingPost> result = new ArrayList<>();
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

            // Step 1 – query returns [entryId, likeCount] pairs (avoids Derby CLOB GROUP BY issue)
            List<Object[]> rows = umgr.getTrendingEntries(MAX_TRENDING);
            Set<String> includedIds = new LinkedHashSet<>();

            System.out.println("\n============= TRENDING BLOGS: FETCHING =============");

            for (Object[] row : rows) {
                String entryId = (String) row[0];
                long likes = (Long) row[1];
                WeblogEntry entry = wmgr.getWeblogEntry(entryId);
                if (entry != null) {
                    result.add(new TrendingPost(entry, likes));
                    includedIds.add(entryId);
                    System.out.println("  LIKED: \"" + entry.getTitle() + "\" -> " + likes + " likes");
                }
            }

            // Step 2 – pad with recent published posts if we don't have enough
            if (result.size() < MAX_TRENDING) {
                System.out.println("  (Padding: have " + result.size() + ", need " + MAX_TRENDING + ")");
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                wesc.setMaxResults(MAX_TRENDING * 2);
                List<WeblogEntry> recent = wmgr.getWeblogEntries(wesc);
                for (WeblogEntry e : recent) {
                    if (!includedIds.contains(e.getId())) {
                        result.add(new TrendingPost(e, 0L));
                        includedIds.add(e.getId());
                        System.out.println("  PADDED: \"" + e.getTitle() + "\" -> 0 likes");
                        if (result.size() >= MAX_TRENDING) {
                            break;
                        }
                    }
                }
            }

            System.out.println("------------- FINAL LIST (" + result.size() + " posts) -------------");
            int rank = 1;
            for (TrendingPost tp : result) {
                System.out.println("  #" + rank + " " + tp.getTitle() + " (" + tp.getLikes() + " likes)");
                rank++;
            }
            System.out.println("====================================================\n");

        } catch (WebloggerException ex) {
            log.error("Error fetching trending blogs", ex);
            System.out.println("  ERROR fetching trending blogs: " + ex.getMessage());
            addError("generic.error.check.logs");
        }

        this.trendingBlogs = result;
        return SUCCESS;
    }

    // ----- getter for JSP -----
    public List<TrendingPost> getTrendingBlogs() {
        return trendingBlogs;
    }

    // ===== Simple POJO exposed to JSP via Struts2 OGNL =====
    public static class TrendingPost {
        private final String id;
        private final String title;
        private final String summary;
        private final String text;
        private final String permalink;
        private final long likes;

        public TrendingPost(WeblogEntry entry, long likes) {
            this.id = entry.getId();
            this.title = entry.getTitle();
            this.summary = entry.getSummary();
            this.text = entry.getText();
            this.permalink = entry.getPermalink();
            this.likes = likes;
        }

        public String getId()        { return id; }
        public String getTitle()     { return title; }
        public String getSummary()   { return summary; }
        public String getText()      { return text; }
        public String getPermalink() { return permalink; }
        public long   getLikes()     { return likes; }
    }
}

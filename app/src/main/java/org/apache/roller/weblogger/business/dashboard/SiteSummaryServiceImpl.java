/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JPA-backed implementation of {@link SiteSummaryService}.
 *
 * Exposes 5 metrics for the Site Summary Dashboard:
 *   1. Total Users
 *   2. Total Blog Posts (all published entries)
 *   3. Top Category  (tech / finance / general  —  score = 0.7 * likes + 0.3 * posts)
 *   4. Most Liked Post
 *   5. Total Likes
 */
public class SiteSummaryServiceImpl implements SiteSummaryService {

    private static final Log log = LogFactory.getLog(SiteSummaryServiceImpl.class);

    /** Fixed set of categories to evaluate for Top Category. */
    private static final List<String> CATEGORIES = Arrays.asList("tech", "finance", "general");

    // -------------------------------------------------------------------------
    // 1. Total Users
    // -------------------------------------------------------------------------

    @Override
    public MetricComputer totalUsers() {
        return () -> {
            try {
                long count = WebloggerFactory.getWeblogger()
                        .getUserManager()
                        .getUserCount();

                return new DashboardMetric("Total Users", String.valueOf(count),
                        Collections.emptyList());

            } catch (Exception e) {
                log.error("Error computing totalUsers", e);
                return new DashboardMetric("Total Users", "N/A",
                        Collections.singletonList(e.getMessage()));
            }
        };
    }

    // -------------------------------------------------------------------------
    // 2. Total Blog Posts — count of ALL published entries across every weblog
    // -------------------------------------------------------------------------

    @Override
    public MetricComputer totalPosts() {
        return () -> {
            try {
                WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager();
                
                if (entryMgr == null) {
                    log.warn("WeblogEntryManager is null!");
                    return new DashboardMetric("Total Blog Posts", "0",
                            Collections.singletonList("Database not initialized"));
                }
                
                long total = entryMgr.getTotalPublishedEntryCount();
                log.info("DEBUG: Total Blog Posts = " + total);
                return new DashboardMetric("Total Blog Posts", String.valueOf(total),
                        Collections.emptyList());

            } catch (Exception e) {
                log.error("Error computing totalPosts. Full Exception:", e);
                e.printStackTrace();
                return new DashboardMetric("Total Blog Posts", "0",
                        Collections.singletonList("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            }
        };
    }

    // -------------------------------------------------------------------------
    // 3. Top Category
    //    Evaluates: tech, finance, general
    //    Score = 0.7 * totalLikesInCategory + 0.3 * totalPostsInCategory
    //    Shows the winner and breakdown for all 3 categories in the details.
    // -------------------------------------------------------------------------

    @Override
    public MetricComputer topCategory() {
        return () -> {
            try {
                WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

                if (emgr == null || umgr == null) {
                    log.warn("WeblogEntryManager or UserManager is null!");
                    return new DashboardMetric("Top Category", "None",
                            Collections.singletonList("Database not initialized"));
                }

                // postCounts: map of categoryName -> number of published posts
                Map<String, Long> postCounts = emgr.getTopCategories(Integer.MAX_VALUE);
                if (postCounts == null) postCounts = Collections.emptyMap();
                
                // likeCounts: map of categoryName -> number of likes/stars on posts in that category
                Map<String, Long> likeCounts = umgr.getCategoryLikeCounts();
                if (likeCounts == null) likeCounts = Collections.emptyMap();

                log.info("DEBUG: postCounts = " + postCounts);
                log.info("DEBUG: likeCounts = " + likeCounts);

                String winner = "None";
                double highestScore = -1;

                // Build detail lines for each category
                List<String> details = new java.util.ArrayList<>();

                for (String cat : CATEGORIES) {
                    long posts = postCounts.getOrDefault(cat, 0L);
                    long likes = likeCounts.getOrDefault(cat, 0L);
                    double score = (0.7 * likes) + (0.3 * posts);

                    // Format: "tech – 5 posts, 12 likes (score: 9.9)"
                    details.add(cat + " – " + posts + " posts, " + likes + " likes"
                            + " (score: " + String.format("%.1f", score) + ")");

                    if (score > highestScore) {
                        highestScore = score;
                        winner = cat;
                    }
                }

                log.info("DEBUG: Top Category winner = " + winner + ", details = " + details);
                return new DashboardMetric("Top Category", winner, details);

            } catch (Exception e) {
                log.error("Error computing topCategory. Full Exception:", e);
                e.printStackTrace();
                return new DashboardMetric("Top Category", "None",
                        Collections.singletonList("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            }
        };
    }

    // -------------------------------------------------------------------------
    // 4. Most Liked Post
    // -------------------------------------------------------------------------

    @Override
    public MetricComputer mostStarredPost() {
        return () -> {
            try {
                UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
                
                if (umgr == null) {
                    log.warn("UserManager is null!");
                    return new DashboardMetric("Most Liked Post", "None yet",
                            Collections.singletonList("Database not initialized"));
                }
                
                String entryId = umgr.getMostStarredEntryId();

                if (entryId == null) {
                    return new DashboardMetric("Most Liked Post", "None yet",
                            Collections.emptyList());
                }

                WeblogEntry entry = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager()
                        .getWeblogEntry(entryId);

                long likeCount = umgr.getLikeCount(entry);
                String title = (entry != null) ? entry.getTitle() : entryId;

                return new DashboardMetric("Most Liked Post", title,
                        Collections.singletonList(likeCount + " likes"));

            } catch (Exception e) {
                log.error("Error computing mostStarredPost. Full Exception:", e);
                e.printStackTrace();
                return new DashboardMetric("Most Liked Post", "None yet",
                        Collections.singletonList("Error: " + e.getClass().getSimpleName()));
            }
        };
    }

    // -------------------------------------------------------------------------
    // 5. Total Likes — sum of all stars/likes across the whole platform
    // -------------------------------------------------------------------------

    @Override
    public MetricComputer totalStars() {
        return () -> {
            try {
                UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
                
                if (userMgr == null) {
                    log.warn("UserManager is null!");
                    return new DashboardMetric("Total Likes", "0",
                            Collections.singletonList("Database not initialized"));
                }
                
                long total = userMgr.getTotalStarCount();
                log.info("DEBUG: Total Likes = " + total);
                return new DashboardMetric("Total Likes", String.valueOf(total),
                        Collections.emptyList());

            } catch (Exception e) {
                log.error("Error computing totalStars. Full Exception:", e);
                e.printStackTrace();
                return new DashboardMetric("Total Likes", "0",
                        Collections.singletonList("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            }
        };
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

/**
 * Service that exposes factory methods for every supported dashboard metric.
 *
 * <p>Each method returns a {@link MetricComputer} — a lightweight lambda / object
 * that, when invoked, performs the actual data retrieval and returns a
 * {@link DashboardMetric}.  Dashboard <em>views</em> assemble whichever subset
 * of computers they need; they never call service methods directly.
 */
public interface SiteSummaryService {

    /** Total number of enabled registered users. */
    MetricComputer totalUsers();

    /** Total number of published blog posts across all weblogs. */
    MetricComputer totalPosts();

    /** Category that contains the most published posts. */
    MetricComputer topCategory();

    /** The blog post that has been starred/liked the most times. */
    MetricComputer mostStarredPost();

    /** Total number of star/like events recorded across the whole platform. */
    MetricComputer totalStars();
}

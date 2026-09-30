/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Arrays;
import java.util.List;

/**
 * <strong>Full</strong> dashboard view — exposes all eight available metrics for
 * a comprehensive engagement overview.
 *
 * <p>This class contains <em>no data-fetching logic whatsoever</em>.  It simply
 * assembles a complete ordered list of {@link MetricComputer} instances from
 * {@link SiteSummaryService}.  Adding a new metric only requires updating this
 * list (and adding a corresponding factory method to {@link SiteSummaryService}).
 */
public class FullDashboardView implements DashboardView {

    @Override
    public List<MetricComputer> getMetrics(SiteSummaryService service) {
        return Arrays.asList(
            service.totalUsers(),
            service.totalPosts(),
            service.topCategory(),
            service.mostStarredPost(),
            service.totalStars()
        );
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Arrays;
import java.util.List;

/**
 * <strong>Minimalist</strong> dashboard view — shows only the two most essential
 * metrics so administrators can get a quick overview at a glance.
 *
 * <p>This class contains <em>no data-fetching logic whatsoever</em>.  It simply
 * declares which {@link MetricComputer} instances belong in this view by
 * selecting them from the provided {@link SiteSummaryService}.
 */
public class MinimalistDashboardView implements DashboardView {

    @Override
    public List<MetricComputer> getMetrics(SiteSummaryService service) {
        return Arrays.asList(
            service.totalUsers(),
            service.topCategory()
        );
    }
}

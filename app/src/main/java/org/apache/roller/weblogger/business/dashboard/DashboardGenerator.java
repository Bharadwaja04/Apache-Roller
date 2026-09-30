/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates dashboard generation.
 *
 * <p>Usage:
 * <pre>
 *   DashboardGenerator generator = new DashboardGenerator(new SiteSummaryServiceImpl());
 *   List&lt;DashboardMetric&gt; metrics = generator.generate(new FullDashboardView());
 * </pre>
 *
 * <p>The generator is intentionally decoupled from both the view and the
 * underlying data source.  Swapping a view or service implementation requires
 * no changes here.
 */
public class DashboardGenerator {

    private final SiteSummaryService service;

    public DashboardGenerator(SiteSummaryService service) {
        this.service = service;
    }

    /**
     * Generate a list of computed metrics defined by the given view.
     *
     * @param view the view that declares which metrics to include
     * @return ordered list of computed {@link DashboardMetric} objects
     */
    public List<DashboardMetric> generate(DashboardView view) {
        List<DashboardMetric> results = new ArrayList<>();
        for (MetricComputer computer : view.getMetrics(service)) {
            try {
                DashboardMetric metric = computer.compute();
                if (metric != null) {
                    results.add(metric);
                }
            } catch (Exception e) {
                // Fail gracefully — one broken metric should not block the rest
                results.add(new DashboardMetric("Error", "N/A",
                    java.util.Collections.singletonList(e.getMessage())));
            }
        }
        return results;
    }
}

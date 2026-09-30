/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.List;

/**
 * A dashboard view definition — declares <em>which</em> metrics belong in this
 * view without containing any computation logic.
 *
 * <p>Implementors simply assemble a list of {@link MetricComputer} instances
 * supplied by the {@link SiteSummaryService}.  Adding or removing metrics from
 * a view never touches the data-fetching code.
 */
public interface DashboardView {

    /**
     * Return the ordered list of metric computers for this view.
     *
     * @param service the source of metric-computing lambdas
     * @return ordered, non-null list of computers; may be empty but never null
     */
    List<MetricComputer> getMetrics(SiteSummaryService service);
}

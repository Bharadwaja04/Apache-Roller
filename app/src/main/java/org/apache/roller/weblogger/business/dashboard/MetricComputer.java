/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

/**
 * Functional interface: computes and returns exactly one {@link DashboardMetric}.
 *
 * <p>Keeping computation in separate implementations of this interface (rather
 * than inside view definitions) ensures that dashboard views remain free of
 * business logic — they only declare <em>which</em> metrics to display.
 */
@FunctionalInterface
public interface MetricComputer {

    /**
     * Compute the metric and return it.
     *
     * @return a fully populated {@link DashboardMetric}, never {@code null}
     */
    DashboardMetric compute();
}

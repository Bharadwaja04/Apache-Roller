/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Collections;
import java.util.List;

/**
 * Immutable DTO holding one dashboard metric.
 *
 * <ul>
 *   <li><b>name</b>   – human-readable label (e.g. "Total Users")</li>
 *   <li><b>value</b>  – formatted scalar value (e.g. "42")</li>
 *   <li><b>details</b>– optional supporting lines (e.g. usernames, post titles)</li>
 * </ul>
 */
public final class DashboardMetric {

    private final String name;
    private final String value;
    private final List<String> details;

    public DashboardMetric(String name, String value, List<String> details) {
        this.name    = name;
        this.value   = value;
        this.details = details == null ? Collections.emptyList()
                                       : Collections.unmodifiableList(details);
    }

    public String getName()         { return name; }
    public String getValue()        { return value; }
    public List<String> getDetails(){ return details; }

    @Override
    public String toString() {
        return "DashboardMetric{name='" + name + "', value='" + value + "'}";
    }
}

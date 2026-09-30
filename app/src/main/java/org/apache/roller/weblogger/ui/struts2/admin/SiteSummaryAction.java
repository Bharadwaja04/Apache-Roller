/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.dashboard.DashboardGenerator;
import org.apache.roller.weblogger.business.dashboard.DashboardMetric;
import org.apache.roller.weblogger.business.dashboard.DashboardView;
import org.apache.roller.weblogger.business.dashboard.FullDashboardView;
import org.apache.roller.weblogger.business.dashboard.MinimalistDashboardView;
import org.apache.roller.weblogger.business.dashboard.SiteSummaryServiceImpl;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

import java.util.Collections;
import java.util.List;

/**
 * Admin action that generates and exposes the Site Summary Dashboard.
 *
 * <p>URL: {@code /roller-ui/siteSummary.rol[?view=mini|full]}
 *
 * <p>Access: site administrators only ({@link GlobalPermission#ADMIN}).
 */
public class SiteSummaryAction extends UIAction {

    private static final Log log = LogFactory.getLog(SiteSummaryAction.class);

    /** Query parameter: "mini" → MinimalistDashboardView; anything else → FullDashboardView. */
    private String view = "full";

    /** Computed metrics forwarded to the JSP. */
    private List<DashboardMetric> metrics = Collections.emptyList();

    public SiteSummaryAction() {
        this.actionName  = "siteSummary";
        this.desiredMenu = "admin";
        this.pageTitle   = "siteSummary.title";
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {
        try {
            DashboardView dashboardView = "mini".equalsIgnoreCase(view)
                ? new MinimalistDashboardView()
                : new FullDashboardView();

            DashboardGenerator generator = new DashboardGenerator(new SiteSummaryServiceImpl());
            metrics = generator.generate(dashboardView);

        } catch (Exception e) {
            log.error("Error generating site summary dashboard", e);
            addError("generic.error.check.logs");
        }
        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Accessors (used by JSP via Struts2 value stack)
    // -------------------------------------------------------------------------

    public List<DashboardMetric> getMetrics() { return metrics; }

    public String getView() { return view; }

    public void setView(String view) { this.view = view; }
}

<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<style>
    .site-dashboard {
        font-family: 'Inter', 'Segoe UI', sans-serif;
        max-width: 1100px;
        margin: 0 auto;
        padding: 20px;
    }
    .site-dashboard h2 {
        font-size: 1.6em;
        color: #1a1a2e;
        margin-bottom: 4px;
    }
    .site-dashboard .subtitle {
        color: #666;
        margin-bottom: 24px;
        font-size: 0.95em;
    }
    .dashboard-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 20px;
    }
    .metric-card {
        background: #ffffff;
        border-radius: 10px;
        padding: 22px 20px 16px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.09);
        border-left: 5px solid #337ab7;
        transition: transform 0.15s ease, box-shadow 0.15s ease;
    }
    .metric-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 6px 18px rgba(0,0,0,0.13);
    }
    /* Distinct colour per card position */
    .metric-card:nth-child(1) { border-left-color: #4e73df; }
    .metric-card:nth-child(2) { border-left-color: #1cc88a; }
    .metric-card:nth-child(3) { border-left-color: #f6c23e; }
    .metric-card:nth-child(4) { border-left-color: #e74a3b; }
    .metric-card:nth-child(5) { border-left-color: #36b9cc; }

    .metric-name {
        font-size: 0.78em;
        color: #888;
        text-transform: uppercase;
        letter-spacing: 1.2px;
        font-weight: 700;
        margin-bottom: 6px;
    }
    .metric-value {
        font-size: 2em;
        font-weight: 700;
        color: #222;
        margin: 8px 0 12px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .metric-details {
        font-size: 0.82em;
        color: #555;
        list-style: none;
        padding: 0;
        margin: 0;
        border-top: 1px solid #f0f0f0;
        padding-top: 10px;
    }
    .metric-details li {
        margin-bottom: 5px;
        line-height: 1.4;
    }
    .view-toggle {
        display: inline-flex;
        border: 1px solid #337ab7;
        border-radius: 6px;
        overflow: hidden;
    }
    .view-toggle .toggle-option {
        padding: 6px 18px;
        font-size: 0.88em;
        font-weight: 600;
        color: #337ab7;
        background: #fff;
        text-decoration: none;
        transition: background 0.15s, color 0.15s;
        border-right: 1px solid #337ab7;
    }
    .view-toggle .toggle-option:last-child {
        border-right: none;
    }
    .view-toggle .toggle-option.active {
        background: #337ab7;
        color: #fff;
    }
    .view-toggle .toggle-option:hover:not(.active) {
        background: #eaf2fb;
    }
</style>

<div class="site-dashboard">

    <h2>Site Summary Dashboard</h2>
    <p class="subtitle">Monitor overall engagement and activity across the entire platform.</p>

    <s:url var="fullUrl" action="siteSummary" namespace="/roller-ui/admin"><s:param name="view">full</s:param></s:url>
    <s:url var="miniUrl" action="siteSummary" namespace="/roller-ui/admin"><s:param name="view">mini</s:param></s:url>
    <div class="view-toggle" style="margin-bottom: 20px;">
        <a href="<s:property value="#miniUrl" />" class="toggle-option <s:if test='view == "mini"'>active</s:if>">
            <s:text name="siteSummary.view.minimalist" />
        </a>
        <a href="<s:property value="#fullUrl" />" class="toggle-option <s:if test='view == "full" || view == ""'>active</s:if>">
            <s:text name="siteSummary.view.full" />
        </a>
    </div>

    <div class="dashboard-grid">
        <s:iterator value="metrics">
            <div class="metric-card">
                <div class="metric-name"><s:property value="name" /></div>
                <div class="metric-value"><s:property value="value" /></div>
                <s:if test="details.size() > 0">
                    <ul class="metric-details">
                        <s:iterator value="details">
                            <li><s:property /></li>
                        </s:iterator>
                    </ul>
                </s:if>
            </div>
        </s:iterator>
    </div>

</div>

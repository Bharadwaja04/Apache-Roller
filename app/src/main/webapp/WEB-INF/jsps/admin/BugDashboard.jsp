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
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <b><s:text name="bugReport.admin.dashboard" /></b>
</p>

<s:actionmessage />
<s:actionerror />

<%-- Filter tabs --%>
<ul class="nav nav-tabs" style="margin-bottom: 15px;">
    <li class="<s:if test='statusFilter == null || statusFilter == \"\"'>active</s:if>">
        <s:url action="adminBugs" var="urlAll" />
        <a href="${urlAll}"><s:text name="bugReport.filter.all" /></a>
    </li>
    <li class="<s:if test='statusFilter == \"OPEN\"'>active</s:if>">
        <s:url action="adminBugs" var="urlOpen"><s:param name="statusFilter">OPEN</s:param></s:url>
        <a href="${urlOpen}"><s:text name="bugReport.status.OPEN" /></a>
    </li>
    <li class="<s:if test='statusFilter == \"RESOLVED\"'>active</s:if>">
        <s:url action="adminBugs" var="urlResolved"><s:param name="statusFilter">RESOLVED</s:param></s:url>
        <a href="${urlResolved}"><s:text name="bugReport.status.RESOLVED" /></a>
    </li>
</ul>

<script>
var confirmDeleteMsg = '<s:text name="bugReport.admin.confirmDelete" />';
</script>

<s:if test="bugReports.isEmpty()">
    <p><s:text name="bugReport.admin.noReports" /></p>
</s:if>
<s:else>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th>#</th>
                <th><s:text name="bugReport.field.title" /></th>
                <th><s:text name="bugReport.field.severity" /></th>
                <th><s:text name="generic.submittedBy" /></th>
                <th><s:text name="generic.submitted" /></th>
                <th><s:text name="generic.status" /></th>
                <th><s:text name="generic.actions" /></th>
            </tr>
        </thead>
        <tbody>
            <s:iterator value="bugReports" var="report" status="rowStatus">
            <tr>
                <td><s:property value="#rowStatus.count" /></td>
                <td><s:property value="#report.title" /></td>
                <td><s:property value="#report.severity" /></td>
                <td><s:if test="#report.reporter != null"><s:property value="#report.reporter.userName" /></s:if></td>
                <td><s:date name="#report.createdTime" format="yyyy-MM-dd HH:mm" /></td>
                <td>
                    <s:if test="#report.status.name() == 'OPEN'">
                        <span class="label label-danger"><s:text name="bugReport.status.OPEN" /></span>
                    </s:if>
                    <s:elseif test="#report.status.name() == 'RESOLVED'">
                        <span class="label label-success"><s:text name="bugReport.status.RESOLVED" /></span>
                    </s:elseif>
                    <s:else>
                        <span class="label label-default"><s:text name="bugReport.status.DELETED" /></span>
                    </s:else>
                </td>
                <td>
                    <s:if test="#report.status.name() == 'OPEN'">
                        <s:form action="adminBugResolve" method="POST" theme="simple" style="display:inline">
                            <s:hidden name="salt" />
                            <s:hidden name="reportId" value="%{#report.id}" />
                            <button type="submit" class="btn btn-xs btn-success">
                                <s:text name="bugReport.admin.resolve" />
                            </button>
                        </s:form>
                    </s:if>
                    <s:if test="#report.status.name() != 'DELETED'">
                        <s:form action="adminBugDelete" method="POST" theme="simple" style="display:inline">
                            <s:hidden name="salt" />
                            <s:hidden name="reportId" value="%{#report.id}" />
                            <button type="submit" class="btn btn-xs btn-danger"
                                    onclick="return confirm(confirmDeleteMsg)">
                                <s:text name="bugReport.admin.delete" />
                            </button>
                        </s:form>
                    </s:if>
                </td>
            </tr>
            </s:iterator>
        </tbody>
    </table>
</s:else>

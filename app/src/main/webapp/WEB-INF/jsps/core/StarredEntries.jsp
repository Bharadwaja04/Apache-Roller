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

<div class="well">
    <h2><s:text name="starredEntries.title" /></h2>

    <s:if test="starredEntries.isEmpty">
        <p><s:text name="generic.none" /></p>
    </s:if>
    <s:else>
        <s:iterator var="entry" value="starredEntries">
            <div class="starred-entry" style="margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
                <h3><s:property value="#entry.title" /></h3>
                <p>
                    <s:text name="macro.weblog.postedby" /> <s:property value="#entry.creatorUserName" />
                    <s:text name="macro.weblog.on" /> <s:date name="#entry.pubTime" format="MMM dd, yyyy" />
                    <s:text name="macro.weblog.in" /> <s:property value="#entry.category.name" />
                </p>
                <p><s:property value="#entry.summary" escapeHtml="false" /></p>
                <p>
                    <a href='<s:property value="#entry.absoluteURL" />' class="btn btn-default btn-sm">
                        <s:text name="macro.weblog.readMore" />
                    </a>
                    <s:url action="star" var="unstarEntryUrl">
                        <s:param name="entryId" value="#entry.id" />
                    </s:url>
                    <a href='<s:property value="unstarEntryUrl" />' class="btn btn-warning btn-sm">
                        <s:text name="yourWebsites.unstar" />
                    </a>
                    < %-- Like count badge: total likes from all users on this post --%>
                    <span class="badge" style="background-color: #e74c3c; color: #fff; font-size: 13px; padding: 4px 8px; border-radius: 12px; margin-left: 8px;">
                        &#9829; <s:property value="likeCounts[#entry.id]" />
                        <s:text name="starredEntries.likes" />
                    </span>
                </p>
            </div>
        </s:iterator>

        <%-- Pagination --%>
        <div class="pagination" style="margin-top: 20px;">
            <s:if test="page > 0">
                <s:url action="starredEntries" var="prevPageUrl">
                    <s:param name="page" value="page - 1" />
                </s:url>
                <a href='<s:property value="prevPageUrl" />' class="btn btn-default">&laquo; <s:text name="commentManagement.prev" /></a>
            </s:if>
            
            <s:if test="(page + 1) * pageSize < count">
                <s:url action="starredEntries" var="nextPageUrl">
                    <s:param name="page" value="page + 1" />
                </s:url>
                <a href='<s:property value="nextPageUrl" />' class="btn btn-default"><s:text name="commentManagement.next" /> &raquo;</a>
            </s:if>
        </div>
    </s:else>

    <div style="margin-top: 30px;">
        <s:url action="menu" var="backToMenuUrl" />
        <a href='<s:property value="backToMenuUrl" />'><s:text name="mainPage.mainMenu" /></a>
    </div>
</div>

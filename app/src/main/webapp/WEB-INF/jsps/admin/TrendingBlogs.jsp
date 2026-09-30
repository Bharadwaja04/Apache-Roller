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

<style>
.trending-container {
    max-width: 800px;
    margin: 20px auto;
    background: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    padding: 20px;
}
.trending-header {
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #ecf0f1;
}
.trending-list {
    list-style: none;
    padding: 0;
    margin: 0;
}
.trending-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border: 1px solid #e0e6ed;
    border-radius: 6px;
    margin-bottom: 10px;
    transition: transform 0.2s, box-shadow 0.2s;
    background: #f8f9fa;
}
.trending-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    background: #ffffff;
    border-color: #bdc3c7;
}
.trending-rank {
    font-size: 24px;
    font-weight: 700;
    color: #95a5a6;
    width: 40px;
    text-align: center;
}
.trending-info {
    flex-grow: 1;
    margin: 0 15px;
}
.trending-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 4px;
}
.trending-title a {
    color: #2c3e50;
    text-decoration: none;
}
.trending-title a:hover {
    color: #3498db;
    text-decoration: underline;
}
.trending-meta {
    font-size: 13px;
    color: #7f8c8d;
}
.trending-likes {
    background: #f39c12;
    color: #fff;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}
.empty-state {
    text-align: center;
    padding: 40px;
    color: #7f8c8d;
    font-size: 16px;
}
</style>

<div class="trending-container">
    <div class="trending-header">
        <h2>&#128293; Top 5 Trending Blogs</h2>
        <p class="text-muted">Discover the most popular content on the platform based on user engagement.</p>
    </div>

    <s:if test="trendingBlogs != null && !trendingBlogs.isEmpty()">
        <ul class="trending-list">
            <s:iterator value="trendingBlogs" status="stat">
                <li class="trending-item">
                    <div class="trending-rank">
                        #<s:property value="#stat.index + 1"/>
                    </div>
                    <div class="trending-info">
                        <div class="trending-title">
                            <a href="<s:property value='permalink'/>" target="_blank">
                                <s:property value="title"/>
                            </a>
                        </div>
                        <div class="trending-summary" style="margin-top: 8px; font-size: 14px; color: #34495e; line-height: 1.5;">
                            <s:if test="summary != null && !summary.isEmpty()">
                                <s:property value="summary"/>
                            </s:if>
                            <s:else>
                                <s:property value="text" escapeHtml="false"/>
                            </s:else>
                        </div>
                    </div>
                    <div class="trending-likes">
                        &#11088; <s:property value="likes"/> Likes
                    </div>
                </li>
            </s:iterator>
        </ul>
    </s:if>
    <s:else>
        <div class="empty-state">
            <p>No trending blogs found. Check back later once users start liking posts!</p>
        </div>
    </s:else>
</div>

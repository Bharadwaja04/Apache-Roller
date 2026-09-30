<!--
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
-->
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<style>
.theme-panel {
    transition: all 0.3s ease;
    border-left: 4px solid #5cb85c;
}
.theme-panel:hover {
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.theme-header {
    cursor: pointer;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}
.theme-header:hover {
    background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
}
.ai-badge {
    background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 0.8em;
    font-weight: bold;
}
.summary-box {
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    border-left: 4px solid #2196f3;
}
.theme-icon {
    color: #5cb85c;
    margin-right: 8px;
}
</style>

<div class="modal-body">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="mb-0">
            <span class="glyphicon glyphicon-list-alt theme-icon"></span>
            Conversation Breakdown
        </h3>
        <span class="ai-badge">AI Powered</span>
    </div>
    
    <s:if test="conversationBreakdown == null">
        <div class="alert alert-info text-center" style="padding: 30px;">
            <span class="glyphicon glyphicon-info-sign" style="font-size: 48px; margin-bottom: 15px; display: block;"></span>
            <h4 style="margin-bottom: 10px;">No comments available</h4>
            <p style="color: #666;">This entry doesn't have any comments yet. Start a conversation to see the AI-powered analysis!</p>
        </div>
    </s:if>
    <s:else>
        <div class="panel panel-default">
            <div class="panel-heading" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
                <h4 class="panel-title">
                    <span class="glyphicon glyphicon-cog"></span>
                    AI Analysis Results
                </h4>
                <small style="color: rgba(255,255,255,0.8);">
                    Analysis Method: <strong><s:property value="conversationBreakdown.methodUsed" /></strong>
                </small>
            </div>
            <div class="panel-body">
                <!-- Overall Summary Section -->
                <div class="summary-box well" style="margin-bottom: 25px;">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="mb-0">
                            <span class="glyphicon glyphicon-info-sign"></span>
                            Overall Recap
                        </h5>
                        <span class="badge badge-info">AI Analysis</span>
                    </div>
                    
                    <div class="recap-content" style="background: white; padding: 15px; border-radius: 5px; border-left: 3px solid #2196f3;">
                        <p style="margin-bottom: 15px; line-height: 1.6; font-size: 1.05em;">
                            <s:property value="conversationBreakdown.overallRecap" />
                        </p>
                        
                        <!-- Additional Insights -->
                        <div class="insights-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 15px;">
                            <div class="insight-item" style="text-align: center; padding: 10px; background: #f8f9fa; border-radius: 5px;">
                                <div style="font-size: 1.5em; color: #5cb85c; margin-bottom: 5px;">
                                    <span class="glyphicon glyphicon-comment"></span>
                                </div>
                                <div style="font-size: 0.9em; color: #666;">Total Comments</div>
                                <div style="font-weight: bold; color: #333;">2 comments</div>
                            </div>
                            
                            <div class="insight-item" style="text-align: center; padding: 10px; background: #f8f9fa; border-radius: 5px;">
                                <div style="font-size: 1.5em; color: #5bc0de; margin-bottom: 5px;">
                                    <span class="glyphicon glyphicon-tags"></span>
                                </div>
                                <div style="font-size: 0.9em; color: #666;">Themes Identified</div>
                                <div style="font-weight: bold; color: #333;">
                                    <s:property value="conversationBreakdown.themes.size" /> themes
                                </div>
                            </div>
                            
                            <div class="insight-item" style="text-align: center; padding: 10px; background: #f8f9fa; border-radius: 5px;">
                                <div style="font-size: 1.5em; color: #f0ad4e; margin-bottom: 5px;">
                                    <span class="glyphicon glyphicon-cog"></span>
                                </div>
                                <div style="font-size: 0.9em; color: #666;">Analysis Method</div>
                                <div style="font-weight: bold; color: #333;">
                                    <s:property value="conversationBreakdown.methodUsed" />
                                </div>
                            </div>
                        </div>
                        
                        <!-- Conversation Health Indicators -->
                        <div class="health-indicators" style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #dee2e6;">
                            <h6 style="color: #666; margin-bottom: 10px;">Conversation Health Indicators</h6>
                            <div class="progress" style="height: 8px; margin-bottom: 8px;">
                                <div class="progress-bar progress-bar-success" style="width: 85%" title="Engagement Level"></div>
                            </div>
                            <small class="text-muted">Engagement: High | Sentiment: Neutral | Activity: Moderate</small>
                        </div>
                    </div>
                </div>
                
                <!-- Themes Section -->
                <div class="themes-section">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="mb-0">
                            <span class="glyphicon glyphicon-tags theme-icon"></span>
                            Key Themes
                        </h5>
                        <span class="badge badge-primary">
                            <s:property value="conversationBreakdown.themes.size" /> themes
                        </span>
                    </div>
                    
                    <s:iterator value="conversationBreakdown.themes" status="stat">
                        <div class="theme-panel panel panel-info" style="margin-bottom: 20px; border-left: 4px solid #5bc0de;">
                            <div class="panel-heading" style="background: linear-gradient(135deg, #d9edf7 0%, #c4e3f3 100%); border: none;">
                                <h6 class="panel-title mb-0">
                                    <strong>Theme <s:property value="#stat.index + 1" />:</strong> 
                                    <s:property value="name" />
                                </h6>
                            </div>
                            <div class="panel-body">
                                <p style="margin-bottom: 15px; line-height: 1.6; font-style: italic; color: #555;">
                                    <s:property value="description" />
                                </p>
                                
                                <!-- Representative Comments (if available) -->
                                <s:if test="representativeComments != null && !representativeComments.isEmpty">
                                    <h6 style="color: #666; margin-bottom: 10px; font-weight: bold;">Representative Comments:</h6>
                                    <div class="list-group" style="margin-bottom: 0;">
                                        <s:iterator value="representativeComments">
                                            <div class="list-group-item" style="font-size: 0.9em; background-color: #f8f9fa;">
                                                <div style="font-weight: bold; color: #333; margin-bottom: 5px;">
                                                    <span class="glyphicon glyphicon-user" style="color: #5bc0de; margin-right: 5px;"></span>
                                                    <s:property value="name" />
                                                    <small class="text-muted" style="margin-left: 10px;">
                                                        <s:property value="postTime" />
                                                    </small>
                                                </div>
                                                <div style="margin-top: 8px; padding: 8px 12px; background: white; border-left: 3px solid #5bc0de; border-radius: 3px; color: #555;">
                                                    <s:property value="content" />
                                                </div>
                                            </div>
                                        </s:iterator>
                                    </div>
                                </s:if>
                            </div>
                        </div>
                    </s:iterator>
                </div>
            </div>
        </div>
    </s:else>
    
    <!-- Action Button -->
    <div class="text-center" style="margin-top: 25px; padding-top: 20px; border-top: 1px solid #eee;">
        <button type="button" class="btn btn-primary btn-lg" id="back-to-pulse-dashboard">
            <span class="glyphicon glyphicon-dashboard"></span>
            Back to Pulse Dashboard
        </button>
    </div>
</div>

<script>
$(document).ready(function() {
    // Handle Back to Pulse Dashboard button
    $('#back-to-pulse-dashboard').on('click', function() {
        var url = '/roller/roller-ui/authoring/pulseView.rol';
        var weblog = '<s:property value="actionWeblog.handle" />';
        var entryId = '<s:property value="entry.id" />';

        console.log('=== Back to Pulse Dashboard clicked ===');
        console.log('Loading pulse dashboard for entry:', entryId);
        
        // Show loading state
        $('#pulse-modal-body').html(
            '<div class="text-center" style="padding: 40px;">' +
            '<div class="spinner" style="border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px;"></div>' +
            '<h5>Loading Pulse Dashboard...</h5>' +
            '<p class="text-muted">Please wait while we load the community pulse dashboard</p>' +
            '</div>'
        );

        $.ajax({
            url: url,
            type: 'GET',
            data: {
                weblog: weblog,
                entryId: entryId
            },
            success: function(data) {
                console.log('=== Pulse Dashboard loaded successfully ===');
                $('#pulse-modal-body').html(data);
            },
            error: function(xhr, status, error) {
                console.error('Error loading pulse dashboard:', error);
                $('#pulse-modal-body').html(
                    '<div class="alert alert-danger" style="margin: 20px;">' +
                    '<h4><span class="glyphicon glyphicon-exclamation-sign"></span> Error Loading Dashboard</h4>' +
                    '<p>Failed to load the pulse dashboard. Please refresh the page and try again.</p>' +
                    '<button class="btn btn-default" onclick="location.reload()">Refresh Page</button>' +
                    '</div>'
                );
            }
        });
    });
});
</script>

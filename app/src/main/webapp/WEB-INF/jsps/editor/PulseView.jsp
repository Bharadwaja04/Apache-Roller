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

<div class="modal-body">
    <s:if test="!hasComments()">
        <div class="alert alert-info text-center" style="padding: 30px;">
            <span class="glyphicon glyphicon-info-sign" style="font-size: 48px; margin-bottom: 15px; display: block;"></span>
            <h4 style="margin-bottom: 10px;"><s:text name="pulse.noComments" /></h4>
            <p style="color: #666;">This entry doesn't have any comments yet. Start a conversation to see the pulse dashboard!</p>
        </div>
    </s:if>
    <s:else>

                    <div class="row">
                        <div class="col-md-4">
                            <div class="panel panel-default" style="border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <div class="panel-heading" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none;">
                                    <h4 class="panel-title" style="margin: 0;">
                                        <span class="glyphicon glyphicon-signal" style="margin-right: 8px;"></span>
                                        <s:text name="pulse.activity.level" />
                                    </h4>
                                </div>
                                <div class="panel-body text-center" style="padding: 25px;">
                                    <h2 style="color: #667eea; font-weight: bold; margin-bottom: 10px;">
                                        <s:property value="discussionOverview.activityLevel" />
                                    </h2>
                                    <p style="color: #666; margin-bottom: 15px;"><s:text name="pulse.comments.count" /></p>
                                    <div class="progress" style="height: 25px; background-color: #e9ecef; border-radius: 12px;">
                                        <div class="progress-bar" role="progressbar"
                                             id="activity-progress-bar"
                                             style="background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); transition: width 0.6s ease;"
                                             aria-valuemin="0" aria-valuemax="100">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-4">
                            <div class="panel panel-default" style="border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <div class="panel-heading" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; border: none;">
                                    <h4 class="panel-title" style="margin: 0;">
                                        <span class="glyphicon glyphicon-comment" style="margin-right: 8px;"></span>
                                        <s:text name="pulse.response.types" />
                                    </h4>
                                </div>
                                <div class="panel-body" style="padding: 20px;">
                                    <table class="table table-condensed" style="margin-bottom: 0;">
                                        <tr style="border-bottom: 1px solid #eee;">
                                            <td style="padding: 12px 8px; color: #555;"><s:text name="pulse.type.questions" /></td>
                                            <td class="text-right" style="padding: 12px 8px;">
                                                <strong style="color: #f5576c; font-size: 18px;"><s:property value="discussionOverview.responseTypes['questions']" /></strong>
                                            </td>
                                        </tr>
                                        <tr style="border-bottom: 1px solid #eee;">
                                            <td style="padding: 12px 8px; color: #555;"><s:text name="pulse.type.feedback" /></td>
                                            <td class="text-right" style="padding: 12px 8px;">
                                                <strong style="color: #f5576c; font-size: 18px;"><s:property value="discussionOverview.responseTypes['feedback']" /></strong>
                                            </td>
                                        </tr>
                                        <tr style="border-bottom: 1px solid #eee;">
                                            <td style="padding: 12px 8px; color: #555;"><s:text name="pulse.type.debate" /></td>
                                            <td class="text-right" style="padding: 12px 8px;">
                                                <strong style="color: #f5576c; font-size: 18px;"><s:property value="discussionOverview.responseTypes['debate']" /></strong>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 12px 8px; color: #555;"><s:text name="pulse.type.other" /></td>
                                            <td class="text-right" style="padding: 12px 8px;">
                                                <strong style="color: #f5576c; font-size: 18px;"><s:property value="discussionOverview.responseTypes['other']" /></strong>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-4">
                            <div class="panel panel-default" style="border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <div class="panel-heading" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; border: none;">
                                    <h4 class="panel-title" style="margin: 0;">
                                        <span class="glyphicon glyphicon-tags" style="margin-right: 8px;"></span>
                                        <s:text name="pulse.recurring.points" />
                                    </h4>
                                </div>
                                <div class="panel-body" style="padding: 20px;">
                                    <s:if test="discussionOverview.recurringPoints.isEmpty">
                                        <p class="text-muted text-center" style="color: #999; font-style: italic;">
                                            <s:text name="pulse.no.keywords" />
                                        </p>
                                    </s:if>
                                    <s:else>
                                        <div class="tag-cloud">
                                            <s:iterator value="discussionOverview.recurringPoints" status="stat">
                                                <span class="label tag-label" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; font-size: 14px; padding: 8px 12px; margin: 4px; border-radius: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                    <s:property value="keyword" />
                                                    <small style="opacity: 0.8;">(<s:property value="frequency" />)</small>
                                                </span>
                                            </s:iterator>
                                        </div>
                                    </s:else>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                </s:else>
            </div>
            
            <!-- Step 1: Add View Breakdown button -->
            <div class="row" style="margin-top: 30px;">
                <div class="col-md-12 text-center">
                    <button class="btn btn-lg btn-primary" id="view-breakdown-btn">
                        <span class="glyphicon glyphicon-list-alt" style="margin-right: 8px;"></span>
                        View Conversation Breakdown
                    </button>
                </div>
            </div>
</div>

<style>
    .tag-cloud {
        line-height: 2.5;
    }
    
    .tag-label {
        margin: 3px;
        display: inline-block;
    }
    
    .spinner {
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3498db;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        animation: spin 1s linear infinite;
        margin: 20px auto;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
</style>

<script>
    // Step 5: Load conversation breakdown via AJAX
    $(document).on('click', '#view-breakdown-btn', function(e) {
        console.log('=== Step 5: Breakdown button clicked ===');
        
        var entryId = '<s:property value="entry.id" />';
        var weblog = '<s:property value="actionWeblog.handle" />';
        console.log('Entry ID:', entryId);
        console.log('Weblog:', weblog);
        
        // Show loading state in modal
        $('#pulse-modal-body').html(
            '<div class="text-center" style="padding: 40px;">' +
            '<div class="spinner" style="border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px;"></div>' +
            '<h5>Analyzing conversation...</h5>' +
            '<p class="text-muted">Please wait while we generate the AI-powered breakdown</p>' +
            '</div>'
        );
        
        // Call the backend action via AJAX
        var url = '/roller/roller-ui/authoring/pulseBreakdown.rol';
        console.log('Calling URL:', url);
        
        $.ajax({
            url: url,
            type: 'GET',
            data: {
                entryId: entryId,
                weblog: weblog
            },
            success: function(data) {
                console.log('=== Breakdown AJAX SUCCESS ===');
                console.log('Data length:', data.length);
                
                // Update modal body with breakdown content
                $('#pulse-modal-body').html(data);
                console.log('Modal body updated with breakdown content');
            },
            error: function(xhr, status, error) {
                console.log('=== BREAKDOWN AJAX ERROR ===');
                console.log('Status:', status);
                console.log('Error:', error);
                console.log('XHR status:', xhr.status);
                console.log('XHR responseText:', xhr.responseText);
                
                // Show error message in modal
                $('#pulse-modal-body').html(
                    '<div class="alert alert-danger" style="margin: 20px;">' +
                    '<h4><span class="glyphicon glyphicon-exclamation-sign"></span> Error Loading Breakdown</h4>' +
                    '<p>Failed to load conversation breakdown. Please try again.</p>' +
                    '<p><strong>Status:</strong> ' + status + '</p>' +
                    '<button class="btn btn-default" onclick="location.reload()">Back to Overview</button>' +
                    '</div>'
                );
            }
        });
        
        e.preventDefault();
    });
</script>

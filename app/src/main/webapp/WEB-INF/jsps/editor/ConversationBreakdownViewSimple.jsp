<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script>
    console.log('=== CONVERSATION BREAKDOWN VIEW LOADED ===');
    console.log('Entry title:', '<s:property value="entry.title" escapeJavaScript="true" />');
    console.log('Conversation breakdown:', '<s:property value="conversationBreakdown" />');
    <s:if test="conversationBreakdown != null">
    console.log('Method used:', '<s:property value="conversationBreakdown.methodUsed" />');
    console.log('Overall recap:', '<s:property value="conversationBreakdown.overallRecap" escapeJavaScript="true" />');
    console.log('Themes count:', '<s:property value="conversationBreakdown.themes.size" />');
    </s:if>
</script>

<div style="padding: 20px;">
    <s:if test="conversationBreakdown == null">
        <div class="alert alert-info text-center" style="padding: 30px;">
            <h4>No comments available for this entry.</h4>
            <p style="color: #666;">This entry doesn't have any comments yet. Start a conversation to see the pulse dashboard!</p>
        </div>
    </s:if>
    <s:else>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <span class="glyphicon glyphicon-list-alt"></span>
                    Conversation Breakdown Analysis
                </h4>
                <small class="text-muted">
                    Method used: <s:property value="conversationBreakdown.methodUsed" />
                </small>
            </div>
            <div class="panel-body">
                
                <div class="well well-sm" style="background-color: #f9f9f9; border-left: 4px solid #337ab7;">
                    <h4>
                        <span class="glyphicon glyphicon-info-sign"></span>
                        Overall Recap
                    </h4>
                    <p><s:property value="conversationBreakdown.overallRecap" /></p>
                </div>
                
                <s:if test="conversationBreakdown.themes.isEmpty">
                    <div class="alert alert-warning">
                        No themes identified.
                    </div>
                </s:if>
                <s:else>
                    <h4>
                        <span class="glyphicon glyphicon-tags"></span>
                        Key Themes
                    </h4>
                    
                    <s:iterator value="conversationBreakdown.themes" status="stat">
                        <div class="panel panel-default" style="margin-bottom: 15px;">
                            <div class="panel-heading" style="background-color: #f0f0f0;">
                                <h5 class="panel-title">
                                    <strong><s:property value="name" /></strong>
                                </h5>
                            </div>
                            <div class="panel-body">
                                <p><em><s:property value="description" /></em></p>
                                
                                <s:if test="representativeComments.isEmpty">
                                    <p class="text-muted">
                                        No representative comments available.
                                    </p>
                                </s:if>
                                <s:else>
                                    <h6>Representative Comments</h6>
                                    <div class="list-group">
                                        <s:iterator value="representativeComments">
                                            <div class="list-group-item">
                                                <div style="font-size: 0.9em;">
                                                    <strong><s:property value="name" /></strong>
                                                    <span class="text-muted" style="font-size: 0.8em;">
                                                        - <s:property value="postTime" />
                                                    </span>
                                                </div>
                                                <div style="margin-top: 5px; padding-left: 10px; border-left: 2px solid #ddd;">
                                                    <s:property value="content" escape="false" />
                                                </div>
                                            </div>
                                        </s:iterator>
                                    </div>
                                </s:else>
                            </div>
                        </div>
                    </s:iterator>
                </s:else>
                
            </div>
        </div>
        
        <div class="row" style="margin-top: 15px;">
            <div class="col-md-12 text-center">
                <button type="button" class="btn btn-default" id="back-to-overview">
                    <span class="glyphicon glyphicon-arrow-left"></span>
                    Back to Overview
                </button>
                <button type="button" class="btn btn-primary" id="regenerate-breakdown">
                    <span class="glyphicon glyphicon-refresh"></span>
                    Regenerate
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    $('#back-to-overview').on('click', function() {
        console.log('=== Back to Overview clicked ===');
        
        var url = 'pulseView.rol';
        var weblog = '<s:property value="actionWeblog.handle" />';
        var entryId = '<s:property value="entry.id" />';

        console.log('Loading pulse overview for entry:', entryId);
        
        // Show loading state
        $('#pulse-modal-body').html(
            '<div class="text-center" style="padding: 40px;">' +
            '<div class="spinner" style="border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px;"></div>' +
            '<h5>Loading pulse overview...</h5>' +
            '<p class="text-muted">Please wait while we load the dashboard</p>' +
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
                console.log('=== Pulse overview loaded successfully ===');
                $('#pulse-modal-body').html(data);
            },
            error: function(xhr, status, error) {
                console.error('Error loading pulse overview:', error);
                $('#pulse-modal-body').html(
                    '<div class="alert alert-danger" style="margin: 20px;">' +
                    '<h4><span class="glyphicon glyphicon-exclamation-sign"></span> Error Loading Overview</h4>' +
                    '<p>Failed to load the pulse dashboard. Please refresh the page and try again.</p>' +
                    '<button class="btn btn-default" onclick="location.reload()">Refresh Page</button>' +
                    '</div>'
                );
            }
        });
    });

    $('#regenerate-breakdown').on('click', function() {
        var url = 'pulseBreakdown.rol';
        var weblog = '<s:property value="actionWeblog.handle" />';
        var entryId = '<s:property value="entry.id" />';

        console.log('Regenerating breakdown for entry:', entryId);

        $('#pulse-modal-body').html(
            '<div class="text-center" style="padding: 40px;">' +
            '<div class="spinner" style="border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px;"></div>' +
            '<h5>Regenerating analysis...</h5>' +
            '<p class="text-muted">Please wait while we regenerate the AI-powered breakdown</p>' +
            '</div>'
        );

        $.ajax({
            url: url,
            type: 'GET',
            data: {
                entryId: entryId,
                weblog: weblog
            },
            success: function(data) {
                console.log('=== Breakdown regenerated successfully ===');
                $('#pulse-modal-body').html(data);
            },
            error: function(xhr, status, error) {
                console.error('Error regenerating breakdown:', error);
                $('#pulse-modal-body').html(
                    '<div class="alert alert-danger" style="margin: 20px;">' +
                    '<h4><span class="glyphicon glyphicon-exclamation-sign"></span> Error Regenerating</h4>' +
                    '<p>Failed to regenerate the breakdown. Please try again.</p>' +
                    '<button class="btn btn-default" onclick="location.reload()">Refresh Page</button>' +
                    '</div>'
                );
            }
        });
    });
</script>

<style>
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
    
    .spinner {
        margin: 20px auto;
    }
</style>

<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script>
    console.log('=== Step 5: PulseBreakdown JSP loaded ===');
    console.log('Entry ID from action: <s:property value="entryId" />');
    console.log('Entry loaded: <s:property value="entry != null" />');
    console.log('Comments count: <s:property value="comments.size" />');
    console.log('Breakdown available: <s:property value="conversationBreakdown != null" />');
    <s:if test="conversationBreakdown != null">
    console.log('Method used: <s:property value="conversationBreakdown.methodUsed" />');
    console.log('Themes count: <s:property value="conversationBreakdown.themes.size" />');
    </s:if>
</script>

<div style="padding: 20px; text-align: center;">
    <h3>Step 5: Pulse Breakdown Test - AI Analysis</h3>
    
    <p><strong>Entry:</strong> <s:property value="entry.title" /></p>
    
    <s:if test="entry != null">
        <div style="background: #e8f5e8; padding: 15px; margin: 10px 0; border-radius: 5px;">
            <p><strong>✅ Entry Loaded Successfully!</strong></p>
            <p><strong>Title:</strong> <s:property value="entry.title" /></p>
        </div>
        
        <div style="background: #e8f0ff; padding: 15px; margin: 10px 0; border-radius: 5px;">
            <h4>Comments Analysis</h4>
            <p><strong>Found <s:property value="comments.size" /> approved comments:</strong></p>
        </div>
        
        <s:if test="conversationBreakdown != null">
            <div style="background: #f0e8ff; padding: 15px; margin: 10px 0; border-radius: 5px; border: 2px solid #8b5cf6;">
                <h4>🤖 AI Conversation Breakdown Analysis</h4>
                <p><strong>Analysis Method:</strong> <s:property value="conversationBreakdown.methodUsed" /></p>
                <p><strong>Themes Identified: <s:property value="conversationBreakdown.themes.size" /></strong></p>
                
                <s:if test="conversationBreakdown.themes.size > 0">
                    <div style="text-align: left; margin-top: 15px;">
                        <h5>📋 Key Themes:</h5>
                        <s:iterator value="conversationBreakdown.themes" status="themeStatus">
                            <div style="background: #faf5ff; padding: 10px; margin: 5px 0; border-left: 4px solid #8b5cf6; border-radius: 3px;">
                                <p><strong>Theme <s:property value="#themeStatus.count" />:</strong></p>
                                <p><strong>Name:</strong> <s:property value="name" /></p>
                                <s:if test="description != null && !description.isEmpty()">
                                    <p><strong>Description:</strong> <s:property value="description" /></p>
                                </s:if>
                            </div>
                        </s:iterator>
                    </div>
                </s:if>
                
                <s:if test="conversationBreakdown.overallRecap != null && !conversationBreakdown.overallRecap.isEmpty()">
                    <div style="background: #f0f9ff; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #0ea5e9;">
                        <h5>📝 Overall Recap:</h5>
                        <p style="font-style: italic;"><s:property value="conversationBreakdown.overallRecap" /></p>
                    </div>
                </s:if>
            </div>
        </s:if>
        
        <s:if test="conversationBreakdown == null">
            <div style="background: #fff3cd; padding: 15px; margin: 10px 0; border-radius: 5px;">
                <p><strong>⚠️ No breakdown analysis available.</strong></p>
                <s:if test="comments.size == 0">
                    <p>This entry doesn't have any comments to analyze.</p>
                </s:if>
                <s:if test="comments.size > 0">
                    <p>Breakdown analysis could not be generated despite having comments.</p>
                </s:if>
            </div>
        </s:if>
    </s:if>
    
    <s:if test="entry == null">
        <div style="background: #ffe8e8; padding: 15px; margin: 10px 0; border-radius: 5px;">
            <p><strong>❌ Entry Not Found!</strong></p>
            <s:if test="hasActionErrors()">
                <div style="color: red;">
                    <s:iterator value="actionErrors">
                        <p><s:property /></p>
                    </s:iterator>
                </div>
            </s:if>
        </div>
    </s:if>
    
    <script>
function goToPulseDashboard() {
    console.log('=== Back to Pulse Dashboard clicked ===');
    var weblog = '<s:property value="actionWeblog.handle" />';
    var entryId = '<s:property value="entry.id" />';
    
    console.log('Weblog:', weblog);
    console.log('Entry ID:', entryId);
    console.log('Current URL:', window.location.href);
    
    // Load pulse dashboard back into the modal
    var url = 'pulseView.rol';
    console.log('Calling AJAX URL:', url);
    console.log('Request data:', {weblog: weblog, entryId: entryId});
    
    $.ajax({
        url: url,
        type: 'GET',
        data: {
            weblog: weblog,
            entryId: entryId
        },
        beforeSend: function(xhr) {
            console.log('AJAX beforeSend - setting up request');
        },
        success: function(data) {
            console.log('=== AJAX SUCCESS ===');
            console.log('Data length:', data.length);
            console.log('First 200 chars:', data.substring(0, 200));
            $('#pulse-modal-body').html(data);
        },
        error: function(xhr, status, error) {
            console.log('=== AJAX ERROR ===');
            console.log('Status:', status);
            console.log('Error:', error);
            console.log('XHR status:', xhr.status);
            console.log('XHR responseText:', xhr.responseText);
            console.log('Response headers:', xhr.getAllResponseHeaders());
            
            $('#pulse-modal-body').html(
                '<div class="alert alert-danger" style="margin: 20px;">' +
                '<h4>Error Loading Dashboard</h4>' +
                '<p>Failed to load the pulse dashboard. Please refresh the page.</p>' +
                '<p><strong>Status:</strong> ' + status + '</p>' +
                '<p><strong>XHR Status:</strong> ' + xhr.status + '</p>' +
                '</div>'
            );
        }
    });
}
</script>

<button onclick="goToPulseDashboard()" class="btn btn-default">Back to Pulse Dashboard</button>
</div>

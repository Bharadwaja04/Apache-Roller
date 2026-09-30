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

<p class="subtitle"><s:text name="configForm.subtitle"/></p>
<p><s:text name="configForm.prompt"/></p>


<s:form action="globalConfig!save" theme="bootstrap" cssClass="form-horizontal">

    <s:hidden name="salt"/>

    <s:iterator var="dg" value="globalConfigDef.displayGroups">

        <h3><s:text name="%{#dg.key}"/></h3>

        <s:iterator var="pd" value="#dg.propertyDefs">

            <%-- special case for comment plugins --%>
            <s:if test="#pd.name == 'users.comments.plugins'">
                <s:checkboxlist label="%{getText(#pd.key)}" name="commentPlugins"
                                list="pluginsList" listKey="id" value="name" listValue="name"/>
            </s:if>

            <%-- special case for front page blog --%>
            <s:elseif test="#pd.name == 'site.frontpage.weblog.handle'">
                <s:select name="%{#pd.name}" label="%{getText(#pd.key)}" value="%{properties[#pd.name].value}"
                          list="weblogs" listKey="handle" listValueKey="handle"/>
            </s:elseif>

            <%-- "string" type means use a simple textbox --%>
            <s:elseif test="#pd.type == 'string'">
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value} "/>
            </s:elseif>

            <%-- "text" type means use a full textarea --%>
            <s:elseif test="#pd.type == 'text'">
                <s:textarea name="%{#pd.name}" label="%{getText(#pd.key)}" rows="#pd.rows" cols="#pd.cols"
                            value="%{properties[#pd.name].value} "/>
            </s:elseif>

            <%-- "boolean" type means use a checkbox --%>
            <s:elseif test="#pd.type == 'boolean'">

                <s:if test="properties[#pd.name].value == 'true'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" checked="true" onchange="formChanged()"/>
                </s:if>
                <s:if test="properties[#pd.name].value != 'true'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" onchange="formChanged()"/>
                </s:if>

            </s:elseif>

            <%-- "integer" use input type number --%>
            <s:elseif test="#pd.type == 'integer'">
                <div class="form-group ">
                    <label class="col-sm-3 control-label"
                           for='globalConfig_<s:property value="#pd.nameWithUnderbars" />'>
                        <s:text name="%{#pd.key}"/>
                    </label>
                    <div class="col-sm-9 controls">
                        <input type="number" name='<s:property value="#pd.name" />' size="35"
                               value='<s:property value="%{properties[#pd.name].value}"/>'
                               id='globalConfig_<s:property value="#pd.nameWithUnderbars" />'
                               class="form-control integer" onkeyup="formChanged()"/>
                    </div>
                </div>

            </s:elseif>

            <%-- "float" use input type number --%>
            <s:elseif test="#pd.type == 'float'">
                <div class="form-group ">
                    <label class="col-sm-3 control-label"
                           for='globalConfig_<s:property value="#pd.nameWithUnderbars" />'>
                        <s:text name="%{#pd.key}"/>
                    </label>
                    <div class="col-sm-9 controls">
                        <input type="number" name='<s:property value="#pd.name" />' size="5"
                               value='<s:property value="properties[#pd.name].value"/>'
                               id='globalConfig_<s:property value="#pd.nameWithUnderbars" />'
                               class="form-control float" onkeyup="formChanged()"/>
                    </div>
                </div>
            </s:elseif>

            <%-- if it's something we don't understand then use textbox --%>
            <s:else>
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value}"/>
            </s:else>

        </s:iterator>

        <img src="<s:url value='/roller-ui/images/spacer.png' />" alt="spacer" style="min-height: 1em"/>

    </s:iterator>

    <input id="saveButton" class="btn btn-default" type="submit" value="<s:text name="generic.save"/>"/>

</s:form>


<script type="text/javascript">

    function formChanged() {
        var saveBookmarkButton = $('#saveButton:first');
        var error = false;

        $("input").each(function () {
            var isInteger = $(this).hasClass("integer");
            var isFloat = $(this).hasClass("float");
            var isBoolean = $(this).hasClass("boolean");

            if (isInteger || isFloat) {

                if (isNaN(this.valueAsNumber)) {
                    $(this).css("background", "#FBB")
                    error = true;

                } else if (isInteger && !Number.isInteger(this.valueAsNumber)) {
                    $(this).css("background", "#FBB")
                    error = true;

                } else {
                    $(this).css("background", "white")
                }

            } else if (isFloat) {

                if (isNaN(this.valueAsNumber)) {
                    $(this).css("background", "#FBB")
                    error = true;

                } else {
                    $(this).css("background", "white")
                }

            } else if (isBoolean) {
                // not sure why this is necessary, value does not track checked state?
                $(this).prop("value", !(!$(this).prop("checked")));
            }

        });

        saveBookmarkButton.attr("disabled", error);
    }

    // ========== Content Processing Pipeline: UI Enhancements ==========

    $(document).ready(function() {

        // 1. Make API key field a password field (hidden for security)
        $("input[type='text']").each(function() {
            if (this.name && this.name.indexOf('contentprocessing.llm.apiKey') !== -1) {
                $(this).attr('type', 'password');
                $(this).attr('autocomplete', 'off');
                $(this).attr('placeholder', 'Enter your LLM API key (hidden for security)');
            }
        });

        // 2. Add placeholder to model field
        $("input[type='text']").each(function() {
            if (this.name && this.name.indexOf('contentprocessing.llm.model') !== -1) {
                $(this).attr('placeholder', 'e.g. llama-3.3-70b-versatile, claude-3-opus, gpt-4');
            }
        });

        // 3. Auto-disable processor checkboxes & fields when pipeline is OFF
        var pipelineCheckbox = null;
        var processorCheckboxes = [];
        var pipelineInputs = [];

        $("input[type='checkbox']").each(function() {
            if (this.name && this.name.indexOf('contentprocessing.pipeline.enabled') !== -1) {
                pipelineCheckbox = $(this);
            } else if (this.name && this.name.indexOf('contentprocessing.') !== -1) {
                processorCheckboxes.push($(this));
            }
        });

        $("input").each(function() {
            if (this.name && this.name.indexOf('contentprocessing.llm.') !== -1) {
                pipelineInputs.push($(this));
            }
        });

        function togglePipelineFields() {
            if (!pipelineCheckbox) return;
            var enabled = pipelineCheckbox.prop('checked');

            // Toggle processor checkboxes
            $.each(processorCheckboxes, function(i, cb) {
                cb.prop('disabled', !enabled);
                var row = cb.closest('.form-group, tr');
                if (row.length) {
                    row.css('opacity', enabled ? '1.0' : '0.4');
                }
            });

            // Toggle API key and model fields
            $.each(pipelineInputs, function(i, input) {
                input.prop('disabled', !enabled);
                var row = input.closest('.form-group, tr');
                if (row.length) {
                    row.css('opacity', enabled ? '1.0' : '0.4');
                }
            });
        }

        if (pipelineCheckbox) {
            pipelineCheckbox.on('change', togglePipelineFields);
            // Run on page load to set initial state
            togglePipelineFields();
        }

        // 4. Re-enable all disabled fields before form submit so values are saved
        $('form').on('submit', function() {
            $(this).find('input:disabled').prop('disabled', false);
        });

    });

</script>


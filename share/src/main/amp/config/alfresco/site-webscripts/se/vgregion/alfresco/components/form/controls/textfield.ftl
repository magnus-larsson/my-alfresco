<#-- @overridden projects/web-framework-commons/config/alfresco/site-webscripts/org/alfresco/components/form/controls/textfield.ftl -->

<#compress>

<#if field.control.params.delimiter??>
   <#assign delimiter=field.control.params.delimiter>
<#else>
   <#assign delimiter="#alf#">
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")?html}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <@renderViewValue field />
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:
         <#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if>
         <#if field.repeating><@formLib.renderFieldHelp field /></#if>
      </label>
      
      <@renderEditValue field />
      <#if !field.repeating><@formLib.renderFieldHelp field /></#if>
   </#if>
</div>

<#macro renderViewValue field>
   <#assign fieldValue=getFieldViewValue(field)>
   <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else><#if field.repeating><@renderViewMultiple fieldValue /><#else>${fieldValue}</#if></#if></span>
</#macro>

<#macro renderViewMultiple fieldValue>
<#assign values = fieldValue?split(delimiter)>
<#if values?size <= 1>
<span>${fieldValue}</span>
<#else>
<ul id="${fieldHtmlId}-list" class="viewmode-value undecorated">
<#list values as value>
<li>${value}</li>
</#list>
</ul>
</#if>
</#macro>

<#macro renderEditValue field>
   <#if field.repeating>
      <@renderEditMultiple field />
   <#else>
      <#assign value = getFieldEditValue(field.value)>
      <@renderEditSingle field=field value=value id=fieldHtmlId name=field.name />
   </#if>
</#macro>

<#macro renderEditMultiple field>
   <#assign values = field.value?split(delimiter)>
   <#if field.mandatory><#assign mandatory = "true"><#else><#assign mandatory = "false"></#if>
   
   <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="${field.value?string}" />
   <a href="javascript:void(0);">
      <img src="${url.context}/res/components/images/add-icon-16.png"
           onclick="javascript:Alfresco.util.addMultiInputTextValue('${fieldHtmlId}', '${field.name}', ${mandatory});"
      />
   </a>
   <ul>
      <#list values as value>
         <#assign fieldValue = getFieldEditValue(value)>
         <#assign fieldHtmlIdInput = fieldHtmlId + "_" + value_index>
         <#assign fieldHtmlNameInput = field.name + "_" + value_index>
         <#assign fieldHtmlIdImage = fieldHtmlId + "_" + value_index + "-image">
         <#assign fieldHtmlIdRemoveLink = fieldHtmlId + "_" + value_index + "-remove-link">
         
         <li>
         <#assign onkeyup = "onkeyup=\"javascript:Alfresco.util.updateMultiInputTextValue('${fieldHtmlId}', ${mandatory});\"">
         <@renderEditSingle field=field value=fieldValue id=fieldHtmlIdInput name=fieldHtmlNameInput onkeyup=onkeyup />
         <#if values?size != 1>
         <a id="${fieldHtmlIdRemoveLink}" href="javascript:void(0);">
            <img src="${url.context}/res/components/images/remove-icon-16.png" 
                 title="${msg("button.remove")?html}"  
                 alt="${msg("button.remove")}"
                 onclick="javascript:Alfresco.util.removeMultiInputTextValue('${fieldHtmlId}', '${fieldHtmlIdInput}', ${mandatory});" 
                 id="${fieldHtmlIdImage}" />
         </a>
         </#if>
         </li>
      </#list>
   </ul>
</#macro>

<#macro renderEditSingle field value id name onkeyup="">
<input id="${id}" name="${name}" tabindex="0"
       ${onkeyup}
       <#if field.control.params.password??>type="password"<#else>type="text"</#if>
       <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
       <#if field.control.params.style??>style="${field.control.params.style}"</#if>
       <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
       <#if field.description??>title="${field.description?html}"</#if>
       <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"<#else>maxlength="1024"</#if> 
       <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
       <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="disabled"</#if> />
</#macro>

<#function getFieldViewValue field>
   <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
      <#return field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
   <#else>
      <#if field.value?is_number>
         <#return field.value?c>
      <#else>
         <#return field.value?html>
      </#if>
   </#if>
   
   <#return "">
</#function>

<#function getFieldEditValue value>
   <#if value?is_number>
      <#return value?c>
   <#else>
      <#return value?html>
   </#if>
</#function>

</#compress>
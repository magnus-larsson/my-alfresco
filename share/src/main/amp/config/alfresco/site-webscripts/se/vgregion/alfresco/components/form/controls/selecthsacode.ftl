<#-- @overriddden projects/web-framework-commons/config/alfresco/site-webscripts/org/alfresco/components/form/controls/selectmany.ftl -->

<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />
<#if field.control.params.size??><#assign size=field.control.params.size><#else><#assign size=5></#if>

<#if field.control.params.optionSeparator??>
   <#assign optionSeparator=field.control.params.optionSeparator>
<#else>
   <#assign optionSeparator=",">
</#if>
<#if field.control.params.labelSeparator??>
   <#assign labelSeparator=field.control.params.labelSeparator>
<#else>
   <#assign labelSeparator="|">
</#if>

<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
   <#if context.properties[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
   <#elseif args[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
   </#if>
</#if>

<#if fieldValue?string != "">
   <#assign values=fieldValue?split(optionSeparator)>
<#else>
   <#assign values=[]>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(fieldValue?is_number) && fieldValue?string == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <#if fieldValue?string == "">
            <#assign valueToShow=msg("form.control.novalue")>
         <#else>
            <#if field.control.params.options?? && field.control.params.options != "" &&
                 field.control.params.options?index_of(labelSeparator) != -1>
               <#assign valueToShow="">
               <#assign firstLabel=true>
                 
               <ul id="${fieldHtmlId}-list" class="viewmode-value undecorated search-control-value-list">
                  <#list field.value?split("${optionSeparator}") as fieldValue>
                     <#assign idx=fieldValue?index_of("${labelSeparator}")>
                     <#if idx == -1>
                        <li>${fieldValue}</li>
                     <#else>
                        <li id="${fieldHtmlId}//${fieldValue?substring(0,idx)}">
                           ${fieldValue?substring(idx+1)}
                        </li> 
                     </#if>
                  </#list>
               </ul>
            <#else>
               <#assign valueToShow=fieldValue?replace(optionSeparator, ", ")>
            </#if>
         </#if>
         <span class="viewmode-value">${valueToShow?html}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}-entry">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="${fieldValue?string}" />
      <#if field.control.params.options?? && field.control.params.options != "">
         <select id="${fieldHtmlId}-entry" name="-" multiple="multiple" size="${size}" tabindex="0"
               onchange="javascript:Alfresco.util.updateHsaCodeValue('${fieldHtmlId}-entry', '${fieldHtmlId}', <#if field.mandatory>true<#else>false</#if>);"
               <#if field.description??>title="${field.description}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <option value="${nameValue?html}"<#if isSelected(nameValue)> selected="selected"</#if>>${nameValue?html}</option>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)[0]?split("#sep#")>
                     <option value="${choice[0]?html}"<#if isSelected(choice[0])> selected="selected"</#if>>${msgValue(choice[1])?html}</option>
                  </#if>
               </#list>
         </select>
         <@formLib.renderFieldHelp field=field />
         <#if field.control.params.mode?? && isValidMode(field.control.params.mode?upper_case)>
            <input id="${fieldHtmlId}-mode" type="hidden" name="${field.name}-mode" value="${field.control.params.mode?upper_case}" />
         </#if>
      <#else>
         <div id="${fieldHtmlId}" class="missing-options">${msg("form.control.selectone.missing-options")}</div>
      </#if>
      
      <script type="text/javascript">
      
         <#-- because the values are actually stored in a .id field, we need to update the selected values from this one -->
         
         YAHOO.util.Event.onDOMReady(function() {
            var hiddenId = '${fieldHtmlId}.id';
            var hiddenIdInput = YAHOO.util.Dom.get(hiddenId), x, y;
            
            if (!hiddenIdInput) {
               return;
            }
            
            var values1 = hiddenIdInput.value.split("${optionSeparator}");
            var values2 = "${fieldValue}".split("${optionSeparator}");
            
            var field = YAHOO.util.Dom.get('${fieldHtmlId}-entry');
            if (field !== null) {
	            var options = field.options;
	            
	            for (x = 0; x < options.length; x++) {
	               var option = options[x];
	               
	               for (y = 0; y < values1.length; y++) {
	                  var value = values1[y];
	
	                  // if't empty values, just go on
	                  if ((value === null || value.length === 0) && (option.label === null || option.label.length === 0)) {
	                     continue;
	                  }
	                  
	                  if (value === option.value) {
	                     option.selected = true;
	                  }
	               }
	
	               for (y = 0; y < values2.length; y++) {
	                  var value = values2[y];
	                  
	                  // if't empty values, just go on
	                  if ((value === null || value.length === 0) && (option.label === null || option.label.length === 0)) {
	                     continue;
	                  }
	                  
	                  if (value === option.label) {
	                     option.selected = true;
	
	                     // this must be here in order for the hidden ID field to be updated accordingly...                     
	                     Alfresco.util.updateHsaCodeValue('${fieldHtmlId}-entry', '${fieldHtmlId}', <#if field.mandatory>true<#else>false</#if>);
	                  }
	               }
	            }
	         }
         });
         
      </script>
      
   </#if>
</div>

<#function isSelected optionValue>
   <#list values as value>
      <#if optionValue == value?string || (value?is_number && value?c == optionValue)>
         <#return true>
      </#if>
   </#list>
   <#return false>
</#function>

<#function isValidMode modeValue>
   <#return modeValue == "OR" || modeValue == "AND">
</#function>

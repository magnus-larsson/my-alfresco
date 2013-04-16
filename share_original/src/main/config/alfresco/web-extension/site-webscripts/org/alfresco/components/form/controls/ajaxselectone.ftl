<#compress>

<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

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

<#if field.control.params.idField??>
   <#assign idField=field.control.params.idField>
<#else>
   <#assign idField="">
</#if>

<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
   <#if context.properties[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
   <#elseif args[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
   </#if>
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
            <#assign valueToShow=fieldValue>
            <#if field.control.params.options?? && field.control.params.options != "">
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)>
                        <#assign valueToShow=nameValue>
                        <#break>
                     </#if>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])>
                        <#assign valueToShow=msgValue(choice[1])>
                        <#break>
                     </#if>
                  </#if>
               </#list>
            </#if>
         </#if>
         <span class="viewmode-value">${valueToShow?html}</span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if>
      <@formLib.renderFieldHelp field=field /></label>
     
         <select id="${fieldHtmlId}" class="ajaxselectone" name="${field.name}" tabindex="0"
               <#if field.description??>title="${field.description?html}"</#if>
               <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled  && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="disabled"</#if>>
         </select>
         
         
         <div id="${fieldHtmlId}-properties" class="ajaxselectone-properties"></div>
      
      <script type="text/javascript" language="javascript" charset="utf-8">
      // <![CDATA[
        //sandbox            
        (function(){
            /* options from configuration of control
             * selectId: id of select
             * ds - datasource url, i.e. the url to the webscript that returns 
             *      data to fill the select with options. The URL can include
             *      "{parent}" if a parent is configured. This will then be 
             *      replaced with the value of the parent.
             * emptyTitle - (optional) first options text if there is no value for 
             *             this field, i.e. a heping text like "Choose..."
             * parent - (optional) Name of parent field. If this is specified
             *          the control will wait with it's ajax request until it
             *          gets a "formValueChanged" event for that control. 
             *          Only works with object finder controls and another
             *          ajaxselectone right now. This way you can chain the
             *          select to load after another value is already choosen.
             * default_value - (optional) default value 
             * 
             */
            var options = {
                emptyTitle: '${field.control.params.emptyTitle!}',
                <#if field.control.params.parent?exists>parent:'${args.htmlid}_prop_${field.control.params.parent}',</#if>
                default_value: '${field.control.params.default!}',
                signalChange: <#if field.mandatory>true<#else>false</#if>,
                baseHtmlId: '${args.htmlid}', //no need to change, used internally
                idField: '${idField}'
            };        
            
            var fieldValue = '${fieldValue?js_string}';
            
            new Alfresco.thirdparty.AjaxSelectOne('${fieldHtmlId}','${url.context}${field.control.params.ds!}',fieldValue,options)
                                   .setMessages(${messages});
        })();
      // ]]>
      </script>
      
   </#if>
</div>

</#compress>
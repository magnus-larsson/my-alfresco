<#-- Customised File -->
<#-- @overridden projects/web-framework-commons/config/alfresco/site-webscripts/org/alfresco/components/form/form.lib.ftl -->
<#-- Alfresco Version: 4.1.4 -->
<#-- Modification: ov -->

<#include "form-original.lib.ftl">

<#macro renderFormContainer formId>
   <div id="${formId}-container" class="form-container">
      <#if form.showCaption?? && form.showCaption>
         <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
      </#if>
      
      <#if form.mode != "view">
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl?html}">
      </#if>
      
      <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>
         <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination?html}" />
      </#if>
      
      <#if form.mode != "view" && form.redirect?? && form.redirect?length &gt; 0>
         <input id="${formId}-redirect" name="alf_redirect" type="hidden" value="${form.redirect?html}" />
      </#if>
      
      <div id="${formId}-fields" class="form-fields">
         <#nested>
      </div>
      
      <#if form.mode != "view">
         <@renderFormButtons formId=formId />
         </form>
      </#if>
      
      <div id="${formId}-warningtext" class="form-warningtext" style="display:none;">
         Man kan bara spara om någon ändring är gjord, samt att alla obligatoriska fält är ifyllda.
      <div>
   </div>
</#macro>

<#macro renderSet set>
   <div class="set">
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         <fieldset><legend>${set.label}</legend>
      <#elseif set.appearance == "bordered-panel">
         <div class="set-bordered-panel">
            <div class="set-bordered-panel-heading">${set.label}</div>
            <div class="set-bordered-panel-body">
      <#elseif set.appearance == "panel">
         <div class="set-panel">
            <div class="set-panel-heading">${set.label}</div>
            <div class="set-panel-body">
      <#elseif set.appearance == "title">
         <div class="set-title">${set.label}</div>
      <#elseif set.appearance == "whitespace">
         <div class="set-whitespace"></div>
      <#elseif set.appearance == "accordion">
         <dl id="bd-menu" class="bd-menu">
            <dt class="a-m-t">${set.label}<span class="indicator"></span></dt>
            <dd class="a-m-d expand">
      </#if>
   </#if>
   
   <#if set.template??>
      <#include "${set.template}" />
   <#else>
      <#list set.children as item>
         <#if item.kind == "set">
            <@renderSet set=item />
         <#else>
            <@renderField field=form.fields[item.id] />
         </#if>
      </#list>
   </#if>
   
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         </fieldset>
      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel">
            </div>
         </div>
      <#elseif set.appearance == "accordion">
            </dd>
         </dl>
      </#if>
   </#if>
   </div>
</#macro>

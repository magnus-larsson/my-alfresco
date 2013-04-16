<#-- @overridden projects/web-framework-commons/config/alfresco/site-webscripts/org/alfresco/components/form/form.lib.ftl -->

<#macro renderFormsRuntime formId>
   <script type="text/javascript">//<![CDATA[
      var form12345 = new Alfresco.FormUI("${formId}", "${args.htmlid?js_string}").setOptions(
      {
         mode: "${form.mode}",
         <#if form.mode == "view">
         arguments:
         {
            itemKind: "${(form.arguments.itemKind!"")?js_string}",
            itemId: "${(form.arguments.itemId!"")?js_string}",
            formId: "${(form.arguments.formId!"")?js_string}"
         }
         <#else>
         enctype: "${form.enctype}",
         fields:
         [
            <#list form.fields?keys as field>
            {
               id : "${form.fields[field].id}"
            }
            <#if field_has_next>,</#if>
            </#list>
         ],
         fieldConstraints: 
         [
            <#list form.constraints as constraint>
            {
               fieldId : "${args.htmlid?js_string}_${constraint.fieldId}", 
               handler : ${constraint.validationHandler}, 
               params : ${constraint.params}, 
               event : "${constraint.event}",
               message : <#if constraint.message??>"${constraint.message?js_string}"<#else>null</#if>
            }
            <#if constraint_has_next>,</#if>
            </#list>
         ]
         </#if>
      }).setMessages(
         ${messages}
      );
   //]]></script>
</#macro> 

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
   </div>
</#macro>

<#macro renderFormButtons formId>         
   <div id="${formId}-buttons" class="form-buttons">
      <#if form.showSubmitButton?? && form.showSubmitButton>
         <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />&nbsp;
      </#if>
      <#if form.showResetButton?? && form.showResetButton>
         <input id="${formId}-reset" type="reset" value="${msg("form.button.reset.label")}" />&nbsp;
      </#if>
       <#if form.showCancelButton?? && form.showCancelButton>
         <input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
      </#if>
   </div>
   <div id="${formId}-warningtext" class="form-warningtext">
     Man kan bara spara om någon ändring är gjord, samt att alla obligatoriska fält är ifyllda.
   </div>
</#macro>   

<#macro renderField field>
   <#if field.control?? && field.control.template??>
      <#assign fieldHtmlId=args.htmlid?js_string + "_" + field.id >
      <#include "${field.control.template}" />
   </#if>
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
      <#elseif set.appearance == "simple-panel">
         <div class="set-panel">
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
      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel" || set.appearance == "simple-panel">
            </div>
         </div>
      <#elseif set.appearance == "accordion">
            </dd>
         </dl>
      </#if>
   </#if>
   </div>
</#macro>

<#macro renderFieldHelp field>
   <#if field.help?? && field.help?length &gt; 0>
      <span class="help-icon">
         <img id="${fieldHtmlId}-help-icon" src="${url.context}/res/components/form/images/help.png" title="${msg("form.field.help")}" tabindex="0"/>
      </span>
      <div class="help-text" id="${fieldHtmlId}-help">${field.help?html}</div>
   </#if>
</#macro>

<#macro renderClear fieldId>
   <span class="selectmany-clear">
      <button id="${fieldId}-clear">${msg("form.control.selectmany.clear")}</button>
   </span>
   <script type="text/javascript" charset="utf-8">
      YAHOO.util.Event.onContentReady('${fieldId}-clear',function(){
         var b = new YAHOO.widget.Button('${fieldId}-clear');
         b.on('click',function(e){
               YAHOO.util.Event.preventDefault(e);
               var options = YAHOO.util.Dom.getChildren('${fieldId}');
               if (options) {
                  for (var i=0; i<options.length; i++) {
                     options[i].selected = false;
                  }
               }
               
               var ctrl = YAHOO.util.Dom.get('${fieldId}');

               if (document.createEvent && ctrl.dispatchEvent) {
                   var evt = document.createEvent("HTMLEvents");
                   evt.initEvent("change", true, true);
                   ctrl.dispatchEvent(evt); // for DOM-compliant browsers
               } else if (ctrl.fireEvent) {
                   ctrl.fireEvent("onchange"); // for IE
               }
            });
      
      });
   </script>
</#macro>

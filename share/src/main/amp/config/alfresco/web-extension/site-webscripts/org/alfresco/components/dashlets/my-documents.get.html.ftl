<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-documents.get.html.ftl -->

<#assign id = args.htmlid>
<#assign jsid = args.htmlid?js_string>
<#assign prefFilter = preferences.filter!"recentlyModifiedByMe">
<#assign prefSimpleView = preferences.simpleView!true>
<script type="text/javascript">//<![CDATA[
(function()
{
   new Alfresco.dashlet.MyDocuments("${jsid}").setOptions(
   {
      filter: "${prefFilter?js_string}",
      validFilters: [<#list filters as filter>"${filter.type?js_string}"<#if filter_has_next>,</#if></#list>],
      simpleView: ${prefSimpleView?string?js_string},
      maxItems: ${maxItems?c}
   }).setMessages(${messages});
   new Alfresco.widget.DashletResizer("${jsid}", "${instance.object.id}");
   new Alfresco.widget.DashletTitleBarActions("${jsid}").setOptions(
   {
      actions:
      [
         {
            cssClass: "help",
            bubbleOnClick:
            {
               message: "${msg("dashlet.help")?js_string}"
            },
            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
         }
      ]
   });
})();
//]]></script>

<div class="dashlet my-documents">
   <div class="title">${msg("header")}</div>
   <div class="toolbar flat-button">
      <div class="hidden">
         <span class="align-left yui-button yui-menu-button" id="${id}-filters">
            <span class="first-child">
               <button type="button" tabindex="0"></button>
            </span>
         </span>
         <select id="${id}-filters-menu">
         <#list filters as filter>
            <option value="${filter.type?html}">${msg("filter." + filter.type)}</option>
         </#list>
         </select>
         <div id="${id}-simpleDetailed" class="align-right simple-detailed yui-buttongroup inline">
            <span class="yui-button yui-radio-button simple-view<#if prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
               <span class="first-child">
                  <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
               </span>
            </span>
            <span class="yui-button yui-radio-button detailed-view<#if !prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
               <span class="first-child">
                  <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
               </span>
            </span>
         </div>
         <div class="clear"></div>
      </div>
   </div>
   <div class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
      <div id="${id}-documents"></div>
   </div>
</div>

<#-- Include file upload templates -->
<#-- Reassigne el to use in templates, must be unique -->
<#assign el=args.htmlid+"-html-upload" >
<#include "/org/alfresco/components/upload/html-upload.get.html.ftl">
<#assign el=args.htmlid+"-flash-upload" >
<#include "/org/alfresco/components/upload/flash-upload.get.html.ftl">    

<#-- file upload initialization -->
<#assign fileUploadConfig = config.scoped["DocumentLibrary"]["file-upload"]!>
<#if fileUploadConfig.getChildValue??>
   <#assign adobeFlashEnabled = fileUploadConfig.getChildValue("adobe-flash-enabled")!"true">
</#if>

<script type="text/javascript">//<![CDATA[
   Alfresco.thirdparty.getCheckInInstance().setOptions({
         adobeFlashEnabled: ${((adobeFlashEnabled!"true") == "true")?string}
   }).setMessages(${messages});
   
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>

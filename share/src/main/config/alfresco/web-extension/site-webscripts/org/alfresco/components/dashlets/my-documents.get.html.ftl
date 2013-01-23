<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-documents.get.html.ftl -->

<div class="dashlet my-documents">
   <div class="title">${msg("header")}</div>
   <div class="toolbar flat-button">
      <div id="${args.htmlid}-filters" class="yui-buttongroup">
         <span id="${args.htmlid}-favourites" class="yui-button yui-radio-button yui-button-checked">
            <span class="first-child">
               <button type="button" name="${args.htmlid}" value="favourites">${msg("filter.favourites")}</button>
            </span>
         </span>
         <span id="${args.htmlid}-editing" class="yui-button yui-radio-button">
            <span class="first-child">
               <button type="button" name="${args.htmlid}" value="editingMe">${msg("filter.editing")}</button>
            </span>
         </span>
         <span id="${args.htmlid}-modified" class="yui-button yui-radio-button">
            <span class="first-child">
               <button type="button" name="${args.htmlid}" value="recentlyModifiedByMe">${msg("filter.modified")}</button>
            </span>
         </span>
      </div>
   </div>
   <div id="${args.htmlid}-documents" class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
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
   var messages = ${messages};
   new Alfresco.dashlet.MyDocuments("${args.htmlid}",null,"${user}")
                       .setMessages(messages);
                       
   Alfresco.thirdparty.getCheckInInstance().setOptions({
         adobeFlashEnabled: ${((adobeFlashEnabled!"true") == "true")?string}
   }).setMessages(messages);
   
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>

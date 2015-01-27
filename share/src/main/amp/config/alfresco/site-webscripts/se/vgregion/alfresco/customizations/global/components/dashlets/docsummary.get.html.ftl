<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/docsummary.get.html.ftl -->

<@markup id="vgr-js" action="after" target="js">
   <@script src="${url.context}/res/components/upload/file-upload.js" group="upload"/>
   <@script src="${url.context}/res/components/upload/html-upload.js" group="upload"/>
   <@script src="${url.context}/res/components/upload/flash-upload.js" group="upload"/>
   <@script src="${url.context}/res/components/upload/dnd-upload.js" group="upload"/>
   <@script type="text/javascript" src="${url.context}/res/components/checkin/checkin.js" group="upload" />
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/vgr-docsummary.js" group="dashlets" />
</@>

<@markup id="vgr-widgets" action="after" target="widgets">
   <#-- file upload initialization -->
   <#assign fileUploadConfig = config.scoped["DocumentLibrary"]["file-upload"]!>
   <#if fileUploadConfig.getChildValue??>
      <#assign adobeFlashEnabled = fileUploadConfig.getChildValue("adobe-flash-enabled")!"true">
   </#if>
   
   <@inlineScript group="upload">
      Alfresco.thirdparty.getCheckInInstance().setOptions({
            adobeFlashEnabled: ${((adobeFlashEnabled!"true") == "true")?string}
      }).setMessages(${messages});
   </@>
</@>

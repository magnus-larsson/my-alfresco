<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-actions.get.html.ftl -->

<@markup id="vgr-css" action="after" target="css">
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/document-details/vgr-document-actions.css" group="document-details" />
</@>

<@markup id="vgr-js" action="after" target="js">
   <@script type="text/javascript" src="${page.url.context}/res/components/document-details/vgr-document-actions.js" group="document-details" />
   <@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/publish-to-storage-action.js" group="document-details" />
</@>

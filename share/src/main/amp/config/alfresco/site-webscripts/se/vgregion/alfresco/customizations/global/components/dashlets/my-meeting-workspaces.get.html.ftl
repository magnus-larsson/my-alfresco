<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-meeting-workspaces.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/publish-to-storage-action.css" group="dashlets" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/publish-to-storage-action-ie.css" group="dashlets" />
</@>

<@markup id="vgr-js" target="js" action="after">
  <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/publish-to-storage.js" group="dashlets"/>
  <@script type="text/javascript" src="${url.context}/res/modules/vgr-delete-site.js" group="dashlets"/>
</@>
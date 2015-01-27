<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-workspaces.get.html.ftl -->


<@markup id="vgr-css" target="css" action="after">
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/publish-to-storage-action.css" group="dashlets" />
  <!--[if lte IE 7]><@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/publish-to-storage-action-ie.css" group="dashlets" /><![endif]-->
</@>

<@markup id="vgr-js" target="js" action="after">
  <#-- JavaScript Dependencies -->
  <#-- Code for automatically redirecting to default-redirect instead of the dashboard page when clicking on a site -->
  <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/publish-to-storage.js" group="dashlets"/>
  <@script type="text/javascript" src="${url.context}/res/modules/vgr-delete-site.js" group="dashlets" />
</@>

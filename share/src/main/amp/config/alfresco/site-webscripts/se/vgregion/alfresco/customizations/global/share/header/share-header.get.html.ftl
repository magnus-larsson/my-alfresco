<#-- @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
  <@link href="${url.context}/res/modules/vgr-create-site.css" group="header"/>
</@markup>

<@markup id="vgr-js" target="js" action="after">
  <@script src="${url.context}/res/modules/vgr-create-site.js" group="header"/>
</@markup>
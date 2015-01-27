<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/data-lists/datagrid.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/search.css" group="datalists" />
</@>

<@markup id="vgr-js" target="js" action="after">
  <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/components/data-lists/vgr-datagrid.js" group="datalists" />
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/search.js" group="datalists" />
</@>


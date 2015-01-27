<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/data-lists/datalists.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/data-lists/vgr-datalists.css" group="datalists" />
</@>

<@markup id="vgr-js-after" target="widgets" action="before">
  <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/components/data-lists/vgr-datalists.js" group="datalists" />
</@>


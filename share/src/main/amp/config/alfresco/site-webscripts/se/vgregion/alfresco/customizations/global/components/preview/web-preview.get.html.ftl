<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/preview/web-preview.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/extras/components/preview/vgr-media-preview.css" group="${dependencyGroup}"/>
</@>
<@markup id="vgr-js" target="js" action="after">
  <@script type="text/javascript" src="${url.context}/res/extras/components/preview/vgr-media-preview.js" group="${dependencyGroup}" />
</@>

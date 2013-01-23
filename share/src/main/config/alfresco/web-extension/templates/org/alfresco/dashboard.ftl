<#-- @overridden projects/slingshot/config/alfresco/templates/org/alfresco/dashboard.ftl -->

<#include "include/alfresco-template.ftl" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/css/notifications.css" />

<#import "import/alfresco-layout.ftl" as layout />
<@templateHeader "transitional" />

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global" protected=true />
      <@region id="title" scope="page" protected=true />
      <@region id="navigation" scope="page" protected=true />
   </div>
   <div id="bd">
      <@layout.grid gridColumns gridClass "component" />
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
   <@script type="text/javascript" src="${page.url.context}/res/yui/cookie/cookie.js"></@script>
   <@script type="text/javascript" src="${page.url.context}/res/modules/notification.js"></@script>
   
</@>
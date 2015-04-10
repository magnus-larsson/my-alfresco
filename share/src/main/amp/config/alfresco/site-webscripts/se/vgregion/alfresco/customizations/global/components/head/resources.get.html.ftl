<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/head/resources.get.html.ftl -->

<@markup id="vgr-yui" action="after" target="yui" scope="global">
   <@script type="text/javascript" src="${url.context}/res/js/bubbling.js"></@script>
   <script type="text/javascript">//<![CDATA[
      YAHOO.Bubbling.unsubscribe = function(layer, handler, scope)
      {
         this.bubble[layer].unsubscribe(handler, scope);
      };
   //]]></script>
</@>

<@markup id="vgr-alfrescoResources" action="after" target="alfrescoResources" scope="global">
   <@script type="text/javascript" src="${url.context}/res/js/vgr-forms-runtime.js"></@script>
</@>

<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-actions.get.html.ftl -->

<#if documentDetailsJSON??>
   <#assign el=args.htmlid?js_string>
   <script type="text/javascript">//<![CDATA[
      new Alfresco.DocumentActions("${el}").setOptions(
      {
         <#if repositoryUrl??>repositoryUrl: "${repositoryUrl}",</#if>
         nodeRef: "${nodeRef?js_string}",
         siteId: <#if site??>"${site?js_string}"<#else>null</#if>,
         containerId: "${container?js_string}",
         rootNode: "${rootNode}",
         replicationUrlMapping: ${replicationUrlMappingJSON!"{}"},
         documentDetails: ${documentDetailsJSON},
         repositoryBrowsing: ${(rootNode??)?string},
         syncMode: "${syncMode}",
         containerType: "${containerType!""}"
      }).setMessages(
         ${messages}
      );
   //]]></script>

   <div id="${el}-body" class="document-actions document-details-panel">
      <h2 id="${el}-heading" class="thin dark">
         ${msg("heading")}
      </h2>
      <div class="doclist">
         <div id="${el}-actionSet" class="action-set"></div>
      </div>
   </div>

   <script type="text/javascript">//<![CDATA[
      Alfresco.util.createTwister("${el}-heading", "DocumentActions");
   //]]></script>
</#if>

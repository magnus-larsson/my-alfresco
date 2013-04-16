<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/documentlibrary/documentlist.get.html.ftl -->

<#include "include/documentlist.lib.ftl" />
<@documentlistTemplate>
<script type="text/javascript">//<![CDATA[
   new Alfresco.DocumentList("${args.htmlid}").setOptions(
   {
      <#if repositoryUrl??>repositoryUrl: "${repositoryUrl}",</#if>
      siteId: "${page.url.templateArgs.site!""}",
      containerId: "${template.properties.container!"documentLibrary"}",
      rootNode: "${rootNode}",
      usePagination: ${(args.pagination!false)?string},
      showFolders: ${(preferences.showFolders!true)?string},
      simpleView: ${(preferences.simpleView!false)?string},
      highlightFile: "${(page.url.args["file"]!"")?js_string}",
      vtiServer: ${vtiServer},
      replicationUrlMapping: ${replicationUrlMappingJSON!"{}"},
      containerType: "${containerType}"
   }).setMessages(
      ${messages}
   );
//]]></script>
</@>

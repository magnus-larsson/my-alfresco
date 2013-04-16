<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-links.get.html.ftl -->

<#if document??>
   <#if document.workingCopy??>
      <!-- Don't display links since this nodeRef points to one of a working copy pair -->
   <#else>
      <#assign el=args.htmlid?html>
      <script type="text/javascript">//<![CDATA[
      new Alfresco.DocumentLinks("${el}").setOptions(
      {
         nodeRef: "${nodeRef?js_string}",
         siteId: <#if site??>"${site?js_string}"<#else>null</#if>,
         fileName: "${document.fileName?js_string}"
      }).setMessages(${messages});
      //]]></script>
      <div id="${el}-body" class="document-links document-details-panel">

         <h2 id="${el}-heading" class="thin dark">${msg("header")}</h2>

         <div class="panel-body">

            <!-- Current document download - (javascript will prefix with the current browser location) -->
            <h3 class="thin dark">${msg("download.header")}</h3>
            <div class="link-info">
               <input id="${el}-download" value="" type="hidden" />
               <div id="${el}-download-url-text" class="url-text" title="${msg("download-links.download.title")}"></div>
               <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("download.copy")}</a>
            </div>

            <!-- Current document link - (javascript will prefix with the current browser location) -->
            <h3 class="thin dark">${msg("document.header")}</h3>
            <div class="link-info">
               <input id="${el}-document" value="" type="hidden" />
               <div id="${el}-document-url-text" class="url-text" title="${msg("download-links.document.title")}"></div>               
               <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("document.copy")}</a>
            </div>

            <!-- Current page url - (javascript will prefix with the current browser location) -->
            <h3 class="thin dark">${msg("page.header")}</h3>
            <div class="link-info">
               <input id="${el}-page" value="" type="hidden" />
               <div id="${el}-page-url-text" class="url-text" title="${msg("download-links.page.title")}"></div>
               <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("page.copy")}</a>
            </div>

         </div>

         <script type="text/javascript">//<![CDATA[
         Alfresco.util.createTwister("${el}-heading", "DocumentLinks");
         //]]></script>

      </div>
   </#if>
</#if>
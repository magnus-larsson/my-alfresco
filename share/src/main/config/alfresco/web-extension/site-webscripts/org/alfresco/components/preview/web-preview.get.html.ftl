<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/preview/web-preview.get.html.ftl -->

<#assign el=args.htmlid?html>
<#if (node?exists)>
<script type="text/javascript">//<![CDATA[
new Alfresco.WebPreview("${el}").setOptions(
{
   nodeRef: "${node.nodeRef}",
   name: "${node.name?js_string}",
   icon: "${node.icon}",
   mimeType: "${node.mimeType}",
   previews: [<#list node.previews as p>"${p}"<#if (p_has_next)>, </#if></#list>],
   size: "${node.size}",
   disableI18nInputFix: ${(args.disableI18nInputFix!config.scoped['DocumentDetails']['document-details'].getChildValue('disable-i18n-input-fix')!"false")?js_string},
   displayImageInWebPreview: ${(args.displayImageInWebPreview!config.scoped['DocumentDetails']['document-details'].getChildValue('display-image-in-web-preview')!"false")?js_string},
   maxImageSizeToDisplay: ${(args.maxImageSizeToDisplay!config.scoped['DocumentDetails']['document-details'].getChildValue('max-image-size-to-display')!500000)?number?c},
   hasPdfRendition: ${node.has_pdf_rendition?string},
   pdfRendition: "${node.pdf_rendition}",
   pluginConditions: ${pluginConditions}
}).setMessages(${messages});
//]]></script>
</#if>
<div class="web-preview shadow">
   <div class="hd">
      <div class="title">
         <h4>
            <img id="${el}-title-img" src="${url.context}/res/components/images/generic-file-32.png" alt="File" />
            <#if node?exists && node.published && !node.will_be_published><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-16.png" alt="Publicerad" title="Publicerad" /></#if>
            <#if node?exists && node.published && node.will_be_published><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-future-16.png" alt="Publiceras i framtiden" title="Kommer att publiceras ${node.availablefrom?datetime?string("yyyy-MM-dd HH:mm")}" /><span style="color: #999; font-size: 70%; margin-right: 2em;">${node.availablefrom?datetime?string("yyyy-MM-dd HH:mm")}</span> </#if>
            <#if node?exists && !node.published && node.published_before && !node.older_version_published><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-before-16.png" alt="Tidigare version är publicerad" title="Tidigare version har varit publicerad" /></#if>
            <#if node?exists && !node.published && node.older_version_published><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-older-version-16.png" alt="Tidigare version är publicerad" title="Tidigare version är publicerad" /></#if>
            <span id="${el}-title-span"></span>
         </h4>
      </div>
   </div>
   <div class="bd">
      <div id="${el}-shadow-swf-div" class="preview-swf">
         <div id="${el}-swfPlayerMessage-div"><#if (node?exists)>${msg("label.preparingPreviewer")}</#if></div>
      </div>
   </div>
</div>

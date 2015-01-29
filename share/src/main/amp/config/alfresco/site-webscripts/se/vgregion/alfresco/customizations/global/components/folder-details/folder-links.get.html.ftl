<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/folder-details/folder-links.get.html.ftl -->

<@markup id="vgr-css" action="after" target="css">
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/folder-details/vgr-folder-links.css" group="folder-details" />
</@>

<@markup id="vgr-js-after" action="before" target="widgets">
   <@script src="${url.context}/res/components/folder-details/vgr-folder-links.js" group="folder-details" />
</@>

<@markup id="vgr-html" action="replace" target="html">
   <@uniqueIdDiv>
      <#if folder??>
         <#assign el=args.htmlid?html>
         <div id="${el}-body" class="folder-links folder-details-panel">
            <h2 id="${el}-heading" class="thin dark">${msg("header")}</h2>
            <div class="panel-body">
               <#-- Current page url - (javascript will prefix with the current browser location) -->
               <h3 class="thin dark">${msg("page.header")}</h3>
               <div class="link-info">
                  <input id="${el}-page" value="" type="hidden"/>
                  <div id="${el}-page-url-text" class="url-text" title="${msg("folder-links.page.title")}"></div>
                  <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("page.copy")}</a>
               </div>
               <#if webdavUrl??>
               <#-- webdav link -->
               <h3 class="thin dark">${msg("webdav.header")}</h3>
               <div class="link-info">
                  <input id="${el}-page" value="${webdavUrl}" />
                  <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("webdav.copy")}</a>
               </div>
               </#if>
            </div>
         </div>
      </#if>
   </@>
</@>
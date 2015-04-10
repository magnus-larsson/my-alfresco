<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/site-members/site-members.get.html.ftl -->

<@markup id="vgr-css" action="after" target="css">
   <@link href="${url.context}/res/components/site-members/vgr-site-members.css" group="site-members" />
</@>

<@markup id="vgr-js" action="after" target="js">
   <@script src="${url.context}/res/components/site-members/vgr-site-members.js" group="site-members" />
</@>

<@markup id="vgr-html" action="replace" target="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="site-members">

         <#-- TITLE -->
         <@markup id="title">
         <div class="title">
            <label for="${el}-term">${msg("site-members.heading")}</label>
         </div>
         </@>

         <#-- LINKS -->
         <@markup id="links">
         <div class="links">
            <#list links![] as link>
            <span class="yui-button yui-link-button">
               <span class="first-child">
                  <a id="${el}-${link.id}" href="${link.href!"#"}" class="${link.cssClass!""}">${msg(link.label)}</a>
               </span>
            </span>
            </#list>
         </div>
         </@markup>

         <#-- FINDER-WRAPPER -->
         <@markup id="finder-wrapper">
         <div class="finder-wrapper">
            <div class="search-controls theme-bg-color-3">
               <div class="search-text"><input id="${el}-term" type="text" class="search-term" /></div>
               <div class="search-button"><button id="${el}-button">${msg("button.search")}</button></div>
               <div class="showall-button"><button id="${el}-showall">${msg("button.showall")}</button></div>
            </div>
            <div id="${el}-members" class="results"></div>
         </div>
         </@>

      </div>
   </@>
</@>

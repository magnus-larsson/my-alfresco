<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/people-finder/people-finder.get.html.ftl -->

<@markup id="vgr-js" action="after" target="js">
   <@script src="${url.context}/res/components/people-finder/vgr-people-finder.js" group="people-finder" />
</@>

<@markup id="vgr-css" action="after" target="css">
   <@link href="${url.context}/res/components/people-finder/vgr-people-finder.css" group="people-finder" />
</@>

<@markup id="vgr-html" action="replace" target="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="people-finder list theme-color-1">
      
         <div class="title theme-color-2">
            <label for="${el}-search-text">
               ${msg("title")}<br/>
               <span class="title-explanation">${msg("people-finder.title.explanation")}</span>
            </label>
         </div>
      
         <div class="finder-wrapper">
            <@markup id="searchBar">
            <div class="search-bar theme-bg-color-3">
               <div class="search-text"><input type="text" id="${el}-search-text" name="-" value="" maxlength="256" tabindex="0"/></div>
               <div class="search-button">
                  <span id="${el}-search-button" class="yui-button yui-push-button"><span class="first-child"><button>${msg("button.search")}</button></span></span>
               </div>
            </div>
            </@markup>

            <@markup id="searchHelp">
            <div id="${el}-help" class="yui-g theme-bg-color-2 help hidden">
               <span class="title">${msg("help.title")}</span>
               <div class="yui-u first">
                  <span class="subtitle">${msg("help.subtitle1")}</span>
                  <span>${msg("help.info1")}</span>
                  <span class="example">${msg("help.example1")}</span>
                  <span>${msg("help.result1")}</span>
                  <span>${msg("help.info2")}</span>
                  <span class="example">${msg("help.example2")}</span>
                  <span>${msg("help.result2")}</span>
                  <span>${msg("help.info3")}</span>
                  <span class="example">${msg("help.example3")}</span>
                  <span>${msg("help.result3")}</span>
               </div>
               <div class="yui-u">
                  <span class="subtitle">${msg("help.subtitle2")}</span>
                  <span>${msg("help.info4")}</span>
                  <span class="example">${msg("help.example4")}</span>
                  <span>${msg("help.result4")}</span>
                  <span>${msg("help.info5")}</span>
                  <span class="example">${msg("help.example5")}</span>
                  <span>${msg("help.result5")}</span>
                  <span>${msg("help.info6")}</span>
                  <span class="example">${msg("help.example6")}</span>
                  <span>${msg("help.result6")}</span>
                  <span class="example">${msg("help.example7")}</span>
                  <span>${msg("help.result7")}</span>
               </div>
            </div>
            </@markup>

            <@markup id="searchResults">
            <div id="${el}-results" class="results hidden"></div>
            </@markup>
         </div>
      </div>
   </@>
</@>

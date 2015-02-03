<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/site/customise-pages.get.html.ftl -->

<@markup id="vgr-html" action="replace" target="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html/>
      <div id="${el}" class="customise-pages">
         
         <!-- Theme -->
         <h2>${msg("header.theme")}</h2>
         <div class="flat-button">
            <select id="${el}-theme-menu">
               <#list themes as t>
               <option value="${t.id}"<#if t.selected> selected="selected"</#if>>${t.title?html}</option>
               </#list>
            </select>
         </div>
         <hr/>
      
         <!-- Default Page -->
         <h2>${msg("label.defaultPage")}</h2>
         <div class="flat-button">
            <select id="${args.htmlid}-default-page-select">
               <#assign none=true>
               <#list pages as page>
                  <#if (page.used)> 
                     <option value="${page.pageId}" <#if page.defaultPage>selected<#assign none=false></#if> >${page.title}</option>
                  </#if>
               </#list>
                  <option value="dashboard" <#if none>selected</#if> >${msg("dashboard")}</option>
            </select>
         </div>
         <hr/>
         
         <!-- Available Pages -->
         <h2>${msg("header.available.sitePages")}</h2>
         <div class="page-list-tooltip theme-border-3 theme-bg-color-2">
            <span>${msg("label.available.tooltip")}</span>
         </div>
         <div class="page-list-wrapper available-pages theme-border-3">
            <ul id="${el}-availablePages-ul" class="page-list">
            <#list pages as page>
               <#if !page.used>
                  <li id="${el}-page-${page.pageId?html}" class="customise-pages-page-list-item">
                     <input type="hidden" name="pageId" value="${page.pageId?html}">
                     <input type="hidden" name="sitePageTitle" value="${(page.sitePageTitle!"")?html}">
                     <img src="${url.context}/res/components/images/page-${page.pageId?html}-64.png"
                          onerror="this.src='${url.context}/res/components/images/page-64.png'"
                          class="theme-border-3"/>
      
                     <h3 class="type"><a href="#">${page.title?html}</a></h3>
      
                     <h3 class="title">${(page.sitePageTitle!page.title)?html}</h3>
                     <div class="type">${page.title?html}</div>
                     <div class="actions">
                        <a href="#" name=".onRenameClick" class="${el}" rel="${page.pageId?html}">${msg("link.rename")}</a> |
                        <a href="#" name=".onRemoveClick" class="${el}" rel="${page.pageId?html}">${msg("link.remove")}</a>
                     </div>
                  </li>
               </#if>
            </#list>
            </ul>
         </div>
      
         <!-- Selected Pages -->
         <h2>${msg("header.current.sitePages")}</h2>
         <div class="page-list-tooltip theme-border-3 theme-bg-color-2">
            <span>${msg("label.current.tooltip")}</span>
         </div>
         <div class="page-list-wrapper current-pages theme-border-3">
            <ul id="${el}-currentPages-ul" class="page-list">
            <#list pages as page>
               <#if page.used>
                  <li id="${el}-page-${page.pageId?html}" class="customise-pages-page-list-item">
                     <input type="hidden" name="pageId" value="${page.pageId?html}">
                     <input type="hidden" name="sitePageTitle" value="${(page.sitePageTitle!"")?html}">
                     <img src="${url.context}/res/components/images/page-${page.pageId?html}-64.png"
                          onerror="this.src='${url.context}/res/components/images/page-64.png'"
                          class="theme-border-3"/>
      
                     <h3 class="type"><a href="#">${page.title?html}</a></h3>
      
                     <h3 class="title">${(page.sitePageTitle!page.title)?html}</h3>
                     <div class="type">${page.title?html}</div>
                     <div class="actions">
                        <a href="#" name=".onRenameClick" class="${el}" rel="${page.pageId?html}">${msg("link.rename")}</a> |
                        <a href="#" name=".onRemoveClick" class="${el}" rel="${page.pageId?html}">${msg("link.remove")}</a>
                     </div>
                  </li>
               </#if>
            </#list>
            </ul>
         </div>
      
         <div class="buttons">
            <input id="${args.htmlid}-save-button" type="button" value="${msg("button.save")}"/>
            <input id="${args.htmlid}-cancel-button" type="button" value="${msg("button.cancel")}"/>
         </div>
      
      </div>
   </@>
 </@>
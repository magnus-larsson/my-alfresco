<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/documentlibrary/toolbar.get.html.ftl -->

<script type="text/javascript">//<![CDATA[
   new Alfresco.DocListToolbar("${args.htmlid?js_string}").setOptions(
   {
      siteId: "${page.url.templateArgs.site!""}",
      rootNode: "${rootNode}",
      hideNavBar: ${(preferences.hideNavBar!false)?string},
      googleDocsEnabled: ${(googleDocsEnabled!false)?string}
   }).setMessages(
      ${messages}
   );
//]]></script>


<#--
   Returns a URL to a site page given a relative URL.
   If the current page is within a Site context, that context is used for the generated link.
   The function understands that &amp; needs to be unescaped when in portlet mode.
-->
<#function siteURL relativeURL=page.url.uri?substring(page.url.context?length) siteId=page.url.templateArgs.site!"">
   <#assign portlet = context.attributes.portletHost!false>
   <#assign portlet_url = (context.attributes.portletUrl!"")>
   <#assign site_url = relativeURL>

   <#if (siteId?length > 0)>
      <#assign site_url = "site/${siteId}/${site_url}">
   </#if>

   <#if site_url?starts_with("/")><#assign site_url = site_url?substring(1)></#if>
   <#if !site_url?starts_with("page/")><#assign site_url = ("page/" + site_url)></#if>
   <#assign site_url = "/" + site_url>

   <#if portlet>
      <#assign site_url = portlet_url?replace("%24%24scriptUrl%24%24", site_url?replace("&amp;", "&")?url)>
   <#else>
      <#assign site_url = url.context + site_url>
   </#if>

   <#return site_url>
</#function>

<#--
   I18N Message string using an array of tokens as the second argument
-->
<#function msgArgs msgId msgTokens>
   <#if msgTokens??>
      <#if msgTokens?is_sequence>
         <#assign templateTokens><#list msgTokens as token>"${token?j_string}"<#if token_has_next>,</#if></#list></#assign>
         <#assign templateSource = r"${msg(msgId," + templateTokens + ")}">
         <#assign inlineTemplate = [templateSource, "msgArgsTemplate"]?interpret>
         <#assign returnValue><@inlineTemplate /></#assign>
         <#return returnValue />
      </#if>
      <#return msg(msgId, msgTokens) />
   </#if>
   <#return msg(msgId) />
</#function>

<div id="${args.htmlid}-body" class="toolbar">

   <div id="${args.htmlid}-headerBar" class="header-bar flat-button theme-bg-2">
      <div class="left">
         <div class="hideable toolbar-hidden DocListTree">
            <div class="create-content">
               <button id="${args.htmlid}-createContent-button" name="createContent">${msg("button.create-content")}</button>
               <div id="${args.htmlid}-createContent-menu" class="yuimenu">
                  <div class="bd">
                     <ul>
                     <#list createContent as content>
                        <#assign href>create-content?nodeType=${content.nodetype?html}&amp;mimeType=${content.mimetype?html}&amp;destination={nodeRef}&amp;itemId=${content.itemid}<#if (content.formid!"") != "">&amp;formId=${content.formid?html}</#if></#assign>
                        <li><a href="${siteURL(href)}" rel="${content.permission!""}"><span class="${content.icon}-file">${msg(content.label)}</span></a></li>
                     </#list>
                     </ul>
                  </div>
               </div>
            </div>
            <div class="separator">&nbsp;</div>
         </div>
         <div class="hideable toolbar-hidden DocListTree">
            <div class="new-folder"><button id="${args.htmlid}-newFolder-button" name="newFolder">${msg("button.new-folder")}</button></div>
            <div class="separator">&nbsp;</div>
         </div>
         <!-- Got to remove the upload button for now, as there is no solution for this yet -->
         <!-- TODO: Solve the problem with uploads and forms -->
         <div class="hideable toolbar-hidden DocListTree">
            <div class="file-upload"><button id="${args.htmlid}-fileUpload-button" name="fileUpload">${msg("button.upload")}</button></div>
            <div class="separator">&nbsp;</div>
         </div>
         <div class="selected-items hideable toolbar-hidden DocListTree DocListFilter TagFilter DocListCategories">
            <button class="no-access-check" id="${args.htmlid}-selectedItems-button" name="doclist-selectedItems-button">${msg("menu.selected-items")}</button>
            <div id="${args.htmlid}-selectedItems-menu" class="yuimenu">
               <div class="bd">
                  <ul>
                  <#list actionSet as action>
                     <li><a type="${action.asset!""}" rel="${action.permission!""}" href="${action.href}"><span class="${action.id}">${msg(action.label)}</span></a></li>
                  </#list>
                     <li><a href="#"><hr /></a></li>
                     <li><a href="#"><span class="onActionDeselectAll">${msg("menu.selected-items.deselect-all")}</span></a></li>
                  </ul>
               </div>
            </div>
         </div>
      </div>
      <div class="right">
         <div class="customize" style="display: none;"><button id="${args.htmlid}-customize-button" name="customize">${msg("button.customize")}</button></div>
         <div class="hide-navbar"><button id="${args.htmlid}-hideNavBar-button" name="hideNavBar">${msg("button.navbar.hide")}</button></div>
         <div class="rss-feed"><button id="${args.htmlid}-rssFeed-button" name="rssFeed">${msg("link.rss-feed")}</button></div>
      </div>
   </div>

   <div id="${args.htmlid}-navBar" class="nav-bar flat-button theme-bg-4">
      <div class="hideable toolbar-hidden DocListTree DocListCategories">
         <div class="folder-up"><button class="no-access-check" id="${args.htmlid}-folderUp-button" name="folderUp">${msg("button.up")}</button></div>
         <div class="separator">&nbsp;</div>
      </div>
      <div id="${args.htmlid}-breadcrumb" class="breadcrumb hideable toolbar-hidden DocListTree DocListCategories"></div>
      <div id="${args.htmlid}-description" class="description hideable toolbar-hidden DocListFilter TagFilter"></div>
   </div>

</div>

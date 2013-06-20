<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/modules/documentlibrary/permissions.get.html.ftl -->

<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   Alfresco.util.ComponentManager.get("${el}").setOptions(
   {
      roles:
      {
         <#list siteRoles as siteRole>"${siteRole}": true<#if siteRole_has_next>,</#if></#list>
      },
      isSitePublic: ${isSitePublic},
      siteId: '${siteId}'
   }).setMessages(
      ${messages}
   );
//]]></script>
<div id="${el}-dialog" class="permissions">
   <div id="${el}-title" class="hd"></div>
   <div class="bd">
      <p/>
      <div class="yui-g">
         <h2>${msg("header.manage")}</h2>
         <p class="desc">${msg("description.manage")}</p>
      </div>
      <div class="groups">
<#list groupNames as group>
         <div class="yui-gd">
            <div class="yui-u first right"><label>${msg("group." + group)} ${msg("label.have")}</label></div>
            <div class="yui-u flat-button">
               <button id="${el}-${group?lower_case}" value="${permissionGroups[group_index]}" class="site-group"></button>
               <select id="${el}-${group?lower_case}-select">
   <#list siteRoles as siteRole>
                  <option value="${siteRole}">${msg("role." + siteRole)}</option>
   </#list>
               </select>
            </div>
         </div>
</#list>
      </div>
      
      <div class="yui-g">
         <div class="button-wrapper">
            <button id="${el}-addUserButton" class="addUserButton">${msg('label.addUser')}</button>
            <div id="${el}-peopleFinder" class="peoplefinder hidden"></div>
        </div>
         <h2>${msg("header.manage.users")}</h2>
         <p class="desc">${msg("description.manage.users")}</p>
      </div>
      <div id="${el}-users" class="users">
        <div class="yui-gd hidden"> <!-- template for user rows -->
            <div class="yui-u first right"><span class="user-label">{LABEL}</span></div>
            <div class="yui-u flat-button">
               <button class="site-user"></button>
               <select>
   <#list siteRoles as siteRole>
                  <option value="${siteRole}">${msg("role." + siteRole)}</option>
   </#list>
               </select>
            </div>
            <div class="removeUser"><button class="remove-user">${msg("label.remove")}</button></div>            
         </div>
      </div>
      
      <div class="actions">
         <div class="yui-gd">
            <div class="yui-u">
               <label>${msg("label.mangerdefaults")}</label>
            </div>
         </div>
      </div>
      <p/>
      <div class="bdft">
         <input type="button" id="${el}-ok" value="${msg("button.save")}" tabindex="0" />
         <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         <button id="${el}-reset-all">${msg("label.reset-all")}</button>
      </div>
   </div>
</div>
<#import "../org/alfresco/repository/invite/invite.lib.ftl" as inviteLib/>
{
   "invites" : [
      <#list invitations as invite>
         {
            "inviteId": "${invite.id}",
            "site":     "${invite.site}"
         }
         <#if invite_has_next>,</#if>
      </#list>
   ]
}
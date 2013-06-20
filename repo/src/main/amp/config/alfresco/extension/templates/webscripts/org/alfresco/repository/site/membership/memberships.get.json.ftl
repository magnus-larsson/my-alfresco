<#-- @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/membership/memberships.get.json.ftl -->
<#-- List memberships Implementation-->
<#import "membership.lib.ftl" as membershipLib />

<#assign userNames = memberInfo?keys />
[
<#list userNames as userName>          
   <@membershipLib.membershipJSON site=site role=memberInfo[userName].memberRole authority=authorities[userName] zone=zones[userName]>
   "isMemberOfGroup": ${memberInfo[userName].memberOfGroup?string},
   </@membershipLib.membershipJSON>
   <#if userName_has_next>,</#if>
</#list>
]
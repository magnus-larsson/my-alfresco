<#-- @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/membership/potentialmembers.get.json.ftl -->

<#import "../../org/alfresco/repository/person/person.lib.ftl" as personLib/>
<#import "../../org/alfresco/repository/groups/authority.lib.ftl" as authorityLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "people":
   [
   <#if peopleFound??>
      <#list peopleFound as person>
         <@personLib.personJSON person=person/><#if person_has_next>,</#if>
      </#list>
   </#if>
   ],
   "data":
   [
   <#if groupsFound??>
      <#list groupsFound as group>   
         <@authorityLib.authorityJSON authority=group /><#if group_has_next>,</#if>
      </#list>
   </#if>
     ],
   "notAllowed":
   [
   <#if notAllowed??>
      <#list notAllowed as na>   
         "${na}"<#if na_has_next>,</#if>
      </#list>
   </#if>
   ]
}
</#escape>
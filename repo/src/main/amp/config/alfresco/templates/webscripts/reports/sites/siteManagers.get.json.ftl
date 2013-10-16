{ "siteManagers": [
   <#list managers as manager>
   { 
     "userid": "${manager.userid}", 
     "fullname": "${manager.fullname}" 
   }<#if manager_has_next>,</#if>
   </#list>
]}

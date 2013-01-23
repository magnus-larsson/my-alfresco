{
  "values": [
<#list values as value>
    { 
      "name": "${value.name?js_string}",
      "internalId": "${value.internalId?js_string}",
      "namespaceId": "${value.namespaceId?js_string}"
    }<#if value_has_next>,</#if>
</#list>
  ]
}

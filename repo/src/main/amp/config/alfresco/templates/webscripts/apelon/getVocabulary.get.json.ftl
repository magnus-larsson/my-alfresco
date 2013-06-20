{
  "values": [
<#list values as value>
    { 
      "name": "${value.properties['apelon:name']?js_string}",
      "internalId": "${value.properties['apelon:internalid']?js_string}",
      "namespaceId": "${value.properties['apelon:namespaceid']?js_string}",
    }<#if value_has_next>,</#if>
</#list>
  ]
}

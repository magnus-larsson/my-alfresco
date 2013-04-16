{
  "values": [
<#list values as value>
    { 
      "name": "${value.name?js_string}",
      "internalId": "${value.internalId?js_string}",
      "namespaceId": "${value.namespaceId?js_string}",
      "properties": {
        <#assign propertyKeys = value.properties?keys>
        <#list propertyKeys as propertyKey>
          "${propertyKey?js_string}": "${value.properties[propertyKey]?js_string}"<#if propertyKey_has_next>,</#if>
        </#list>
      }
    }<#if value_has_next>,</#if>
</#list>
  ]
}

{
  "values": [
<#list values as value>
    { 
      "name": "${value.properties['apelon:name']?js_string}",
      "internalId": "${value.properties['apelon:internalid']?js_string}",
      "namespaceId": "${value.properties['apelon:namespaceid']?js_string}",
      "properties": {
        <#list value.childAssocs["cm:contains"] as property>
          <#if property.typeShort == 'apelon:property'>
          <#assign propValues = property.properties['apelon:value']>
          <#list propValues as propValue>
          "${property.properties['apelon:key']?js_string}" : "${propValue?js_string}"<#if propValue_has_next>,</#if>
          </#list><#if property_has_next>,</#if>
          </#if>
        </#list>
      }
    }<#if value_has_next>,</#if>
</#list>
  ]
}

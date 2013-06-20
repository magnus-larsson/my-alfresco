{
  "values": [
<#list values as value>
    { 
      "organisationalUnit": "${value.organisationalUnit?js_string}",
      "distinguishedName": "${value.distinguishedName?js_string}",
      "hsaIdentity": "${value.hsaIdentity?js_string}",
      }
    }<#if value_has_next>,</#if>
</#list>
  ]
}

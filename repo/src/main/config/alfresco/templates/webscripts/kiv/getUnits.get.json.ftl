{
  "values": [
  <#list values as v>
    {
        "id": "${v.properties['kiv:hsaidentity']?js_string}",
        "label": "${v.properties['kiv:ou']?js_string}",
        "isLeaf": ${(v.children?size = 0)?string},
        "name": "${v.name?js_string}",
        "dn": "${v.properties['kiv:dn']?js_string}"
    }
    <#if v_has_next>,</#if>
  </#list>]
}


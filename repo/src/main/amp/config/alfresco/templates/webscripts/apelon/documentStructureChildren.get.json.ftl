{
  "values": [
  <#list values as v>
  	{
  		"id": "${(v.properties['apelon:internalid']?js_string)!''}",
  		"label": "${(v.properties['apelon:name']?js_string)!''}",
  		"isLeaf": ${isLeaf(v.children)?string},
  		"name": "${(v.name?js_string)!''}",
        "dn": "${v.properties['apelon:internalid']?js_string}"
  	}
  	<#if v_has_next>,</#if>
  </#list>]
}

<#function isLeaf children>
    <#assign count = 0>

    <#list children as child>
        <#if child.typeShort == 'apelon:documentStructure'>
            <#assign count = count + 1>
        </#if>
    </#list>

    <#return count == 0>
</#function>
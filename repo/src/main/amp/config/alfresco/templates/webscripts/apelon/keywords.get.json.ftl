{
"values": [
<#list values as v>
{
"id": "${escape(v.properties['apelon:internalid'])}",
"label": "${escape(getLabel(v))}",
"isLeaf": ${isLeaf(v.children)?string},
"name": "${escape(v.name)}",
"dn": "${escape(paths[v_index])}"
}
    <#if v_has_next>,</#if>
</#list>]
}

<#function escape value>
    <#return value?replace("´", "")?replace("\'", "")?replace("'", "")?js_string>
</#function>

<#function isLeaf children>
    <#assign count = 0>

    <#list children as child>
        <#if child.typeShort == 'apelon:swemesh'>
            <#assign count = count + 1>
        </#if>
    </#list>

    <#return count == 0>
</#function>

<#function getLabel node>
    <#assign label = node.properties['apelon:name']?js_string>

    <#list node.children as child>
        <#if child.name == 'preferredSynonym'>
            <#assign label = child.properties['apelon:value'][0]>
        </#if>
    </#list>

    <#return label?replace("´", "\'")>
</#function>








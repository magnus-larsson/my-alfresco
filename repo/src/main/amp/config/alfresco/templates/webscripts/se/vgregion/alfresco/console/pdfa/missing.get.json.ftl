<#compress>
{
"recordsReturned": ${recordsReturned?c},
"totalRecords": ${totalRecords?c},
"startIndex": ${startIndex?c},
"pageSize": ${pageSize?c},
"documents": [
    <#list nodes as node>
    {
    "storagePath": "${node.storagePath!''?js_string}",
    "sourcePath": "${node.sourcePath!''?js_string}",
    "name": "${node.name!''?js_string}",
    "nodeRef": "${node.nodeRef!''?js_string}",
    "filename": "${node.filename!''?js_string }"
    }<#if node_has_next>,</#if>
    </#list>
]
}
</#compress>
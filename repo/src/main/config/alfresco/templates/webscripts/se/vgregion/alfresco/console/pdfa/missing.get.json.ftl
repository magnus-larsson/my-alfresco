<#compress>
{
"recordsReturned": ${recordsReturned?c},
"totalRecords": ${totalRecords?c},
"startIndex": ${startIndex?c},
"pageSize": ${pageSize?c},
"documents": [
    <#list nodes as node>
    {
    "storagePath": "${node.storagePath}",
    "sourcePath": "${node.sourcePath}",
    "name": "${node.name}",
    "nodeRef": "${node.nodeRef}",
    "filename": "${node.filename!'' }"
    }<#if node_has_next>,</#if>
    </#list>
]
}
</#compress>
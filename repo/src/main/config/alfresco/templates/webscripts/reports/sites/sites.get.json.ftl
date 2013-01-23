<#compress>
{
"recordsReturned": ${recordsReturned?c},
"totalRecords": ${totalRecords?c},
"startIndex": ${startIndex?c},
"pageSize": ${pageSize?c},
"documents": [
    <#list sites as site>
    {
    "title": "${site.title}",
    "shortName": "${site.shortName}",
    "size": "${((site.size/1024)/1024)?round}",
    "members": "${site.members?round}",
    "lastActivity": "${site.lastActivity}"
    }<#if site_has_next>,</#if>
    </#list>
]
}
</#compress>
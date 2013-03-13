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
    "shortName": "${site.shortName}"
    }<#if site_has_next>,</#if>
    </#list>
]
}
</#compress>

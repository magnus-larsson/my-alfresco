<#compress>
{
"recordsReturned": ${recordsReturned?c},
"totalRecords": ${totalRecords?c},
"startIndex": ${startIndex?c},
"pageSize": ${pageSize?c},
"documents": [],
"internalUsers": [
    <#list users.internal as user>
    {
    "userName": "${user.userName}",
    "fullName": "${user.fullName}",
    "logins": "${user.logins?round}",
    "lastActivity": "${user.lastActivity?datetime}"
    }<#if user_has_next>,</#if>
    </#list>
],
"externalUsers": [
    <#list users.external as user>
    {
    "userName": "${user.userName}",
    "fullName": "${user.fullName}",
    "logins": "${user.logins?round}",
    "lastActivity": "${user.lastActivity?datetime}"
    }<#if user_has_next>,</#if>
    </#list>
]
}
</#compress>
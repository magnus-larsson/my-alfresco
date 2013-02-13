<#compress>
{
"internal": {

	"recordsReturned": ${internal.recordsReturned?c},
	"totalRecords": ${internal.totalRecords?c},
	"startIndex": ${internal.startIndex?c},
	"pageSize": ${internal.pageSize?c},
	"users": [
	    <#list internal.users as user>
	    {
	    "userName": "${user.userName}",
	    "fullName": "${user.fullName}",
	    "logins": "${user.logins?round}",
	    "lastActivity": "${user.lastActivity?datetime}"
	    }<#if user_has_next>,</#if>
	    </#list>
	]
},
"external": {

	"recordsReturned": ${external.recordsReturned?c},
	"totalRecords": ${external.totalRecords?c},
	"startIndex": ${external.startIndex?c},
	"pageSize": ${external.pageSize?c},
	"users": [
	    <#list external.users as user>
	    {
	    "userName": "${user.userName}",
	    "fullName": "${user.fullName}",
	    "logins": "${user.logins?round}",
	    "lastActivity": "${user.lastActivity?datetime}"
	    }<#if user_has_next>,</#if>
	    </#list>
	]
}
}
</#compress>
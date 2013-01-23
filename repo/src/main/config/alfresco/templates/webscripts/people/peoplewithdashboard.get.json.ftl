<#import "../org/alfresco/repository/person/person.lib.ftl" as personLib/>
{
"users" : [
	<#list users as user>
	{
	    "userName": "${user}"
    }<#if user_has_next>,</#if>
	</#list>
]
}

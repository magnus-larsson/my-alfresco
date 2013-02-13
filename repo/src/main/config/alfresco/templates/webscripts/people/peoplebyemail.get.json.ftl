<#import "../org/alfresco/repository/person/person.lib.ftl" as personLib/>
{
"people" : [
	<#list peoplelist as person>
	{
		<@personLib.personJSONinner person=person/>,
	    "nodeRef": "${person.nodeRef}"
    }<#if person_has_next>,</#if>
	</#list>
]
}

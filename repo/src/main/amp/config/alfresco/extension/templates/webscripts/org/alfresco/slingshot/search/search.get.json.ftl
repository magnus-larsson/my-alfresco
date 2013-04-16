<#-- @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.get.html.ftl -->

<#compress>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
         "permissions" : {
            "published": ${item.pub.published?string},
            "unpublished": ${(!item.pub.published)?string},
            "published-before": ${item.pub.hasbeen?string},
            "will-be-published": ${item.pub.future?string},
            "older-version-published": ${item.pub.publishedold?string},
            "no-source-and-published": ${item.no_source_and_published?string},
            "unknown-source-and-published": ${item.unknown_source_and_published?string}
         },
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			<#if item.dc_source_origin??>
			"dc_source_origin": "${item.dc_source_origin}",
			</#if>
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>

</#compress>